package com.ammar.sharing.tools;

import android.os.Handler;
import android.os.Looper;

import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.models.Device;
import com.ammar.sharing.network.Request;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DevicesDiscoverer {
    private DeviceFoundLambda mDeviceFoundCallback;
    private ScheduledExecutorService mScheduledExecutorService;
    private ScheduledFuture<?> mScheduledFuture;

    // Listens to devices response
    private ServerSocket mServerSocket;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsRunning = false;

    public void setDeviceFoundCallback(DeviceFoundLambda callback) {
        mDeviceFoundCallback = callback;
    }

    public void findDevices() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] message = "0d1f8be2-300b-438d-bbe5-1cdac3adb9e2".getBytes();
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            int port = 34839;
            DatagramPacket packet = new DatagramPacket(message, message.length, broadcastAddress, port);

            Runnable packetSender = () -> {
                try {
                    socket.send(packet);
                } catch (IOException ignore) {
                }
            };

            mIsRunning = true;
            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
            mScheduledFuture = mScheduledExecutorService.scheduleWithFixedDelay(packetSender, 0, 5, TimeUnit.SECONDS);
        } catch (SocketException | UnknownHostException ignored) {
        }
    }

    // returns time left in milliseconds
    public long getTimeLeftForNextBroadcast() {
        if (mScheduledFuture != null && !mScheduledFuture.isDone()) {
            return mScheduledFuture.getDelay(TimeUnit.MILLISECONDS);
        }
        return -1;
    }

    public void stop() throws IOException {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
        }
        if (mServerSocket != null) {
            mServerSocket.close();
        }
        mIsRunning = false;
    }

    private void listenToDevicesResponse() throws IOException {
        mServerSocket = new ServerSocket(34839);
        while (mIsRunning) {
            Socket clientSocket;
            try {
                clientSocket = mServerSocket.accept();
            } catch (SocketException e) {
                return;
            }
            Request request = new Request(clientSocket);

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

            } catch (BadRequestException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
        mServerSocket.close();
    }


    @FunctionalInterface
    public interface DeviceFoundLambda {
        void accept(Device device);
    }

}
