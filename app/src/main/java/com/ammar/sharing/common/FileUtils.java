package com.ammar.sharing.common;


import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.models.Sharable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class FileUtils {
    public static final int FILE_TYPE_IMAGE = 0;
    public static final int FILE_TYPE_VIDEO = 1;
    public static final int FILE_TYPE_AUDIO = 2;
    public static final int FILE_TYPE_DOCUMENT = 3;

    private FileUtils() {
    }

    public static String getFileTypeName(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        return ext.toUpperCase();
    }


    /**
     * @param img            ImageView to load icon into
     * @param fileTypeNameTV TextView we will add a small icon to
     * @param file           file of which icon or image will be loaded
     * @return return true if we should remove the line under the image. false otherwise
     */
    public static boolean setFileIcon(ImageView img, TextView fileTypeNameTV, File file) {
        String filename = file.getName();
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        switch (ext) {
            case "pdf":
                img.setImageResource(R.drawable.icon_pdf);
                return false;
            case "apk":
                PackageManager pm = img.getContext().getApplicationContext().getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getPath(), 0);
                if (packageInfo != null) {
                    ApplicationInfo appInfo = packageInfo.applicationInfo;
                    appInfo.sourceDir = file.getPath();
                    appInfo.publicSourceDir = file.getPath();
                    Drawable appIcon = appInfo.loadIcon(pm);
                    img.setImageDrawable(appIcon);
                    return false;
                } else break;
            default:
                String mime = Utils.getMimeType(file.getName(), false);
                boolean isImage = mime.startsWith("image/");
                boolean isVideo = mime.startsWith("video/");
                if (isImage || isVideo) {
                    img.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            img.getViewTreeObserver().removeOnPreDrawListener(this);
                            int iW = img.getMeasuredWidth();
                            int iH = img.getMeasuredHeight();
                            fileTypeNameTV.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(img.getResources(), isImage ? R.drawable.icon_image : R.drawable.icon_video, null), null);
                            Glide.with(img.getContext())
                                    .load(file)
                                    .error(isImage ? R.drawable.icon_image : R.drawable.icon_video)
                                    .addListener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                            img.setPadding(0, 0, 0, 0);
                                            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            return false;
                                        }
                                    })
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .override(iW, iH)
                                    .into(img);


                            return true;
                        }
                    });
                    return true;
                } else if (mime.startsWith("audio/")) {
                    Glide.with(img.getContext())
                            .load(R.drawable.icon_audio)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(img);
                    return false;
                } else if (Utils.isDocumentType(mime)) {
                    Glide.with(img.getContext())
                            .load(R.drawable.icon_document)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(img);
                }
                break;
        }
        Glide.with(img.getContext())
                .load(R.drawable.icon_file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(img);
        return false;
    }

    public static Bitmap decodeSampledImage(File file, int iW, int iH) {
        // get image bounds
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        // get inSampleSize
        options.inSampleSize = calculateInSampleSize(options, iW, iH);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static byte[] readWholeFile(File file) throws IOException {
        long size = file.length();
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] data = new byte[(int) size];
            in.read(data);
            return data;
        }
    }

    public static void overwriteFile(File file, byte[] data) throws IOException {
        file.createNewFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
    }

    public static void findFilesTypesRecursively(File root, ArrayList<File> filesType, int type) {
        findFilesTypesRecursively(root, filesType, type, 0);
    }

    private static void findFilesTypesRecursively(File root, ArrayList<File> filesType, int type, int depth) {

        FileFilter fileFilter;
        switch (type) {
            case FILE_TYPE_IMAGE:
                fileFilter = (file) -> {
                    String name = file.getName();
                    return Utils.getMimeType(name, false).startsWith("image/") || file.isDirectory();
                };
                break;
            case FILE_TYPE_VIDEO:
                fileFilter = (file) -> {
                    String name = file.getName();
                    return Utils.getMimeType(name, false).startsWith("video/") || file.isDirectory();
                };
                break;
            case FILE_TYPE_AUDIO:
                fileFilter = (file) -> {
                    String name = file.getName();
                    return Utils.getMimeType(name, false).startsWith("audio/") || file.isDirectory();
                };
                break;
            case FILE_TYPE_DOCUMENT:
                fileFilter = (file) -> {
                    String name = file.getName();
                    return Utils.isDocumentType(Utils.getMimeType(name, true)) || file.isDirectory();
                };
                break;
            default:
                throw new RuntimeException("Not a file type");
        }

        File[] currentList = root.listFiles(fileFilter);
        if( currentList == null ) return;
        for (File i : currentList) {
            if (i.isDirectory()) {
                if( depth < 3 ) {
                    findFilesTypesRecursively(i, filesType, type, depth + 1);
                }
            } else if (i.isFile()) {
                filesType.add(i);
            }
        }
    }


    public static void zipFilesToOutStream(OutputStream out, Sharable[] sharables) {

    }
}
