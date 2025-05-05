package com.ammar.sharing.activities.GetFilesActivity.adapterR.viewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val fileIcon = itemView.findViewById<ImageView>(R.id.IV_FileIcon)
    val fileName = itemView.findViewById<TextView>(R.id.TV_FileName)
    val fileSize = itemView.findViewById<TextView>(R.id.TV_FileSize)
    val fileViewOrSelectViewSwitcher = itemView.findViewById<ViewSwitcher>(R.id.VS_ViewOrSelect)
}
