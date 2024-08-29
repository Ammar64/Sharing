package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.activities.AddFilesActivity2.AddFilesActivity2;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.DirectoryViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.FileTypeViewHolder;

import java.io.File;

public class FilesViewerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public AddFilesActivity2 activity;
    public final RecyclerView recyclerView;
    private FSObject[] files;
    private FSObject[] displayedFiles;
    private File currentDir;

    private int lastDirIndex;
    public FilesViewerAdapter(AddFilesActivity2 activity, RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;

        ((GridLayoutManager) this.recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 2;
            }
        });
    }



    // view types
    public static final int VIEW_TYPE_PATH = 0;
    public static final int VIEW_TYPE_FILE_TYPES = 1;
    public static final int VIEW_TYPE_DIRECTORIES = 2;
    public static final int VIEW_TYPE_FILES = 3;
    public static final int VIEW_TYPE_SPACE = 4;

//    @Override
//    public int getItemViewType(int position) {
//        if (position == 0) return VIEW_TYPE_PATH;
//        if (position == 1) return VIEW_TYPE_FILE_TYPES;
//        position -= 2;
//
//        // TODO
//        boolean hasSpaceView = false;;
//        if ((position - 1 == lastDirIndex) && hasSpaceView) {
//            return VIEW_TYPE_SPACE;
//        }
//        if (position > lastDirIndex && hasSpaceView) position--;
//        return displayedFiles[position].isDirectory() ? VIEW_TYPE_DIRECTORIES : VIEW_TYPE_FILES;
//    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FileTypeViewHolder.Companion.makeFileTypeViewHolder(activity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 100;
    }


    private int getLastDirectoryIndex() {
        // use binary search :)
        int low = 0;
        int high = displayedFiles.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (displayedFiles[mid].isDirectory() && ((displayedFiles.length >= mid + 2 && (!displayedFiles[mid + 1].isDirectory()))
                    || displayedFiles.length <= mid + 1)) {
                return mid;
            }
            if ( ( !displayedFiles[mid].isDirectory() ) && (displayedFiles.length >= mid + 2 && (!displayedFiles[mid + 1].isDirectory()))) {
                high = mid - 1;
            } else if (displayedFiles[mid].isDirectory() && displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].isDirectory()) {
                low = mid + 1;
            } else if ((!displayedFiles[mid].isDirectory()) && displayedFiles.length <= mid + 1) { // only files
                return -1;
            }
        }
        return -1;  // dir empty
    }


}
