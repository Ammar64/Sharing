package com.ammar.filescenter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.components.Server;
import com.ammar.filescenter.services.objects.Downloadable;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;

/** @noinspection unused */
public class NetworkService extends Service {

    public static final String ACTION_TOGGLE_SERVER = "com.ammar.filescenter.services.TOGGLE_SERVER";
    public static final String ACTION_STOP_SERVICE = "com.ammar.filescenter.services.STOP_SERVICE";
    public static final String ACTION_GET_SERVER_STATUS = "com.ammar.filescenter.services.GET_SERVER_STATUS";
    public static final String ACTION_UPDATE_NOTIFICATION_TEXT = "com.ammar.filescenter.services.UPDATE_NOTIFICATION_STRING";
    public static final String ACTION_ADD_DOWNLOADABLE = "com.ammar.filescenter.services.ADD_DOWNLOADABLE";
    public static final String ACTION_GET_DOWNLOADABLE = "com.ammar.filescenter.services.GET_DOWNLOADABLE";
    public static final String ACTION_MODIFY_DOWNLOADABLE = "com.ammar.filescenter.services.MODIFY_DOWNLOADABLE";

    public static final String EXTRA_SERVER_STATUS = "com.ammar.filescenter.services.SERVER_STATUS";
    public static final String EXTRA_SERVER_ADDRESS = "com.ammar.filescenter.services.SERVER_ADDRESS";
    public static final String EXTRA_SERVER_PORT = "com.ammar.filescenter.services.SERVER_PORT";

    public static final String EXTRA_FILE_PATHS = "com.ammar.filescenter.services.FILE_PATHS";
    public static final String EXTRA_DOWNLOADABLES_ARRAY = "com.ammar.filescenter.services.DOWNLOADABLES_LIST";
    public static final String EXTRA_MODIFY_TYPE = "com.ammar.filescenter.services.MODIFY_TYPE";
    public static final String EXTRA_MODIFY_DELETE_UUID = "com.ammar.filescenter.services.MODIFY_DELETE_UUID";

    public static final String VALUE_MODIFY_DELETE = "com.ammar.filescenter.services.VALUE_MODIFY_DELETE";

    private final int FOREGROUND_NOTIFICATION_ID = 1;

    public static final int PORT_NUMBER = 2999;
    public static AssetManager assetManager;


    private final Server server = new Server();
    final Intent serverStatusIntent = new Intent();

    @Override
    public void onCreate() {
        super.onCreate();
        serverStatusIntent.setAction(ACTION_GET_SERVER_STATUS);
        assetManager = getAssets();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MYLOG", "Service NetworkService started");
        if (intent == null) {
            Log.e("MYLOG", "intent is null in Service that's wrong");
            return START_STICKY;
        }
        String actionReceived = intent.getAction();
        String action = actionReceived != null ? actionReceived : "";
        switch (action) {
            case ACTION_TOGGLE_SERVER:
                toggleServer();
                break;
            case ACTION_STOP_SERVICE:
                stopSelf();
            case ACTION_GET_SERVER_STATUS:
                sendServerStatusToActivity();
                break;
            case ACTION_UPDATE_NOTIFICATION_TEXT:
                startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
                break;
            case ACTION_ADD_DOWNLOADABLE:
                ArrayList<String> files = intent.getStringArrayListExtra(EXTRA_FILE_PATHS);
                assert files != null;
                for (String i : files) {
                    server.downloadablesList.add(new Downloadable(i));
                }
                break;
            case ACTION_GET_DOWNLOADABLE:
                Intent downloadablesIntent = new Intent();
                downloadablesIntent.setAction(ACTION_GET_DOWNLOADABLE);
                downloadablesIntent.putExtra(EXTRA_DOWNLOADABLES_ARRAY, server.downloadablesList);

                //noinspection deprecation
                LocalBroadcastManager.getInstance(this).sendBroadcast(downloadablesIntent);
                break;
            case ACTION_MODIFY_DOWNLOADABLE:
                String type = intent.getStringExtra(EXTRA_MODIFY_TYPE);
                assert type != null;
                if (type.equals(VALUE_MODIFY_DELETE)) {
                    UUID uuid = (UUID) intent.getSerializableExtra(EXTRA_MODIFY_DELETE_UUID);
                    for (Downloadable i : server.downloadablesList) {
                        if (i.getUUID().equals(uuid)) {
                            server.downloadablesList.remove(i);
                        }
                    }
                }
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    private void toggleServer() {
        if (server.isRunning()) {
            server.Stop();
        } else {
            server.Start();
        }
        sendServerStatusToActivity();
    }


    private void sendServerStatusToActivity() {
        Intent intent = new Intent();
        intent.setAction(ACTION_GET_SERVER_STATUS);
        boolean isServerRunning = server.isRunning();

        // stop notification or start it
        if (isServerRunning) {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
        } else {
            stopForegroundAndNotification();
        }

        intent.putExtra(EXTRA_SERVER_STATUS, isServerRunning);
        //noinspection deprecation
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public Notification buildNotification(Context context) {
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channel ID
        String channelId = "com.ammar.filescenter";
        // Notification channel name
        CharSequence channelName = "Server";
        // Notification importance level

        // Check if Android version is Oreo or higher, as notification channels are required from Oreo onwards
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // Create a notification channel
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            // Configure the notification channel
            channel.setDescription("Your channel description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // Register the notification channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        String address = NetworkService.getIpAddress();
        if (address == null) address = "0.0.0.0";
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId).setContentTitle("Server is running").setSmallIcon(android.R.drawable.ic_dialog_info);

        return new NotificationCompat.BigTextStyle(builder).bigText(getResources().getString(R.string.svr_notification_message, address, String.format(Locale.ENGLISH, "%d", PORT_NUMBER))).build();
    }

    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            if (ex.getMessage() == null) return null;
            Log.e("MYLOG", ex.getMessage());
        }
        return null;
    }


    private void stopForegroundAndNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    public Downloadable getDownloadableWithUUID(String uuid) {
        for (Downloadable i : server.downloadablesList) {
            if (uuid.equals(i.getUUID().toString())) {
                return i;
            }
        }
        return null;
    }

    public static byte[] readFileFromAssets(String filepath) throws IOException {
        InputStream input = assetManager.open(filepath);
        int size = input.available();
        byte[] content = new byte[size];
        int numBytes = input.read(content);
        input.close();
        if (numBytes != size) {
            throw new RuntimeException("Error reading file");
        }
        return content;

    }
}
