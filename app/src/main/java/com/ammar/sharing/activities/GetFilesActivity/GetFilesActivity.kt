package com.ammar.sharing.activities.GetFilesActivity

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.activities.GetFilesActivity.ViewMode.Companion.fromInt
import com.ammar.sharing.activities.GetFilesActivity.adapterR.GetFilesAdapter
import com.ammar.sharing.custom.ui.DefaultActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.util.Stack

class GetFilesActivity : DefaultActivity() {
    lateinit var mPreferences: SharedPreferences

    var mViewMode: ViewMode = ViewMode.LIST
    var mSortBy: SortBy = SortBy.NAME
    var mAscending = true

    val startingDirectory: File = Environment.getExternalStorageDirectory()
    var currentDirectory = startingDirectory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getfiles)
        setupPreferences()
        setupItems()
    }

    private fun setupPreferences() {
        mPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        mViewMode = ViewMode.fromInt(mPreferences.getInt(SHARED_PREF_VIEW_MODE, ViewMode.LIST.ordinal))!!
        mSortBy   = SortBy.fromInt(mPreferences.getInt(SHARED_PREF_SORT_BY, SortBy.NAME.ordinal))!!
        mAscending = mPreferences.getBoolean(SHARED_PREF_ASCENDING, true)
    }

    private var mAppBar: Toolbar? = null
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerAdapter: GetFilesAdapter? = null
    private var mRecyclerViewStateStack: Stack<Parcelable> = Stack();

    private fun setupItems() {
        mAppBar = findViewById<Toolbar>(R.id.TB_Toolbar)
        mAppBar?.setTitle(R.string.internal_storage)
        mRecyclerView = findViewById<RecyclerView>(R.id.RV_GetFiles)
        mRecyclerAdapter = GetFilesAdapter(this)

        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.adapter = mRecyclerAdapter

        onBackPressedDispatcher.addCallback { goBack() }

        changeDirectory(startingDirectory)
    }

    fun changeDirectory(file: File) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val files = file.listFiles()
                if( files != null ) {
                    currentDirectory = file
                    val checkableFile = CheckableFile.filesToCheckableFiles(files)
                    withContext(Dispatchers.Main) {
                        mRecyclerViewStateStack.push(mRecyclerView?.layoutManager?.onSaveInstanceState())
                        mRecyclerAdapter?.mFiles = checkableFile
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GetFilesActivity,
                            R.string.permission_denied,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun goBack() {
        if( currentDirectory == startingDirectory ) {
            finish()
            return
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val files = currentDirectory.parentFile?.listFiles()
                if( files != null ) {
                    currentDirectory = currentDirectory.parentFile!!
                    val checkableFile = CheckableFile.filesToCheckableFiles(files)
                    withContext(Dispatchers.Main) {
                        mRecyclerView?.layoutManager?.onRestoreInstanceState(mRecyclerViewStateStack.pop())
                        mRecyclerAdapter?.mFiles = checkableFile
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GetFilesActivity,
                            R.string.permission_denied,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // Save settings
    override fun onPause() {
        super.onPause()
        mPreferences.edit {
            putInt(SHARED_PREF_VIEW_MODE, mViewMode.ordinal)
            putInt(SHARED_PREF_SORT_BY,   mSortBy.ordinal)
            putBoolean(SHARED_PREF_ASCENDING, mAscending)
        }
    }

    companion object {
        const val SHARED_PREFS_NAME: String = "GetFilesActivityPrefs"

        const val SHARED_PREF_VIEW_MODE: String = "view_mode"
        const val SHARED_PREF_SORT_BY: String = "sort_by"
        const val SHARED_PREF_ASCENDING: String = "ascending"
    }
}
