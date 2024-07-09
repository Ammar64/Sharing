package com.ammar.filescenter.activities.AddFilesActivity.adaptersR;

import android.app.Activity;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddFilesActivity.AddFilesActivity;
import com.ammar.filescenter.activities.AddFilesActivity.FileTypeName;
import com.ammar.filescenter.common.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Stack;

public class StorageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DIR = 0;
    private static final int TYPE_FILE = 1;
    private static final int TYPE_SPACE = 2;

    private final File internalStorage = Environment.getExternalStorageDirectory();
    private File currentDir = internalStorage;
    private File[] files;

    private int lastDirIndex = -1;
    private AddFilesActivity act;

    private final Stack<Parcelable> recyclerViewStates = new Stack<>();
    private final Animation anim;

    public StorageAdapter(AddFilesActivity act) {
        this.act = act;
        viewDirectory(currentDir);
        anim = AnimationUtils.loadAnimation(act, R.anim.appear);

        act.getOnBackPressedDispatcher().addCallback(act, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentDir.compareTo(internalStorage) != 0) {
                    viewDirectory(currentDir.getParentFile(), true);
                } else {
                    act.setResult(Activity.RESULT_CANCELED);
                    act.finish();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {

        boolean hasSpaceView = (lastDirIndex & 1) == 0;
        if ((position - 1 == lastDirIndex) && hasSpaceView) {
            return TYPE_SPACE;
        }
        if (position > lastDirIndex && hasSpaceView) position--;
        return files[position].isDirectory() ? TYPE_DIR : TYPE_FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_DIR:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_directory, parent, false);
                return new DirectoryViewHolder(view);
            case TYPE_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_file, parent, false);
                return new FileViewHolder(view);
            case TYPE_SPACE:
                view = new Space(parent.getContext());
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new EmptySpace(view);
        }
        throw new RuntimeException("No such view type");
    }

    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_DIR:
                DirectoryViewHolder dirHolder = (DirectoryViewHolder) holder;
                dirHolder.setup(files[position], (view) -> {
                    viewDirectory(files[position]);
                });
                break;
            case TYPE_FILE:
                // we might have added an empty space
                int filePos = (lastDirIndex & 1) == 0 ? position - 1 : position;
                FileViewHolder fileHolder = (FileViewHolder) holder;
                fileHolder.setup(this, filePos);
                break;
            default:
                break;
        }
        // animate when scroll down only
        if (position > lastPosition) {
            holder.itemView.startAnimation(anim);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }


    @Override
    public int getItemCount() {
        // if the last directory index is even add 1 to item count to put empty space in it
        // if no directories present then lastDirIndex will be -1 and (-1 & 1) is 1
        return (lastDirIndex & 1) == 1 ? files.length : files.length + 1;
    }

    private void viewDirectory(File dir, boolean pop) {
        this.currentDir = dir;
        File[] listedFiles = currentDir.listFiles();
        if (listedFiles == null) { // permission denied
            Toast.makeText(act, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        this.files = listedFiles;

        if (!pop)
            recyclerViewStates.push(act.recyclerView.getLayoutManager().onSaveInstanceState());
        else
            act.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewStates.pop());

        sortFiles(files);
        lastDirIndex = getLastDirectoryIndex();

        if (this.files.length == 0) {
            act.folderEmptyTV.setVisibility(View.VISIBLE);
            act.folderEmptyTV.startAnimation(anim);
        } else act.folderEmptyTV.setVisibility(View.GONE);

        notifyDataSetChanged();
        lastPosition = -1; // to animate all
        if (internalStorage.compareTo(dir) == 0) {
            act.setTitle(R.string.internal_storage);
        } else {
            act.setTitle(dir.getName());
        }
        //act.recyclerView.startLayoutAnimation();

    }

    private void viewDirectory(File dir) {
        viewDirectory(dir, false);
    }

    // sort files but put directories first
    private void sortFiles(File[] files) {
        Arrays.sort(files, (l, r) -> {
            if (l.isDirectory() && r.isFile()) return -1;
            if (l.isFile() && r.isDirectory()) return 1;
            else return l.compareTo(r);
        });
    }

    // insert a file into selectedFilesPath array in ordered way
    private void selectFilePath(String path) {
        ListIterator<String> iterator = act.selectedFilesPath.listIterator();
        while (iterator.hasNext()) {
            int cmp = path.compareTo(iterator.next());
            if (cmp < 0) {
                iterator.previous();
                iterator.add(path);
                return;
            }
        }
        iterator.add(path);
    }

    private void unselectFilePath(String path) {
        act.selectedFilesPath.remove(path);
    }

    private int getLastDirectoryIndex() {
        // use binary search :)
        int low = 0;
        int high = files.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (files[mid].isDirectory() && ((files.length >= mid + 2 && files[mid + 1].isFile())
                    || files.length <= mid + 1)) {
                return mid;
            }
            if (files[mid].isFile() && (files.length >= mid + 2 && files[mid + 1].isFile())) {
                high = mid - 1;
            } else if (files[mid].isDirectory() && files.length >= mid + 2 && files[mid + 1].isDirectory()) {
                low = mid + 1;
            } else if (files[mid].isFile() && files.length <= mid + 1) { // only files
                return -1;
            }
        }
        return -1;  // dir empty
    }

    private static class DirectoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView dirNameTV;

        public DirectoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dirNameTV = itemView.findViewById(R.id.TV_DirectoryName);
        }

        public void setup(File file, View.OnClickListener onClickListener) {
            dirNameTV.setText(file.getName());
            itemView.setOnClickListener(onClickListener);
        }

    }


    private static class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fileImageIV;
        private final TextView fileNameTV;
        private final TextView fileSizeTV;
        private final TextView fileTypeNameTV;
        private final CheckBox fileCB;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImageIV = itemView.findViewById(R.id.IV_FileImage);
            fileNameTV = itemView.findViewById(R.id.TV_FileName);
            fileSizeTV = itemView.findViewById(R.id.TV_FileSize);
            fileTypeNameTV = itemView.findViewById(R.id.TV_FileTypeName);
            fileCB = itemView.findViewById(R.id.CB_SelectFile);

        }

        public void setup(StorageAdapter adapter, int pos) {

            File file = adapter.files[pos];

            fileImageIV.setImageResource(R.drawable.icon_file_red);
            fileNameTV.setText(file.getName());
            fileSizeTV.setText(Utils.getFormattedSize(file.length()));
            fileCB.setChecked(Collections.binarySearch(adapter.act.selectedFilesPath, file.getPath()) >= 0);

            String typeName = FileTypeName.getFileTypeName(file.getName());
            if (!typeName.equals("*/*") && !typeName.isEmpty())
                fileTypeNameTV.setText(adapter.act.getString(R.string.file_type, typeName));
            else fileTypeNameTV.setText("");

            itemView.setOnClickListener((view) -> {
                boolean isChecked = fileCB.isChecked();
                if (!isChecked) {
                    adapter.selectFilePath(file.getPath());
                } else {
                    adapter.unselectFilePath(file.getPath());
                }
                fileCB.setChecked(!isChecked);
            });
        }
    }

    private static class EmptySpace extends RecyclerView.ViewHolder {
        public EmptySpace(@NonNull View itemView) {
            super(itemView);
        }
    }
}
