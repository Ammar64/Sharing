package com.ammar.sharing.activities.GetFilesActivity.adapterR.viewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R

class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dirName = itemView.findViewById<TextView>(R.id.TV_DirName)
}
