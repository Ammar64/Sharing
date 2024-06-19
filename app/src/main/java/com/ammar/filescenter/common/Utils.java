package com.ammar.filescenter.common;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.ammar.filescenter.activities.MainActivity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Utils {

    static {
        System.loadLibrary("NativeQRCodeGen");
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
    public static void setRes(Resources res) {
        Utils.res = res;
    }

    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                res.getDisplayMetrics()
        );
    }


    public static String readLineUTF8(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int c;
        for (c = inputStream.read(); c != -1 ; c = inputStream.read()) {
            if( c == '\n' ) break;
            if( c == '\r' ) {
                if(inputStream.read() == '\n' ) break;
                else throw new RuntimeException("No \\n after \\r");
            }
            byteArrayOutputStream.write(c);
        }
        if (c == -1 && byteArrayOutputStream.size() == 0) {
            return "";
        }
        return byteArrayOutputStream.toString("UTF-8");
    }


    public static String getPathFromUri(final Context context, final Uri uri) {

        // final boolean isKitKat = true;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public static native byte[] encodeTextToQR(String text);
    public static Bitmap QrCodeArrayToBitmap(byte[] qrCodeBytes) {
        int qrColor;
        if(MainActivity.darkMode)
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
    public static File createNewFile(String upload_dir, String fullFileName) {
        int num = 1;
        int ext_index = fullFileName.lastIndexOf('.');

        String fileNameNoExt = ext_index < 0 ? fullFileName : fullFileName.substring(0, ext_index);
        String fileExtension = ext_index < 0 ? "" : fullFileName.substring(ext_index);

        File upload_file = new File(upload_dir, fullFileName);
        while(upload_file.exists()) {
            String localFileName = String.format(Locale.ENGLISH, "%s (%d)%s", fileNameNoExt, num++, fileExtension);
            upload_file = new File(upload_dir, localFileName);
        }
        return upload_file;
    }
}
