package com.ammar.sharing.activities.GetFilesActivity.adapterR

import android.annotation.SuppressLint
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.activities.GetFilesActivity.CheckableFile
import com.ammar.sharing.activities.GetFilesActivity.GetFilesActivity
import com.ammar.sharing.activities.GetFilesActivity.adapterR.viewHolders.DirectoryViewHolder
import com.ammar.sharing.activities.GetFilesActivity.adapterR.viewHolders.FileViewHolder
import com.ammar.sharing.common.utils.FileUtils
import com.ammar.sharing.common.utils.Utils


@SuppressLint("NotifyDataSetChanged")
class GetFilesAdapter(val activity: GetFilesActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // View types
    companion object {
        private const val DIRECTORY_VIEW_TYPE = 0
        private const val FILE_VIEW_TYPE = 1

        private const val ONE_FILE_SELECT_MODE = 0
        private const val MULTIPLE_FILES_SELECT_MODE = 1
    }

    var mFiles: List<CheckableFile> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectMode: Int = ONE_FILE_SELECT_MODE
        set(value) {
            field = value
            if( value == ONE_FILE_SELECT_MODE ) {
                activity.mAppBar?.menu?.clear()
                activity.mAppBar?.setNavigationIcon(R.drawable.ic_menu)
                activity.mGetFilesDL?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else if(value == MULTIPLE_FILES_SELECT_MODE) {
                activity.mAppBar?.inflateMenu(R.menu.menu_select)
                activity.mAppBar?.setNavigationIcon(null)
                activity.mGetFilesDL?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        if( mFiles[position].file.isDirectory ) {
            return DIRECTORY_VIEW_TYPE
        } else {
            return FILE_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when( viewType ) {
            DIRECTORY_VIEW_TYPE ->  {
                val dirView = LayoutInflater.from(parent.context).inflate(R.layout.activity_getfiles_row_directory, parent, false)
                return DirectoryViewHolder(dirView);
            }
            FILE_VIEW_TYPE -> {
                val fileView = LayoutInflater.from(parent.context).inflate(R.layout.activity_getfiles_row_file, parent, false)
                return FileViewHolder(fileView);
            }
            else -> {
                throw RuntimeException("GetFilesAdapter.onCreateViewHolder: unexpected viewType")
            }
        }
    }

    var selectCount = 0
        set(value) {
            field = if( value < 0 ) 0 else value
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when(viewType) {
            DIRECTORY_VIEW_TYPE -> {
                val dirHolder = holder as DirectoryViewHolder
                dirHolder.dirName.text = mFiles[position].file.name
                dirHolder.itemView.setOnClickListener {
                    if( selectMode == ONE_FILE_SELECT_MODE ) {
                        activity.changeDirectory(mFiles[position].file)
                    }
                }
            }
            FILE_VIEW_TYPE -> {
                val fileHolder = holder as FileViewHolder
                val file = mFiles[position]
                fileHolder.fileName.text = file.file.name
                fileHolder.fileSize.text = Utils.getFormattedSize(file.file.length())
                FileUtils.inferFileIcon(fileHolder.fileIcon, file.file)

                fileHolder.viewSwitcher.displayedChild = selectMode
                fileHolder.checkBox.isChecked = file.isChecked
                if(file.isChecked) {
                    fileHolder.itemView.setBackgroundColor(0x404790d8)
                } else {
                    val value = TypedValue()
                    activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
                    fileHolder.itemView.setBackgroundColor(value.data)
                }

                fileHolder.itemView.setOnClickListener {
                    if( selectMode == MULTIPLE_FILES_SELECT_MODE ) {
                        if( !file.isChecked ) {
                            file.isChecked = true
                            selectCount++
                        } else {
                            file.isChecked = false
                            selectCount--
                        }
                        if(selectCount == 0) {
                            selectMode = ONE_FILE_SELECT_MODE
                        }
                    }
                    notifyItemChanged(position)
                }
                fileHolder.itemView.setOnLongClickListener {
                    if(selectMode != MULTIPLE_FILES_SELECT_MODE) {
                        file.isChecked = true
                        selectCount++
                        selectMode = MULTIPLE_FILES_SELECT_MODE
                    }
                    true
                }
                fileHolder.viewBtn.setOnClickListener {
                    val uri =
                        FileProvider.getUriForFile(
                             activity,
                            activity.getApplicationContext()
                                .getPackageName() + ".provider",
                            file.file
                        )
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(uri, Utils.getMimeType(file.file.name, true))
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

}
