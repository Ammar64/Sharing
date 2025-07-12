package com.ammar.sharing.activities.GetFilesActivity

import android.os.Environment
import android.os.Parcelable
import com.ammar.sharing.R
import com.ammar.sharing.common.utils.Utils
import java.io.File

class NavigationManager(startingDir: File) {
    val navTracker = arrayListOf<Segment>()

    init {
        if( startingDir == Environment.getExternalStorageDirectory() ) {
            navTracker.add(Segment(Utils.getRes().getString(R.string.internal_storage), null))
        } else {
            navTracker.add(Segment(startingDir.name, null))
        }
    }

    inner class Segment(
        val name: String,
        var state: Parcelable?// RecyclerView State
    )
}
