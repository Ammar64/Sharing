package com.ammar.sharing.custom.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import androidx.core.graphics.createBitmap

class OverlayTransformation(context: Context, @param:DrawableRes private val overlayResId: Int) :
    BitmapTransformation() {
    private val context: Context = context.applicationContext

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val overlay = BitmapFactory.decodeResource(context.resources, overlayResId)

        val result =
            createBitmap(toTransform.getWidth(), toTransform.getHeight(), toTransform.getConfig()!!)

        val canvas = Canvas(result)
        canvas.drawBitmap(toTransform, 0f, 0f, null)

        // Calculate center position
        val left = (toTransform.getWidth() - overlay.getWidth()) / 2f
        val top = (toTransform.getHeight() - overlay.getHeight()) / 2f

        canvas.drawBitmap(overlay, left, top, null) // Draw overlay centered

        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("OverlayTransformation$overlayResId").toByteArray(CHARSET))
    }
}