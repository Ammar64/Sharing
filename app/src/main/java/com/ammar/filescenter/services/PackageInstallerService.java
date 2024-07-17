package com.ammar.filescenter.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.custom.data.QueueMutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageInstallerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // L for LOG
    // D for dismiss
    // T for time (dismiss dialog after T time)
    // P for progress (to show or not)

    public static QueueMutableLiveData<Bundle> logNotifier = new QueueMutableLiveData<>();
    Thread worker = null;
    PackageInstaller.Session session = null;

    // The reason we need to implement a package installer is so user can
    // send this app to the other device through the app itself and then
    // use the installer to install multi apk packages.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String act = intent.getAction();
        if( act == null ) return START_NOT_STICKY;

        switch (act) {
            case Consts.ACTION_TRIGGER_APKS_INSTALL:
                if (intent.getData() == null || worker != null) {
                    stopSelf();
                    return START_NOT_STICKY;
                }

                worker = new Thread(() -> {

                    try (InputStream in = getContentResolver().openInputStream(intent.getData())) {
                        installApksFile(in);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        stopSelf();
                    }
                });
                worker.start();
                break;

            case Consts.ACTION_PACKAGE_INSTALLER:
                Bundle extras = intent.getExtras();
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

                Bundle bundle = new Bundle();
                switch (status) {
                    case PackageInstaller.STATUS_PENDING_USER_ACTION:
                        bundle.putString("L", "WAITING FOR USER TO ACCEPT.");
                        logNotifier.postValue(bundle);
                        Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(confirmIntent);
                        break;
                    case PackageInstaller.STATUS_SUCCESS:
                        bundle.putString("L", "PACKAGE INSTALLED SUCCESSFULLY");
                        bundle.putBoolean("D", true);
                        bundle.putInt("T", 1500);
                        logNotifier.postValue(bundle);
                        break;
                    case PackageInstaller.STATUS_FAILURE:
                    case PackageInstaller.STATUS_FAILURE_ABORTED:
                    case PackageInstaller.STATUS_FAILURE_BLOCKED:
                    case PackageInstaller.STATUS_FAILURE_CONFLICT:
                    case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                    case PackageInstaller.STATUS_FAILURE_INVALID:
                    case PackageInstaller.STATUS_FAILURE_STORAGE:
                        bundle.putString("L", "PACKAGE INSTALLED FAILED Because " + message );
                        logNotifier.postValue(bundle);
                        break;
                    default:
                        Toast.makeText(this, "Unrecognized status received from installer: " + status,
                                Toast.LENGTH_SHORT).show();
                }
                break;
            case Consts.ACTION_STOP_INSTALLER:
                session.abandon();
                worker.interrupt();
                session = null;
                worker = null;
                stopSelf();
                break;
        }
        return START_NOT_STICKY;
    }


    private void extractApksToSession(InputStream in, PackageInstaller.Session session) throws IOException {
        // An apks is basically a zip file that contains apk files and sometimes other files
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry;

        Bundle bundle = new Bundle();
        bundle.putString("L", "Extracting apks file to session...");
        logNotifier.postValue(bundle);
        bundle.putBoolean("P", true);
        while ((entry = zin.getNextEntry()) != null) {
            if( !entry.getName().endsWith(".apk") ) continue; // skip any file that is not an apk file
            bundle.putString("L", "extracting " + entry.getName());
            logNotifier.postValue(bundle);
            extractApkToSession(zin, entry, session);
            zin.closeEntry();
        }
        bundle.putBoolean("P", false);
        bundle.putString("L", "EXTRACTING DONE");
        logNotifier.postValue(bundle);
    }

    private void extractApkToSession(ZipInputStream zin, ZipEntry entry, PackageInstaller.Session session) throws IOException {
            try(OutputStream out = session.openWrite(entry.getName(), 0, entry.getSize())) {
                byte[] buffer = new byte[16 * 1024];
                int bytesRead;
                while ((bytesRead = zin.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            }
    }

    private void installApksFile(InputStream in) {
        try {
            if( session != null ) return;
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = installer.createSession(params);
            session = installer.openSession(sessionId);
            extractApksToSession(in, session);
            Intent intent = new Intent(this, PackageInstallerService.class);
            intent.setAction(Consts.ACTION_PACKAGE_INSTALLER);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE);
            IntentSender statusReceiver = pendingIntent.getIntentSender();

            session.commit(statusReceiver);
        } catch (Exception e) {
            session.abandon();

        }
    }

}
