package com.ammar.sharing.common.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Callable;

public class Utils {

    static {
        System.loadLibrary("nativeutils");
    }

    public static String getFormattedSize(long size) {
        double s = size;
        String[] levels = {"B", "KB", "MB", "GB", "TB", "PB"};
        int level = 0;
        boolean isGood = false;
        while (!isGood) {
            if (s > 1200 && level < levels.length) {
                s /= 1024;
                level++;
            } else {
                isGood = true;
            }
        }
        return String.format(Locale.ENGLISH, "%.2f %s", s, levels[level]);
    }


    private static Resources res;
    private static SharedPreferences settings;
    private static PackageManager pm;
    private static ContentResolver cr;
    private static AssetManager assetManager;


    public static void setupUtils(Context ctx) {
        Utils.res = ctx.getResources();
        Utils.assetManager = ctx.getAssets();
        Utils.settings = ctx.getSharedPreferences(Consts.PREF_SETTINGS, Context.MODE_PRIVATE);
        Utils.pm = ctx.getPackageManager();
        Utils.cr = ctx.getContentResolver();
        Utils.applicationContext = ctx;

    }

    private static Context applicationContext;
    public static Context getAppCtx() {
        return applicationContext;
    }
    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                res.getDisplayMetrics()
        );
    }

    public static SharedPreferences getSettings() {
        return Utils.settings;
    }

    public static String readLineUTF8(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != -1; c = inputStream.read()) {
            if (c == '\n') break;
            if (c == '\r') {
                if (inputStream.read() == '\n') break;
                else throw new RuntimeException("No \\n after \\r");
            }
            byteArrayOutputStream.write(c);
        }
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return "";
        }
        return byteArrayOutputStream.toString("UTF-8");
    }

    public static native byte[] encodeTextToQR(String text);


    public static Bitmap QrCodeArrayToBitmap(byte[] qrCodeBytes, boolean darkMode) {
        int qrColor;
        if (darkMode)
            qrColor = Color.WHITE;
        else
            qrColor = Color.BLACK;

        int size = (int) Math.sqrt(qrCodeBytes.length);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int pixel = qrCodeBytes[size * j + i] == 1 ? qrColor : Color.TRANSPARENT;
                bitmap.setPixel(j, i, pixel);
            }
        }

        return getResizedBitmap(bitmap, 5000, 5000);
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }

    @NonNull
    public static File createNewFile(File upload_dir, String fullFileName) {
        int num = 1;
        int ext_index = fullFileName.lastIndexOf('.');

        String fileNameNoExt = ext_index < 0 ? fullFileName : fullFileName.substring(0, ext_index);
        String fileExtension = ext_index < 0 ? "" : fullFileName.substring(ext_index);

        File upload_file = new File(upload_dir, fullFileName);
        while (upload_file.exists()) {
            String localFileName = String.format(Locale.ENGLISH, "%s (%d)%s", fileNameNoExt, num++, fileExtension);
            upload_file = new File(upload_dir, localFileName);
        }
        return upload_file;
    }


    public static String getMimeType(String name, boolean extraTypes) {
        String type = null;

        // extension
        String extension = null;
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex != -1) extension = name.substring(dotIndex + 1);

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (type == null) {
            if (extraTypes)
                type = getExtraTypes(name.substring(name.lastIndexOf(".") + 1));
            else type = "*/*";
        }
        return type;
    }

    public static String getMimeType(String name) {
        return getMimeType(name, true);
    }

    private static String getExtraTypes(String ext) {
        return switch (ext) {
            case "html" -> "text/html";
            case "apk" -> "application/vnd.android.package-archive";
            case "doc", "dot" -> "application/msword";
            case "docx" ->
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "dotx" ->
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
            case "docm" -> "application/vnd.ms-word.document.macroEnabled.12";
            case "dotm" -> "application/vnd.ms-word.template.macroEnabled.12";
            case "xls", "xlt", "xla" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xltx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
            case "xlsm" -> "application/vnd.ms-excel.sheet.macroEnabled.12";
            case "xltm" -> "application/vnd.ms-excel.template.macroEnabled.12";
            case "xlam" -> "application/vnd.ms-excel.addin.macroEnabled.12";
            case "xlsb" -> "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pot" -> "application/vnd.ms-powerpoint";
            case "pps" -> "application/vnd.ms-powerpoint";
            case "ppa" -> "application/vnd.ms-powerpoint";
            case "pptx" ->
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "potx" -> "application/vnd.openxmlformats-officedocument.presentationml.template";
            case "ppsx" -> "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
            case "ppam" -> "application/vnd.ms-powerpoint.addin.macroEnabled.12";
            case "pptm" -> "application/vnd.ms-powerpoint.presentation.macroEnabled.12";
            case "potm" -> "application/vnd.ms-powerpoint.template.macroEnabled.12";
            case "ppsm" -> "application/vnd.ms-powerpoint.slideshow.macroEnabled.12";
            case "mdb" -> "application/vnd.ms-access";
            default -> "*/*";
        };
    }

    public static boolean isDocumentType(String mimeType) {
        return mimeType.equals("application/pdf")
                || mimeType.equals("application/msword")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.template")
                || mimeType.equals("application/vnd.ms-word.document.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-word.template.macroEnabled.12")

                || mimeType.equals("application/vnd.ms-excel")

                || mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.template")
                || mimeType.equals("application/vnd.ms-excel.sheet.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-excel.template.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-excel.addin.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-excel.sheet.binary.macroEnabled.12")

                || mimeType.equals("application/vnd.ms-powerpoint")

                || mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.template")
                || mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.slideshow")
                || mimeType.equals("application/vnd.ms-powerpoint.addin.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-powerpoint.presentation.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-powerpoint.template.macroEnabled.12")
                || mimeType.equals("application/vnd.ms-powerpoint.slideshow.macroEnabled.12")

                || mimeType.equals("application/vnd.ms-acces");
    }

    public static void createAppDirs() throws IOException {
        boolean dirsMade = true;
        dirsMade &= Consts.Sharing.mkdirs();
        dirsMade &= Consts.appsDir.mkdir();
        dirsMade &= Consts.imagesDir.mkdir();
        dirsMade &= Consts.audioDir.mkdir();
        dirsMade &= Consts.otherDir.mkdir();
        dirsMade &= Consts.videosDir.mkdir();
        dirsMade &= Consts.documentsDir.mkdir();

        if (!dirsMade) throw new IOException("Failed to make app directories");
    }

    public static void setLocale(Context context, String languageTag) {
        Locale locale;
        if (languageTag == null || languageTag.isEmpty()) {
            locale = Consts.systemLocale;
        } else {
            locale = Locale.forLanguageTag(languageTag);
        }
        assert locale != null;
        Locale.setDefault(locale);
        Resources appRes = context.getApplicationContext().getResources();
        Resources actRes = context.getResources();

        Configuration appConfig = appRes.getConfiguration();
        appConfig.setLocale(locale);
        appConfig.setLayoutDirection(locale);
        appRes.updateConfiguration(appConfig, appRes.getDisplayMetrics());

        Configuration actConfig = actRes.getConfiguration();
        actConfig.setLocale(locale);
        actConfig.setLayoutDirection(locale);
        actRes.updateConfiguration(actConfig, actRes.getDisplayMetrics());
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static byte[] readRawRes(@RawRes int id) throws IOException {
        try (InputStream in = res.openRawResource(id)) {
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            return buffer;
        }
    }

    public static void showErrorDialog(String title, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        Log.e("ERROR_DIALOG", title + "\n" + message);
        Data.alertNotifier.postValue(bundle);
    }

    public static File getUploadDir(String fileName) {
        String mimeType = Utils.getMimeType(fileName);
        if (mimeType.startsWith("image/")) {
            return Consts.imagesDir;
        } else if (mimeType.equals("application/vnd.android.package-archive")) { // apk files in apps folder
            return Consts.appsDir;
        } else if (mimeType.startsWith("video/")) {
            return Consts.videosDir;
        } else if (mimeType.startsWith("audio/")) {
            return Consts.audioDir;
        } else if (Utils.isDocumentType(mimeType)) {
            return Consts.documentsDir;
        } else if (fileName.substring(fileName.lastIndexOf(".")).equals(".apks")) { // apks files should go to apps folder
            return Consts.appsDir;
        } else {
            return Consts.otherDir;
        }
    }

    public static Resources getRes() {
        return res;
    }
    public static PackageManager getPm() {return pm;}

    public static AssetManager getAssetManager() { return assetManager; }
    public static String getFormattedTime(long milliSeconds) {
        long x = milliSeconds / 1000;
        final long seconds = x % 60;
        x /= 60;
        final long minutes = x % 60;
        x /= 60;
        final long hours = x % 24;
        x /= 24;
        final long days = x;


        try {
            int timesUsed = 0;
            StringBuilder timeStringBuilder = new StringBuilder();

            Callable<String> buildTimeString = () -> {
                String timeString = timeStringBuilder.toString();
                return timeString.substring(0, timeString.length() - 1);
            };

            if (days != 0) {
                timeStringBuilder.append(res.getString(R.string.days, days)).append(' ');
                timesUsed++;
            }

            if (hours != 0) {
                timeStringBuilder.append(res.getString(R.string.hours, hours)).append(' ');
                timesUsed++;
            }

            if (timesUsed >= 2) return buildTimeString.call();

            if (minutes != 0) {
                timeStringBuilder.append(res.getString(R.string.minutes, minutes)).append(' ');
                timesUsed++;
            }

            if (timesUsed >= 2) return buildTimeString.call();

            if (seconds != 0) {
                timeStringBuilder.append(res.getString(R.string.seconds, seconds)).append(' ');
                timesUsed++;
            }

            if( timesUsed == 0 ) {
                return res.getString(R.string.milliSeconds, milliSeconds);
            }

            return buildTimeString.call();
        } catch (Exception e) {
            return "Couldn't get time";
        }
    }

    public static boolean isLangSupported(String lang) {
        assert Consts.langCodes != null;
        for(final String i : Consts.langCodes) {
            if(i.equals(lang)) return true;
        }
        return false;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static ContentResolver getCR() {
        return cr;
    }

    private static WifiManager.MulticastLock mMulticastLock;

    public static void aquireMulticastLock() {
        if( mMulticastLock == null ) {
            @SuppressLint("WifiManagerLeak") // This the application context
            WifiManager wifiManager = (WifiManager) Utils.getAppCtx().getSystemService(Context.WIFI_SERVICE);
            mMulticastLock = wifiManager.createMulticastLock("multicastLock");
            mMulticastLock.setReferenceCounted(true);
        }
        mMulticastLock.acquire();
    }

    public static void releaseMulticastLock() {
        if (mMulticastLock != null) {
            mMulticastLock.release();
        }
    }

}
