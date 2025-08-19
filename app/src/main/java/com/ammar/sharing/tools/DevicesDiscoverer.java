package com.ammar.sharing.tools;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.lambda.LambdaTakeLongNoReturn;
import com.ammar.sharing.models.Device;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;
import com.ammar.sharing.network.exceptions.BadRequestException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kotlin.concurrent.ThreadsKt;

public class DevicesDiscoverer {
    public static final int PORT = 34839;
    public static final byte[] MESSAGE = "0d1f8be2-300b-438d-bbe5-1cdac3adb9e2".getBytes();
    private DeviceFoundLambda mDeviceFoundCallback;
    private final ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> mScheduledFuture;

    // Listens to devices response
    private ServerSocket mServerSocket;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsRunning = false;

    public void setDeviceFoundCallback(DeviceFoundLambda callback) {
        mDeviceFoundCallback = callback;
    }

    private DatagramSocket mDatagramSocket;

    public void findDevices() {

        mScheduledExecutorService.execute(() -> {
            try {
                mDatagramSocket = new DatagramSocket();
                InetAddress multicastAddress = InetAddress.getByName(Consts.MULTICAST_DISCOVERY_GROUP);
                DatagramPacket packet = new DatagramPacket(MESSAGE, MESSAGE.length, multicastAddress, Consts.MULTICAST_DISCOVERY_PORT);
                Runnable packetSender = () -> {
                    try {
                        Log.d("FIND", "try to send packet");
                        mDatagramSocket.send(packet);
                        Log.d("FIND", "Packet sent");
                    } catch (IOException e) {
                        Utils.showErrorDialog("packetSender", "Failed to send packet. Error:\n" + e.getMessage());
                    }
                };

                mIsRunning = true;
                mScheduledFuture = mScheduledExecutorService.scheduleWithFixedDelay(packetSender, 0, 5, TimeUnit.SECONDS);
                mScheduledExecutorService.execute(this::listenToDevicesResponse);
            } catch (SocketException | UnknownHostException e) {
                Utils.showErrorDialog("DeviceDiscoverer.findDevices()", e.getMessage());
            }
        });
    }

    // returns time left in milliseconds
    public long getTimeLeftForNextBroadcast() {
        if (mScheduledFuture != null && !mScheduledFuture.isDone()) {
            return mScheduledFuture.getDelay(TimeUnit.MILLISECONDS);
        }
        return -1;
    }

    public void setGetTimeLeftCallback(int interval, LambdaTakeLongNoReturn callback) {
        mScheduledExecutorService.scheduleWithFixedDelay(() -> {
            long timeLeft = getTimeLeftForNextBroadcast();
            callback.accept(timeLeft);
        }, 0, interval, TimeUnit.MILLISECONDS);
    }


    public void stop() {
        mIsRunning = false;
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
        }
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
            if (mDatagramSocket != null) {
                mDatagramSocket.close();
            }
        } catch (IOException e) {
            Utils.showErrorDialog("DeviceDiscoverer.stop(). IOException", e.getMessage());
        }
    }

    private void listenToDevicesResponse() {

        try {
            mServerSocket = new ServerSocket(DevicesDiscoverer.PORT);
            Log.d("FIND", "Before the while loop");

            while (mIsRunning) {
                Log.d("FIND", "Start accepting");
                Socket clientSocket;
                try {
                    clientSocket = mServerSocket.accept();
                } catch (SocketException e) {
                    Utils.showErrorDialog("DevicesDiscoverer.listenToDevicesResponse(). SocketException", e.getMessage());
                    continue;
                }
                Request request = new Request(clientSocket);
                request.readSocket();

                Response response = new Response(clientSocket);
                try {
                    String deviceInfo = request.getBody();
                    JSONObject deviceInfoJSON = new JSONObject(deviceInfo);

                    String deviceName = deviceInfoJSON.getString("name");
                    String deviceOS = deviceInfoJSON.getString("os");
                    String deviceIp = clientSocket.getRemoteSocketAddress().toString();

                    Device device = new Device(deviceName, deviceIp, OS.valueOf(deviceOS));

                    if (mDeviceFoundCallback != null) {
                        mHandler.post(() -> mDeviceFoundCallback.accept(device));
                    }

                    JSONObject thisDeviceInfoJSON = DevicesDiscoverer.getDeviceJSONInfo();
                    assert thisDeviceInfoJSON != null;
                    response.setHeader("Connection", "close");
                    response.sendResponse(thisDeviceInfoJSON.toString().getBytes(StandardCharsets.UTF_8));
                    response.close();
                } catch (BadRequestException | JSONException e) {
                    Utils.showErrorDialog("DevicesDiscoverer.listenToDevicesResponse() BadRequestException | JSONException", e.getMessage());
                    response.close();
                }
            }
            mServerSocket.close();
        } catch (IOException e) {
            Utils.showErrorDialog("DevicesDiscoverer.listenToDevicesResponse() IOException", e.getMessage());
        }
    }

    public static JSONObject getDeviceJSONInfo() {
        String userDeviceName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            userDeviceName = Settings.Global.getString(Utils.getCR(), Settings.Global.DEVICE_NAME);
        }
        if (userDeviceName == null)
            userDeviceName = Settings.Secure.getString(Utils.getCR(), "bluetooth_name");
        if (userDeviceName == null)
            userDeviceName = "Unknown";

        JSONObject deviceInfoJson;
        try {
            deviceInfoJson = new JSONObject();
            deviceInfoJson.put("name", userDeviceName);
            deviceInfoJson.put("os", OS.ANDROID.toString());
        } catch (JSONException e) {
            Utils.showErrorDialog("DevicesListener.listenToDevices(). JSONException. while sending request", e.getMessage());
            return null;
        }
        return deviceInfoJson;
    }

    @FunctionalInterface
    public interface DeviceFoundLambda {
        void accept(Device device);
    }

}
