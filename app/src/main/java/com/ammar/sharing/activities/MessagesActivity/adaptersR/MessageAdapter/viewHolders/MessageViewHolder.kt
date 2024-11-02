package com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.viewHolders

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.common.utils.Utils

class MessageViewHolder private constructor(itemView: LinearLayout) : RecyclerView.ViewHolder(itemView) {
    lateinit var contentTV: TextView
    var authorTV: TextView? = null

    companion object {
        fun constructNewMessageViewHolder(context: Context, isRemote: Boolean): MessageViewHolder {

            val cornerRadius = Utils.dpToPx(12F)
            val messageContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val padding = Utils.dpToPx(8f).toInt()
                setPadding(padding)
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
            messageContainer.background = shapeDrawable
            messageContainer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                if( isRemote ) {
                    marginEnd = Utils.dpToPx(18f).toInt()
                    marginStart = Utils.dpToPx(4f).toInt()
                } else {
                    marginEnd = Utils.dpToPx(4f).toInt()
                    marginStart = Utils.dpToPx(18f).toInt()
                }
            }

            val messageContentTV = TextView(context).apply {
                if(isRemote) {
                    setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_color_dark, null))
                } else {
                    setTextColor(ResourcesCompat.getColor(context.resources, R.color.text_color_light, null))
                }
                setTextIsSelectable(true)
            }

            messageContainer.addView(messageContentTV);


            val linearLayout = LinearLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = Utils.dpToPx(8f).toInt()
                }
                gravity = if( isRemote ) Gravity.END else Gravity.START
                addView(messageContainer)
            }

            return MessageViewHolder(linearLayout).apply {
                contentTV = messageContentTV
                // specify author
                if( isRemote ) {
                    val messageAuthorTV = TextView(context).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                        setTextColor(0x77111111)
                        gravity = Gravity.START
                    }
                    messageContainer.addView(messageAuthorTV)
                    authorTV = messageAuthorTV
                }
            };
        }
    }
}