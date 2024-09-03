package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter;

import android.app.AlertDialog;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Space;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddFilesActivity2.AddFilesActivity2;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models.FSObject;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.DirectoryViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.FileTypeViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.FileViewHolder;
import com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.viewHolders.PathViewHolder;
import com.ammar.sharing.common.MathsUtils;
import com.google.android.material.animation.AnimationUtils;

import java.io.File;
import java.util.LinkedList;

public class FilesViewerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public AddFilesActivity2 activity;
    public final RecyclerView recyclerView;
    private FSObject[] files;
    private FSObject[] displayedFiles;
    private File currentDir;

    private int lastDirIndex;
    private boolean hasSpaceView;
    public boolean multiSelectMode = false;

    public FilesViewerAdapter(AddFilesActivity2 activity, RecyclerView recyclerView) {
        this.activity = activity;
        this.recyclerView = recyclerView;

        currentDir = Environment.getExternalStorageDirectory();
        files = FSObject.listDirectorySorted(currentDir, FSObject.SortType.BY_NAME);
        displayedFiles = files;
        lastDirIndex = getLastDirectoryIndex();
        hasSpaceView = shouldHaveSpaceView();
        ((GridLayoutManager) this.recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = getItemViewType(position);
                int spanCount = activity.getSpanCount();
                return switch (viewType) {
                    case VIEW_TYPE_PATH -> spanCount;
                    case VIEW_TYPE_FILE_TYPES -> spanCount;
                    case VIEW_TYPE_DIRECTORIES, VIEW_TYPE_FILES -> 1;
                    case VIEW_TYPE_SPACE -> spanCount - ((lastDirIndex + 1) % spanCount);
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

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_PATH;
        if (position == 1) return VIEW_TYPE_FILE_TYPES;
        position -= 2;
        if ((position - 1 == lastDirIndex) && hasSpaceView) {
            return VIEW_TYPE_SPACE;
        }
        Log.d("Adapter", "LastDirIndex: " + lastDirIndex);
        if (position > lastDirIndex && hasSpaceView) position--;
        return displayedFiles[position].getFile().isDirectory() ? VIEW_TYPE_DIRECTORIES : VIEW_TYPE_FILES;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch (viewType) {
            case VIEW_TYPE_PATH -> PathViewHolder.Companion.makePathViewHolder(activity);
            case VIEW_TYPE_FILE_TYPES ->
                    FileTypeViewHolder.Companion.makeFileTypeViewHolder(activity);
            case VIEW_TYPE_DIRECTORIES ->
                    DirectoryViewHolder.Companion.makeDirectoryViewHolder(activity);
            case VIEW_TYPE_FILES -> FileViewHolder.Companion.makeFileViewHolder(activity);
            case VIEW_TYPE_SPACE -> new RecyclerView.ViewHolder(new Space(activity)) {
            };
            default -> throw new RuntimeException("");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DirectoryViewHolder dirHolder) {
            Log.d("Adapter", "ItemCount: " + getItemCount());
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
        return displayedFiles.length + (hasSpaceView ? 3 : 2);
    }

    public void cd(File dir) {
        // cd
        FSObject[] filesBak = files;
        files = FSObject.listDirectorySorted(dir, FSObject.SortType.BY_NAME);
        // it returns null on error and that error is probably permission denied.
        if( files == null ) {
            Toast.makeText(activity, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            files = filesBak;
            return;
        }
        displayedFiles = files;
        currentDir = dir;
        lastDirIndex = getLastDirectoryIndex();
        hasSpaceView = shouldHaveSpaceView();


        // get dirs and files views and hide them
        int childCount = recyclerView.getChildCount();
        LinkedList<View> views = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            View view = recyclerView.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof FileViewHolder || viewHolder instanceof DirectoryViewHolder) {
                view.setVisibility(View.GONE);
                views.addLast(view);
            }
        }

            notifyDataSetChanged();
            new AlertDialog.Builder(activity)
                    .setMessage("currentDir: " + currentDir.getAbsolutePath()+ "\n" +
                            "lastDirIndex: " + lastDirIndex + "\n" +
                            "spanCount: " + activity.getSpanCount() + "\n" +
                            "hasSpace: " + hasSpaceView + "\n" +
                            "displayedFilesLength: " + displayedFiles.length +"\n" +
                            "TEST: 3 % 4 == " + 3 % 4).show();

            // show and animate
            Animation downToUp = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0, Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            downToUp.setDuration(500L);
            for (View i : views) {
                i.setVisibility(View.VISIBLE);
            }
            for (View i : views) {
                i.startAnimation(downToUp);
            }
    }

    // go back
    public void cdDotDot() {
        if( currentDir == Environment.getExternalStorageDirectory() ) return;
        File parent = currentDir.getParentFile();
        cd(parent);

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
            if ((!displayedFiles[mid].getFile().isDirectory()) && (displayedFiles.length >= mid + 2 && (!displayedFiles[mid + 1].getFile().isDirectory()))) {
                high = mid - 1;
            } else if (displayedFiles[mid].getFile().isDirectory() && displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].getFile().isDirectory()) {
                low = mid + 1;
            } else if ((!displayedFiles[mid].getFile().isDirectory()) && displayedFiles.length <= mid + 1) { // only files
                return -1;
            }
        }
        return -1;  // dir empty
    }

    private boolean shouldHaveSpaceView() {
        return !MathsUtils.isDividableBy(lastDirIndex+1, activity.getSpanCount()); //&& (lastDirIndex + 1) > activity.getSpanCount();
    }

}
