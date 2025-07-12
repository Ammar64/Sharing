package com.ammar.sharing.activities.GetFilesActivity

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.activities.GetFilesActivity.adapterR.GetFilesAdapter
import com.ammar.sharing.activities.GetFilesActivity.adapterR.PathSegmentsAdapter
import com.ammar.sharing.common.utils.FileUtils
import com.ammar.sharing.custom.ui.DefaultActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Stack

class GetFilesActivity : DefaultActivity() {
    private lateinit var mPreferences: SharedPreferences

    private var mViewMode: ViewMode = ViewMode.LIST
    private var mSortBy: SortBy = SortBy.NAME
    private var mAscending = true

    private val startingDirectory: File = Environment.getExternalStorageDirectory()
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

    lateinit var mAppBar: Toolbar
    lateinit var mGetFilesDL: DrawerLayout
    private lateinit var mNavView: NavigationView
    private lateinit var mPathsSegmentsRecyclerView: RecyclerView
    private lateinit var mPathsSegmentsAdapter: PathSegmentsAdapter
    private lateinit var mViewSwitcher: ViewSwitcher
    private lateinit var mFilesRecyclerView: RecyclerView
    private lateinit var mFilesRecyclerAdapter: GetFilesAdapter
    private var mRecyclerViewStateStack: Stack<Parcelable?> = Stack()

    private fun setupItems() {
        mGetFilesDL = findViewById(R.id.DL_GetFilesDrawerLayout)
        mNavView = findViewById(R.id.NV_Files)

        mAppBar = findViewById(R.id.TB_Toolbar)
        mAppBar.setTitle(R.string.internal_storage)
        mAppBar.setNavigationIcon(R.drawable.ic_menu)
        mAppBar.setNavigationOnClickListener {
            mGetFilesDL.openDrawer(GravityCompat.START)
        }

        mPathsSegmentsRecyclerView = findViewById(R.id.RV_PathSegments)
        mPathsSegmentsAdapter = PathSegmentsAdapter()
        mPathsSegmentsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        mViewSwitcher = findViewById(R.id.VS_GetFilesRCORNoFilesFound)

        mFilesRecyclerView = findViewById(R.id.RV_GetFiles)
        mFilesRecyclerAdapter = GetFilesAdapter(this)

        mFilesRecyclerView.setHasFixedSize(true)
        mFilesRecyclerView.itemAnimator = null
        mFilesRecyclerView.adapter = mFilesRecyclerAdapter

        mNavView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.MI_InternalStorage -> {
                    mRecyclerViewStateStack.clear()
                    currentDirectory = startingDirectory
                    changeDirectory(currentDirectory)
                }
                R.id.MI_Camera -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
                }
                R.id.MI_Screenshots -> {
                    // /storage/emulated/0/ScreenShots is what we get when we use Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
                    // which doesn't exist in all devices
                    val pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val screenshotsDir = File(pix, "Screenshots")
                    if(screenshotsDir.exists()) {
                        changeDirectory(screenshotsDir)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val defaultScreenshotsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
                        if(screenshotsDir.exists()) {
                            changeDirectory(defaultScreenshotsDir)
                        } else {
                            changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                        }
                    } else {
                        changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                    }
                }
                R.id.MI_Images -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                }
                R.id.MI_Videos -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
                }
                R.id.MI_Audio -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
                }
                R.id.MI_Documents -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))
                }
                R.id.MI_Downloads -> {
                    changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                }
                R.id.MI_SharingDirectory -> {
                    changeDirectory(File(Environment.getExternalStorageDirectory(), "Sharing"))
                }
            }
            mGetFilesDL.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback { goBack() }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val files = startingDirectory.listFiles()
                val checkableFile = CheckableFile.filesToCheckableFiles(files!!)
                mFilesRecyclerAdapter.mFiles = checkableFile
            }
        }
    }

    fun changeDirectory(file: File) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if( currentDirectory == file ) {
                    return@withContext
                }
                if( !file.exists() ) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@GetFilesActivity,
                            R.string.folder_doesnt_exist,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@withContext
                }
                val files = file.listFiles()
                if( files != null ) {
                    val depth = FileUtils.getFileDepthInDir(currentDirectory, file)
                    currentDirectory = file
                    if (files.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            mViewSwitcher.displayedChild = 1
                        }
                    } else {
                        val checkableFile = CheckableFile.filesToCheckableFiles(files)
                        withContext(Dispatchers.Main) {
                            mViewSwitcher.displayedChild = 0
                            if( depth == -1 ) {
                                val d = FileUtils.getFileDepthInDir(startingDirectory, currentDirectory)
                                mRecyclerViewStateStack.clear()
                                mRecyclerViewStateStack.addAll(arrayOfNulls(d))
                            } else {
                                mRecyclerViewStateStack.push(mFilesRecyclerView.layoutManager?.onSaveInstanceState())
                                mRecyclerViewStateStack.addAll(arrayOfNulls(depth - 1))
                            }
                            mFilesRecyclerAdapter.mFiles = checkableFile
                            mFilesRecyclerView.scrollToPosition(0)
                        }
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

    private fun goBack() {
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
                        mViewSwitcher.displayedChild = 0
                        val state = mRecyclerViewStateStack.pop()
                        if(state != null) mFilesRecyclerView.layoutManager?.onRestoreInstanceState(state)
                        mFilesRecyclerAdapter.mFiles = checkableFile
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
