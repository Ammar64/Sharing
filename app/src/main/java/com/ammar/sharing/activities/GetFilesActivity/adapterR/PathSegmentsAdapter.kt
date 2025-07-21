package com.ammar.sharing.activities.GetFilesActivity.adapterR

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R

class PathSegmentsAdapter : RecyclerView.Adapter<PathSegmentsAdapter.ViewHolder>() {

    val mPathSegmentsArray = arrayOf<PathSegment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_getfiles_path_segment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mPathSegmentsArray.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pathSegment.text = mPathSegmentsArray[position].segment
    }

    inner class PathSegment {
        val segment: String
        val isRoot: Boolean // this could be internal storage
        constructor(segment: String) {
            this.segment = segment
            this.isRoot = false
        }

        constructor(rootName: String, isRoot: Int) {
            this.segment = ""
            this.isRoot = true
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pathSegment = itemView.findViewById<TextView>(R.id.TV_PathSegment)
    }
}