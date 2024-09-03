package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.FilesViewerAdapter
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject
import com.ammar.sharing.common.Utils
import com.ammar.sharing.custom.ui.AdaptiveTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun makeDirectoryViewHolder(context: Context): DirectoryViewHolder {
            val cardView = CardView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.MATCH_PARENT,
                    Utils.dpToPx(40f).toInt()
                ).apply {
                    val marginV = Utils.dpToPx(4f).toInt()
                    val marginH = Utils.dpToPx(8f).toInt()
                    topMargin = marginV
                    bottomMargin = marginV
                    leftMargin = marginH
                    rightMargin = marginH
                }
                radius = Utils.dpToPx(20f)
                cardElevation = 0f
                setCardBackgroundColor(0x53000000)
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
                foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
            }


            val constraintLayout = ConstraintLayout(context).apply {
                layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                val paddingH = Utils.dpToPx(10f).toInt()
                setPadding(paddingH, 0, paddingH, 0)
                id = View.generateViewId()
            }

            val imageView = ImageView(context).apply {
                val size = Utils.dpToPx(30f).toInt()
                layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                    startToStart = constraintLayout.id
                    topToTop = constraintLayout.id
                    bottomToBottom = constraintLayout.id
                }
                id = View.generateViewId()
                Glide.with(context)
                    .load(R.drawable.icon_folder)
                    .override(size)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(this)
            }

            val folderNameTV = AdaptiveTextView(context).apply {
                layoutParams = ConstraintLayout.LayoutParams( 0, ConstraintLayout.LayoutParams.WRAP_CONTENT ).apply {
                    topToTop = imageView.id
                    bottomToBottom = imageView.id
                    startToEnd = imageView.id
                    endToEnd = constraintLayout.id
                    marginStart = Utils.dpToPx(10f).toInt()
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                lightModeColor = Color.rgb(255, 255, 255)
            }

            constraintLayout.addView(imageView)
            constraintLayout.addView(folderNameTV)
            cardView.addView(constraintLayout)

            cardView.isClickable = true

            return DirectoryViewHolder(cardView).apply {
                this.folderNameTV = folderNameTV
            }
        }
    }

    var folderNameTV: AdaptiveTextView? = null
    fun setup(file: FSObject) {
        folderNameTV!!.text = file.file.name;


        val adapter = bindingAdapter as FilesViewerAdapter;
        itemView.setOnClickListener {
            adapter.cd(file.file)
            Toast.makeText(itemView.context, "DIR WORKING", Toast.LENGTH_SHORT).show();
        }
    }
}
