package com.ammar.sharing.custom.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class OverlayTransformation extends BitmapTransformation {

    private final Context context;
    private final int overlayResId;

    public OverlayTransformation(Context context, @DrawableRes int overlayResId) {
        this.context = context.getApplicationContext();
        this.overlayResId = overlayResId;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap overlay = BitmapFactory.decodeResource(context.getResources(), overlayResId);

        Bitmap result = Bitmap.createBitmap(toTransform.getWidth(), toTransform.getHeight(), toTransform.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(toTransform, 0f, 0f, null);

        // Calculate center position
        float left = (toTransform.getWidth() - overlay.getWidth()) / 2f;
        float top = (toTransform.getHeight() - overlay.getHeight()) / 2f;

        canvas.drawBitmap(overlay, left, top, null); // Draw overlay centered

        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(("OverlayTransformation" + overlayResId).getBytes(CHARSET));
    }
}