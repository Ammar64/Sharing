package com.ammar.sharing.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.lambda.MyConsumer;
import com.ammar.sharing.models.Device;
import com.ammar.sharing.tools.DevicesDiscoverer.DiscoverPacket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import kotlin.jvm.functions.Function3;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DevicesListener {
    private MyConsumer<Device> mDeviceListenedCallback;
    private Function3<String, Integer, ArrayList<String>, Void> mStartCommunicationCallback;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
    private final LinkedList<String> mListenedDevices = new LinkedList<>();

    public void setOnDeviceListenedCallback(MyConsumer<Device> callback) {
        mDeviceListenedCallback = callback;
    }

    public void setOnStartCommunicationCallback(Function3<String, Integer, ArrayList<String>, Void> callback) {
        mStartCommunicationCallback = callback;
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
            byte[] buffer = new byte[72];
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

    static Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private void handleSignalReceived(DatagramPacket datagramPacket) {
        byte[] received = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        if (received.length != DiscoverPacket.SIZE) {
            Log.e("FIND", "Invalid discover host packet (not the same size).");
            return;
        }
        byte[] operationMessage = new byte[DiscoverPacket.DISCOVER_DEVICE_MESSAGE.length];
        System.arraycopy(received, 0, operationMessage, 0, DiscoverPacket.DISCOVER_DEVICE_MESSAGE.length);

        if (Arrays.equals(operationMessage, DiscoverPacket.DISCOVER_DEVICE_MESSAGE)) {
            Log.e("FIND", "received device discovery message");
            byte[] identifier = new byte[DiscoverPacket.IDENTIFIER.length];
            System.arraycopy(received, DiscoverPacket.DISCOVER_DEVICE_MESSAGE.length, identifier, 0, DiscoverPacket.IDENTIFIER.length);
            handleDiscoverHostMessage(datagramPacket, identifier);
        } else if (Arrays.equals(operationMessage, DiscoverPacket.START_COMMUNICATING_MESSAGE)) {
            Log.e("FIND", "received start communication message");
            byte[] identifier = new byte[DiscoverPacket.IDENTIFIER.length];
            System.arraycopy(received, DiscoverPacket.START_COMMUNICATING_MESSAGE.length, identifier, 0, DiscoverPacket.IDENTIFIER.length);
            handleStartCommunicationMessage(datagramPacket, identifier);
        } else {
            Log.e("FIND", "Unknown operation message");
        }
    }

    private void handleDiscoverHostMessage(DatagramPacket datagramPacket, byte[] deviceIdentifier) {
        Response response = null;
        try {
            String deviceUUID = new String(deviceIdentifier, StandardCharsets.UTF_8);
            if (!UUID_REGEX.matcher(deviceUUID).matches()) {
                Log.e("FIND", "Invalid device UUID");
                return;
            }
            if (mListenedDevices.contains(deviceUUID)) {
                Log.d("FIND", "Device already listened");
                return;
            } else {
                Log.d("FIND", "Added a new device");
                mListenedDevices.add(deviceUUID);
            }


            JSONObject deviceInfoJson = DevicesDiscoverer.getDeviceJSONInfo();
            if (deviceInfoJson == null) {
                Log.e("FIND", "invalid device info JSON");
                return;
            }
            deviceInfoJson.put("action", "device-info");

            String sourceIp = datagramPacket.getAddress().toString();
            // This an OKHTTP Request. not our com.ammar.sharing.network.Request
            Request request = new Request.Builder()
                    .url(String.format(Locale.ENGLISH, "http://%s:%d", sourceIp, DevicesDiscoverer.PORT))
                    .method("POST", RequestBody.create(deviceInfoJson.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();
            Call call = client.newCall(request);
            // This is also okHTTP response
            response = call.execute();
            if (!Utils.verifyResponse(response)) return;
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

        } catch (IOException | JSONException e) {
            Utils.showErrorDialog("DevicesListener.handleDiscoverHostMessage().", e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    private void handleStartCommunicationMessage(DatagramPacket datagramPacket, byte[] deviceIdentifier) {
        Response response = null;
        try {
            String deviceUUID = new String(deviceIdentifier, StandardCharsets.UTF_8);
            if (!UUID_REGEX.matcher(deviceUUID).matches()) {
                Log.e("FIND", "Invalid device UUID");
                return;
            }

            String sourceIp = datagramPacket.getAddress().toString();
            // This an OKHTTP Request. not our com.ammar.sharing.network.Request
            Request request = new Request.Builder()
                    .url(String.format(Locale.ENGLISH, "http://%s:%d", sourceIp, DevicesDiscoverer.PORT))
                    .method("POST", RequestBody.create("\"action\": \"start-communication\"", MediaType.parse("application/json; charset=utf-8")))
                    .build();
            Call call = client.newCall(request);
            // This is also okHTTP response
            response = call.execute();
            if (!Utils.verifyResponse(response)) return;

            String body = response.body().string();
            try {
                JSONObject jsonBody = new JSONObject(body);
                String message = jsonBody.getString("message");

                JSONArray filesJson = jsonBody.getJSONArray("files");
                ArrayList<String> files = new ArrayList<>(filesJson.length());

                for(int i = 0; i < filesJson.length(); i++) {
                    files.add(filesJson.getString(i));
                }
                int port = jsonBody.getInt("port");
                if ("start-communication".equals(message)) {
                    if (mStartCommunicationCallback != null) {
                        mStartCommunicationCallback.invoke(sourceIp, port, files);
                    }
                } else {
                    Utils.showErrorDialog("unexpected response", "unexpected message: " + message);
                }
            } catch (JSONException | IllegalArgumentException e) {
                Utils.showErrorDialog("DevicesListener.listenToDevices(). when receiving response", e.getMessage());
            }

        } catch (IOException e) {
            Utils.showErrorDialog("DevicesListener.handleStartCommunicationMessage().", e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
