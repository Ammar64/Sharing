package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject
import com.ammar.sharing.common.FileUtils
import com.ammar.sharing.common.Utils
import com.ammar.sharing.custom.ui.AdaptiveTextView
import java.io.File

class FileViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun makeFileViewHolder(context: Context): FileViewHolder {
            val cardView = CardView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Utils.dpToPx(200f).toInt()
                ).apply {
                    val margin = Utils.dpToPx(8F).toInt()
                    this.topMargin = margin
                    this.bottomMargin = margin
                    this.leftMargin = margin
                    this.rightMargin = margin
                }
                setCardBackgroundColor(0x53000000)
                cardElevation = Utils.dpToPx(0F)
                radius = Utils.dpToPx(12f)

                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
                foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
            }

            val constraintLayout = ConstraintLayout(context).apply { id = View.generateViewId() }

            val checkBox = CheckBox(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    topToTop = constraintLayout.id
                    endToEnd = constraintLayout.id

                    val margin = Utils.dpToPx(4f).toInt()
                    topMargin = margin
                    marginEnd = margin
                }
                scaleX = 1.2f
                scaleY = 1.2f
                isClickable = false
                isFocusable = false
                background = null
            }

            val imageView = ImageView(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.MATCH_PARENT
                    height = Utils.dpToPx(150F).toInt()
                    topToTop = constraintLayout.id
                    startToStart = constraintLayout.id
                    endToEnd = constraintLayout.id
                }
                val padding = Utils.dpToPx(40F).toInt()
                setPadding(padding, padding, padding, padding)
                id = View.generateViewId()
            }

            val fileTypeTV = AdaptiveTextView(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    bottomToBottom = imageView.id
                    startToStart = imageView.id
                    marginStart = Utils.dpToPx(4F).toInt()
                }
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                lightModeColor = Color.rgb(255, 255, 255)
                elevation = Utils.dpToPx(5F)
                compoundDrawablePadding = Utils.dpToPx(5F).toInt()
            }

            val lineView = View(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.MATCH_PARENT
                    height = Utils.dpToPx(1F).toInt()
                    startToStart = constraintLayout.id
                    endToEnd = constraintLayout.id
                    topToBottom = imageView.id
                }
                id = View.generateViewId()
                setBackgroundColor(0xAAFFFFFF.toInt())
            }

            val fileNameTV = AdaptiveTextView(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    startToStart = constraintLayout.id
                    topToBottom = lineView.id
                    topMargin = Utils.dpToPx(-4F).toInt()
                    marginStart = Utils.dpToPx(4F).toInt()
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                lightModeColor = Color.rgb(255, 255, 255)
                id = View.generateViewId()
            }

            val fileSizeTV = AdaptiveTextView(context).apply {
                constraintLayout.addView(this)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    topToBottom = fileNameTV.id
                    startToStart = constraintLayout.id
                    topMargin = Utils.dpToPx(-7F).toInt()
                    marginStart = Utils.dpToPx(4F).toInt()
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11F)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                lightModeColor = Color.rgb(255, 255, 255)
            }

            cardView.addView(constraintLayout)
            cardView.setOnClickListener {
                checkBox.toggle();
            }
            return FileViewHolder(cardView).apply {
                this.imageView = imageView
                this.checkBox = checkBox
                this.fileTypeTV = fileTypeTV
                this.fileNameTV = fileNameTV
                this.fileSizeTV = fileSizeTV
            }
        }
    }

    var imageView: ImageView? = null;
    var checkBox: CheckBox? = null
    var fileTypeTV: AdaptiveTextView? = null
    var fileNameTV: AdaptiveTextView? = null
    var fileSizeTV: AdaptiveTextView? = null

    fun setup(file: FSObject) {
        FileUtils.setFileIcon(imageView, fileTypeTV, file.file)
        fileNameTV!!.text = file.file.name
        fileSizeTV!!.text = Utils.getFormattedSize(file.file.length())
    }
}
