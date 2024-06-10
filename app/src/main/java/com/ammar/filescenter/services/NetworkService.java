package com.ammar.filescenter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ammar.filescenter.R;
import com.ammar.filescenter.custom.data.QueueMutableLiveData;
import com.ammar.filescenter.services.components.Server;
import com.ammar.filescenter.services.models.AppUpload;
import com.ammar.filescenter.services.models.Upload;
import com.ammar.filescenter.services.objects.AppDownloadable;
import com.ammar.filescenter.services.objects.Downloadable;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @noinspection unused
 */
public class NetworkService extends Service {

    public static final String ACTION_TOGGLE_SERVER = "com.ammar.filescenter.services.TOGGLE_SERVER";
    public static final String ACTION_STOP_SERVICE = "com.ammar.filescenter.services.STOP_SERVICE";
    public static final String ACTION_GET_SERVER_STATUS = "com.ammar.filescenter.services.GET_SERVER_STATUS";
    public static final String ACTION_UPDATE_NOTIFICATION_TEXT = "com.ammar.filescenter.services.UPDATE_NOTIFICATION_STRING";
    public static final String ACTION_ADD_DOWNLOADABLE = "com.ammar.filescenter.services.ADD_DOWNLOADABLE";
    public static final String ACTION_ADD_APPS_DOWNLOADABLE = "com.ammar.filescenter.services.ADD_APP_DOWNLOADABLE";
    public static final String ACTION_GET_DOWNLOADABLE = "com.ammar.filescenter.services.GET_DOWNLOADABLE";
    public static final String ACTION_MODIFY_DOWNLOADABLE = "com.ammar.filescenter.services.MODIFY_DOWNLOADABLE";
    public static final String ACTION_DEVICE_CONNECTED = "com.ammar.filescenter.services.DEVICE_CONNECTED";
    public static final String ACTION_REMOVE_DOWNLOAD = "ACTION_REMOVE_DOWNLOAD";
    public static final String ACTION_CANCEL_UPLOAD = "ACTION_UPLOAD_CANCEL";
    public static final String ACTION_ADD_DOWNLOADS = "ACTION_ADD_DOWNLOADS";
    public static final String ACTION_ADD_APPS_DOWNLOADS = "ACTION_ADD_APPS_DOWNLOADS";
    public static final String ACTION_GET_DOWNLOADS = "ACTION_GET_DOWNLOADS";

    public static final String EXTRA_SERVER_STATUS = "com.ammar.filescenter.services.SERVER_STATUS";
    public static final String EXTRA_SERVER_ADDRESS = "com.ammar.filescenter.services.SERVER_ADDRESS";
    public static final String EXTRA_SERVER_PORT = "com.ammar.filescenter.services.SERVER_PORT";
    public static final String EXTRA_FILE_PATHS = "com.ammar.filescenter.services.FILE_PATHS";
    public static final String EXTRA_DOWNLOADABLES_ARRAY = "com.ammar.filescenter.services.DOWNLOADABLES_LIST";
    public static final String EXTRA_APPS_NAMES = "com.ammar.filescenter.services.APPS_NAME";
    public static final String EXTRA_MODIFY_TYPE = "com.ammar.filescenter.services.MODIFY_TYPE";
    public static final String EXTRA_MODIFY_DELETE_UUID = "com.ammar.filescenter.services.MODIFY_DELETE_UUID";
    public static final String EXTRA_CONNECTED_DEVICES_LIST = "com.ammar.filescenter.services.CONNECTED_DEVICES_LIST";
    public static final String EXTRA_DOWNLOAD_REMOVE = "EXTRA_DOWNLOAD_REMOVE";
    public static final String EXTRA_UPLOAD_CANCEL = "EXTRA_UPLOAD_CANCEL";
    public static final String EXTRA_DOWNLOAD_UUID = "EXTRA_DOWNLOAD_UUID";


    // observers
    public static final MutableLiveData<Boolean> serverStatusObserver = new MutableLiveData<>();
    public static final MutableLiveData<Bundle> downloadsListObserver = new MutableLiveData<>();
    public static final MutableLiveData<List<Upload>> filesListObserver = new MutableLiveData<>();
    public static final MutableLiveData<Bundle> filesListNotifier = new MutableLiveData<>();
    public static final String DATA_DOWNLOADS_LIST = "DATA_DOWNLOADS_LIST";
    public static final String VALUE_MODIFY_DELETE = "com.ammar.filescenter.services.VALUE_MODIFY_DELETE";
    public static final QueueMutableLiveData<Bundle> filesSendNotifier = new QueueMutableLiveData<>();

    private final int FOREGROUND_NOTIFICATION_ID = 1;
    public static final int PORT_NUMBER = 2999;
    public static AssetManager assetManager;


    private final Server server = new Server(this);
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
            case ACTION_ADD_APPS_DOWNLOADABLE:
                ArrayList<String> appsName = intent.getStringArrayListExtra(EXTRA_APPS_NAMES);
                for (String i : appsName) {
                    AppDownloadable app = new AppDownloadable(this, i);
                    server.downloadablesList.add(app);
                    server.downloadablesList.addAll(Arrays.asList(app.splits));
                }
                break;




            case ACTION_ADD_DOWNLOADS:
                ArrayList<String> filePaths = intent.getStringArrayListExtra(EXTRA_FILE_PATHS);
                assert filePaths != null;
                for( String i : filePaths ) {
                    Server.filesList.add( new Upload(i));
                }
                break;
            case ACTION_ADD_APPS_DOWNLOADS:
                ArrayList<String> packages_name = intent.getStringArrayListExtra(EXTRA_APPS_NAMES);
                if( packages_name != null ) {
                    for( String i : packages_name ) {
                        try {
                            Server.filesList.add(new AppUpload(this, i));
                        } catch (PackageManager.NameNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
            case ACTION_GET_DOWNLOADS:
                filesListObserver.postValue(Server.filesList);
            case ACTION_REMOVE_DOWNLOAD:
                String uuid = intent.getStringExtra(EXTRA_DOWNLOAD_UUID);

                int index = 0;
                for( Upload i : Server.filesList ) {
                    if( i.getUUID().equals(uuid) ) {
                        Server.filesList.remove(index);
                        break;
                    }
                    index++;
                }
                Bundle remove_info = new Bundle();
                remove_info.putChar("action", 'R');
                remove_info.putInt("index", index);
                NetworkService.filesListNotifier.postValue(remove_info);
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

    private boolean isRunningFirstTime = true;
    private void sendServerStatusToActivity() {
        Intent intent = new Intent();
        intent.setAction(ACTION_GET_SERVER_STATUS);
        boolean isServerRunning = server.isRunning();

        // stop notification or start it
        if (isServerRunning && isRunningFirstTime) {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
            isRunningFirstTime = false;
        } else {
            stopForegroundAndNotification();
            isRunningFirstTime = true;
        }

        // notify observer.
        serverStatusObserver.postValue(isServerRunning);

        intent.putExtra(EXTRA_SERVER_STATUS, isServerRunning);
        intent.putExtra(EXTRA_CONNECTED_DEVICES_LIST, Server.connectedDevices);
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
