package com.ammar.sharing.activities.GetFilesActivity

import java.io.File

data class CheckableFile(
    val file: File,
    var isChecked: Boolean = false
) {
    companion object {
        fun filesToCheckableFiles(files: Array<File>): ArrayList<CheckableFile> {
            val checkableFiles = ArrayList<CheckableFile>(files.size)
            for( file in files ) {
                checkableFiles.add(CheckableFile(file, false))
            }
            return checkableFiles
        }
    }
}

enum class ViewMode {
    LIST,
    GRID;
    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value }
    }
}

enum class SortBy {
    NAME,
    LAST_MODIFIED,
    SIZE;

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value }
    }
}
