package com.ammar.sharing.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.lambda.MyConsumer;
import com.ammar.sharing.models.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DevicesListener {
    private MyConsumer<Device> mDeviceListenedCallback;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    public void setOnDeviceListenedCallback(MyConsumer<Device> callback) {
        mDeviceListenedCallback = callback;
    }

    private boolean mIsRunning;

    OkHttpClient client = new OkHttpClient();

    public void listenToDevices() {
        mIsRunning = true;
        mExecutorService.execute(this::startListener);
    }


    public void stop() {
        mIsRunning = false;
        try {
            if (mMulticastSocket != null) {
                mMulticastSocket.leaveGroup(InetAddress.getByName(Consts.MULTICAST_DISCOVERY_GROUP));
                mMulticastSocket.close();
            }
        } catch (IOException e) {
            Utils.showErrorDialog("DevicesListener.stop(). IOException", e.getMessage());
        }
        mExecutorService.shutdown();
    }

    private MulticastSocket mMulticastSocket;

    private void startListener() {
        Utils.aquireMulticastLock();
        try {
            mMulticastSocket = new MulticastSocket(Consts.MULTICAST_DISCOVERY_PORT);
            mMulticastSocket.joinGroup(InetAddress.getByName(Consts.MULTICAST_DISCOVERY_GROUP));
            byte[] buffer = new byte[512];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            while (mIsRunning) {
                Log.d("FIND", "start receiving");
                mMulticastSocket.receive(datagramPacket);
                Log.d("FIND", "Packet received");
                mExecutorService.execute(() -> handleSignalReceived(datagramPacket));
            }
        } catch (SocketException e) {
            Utils.showErrorDialog("DevicesListener.startListener(). SocketException", e.getMessage());
        } catch (IOException e) {
            Utils.showErrorDialog("DevicesListener.startListener(). " + e.getClass().getName(), e.getMessage());
        } finally {
            Utils.releaseMulticastLock();
        }
    }

    private void handleSignalReceived(DatagramPacket datagramPacket) {
        Response response = null;
        try {
            byte[] received = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
            if (Arrays.equals(DevicesDiscoverer.MESSAGE, received)) {
                JSONObject deviceInfoJson = DevicesDiscoverer.getDeviceJSONInfo();
                String sourceIp = datagramPacket.getAddress().toString();
                // This an OKHTTP Request. not our com.ammar.sharing.network.Request
                Request request = new Request.Builder()
                        .url(String.format(Locale.ENGLISH, "http://%s:%d", sourceIp, DevicesDiscoverer.PORT))
                        .method("POST", RequestBody.create(deviceInfoJson.toString(), MediaType.parse("application/json; charset=utf-8")))
                        .build();

                Call call = client.newCall(request);
                // This is also okHTTP response
                response = call.execute();
                if (response.code() != 200) {
                    Utils.showErrorDialog("Unexpected", "Response code is not 200");
                    return;
                }
                if (response.body().contentLength() >= Consts.MAX_NON_FILE_CONTENT_LENGTH) {
                    Utils.showErrorDialog("Unexpected", "Device listener content length is too long");
                    return;
                }
                String body = response.body().string();
                try {
                    JSONObject jsonBody = new JSONObject(body);
                    Device device = new Device(
                            jsonBody.getString("name"),
                            sourceIp,
                            OS.valueOf(jsonBody.getString("os")));
                    if (mDeviceListenedCallback != null) {
                        mHandler.post(() -> mDeviceListenedCallback.accept(device));
                    }
                } catch (JSONException | IllegalArgumentException e) {
                    Utils.showErrorDialog("DevicesListener.listenToDevices(). when receiving response", e.getMessage());
                }
            }
        } catch (IOException e) {
            Utils.showErrorDialog("DevicesListener.handleSignalReceived().", e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}
