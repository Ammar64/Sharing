package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.common.Utils
import com.ammar.sharing.custom.ui.AdaptiveTextView

class FileTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {

        fun makeFileTypeViewHolder(context: Context): FileTypeViewHolder {
            val fileTypes = listOf<FileType>(
                FileType(R.string.images, R.drawable.icon_image),
                FileType(R.string.videos, R.drawable.icon_video),
                FileType(R.string.audio, R.drawable.icon_audio),
                FileType(R.string.documents, R.drawable.icon_document),
            )

            val scrollView = HorizontalScrollView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val padding = Utils.dpToPx(8f).toInt()
                setPadding(padding, padding, padding, padding)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    horizontalScrollbarThumbDrawable = null
                }
            }

            val linearLayout = LinearLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            for (i in fileTypes) {
                linearLayout.addView( makeFileTypeElement(context, i.name, i.icon) )
            }
            scrollView.addView(linearLayout)
            return FileTypeViewHolder(scrollView)
        }

        private data class FileType(@StringRes val name: Int, @DrawableRes val icon: Int)

        private fun makeFileTypeElement(
            context: Context,
            @StringRes text: Int,
            @DrawableRes icon: Int
        ): CardView {
            val cardView = CardView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    cardElevation = 0f
                    radius = Utils.dpToPx(15f)
                    setCardBackgroundColor(0x77000000)
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue,
                        true
                    )
                    foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            }

            val textView = AdaptiveTextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                val paddingH = Utils.dpToPx(8f).toInt()
                val paddingV = Utils.dpToPx(4f).toInt()
                setPadding(paddingH, paddingV, paddingH, paddingV)

                // this is the same as paddingV
                compoundDrawablePadding = paddingV
                setText(text)
                setModifyDrawableColor(false)
                setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
            }
            cardView.addView(textView)
            return cardView
        }
    }
}
