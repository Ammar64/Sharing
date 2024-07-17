package com.ammar.filescenter.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageInstallerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static MutableLiveData<Bundle> installInfoNotifier = new MutableLiveData<>();
    Thread worker = null;
    PackageInstaller.Session session = null;

    // The reason we need to implement a package installer is so user can
    // send this app to the other device through the app itself and then
    // use the installer to install multi apk packages.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String act = intent.getAction();
        if (act == null) return START_NOT_STICKY;
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
                        bundle.putString("title", getString(R.string.installing));
                        bundle.putString("text", getString(R.string.installing));
                        installInfoNotifier.postValue(bundle);
                        Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(confirmIntent);
                        break;
                    case PackageInstaller.STATUS_SUCCESS:
                        bundle.putString("title", getString(R.string.done));
                        bundle.putString("text", getString(R.string.package_installed_success));
                        bundle.putBoolean("stopProgress", true);
                        bundle.putBoolean("buttonOk", true);
                        installInfoNotifier.postValue(bundle);
                        break;
                    case PackageInstaller.STATUS_FAILURE:
                    case PackageInstaller.STATUS_FAILURE_ABORTED:
                    case PackageInstaller.STATUS_FAILURE_BLOCKED:
                    case PackageInstaller.STATUS_FAILURE_CONFLICT:
                    case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                    case PackageInstaller.STATUS_FAILURE_INVALID:
                    case PackageInstaller.STATUS_FAILURE_STORAGE:
                        bundle.putString("title", getString(R.string.failed));
                        bundle.putString("text", message);
                        bundle.putBoolean("stopProgress", true);
                        installInfoNotifier.postValue(bundle);
                        break;
                    default:
                        Toast.makeText(this, "Unrecognized status received from installer: " + status,
                                Toast.LENGTH_SHORT).show();
                }
                break;
            case Consts.ACTION_STOP_INSTALLER:
                if (session != null) session.abandon();
                if (worker != null) worker.interrupt();
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
        while ((entry = zin.getNextEntry()) != null) {
            if (!entry.getName().endsWith(".apk"))
                continue; // skip any file that is not an apk file
            bundle.putString("text", getString(R.string.extracting_x, entry.getName()));
            installInfoNotifier.postValue(bundle);
            extractApkToSession(zin, entry, session);
            zin.closeEntry();
        }
        bundle.putString("text", getString(R.string.extracting_done));
        installInfoNotifier.postValue(bundle);

    }

    private void extractApkToSession(ZipInputStream zin, ZipEntry entry, PackageInstaller.Session session) throws IOException {
        try (OutputStream out = session.openWrite(entry.getName(), 0, entry.getSize())) {
            byte[] buffer = new byte[16 * 1024];
            int bytesRead;
            while ((bytesRead = zin.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void installApksFile(InputStream in) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("title", getString(R.string.initialising));
            installInfoNotifier.postValue(bundle);

            if (session != null) return;
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
