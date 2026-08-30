package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.HeaderViewHolder
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders.ProgressViewHolder
import com.ammar.sharing.activities.MainActivity.fragments.BrowserShareFragment
import com.ammar.sharing.nativebackend.TransferOperationsManager

// this adapter for the recycler view you see when you open the app
// it takes the entire screen except for the top and bottom bars
class ShareAdapter(private val fragment: BrowserShareFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return TYPE_HEADER
        else if (position == getItemCount() - 1) return TYPE_FOOTER
        else return TYPE_PROGRESS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PROGRESS -> {
                val progressInflater = LayoutInflater.from(parent.getContext())
                val layout = progressInflater.inflate(R.layout.row_transfer_view, parent, false)
                return ProgressViewHolder(this, layout)
            }

            TYPE_HEADER -> {
                val headerInflater = LayoutInflater.from(parent.getContext())
                val header = headerInflater.inflate(R.layout.row_transfer_header, parent, false)
                return HeaderViewHolder(header, fragment)
            }

            TYPE_FOOTER -> {
                val space = Space(parent.getContext())
                val out = TypedValue()
                parent.getContext().getTheme()
                    .resolveAttribute(android.R.attr.actionBarSize, out, true)
                val size = TypedValue.complexToDimensionPixelSize(
                    out.data,
                    parent.getContext().getResources().getDisplayMetrics()
                )
                val params = ViewGroup.LayoutParams(0, size)
                space.setLayoutParams(params)
                return object : RecyclerView.ViewHolder(space) {
                }
            }
        }
        throw RuntimeException("Invalid View Type in TransferAdapter")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_PROGRESS -> (holder as ProgressViewHolder).setup(
                TransferOperationsManager.getTransferOperationAtIndex(
                    position - 1
                )
            )

            TYPE_HEADER -> {
                (holder as HeaderViewHolder).updateUnseenMessagesNum()
                holder.updateViewCertButtonStatus()
            }
        }
    }

    override fun getItemCount(): Int {
        return TransferOperationsManager.getOperationsCount() + 2
    }


    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_PROGRESS = 2
        private const val TYPE_FOOTER = 3
    }
}