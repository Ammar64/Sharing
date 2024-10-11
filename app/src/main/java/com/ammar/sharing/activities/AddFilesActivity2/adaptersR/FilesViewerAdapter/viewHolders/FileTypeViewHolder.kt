package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders

import android.content.Context
import android.os.Build
import android.os.Environment
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
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.FilesViewerAdapter
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject
import com.ammar.sharing.common.FileUtils
import com.ammar.sharing.common.Utils
import com.ammar.sharing.custom.ui.AdaptiveTextView
import java.io.File

class FileTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {

        fun makeFileTypeViewHolder(context: Context, adapter: FilesViewerAdapter): FileTypeViewHolder {
            val fileTypes = listOf(
                FileType(adapter ,R.string.images, R.drawable.icon_image, FileUtils.FILE_TYPE_IMAGE),
                FileType(adapter ,R.string.videos, R.drawable.icon_video, FileUtils.FILE_TYPE_VIDEO),
                FileType(adapter ,R.string.audio, R.drawable.icon_audio, FileUtils.FILE_TYPE_AUDIO),
                FileType(adapter ,R.string.documents, R.drawable.icon_document, FileUtils.FILE_TYPE_DOCUMENT),

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
                linearLayout.addView( makeFileTypeElement(context, i) )
            }
            scrollView.addView(linearLayout)
            return FileTypeViewHolder(scrollView)
        }

        private data class FileType(val adapter: FilesViewerAdapter ,@StringRes val name: Int, @DrawableRes val icon: Int, val fileType: Int) {
            val onClick: (View) -> Unit = {
                val filesArray = ArrayList<File>();
                FileUtils.findFilesTypesRecursively(Environment.getExternalStorageDirectory(), filesArray, fileType);
                val javaFiles = filesArray.toArray(emptyArray<File>())
                val files = Array(javaFiles.size) { FSObject(javaFiles[it]) }
                adapter.showArrayOfFiles(files)
            }
        }

        private fun makeFileTypeElement(
            context: Context,
            fileType: FileType
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
                    setOnClickListener( fileType.onClick )
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
                setText(fileType.name)
                setModifyDrawableColor(false)
                setCompoundDrawablesRelativeWithIntrinsicBounds(fileType.icon, 0, 0, 0)
            }
            cardView.addView(textView)
            return cardView
        }
    }
}
