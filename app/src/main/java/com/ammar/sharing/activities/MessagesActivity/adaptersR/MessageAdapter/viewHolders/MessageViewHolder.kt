package com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.viewHolders

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.common.utils.Utils
import java.util.Locale

class MessageViewHolder(itemView: LinearLayout) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView[0] as TextView
    companion object {
        fun constructNewMessageView(context: Context, isRemote: Boolean): LinearLayout {

            val cornerRadius = Utils.dpToPx(12F)
            val messageView = TextView(context).apply {
                val padding = Utils.dpToPx(8f).toInt()
                setPadding(padding)
                if(isRemote) {
                    setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_color_dark, null))
                } else {
                    setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_color_light, null))
                }
            }
            // setup corners
            val config = context.resources.configuration;
            val layoutDirection = config.layoutDirection;

            val isRTL: Boolean
            if( layoutDirection == View.LAYOUT_DIRECTION_RTL ){
                isRTL = true
            } else {
                isRTL = false;
            }

            val bottomStartCorner: Float
            val bottomEndCorner: Float

            if( isRemote ) {
                bottomStartCorner = cornerRadius
                bottomEndCorner = 0f
            } else {
                bottomStartCorner = 0f
                bottomEndCorner = cornerRadius
            }

            val bottomLeftCorner: Float
            val bottomRightCorner: Float
            if( isRTL ) {
                bottomRightCorner = bottomStartCorner
                bottomLeftCorner = bottomEndCorner
            } else {
                bottomRightCorner = bottomEndCorner
                bottomLeftCorner = bottomStartCorner
            }

            val radiuses = floatArrayOf(
                cornerRadius, cornerRadius,
                cornerRadius, cornerRadius,
                bottomRightCorner, bottomRightCorner,
                bottomLeftCorner, bottomLeftCorner
            )
            val shape = RoundRectShape(radiuses, null, null)
            val shapeDrawable = ShapeDrawable(shape)
            shapeDrawable.paint.color = if(isRemote) Color.rgb(235, 235, 235) else ResourcesCompat.getColor(context.resources, R.color.colorSecondary, null)
            messageView.background = shapeDrawable
            messageView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                if( isRemote ) {
                    marginEnd = Utils.dpToPx(18f).toInt()
                    marginStart = Utils.dpToPx(4f).toInt()
                } else {
                    marginEnd = Utils.dpToPx(4f).toInt()
                    marginStart = Utils.dpToPx(18f).toInt()
                }
            }

            val linearLayout = LinearLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = Utils.dpToPx(8f).toInt()
                }
                gravity = if( isRemote ) Gravity.END else Gravity.START
                addView(messageView)
            }

            return linearLayout;
        }
    }
}