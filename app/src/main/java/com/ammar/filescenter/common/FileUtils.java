package com.ammar.filescenter.common;


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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ammar.filescenter.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class FileUtils {
    private FileUtils() {
    }

    public static String getFileTypeName(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        return ext.toUpperCase();
    }


    /**
     * @param img ImageView to load icon into
     * @param file file of which icon or image will be loaded
     * @return return true if we should remove the line under the image. false otherwise
     */
    public static boolean setFileIcon(ImageView img, File file) {
        int paddingPx = (int) Utils.dpToPx(40);
        img.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        String filename = file.getName();
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
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
                if (mime.startsWith("image/")) {
                    img.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            img.getViewTreeObserver().removeOnPreDrawListener(this);
                            int iW = img.getMeasuredWidth();
                            int iH = img.getMeasuredHeight();

                            Glide.with(img.getContext())
                                    .load(file)
                                    .error(R.drawable.icon_image)
                                    .addListener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                            img.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                                            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                            return false;
                                        }
                                        @Override
                                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                            img.setPadding(0, 0, 0, 0);
                                            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            return false;
                                        }
                                    })
                                    .override(iW, iH)
                                    .into(img);

                            return true;
                        }
                    });
                    return true;
                } else if( mime.startsWith("video/") ) {
                    Glide.with(img.getContext())
                            .load(R.drawable.icon_video)
                            .into(img);
                    return false;
                }
                break;
        }
        Glide.with(img.getContext())
                .load(R.drawable.icon_file)
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

}
