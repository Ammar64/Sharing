package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter;

import android.app.AlertDialog;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.activities.AddFilesActivity2.AddFilesActivity2;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.DirectoryViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.FileTypeViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.FileViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.PathViewHolder;
import com.ammar.sharing.common.MathsUtils;

import java.io.File;

public class FilesViewerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public AddFilesActivity2 activity;
    public final RecyclerView recyclerView;
    private FSObject[] files;
    private FSObject[] displayedFiles;
    private File currentDir;

    private int lastDirIndex;
    private boolean hasSpaceView;

    public FilesViewerAdapter(AddFilesActivity2 activity, RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;

        currentDir = Environment.getExternalStorageDirectory();
        displayedFiles = FSObject.listDirectorySorted(currentDir, FSObject.SortType.BY_NAME);
        lastDirIndex = getLastDirectoryIndex();
        hasSpaceView = MathsUtils.isDividableBy(lastDirIndex, activity.getSpanCount());
        ((GridLayoutManager) this.recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = getItemViewType(position);
                int spanCount = activity.getSpanCount();
                return switch (viewType) {
                    case VIEW_TYPE_PATH -> spanCount;
                    case VIEW_TYPE_FILE_TYPES -> spanCount;
                    case VIEW_TYPE_DIRECTORIES, VIEW_TYPE_FILES -> 1;
                    case VIEW_TYPE_SPACE -> spanCount - (lastDirIndex % spanCount);
                    default -> throw new RuntimeException("");
                };
            }
        });
    }



    // view types
    public static final int VIEW_TYPE_PATH = 0;
    public static final int VIEW_TYPE_FILE_TYPES = 1;
    public static final int VIEW_TYPE_DIRECTORIES = 2;
    public static final int VIEW_TYPE_FILES = 3;
    public static final int VIEW_TYPE_SPACE = 4;

    boolean shown = false;
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_PATH;
        if (position == 1) return VIEW_TYPE_FILE_TYPES;
        position -= 2;
        if ((position == lastDirIndex) && hasSpaceView) {
            return VIEW_TYPE_SPACE;
        }

        if (position > lastDirIndex && hasSpaceView) position--;
        return displayedFiles[position].getFile().isDirectory() ? VIEW_TYPE_DIRECTORIES : VIEW_TYPE_FILES;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch (viewType) {
            case VIEW_TYPE_PATH -> PathViewHolder.Companion.makePathViewHolder(activity);
            case VIEW_TYPE_FILE_TYPES -> FileTypeViewHolder.Companion.makeFileTypeViewHolder(activity);
            case VIEW_TYPE_DIRECTORIES -> DirectoryViewHolder.Companion.makeDirectoryViewHolder(activity);
            case VIEW_TYPE_FILES -> FileViewHolder.Companion.makeFileViewHolder(activity);
            case VIEW_TYPE_SPACE -> new RecyclerView.ViewHolder(new Space(activity)) {};
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if( holder instanceof DirectoryViewHolder dirHolder) {
            position -= 2;
            dirHolder.setup(displayedFiles[position]);
        } else if (holder instanceof FileViewHolder fileHolder) {
            int numSpace = hasSpaceView ? 1 : 0;
            position -= 2 + numSpace;
            fileHolder.setup(displayedFiles[position]);
        }
    }

    @Override
    public int getItemCount() {
        int numSpace = hasSpaceView ? 1 : 0;
        return displayedFiles.length + numSpace + 2;
    }


    private int getLastDirectoryIndex() {
        // use binary search :)
        int low = 0;
        int high = displayedFiles.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (displayedFiles[mid].getFile().isDirectory() && ((displayedFiles.length >= mid + 2 && (!displayedFiles[mid + 1].getFile().isDirectory()))
                    || displayedFiles.length <= mid + 1)) {
                return mid;
            }
            if ( ( !displayedFiles[mid].getFile().isDirectory() ) && (displayedFiles.length >= mid + 2 && (!displayedFiles[mid + 1].getFile().isDirectory()))) {
                high = mid - 1;
            } else if (displayedFiles[mid].getFile().isDirectory() && displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].getFile().isDirectory()) {
                low = mid + 1;
            } else if ((!displayedFiles[mid].getFile().isDirectory()) && displayedFiles.length <= mid + 1) { // only files
                return -1;
            }
        }
        return -1;  // dir empty
    }


}
