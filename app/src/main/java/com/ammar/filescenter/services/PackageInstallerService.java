package com.ammar.filescenter.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContentResolverCompat;

import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.custom.data.QueueMutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import kotlin.enums.EnumEntries;

public class PackageInstallerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static QueueMutableLiveData<String> logNotifier = new QueueMutableLiveData<>();
    Thread worker = null;
    // TODO: Learn how to install multi apk packages
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
                    String workingDirName = String.format(Locale.ENGLISH, "App-%s", UUID.randomUUID().toString());
                    File workingDir = new File(getCacheDir(), workingDirName);
                    if (!workingDir.exists()) workingDir.mkdir();

                    try (InputStream in = getContentResolver().openInputStream(intent.getData())) {
                        extractApks(in, workingDir);
                        installApkFiles(workingDir);
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

                switch (status) {
                    case PackageInstaller.STATUS_PENDING_USER_ACTION:
                        Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(confirmIntent);
                        break;
                    case PackageInstaller.STATUS_SUCCESS:
                        logNotifier.postValue("PACKAGE INSTALLED SUCCESSFULLY");
                        break;
                    case PackageInstaller.STATUS_FAILURE:
                    case PackageInstaller.STATUS_FAILURE_ABORTED:
                    case PackageInstaller.STATUS_FAILURE_BLOCKED:
                    case PackageInstaller.STATUS_FAILURE_CONFLICT:
                    case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                    case PackageInstaller.STATUS_FAILURE_INVALID:
                    case PackageInstaller.STATUS_FAILURE_STORAGE:
                        logNotifier.postValue("PACKAGE INSTALLED FAILED Because " + message );
                        break;
                    default:
                        Toast.makeText(this, "Unrecognized status received from installer: " + status,
                                Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return START_NOT_STICKY;
    }


    private void extractApks(InputStream in, File workingDir) throws IOException {
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry;

        logNotifier.postValue("Extracting apks file...");
        while ((entry = zin.getNextEntry()) != null) {
            if( !entry.getName().endsWith(".apk") ) continue;
            extractFileFromZip(zin, entry, workingDir);
            zin.closeEntry();
            logNotifier.postValue("            extracted " + entry.getName());
        }
        logNotifier.postValue("Extracting done.");

    }
    // you must close entry after passing it here
    private void extractFileFromZip(ZipInputStream in, ZipEntry entry, File workingDir) throws IOException {
        File file = new File(workingDir, entry.getName());
        if(file.createNewFile()) {
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[16 * 1024];
            int bytesRead = 0;
            while( (bytesRead = in.read(buffer)) >= 0 ) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
        } else {
            throw new ApksExtractFailed();
        }

    }

    private void installApkFiles(File workingDir) {
        PackageInstaller.Session session = null;
        try {
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = installer.createSession(params);
            session = installer.openSession(sessionId);
            addApkFilesToSession(workingDir, session);
            Intent intent = new Intent(this, PackageInstallerService.class);
            intent.setAction(Consts.ACTION_PACKAGE_INSTALLER);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE);
            IntentSender statusReceiver = pendingIntent.getIntentSender();

            session.commit(statusReceiver);
        } catch (Exception e) {
            session.abandon();
            throw new RuntimeException(e);
        }
    }

    private void addApkFilesToSession(File workingDir, PackageInstaller.Session session) {
        logNotifier.postValue("WRITING APK FILES TO SESSION...");
        File[] apkFiles = workingDir.listFiles();
        if(apkFiles == null) throw new RuntimeException("Couldn't get extracted apk files");
        for(File i : apkFiles) {

            try(OutputStream out = session.openWrite(i.getName(), 0, i.length())){
                FileInputStream in = new FileInputStream(i);
                byte[] buffer = new byte[16 * 1024];
                int bytesRead;
                while((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
                in.close();
                logNotifier.postValue("            Wrote " + i.getName() + " to session");

            } catch (IOException e) {

            }
        }
        logNotifier.postValue("WRITING APK FILES TO SESSION DONE!");
    }
    static public class ApksExtractFailed extends RuntimeException {
        public ApksExtractFailed() {
            super("Failed to extract apks file content");
        }
    }
}
