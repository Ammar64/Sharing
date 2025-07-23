package com.ammar.sharing.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.network.sessions.CLISession;
import com.ammar.sharing.network.sessions.DownloadSession;
import com.ammar.sharing.network.sessions.DynamicAssetsSession;
import com.ammar.sharing.network.sessions.MessagesSession;
import com.ammar.sharing.network.sessions.NoJSSession;
import com.ammar.sharing.network.sessions.RedirectSession;
import com.ammar.sharing.network.sessions.UploadSession;
import com.ammar.sharing.network.sessions.UserSession;
import com.ammar.sharing.network.websocket.sessions.InfoWSSession;
import com.ammar.sharing.network.websocket.sessions.MessagesWSSession;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

/**
 * @noinspection unused
 */
public class ServerService extends Service {
    private final int FOREGROUND_NOTIFICATION_ID = 1;
    private Server server;
    final Intent serverStatusIntent = new Intent(ServerService.ACTION_GET_SERVER_STATUS);

    // actions
    public static final String ACTION_MULTIPLE_ACTIONS = "ACTION_MULTIPLE_ACTIONS";
    public static final String ACTION_TOGGLE_SERVER = "ACTION_TOGGLE_SERVER";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_GET_SERVER_STATUS = "ACTION_GET_SERVER_STATUS";
    public static final String ACTION_RESTART_SERVER = "ACTION_RESTART_SERVER";
    public static final String ACTION_UPDATE_NOTIFICATION_TEXT = "ACTION_UPDATE_NOTIFICATION_TEXT";
    public static final String ACTION_ADD_FILES_PATHS = "ACTION_ADD_FILES_PATHS";
    public static final String ACTION_ADD_APPS_PACKAGES_NAMES = "ACTION_ADD_APPS_PACKAGES_NAMES";
    public static final String ACTION_ADD_URIS = "ACTION_ADD_URIS";
    public static final String ACTION_REMOVE_DOWNLOAD = "ACTION_REMOVE_DOWNLOAD";
    public static final String ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN = "ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN";

    // extras
    public static final String EXTRA_FILES_PATHS = "EXTRA_FILES_PATHS";
    public static final String EXTRA_APPS_PACKAGES = "EXTRA_APPS_PACKAGES";
    public static final String EXTRA_URIS = "EXTRA_URIS";
    public static final String EXTRA_DOWNLOAD_UUID = "EXTRA_DOWNLOAD_UUID";
    public static final String EXTRA_ACTIONS = "EXTRA_ACTIONS";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        server = new Server(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        String actionReceived = intent.getAction();
        String action = actionReceived != null ? actionReceived : "";
        if( ServerService.ACTION_MULTIPLE_ACTIONS.equals(action) ) {
            ArrayList<String> actions = intent.getStringArrayListExtra(ServerService.EXTRA_ACTIONS);
            assert actions != null;
            for(String i : actions) {
                performAction(i, intent);
            }
        } else {
            performAction(action, intent);
        }
        return START_STICKY;
    }

    public void performAction(@NonNull String action, Intent intent) {
        switch (action) {
            case ServerService.ACTION_TOGGLE_SERVER:
                toggleServer();
                break;
            case ServerService.ACTION_STOP_SERVICE:
                stopSelf();
            case ServerService.ACTION_GET_SERVER_STATUS:
                Data.serverStatusObserver.postValue(server.isRunning());
                break;
            case ServerService.ACTION_RESTART_SERVER:
                restartServer();
                Toast.makeText(this, getResources().getString(R.string.server_port_changed, Server.PORT_NUMBER), Toast.LENGTH_SHORT).show();
                break;
            case ServerService.ACTION_UPDATE_NOTIFICATION_TEXT:
                startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
                break;
            case ServerService.ACTION_ADD_FILES_PATHS:
                ArrayList<String> filePaths = intent.getStringArrayListExtra(ServerService.EXTRA_FILES_PATHS);
                assert filePaths != null;
                for (String i : filePaths) {
                    Sharable.sharablesList.add(new Sharable(i));
                }
                Bundle fb = new Bundle();
                fb.putChar("action", 'A');
                Data.downloadsListNotifier.postValue(fb);
                break;
            case ServerService.ACTION_ADD_APPS_PACKAGES_NAMES:
                ArrayList<String> packages_name = intent.getStringArrayListExtra(ServerService.EXTRA_APPS_PACKAGES);
                if (packages_name != null) {
                    for (String i : packages_name) {
                        try {
                            Sharable.sharablesList.add(new SharableApp(this, i));
                        } catch (PackageManager.NameNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                Bundle ab = new Bundle();
                ab.putChar("action", 'A');
                Data.downloadsListNotifier.postValue(ab);
                break;
            case ServerService.ACTION_ADD_URIS:
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(ServerService.EXTRA_URIS);
                assert uris != null;
                for (Uri i : uris) {
                    Sharable.sharablesList.add(new Sharable(getContentResolver(), i));
                }
                Bundle ub = new Bundle();
                ub.putChar("action", 'A');
                Data.downloadsListNotifier.postValue(ub);
                break;
            case ServerService.ACTION_REMOVE_DOWNLOAD:
                String uuid = intent.getStringExtra(ServerService.EXTRA_DOWNLOAD_UUID);
                int index = 0;
                for (Sharable i : Sharable.sharablesList) {
                    if (i.getUUID().toString().equals(uuid)) {
                        Sharable.sharablesList.remove(index);
                        break;
                    }
                    index++;
                }
                Bundle remove_info = new Bundle();
                remove_info.putChar("action", 'R');
                remove_info.putInt("index", index);
                Data.downloadsListNotifier.forcePostValue(remove_info);
                break;
            case ServerService.ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN:
                if (!server.isRunning()) {
                    Log.d("MYLOG", "Stopping App process");
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            default:
                break;
        }
    }

    private void toggleServer() {
        if (server.isRunning()) {
            User.closeAllSockets();
            server.Stop();
        } else {
            server.Start();
        }
        toggleForegroundAndReportToActivity();
    }

    private void restartServer() {
        if (server.isRunning()) {
            User.closeAllSockets();
            server.Stop();
            server.Start();
        }
        toggleForegroundAndReportToActivity();
    }

    private boolean isRunningFirstTime = true;
    private void toggleForegroundAndReportToActivity() {
        boolean isServerRunning = server.isRunning();

        // stop notification or start it
        if (isServerRunning && isRunningFirstTime) {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
            isRunningFirstTime = false;
        } else if(isServerRunning) {
            // update notification
            Notification notification = buildNotification(this);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
        } else {
            stopForegroundAndNotification();
            isRunningFirstTime = true;
        }

        // notify observer.
        Data.serverStatusObserver.postValue(isServerRunning);
    }

    public Notification buildNotification(Context context) {
        String address = ServerService.getIpAddress();
        if (address == null) address = "localhost";
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Consts.SERVER_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.svr_running))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        return new NotificationCompat
                .BigTextStyle(builder)
                .bigText(getResources().getString(R.string.svr_notification_message, address, String.format(Locale.ENGLISH, "%d", Server.PORT_NUMBER)))
                .build();
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

    public Server getServer() {
        return server;
    }
}
