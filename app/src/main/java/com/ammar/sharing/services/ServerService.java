package com.ammar.sharing.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.Global;
import com.ammar.sharing.common.LiveDataSingletons;
import com.ammar.sharing.nativebackend.lambda.LambdaReturnInt;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.nativebackend.DownloadItemsManager;
import com.ammar.sharing.network.WebServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    // actions
    public static final String ACTION_MULTIPLE_ACTIONS = "ACTION_MULTIPLE_ACTIONS";
    public static final String ACTION_TOGGLE_SERVER = "ACTION_TOGGLE_SERVER";
    public static final String ACTION_STOP_SERVICE_IF_SERVER_OFF = "ACTION_STOP_SERVICE_IF_SERVER_OFF";
    public static final String ACTION_POST_SERVER_STATUS = "ACTION_GET_SERVER_STATUS";
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
    public static final String EXTRA_DOWNLOAD_INDEX = "EXTRA_DOWNLOAD_INDEX";
    public static final String EXTRA_ACTIONS = "EXTRA_ACTIONS";
    final Intent serverStatusIntent = new Intent(ServerService.ACTION_POST_SERVER_STATUS);
    private final int FOREGROUND_NOTIFICATION_ID = 1;
    private final IBinder binder = new LocalBinder();
    private WebServer webServer;
    private boolean isRunningFirstTime = true;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        webServer = new WebServer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        String actionReceived = intent.getAction();
        String action = actionReceived != null ? actionReceived : "";
        if (ServerService.ACTION_MULTIPLE_ACTIONS.equals(action)) {
            ArrayList<String> actions = intent.getStringArrayListExtra(ServerService.EXTRA_ACTIONS);
            assert actions != null;
            for (String i : actions) {
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
            case ServerService.ACTION_STOP_SERVICE_IF_SERVER_OFF:
                if (!webServer.isRunning()) {
                    stopSelf();
                }
            case ServerService.ACTION_POST_SERVER_STATUS:
                LiveDataSingletons.serverStatusObserver.postValue(webServer.isRunning());
                break;
            case ServerService.ACTION_RESTART_SERVER:
                restartServer();
                Toast.makeText(this, getResources().getString(R.string.server_port_changed, WebServer.PORT_NUMBER), Toast.LENGTH_SHORT).show();
                break;
            case ServerService.ACTION_UPDATE_NOTIFICATION_TEXT:
                startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
                break;
            case ServerService.ACTION_ADD_FILES_PATHS:
                ArrayList<String> filePaths = intent.getStringArrayListExtra(ServerService.EXTRA_FILES_PATHS);
                assert filePaths != null;
                for (String i : filePaths) {
                    File file = new File(i);
                    DownloadItemsManager.addNewDownloadItem(file.getName(), file.length(), () -> {
                        try {
                            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).detachFd();
                        } catch (IOException e) {
                            return -1;
                        }
                    });
                    Bundle fb = new Bundle();
                    fb.putChar("action", 'A');
                    LiveDataSingletons.downloadsListNotifier.postValue(fb);
                }
                break;
            case ServerService.ACTION_ADD_APPS_PACKAGES_NAMES:
                ArrayList<String> packages_name = intent.getStringArrayListExtra(ServerService.EXTRA_APPS_PACKAGES);
                if (packages_name != null) {
                    add_apps(packages_name);
                }
                Bundle ab = new Bundle();
                ab.putChar("action", 'A');
                LiveDataSingletons.downloadsListNotifier.postValue(ab);
                break;
            case ServerService.ACTION_ADD_URIS:
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(ServerService.EXTRA_URIS);
                assert uris != null;
                for (Uri i : uris) {
                    Sharable.sharablesList.add(new Sharable(getContentResolver(), i));
                }
                Bundle ub = new Bundle();
                ub.putChar("action", 'A');
                LiveDataSingletons.downloadsListNotifier.postValue(ub);
                break;
            case ServerService.ACTION_REMOVE_DOWNLOAD:
                int index = intent.getIntExtra(ServerService.EXTRA_DOWNLOAD_INDEX, -1);
                if (index <= -1) {
                    Log.e("MYLOG", "ServerService.EXTRA_DOWNLOAD_INDEX was not supplied");
                    break;
                }
                ;
                DownloadItemsManager.removeDownloadItem(index);
                Bundle remove_info = new Bundle();
                remove_info.putChar("action", 'R');
                remove_info.putInt("index", index);
                LiveDataSingletons.downloadsListNotifier.forcePostValue(remove_info);
                break;
            case ServerService.ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN:
                if (!webServer.isRunning()) {
                    Log.d("MYLOG", "Stopping App process");
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            default:
                break;
        }
    }

    private void add_apps(ArrayList<String> packages_name) {
        for (String i : packages_name) {
            try {
                var pm = this.getPackageManager();
                var appInfo = pm.getApplicationInfo(i, 0);
                var baseApkPath = appInfo.publicSourceDir;
                var app_name = appInfo.loadLabel(pm).toString();
                var baseFile = new File(baseApkPath);
                LambdaReturnInt base_file_fd_opener = () -> {
                    try {
                        return ParcelFileDescriptor.open(baseFile, ParcelFileDescriptor.MODE_READ_ONLY).detachFd();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                };
                // check for splits
                String[] splitsDirs = appInfo.splitPublicSourceDirs;
                if (splitsDirs != null) {
                    var item_name = String.format(Locale.ENGLISH, "%s.apks", app_name);
                    // enough space for each split file + the base apk
                    var children_names = new String[1 + splitsDirs.length];
                    var children_fd_openers = new LambdaReturnInt[1 + splitsDirs.length];
                    // total size of each file in the apks
                    long total_size = 0;


                    total_size += baseFile.length();
                    children_names[0] = baseFile.getName();
                    children_fd_openers[0] = base_file_fd_opener;

                    for (int j = 0; j < splitsDirs.length; j++) {
                        var splitFile = new File(splitsDirs[j]);
                        total_size += splitFile.length();
                        children_names[j + 1] = splitFile.getName();
                        children_fd_openers[j + 1] = () -> {
                            try {
                                return ParcelFileDescriptor.open(splitFile, ParcelFileDescriptor.MODE_READ_ONLY).detachFd();
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        };
                    }

                    DownloadItemsManager.addNewGroupedDownloadItem(item_name, total_size, children_names, children_fd_openers);

                } else {
                    var item_name = String.format(Locale.ENGLISH, "%s.apk", app_name);
                    long size = baseFile.length();
                    DownloadItemsManager.addNewDownloadItem(item_name, size, base_file_fd_opener);
                }

            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void toggleServer() {
        if (webServer.isRunning()) {
            webServer.Stop();
        } else {
            webServer.Start();
        }
        toggleForegroundAndReportToActivity();
    }

    private void restartServer() {
        if (webServer.isRunning()) {
            webServer.Stop();
            webServer.Start();
        }
        toggleForegroundAndReportToActivity();
    }

    private void toggleForegroundAndReportToActivity() {
        boolean isServerRunning = webServer.isRunning();

        // stop notification or start it
        if (isServerRunning && isRunningFirstTime) {
            startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(this));
            isRunningFirstTime = false;
        } else if (isServerRunning) {
            // update notification
            Notification notification = buildNotification(this);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
        } else {
            stopForegroundAndNotification();
            isRunningFirstTime = true;
        }

        // notify observer.
        LiveDataSingletons.serverStatusObserver.postValue(isServerRunning);
    }

    public Notification buildNotification(Context context) {
        String address = ServerService.getIpAddress();
        if (address == null) address = "localhost";
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Global.SERVER_NOTIFICATION_CHANNEL_ID).setContentTitle(getResources().getString(R.string.svr_running)).setSmallIcon(android.R.drawable.ic_dialog_info).setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        return new NotificationCompat.BigTextStyle(builder).bigText(getResources().getString(R.string.svr_notification_message, address, String.format(Locale.ENGLISH, "%d", WebServer.PORT_NUMBER))).build();
    }

    private void stopForegroundAndNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    public WebServer getServer() {
        return webServer;
    }

    public class LocalBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }
}
