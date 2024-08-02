package com.ammar.filescenter.activities.AddFilesActivity.adaptersR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Environment;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddFilesActivity.AddFilesActivity;
import com.ammar.filescenter.common.FileUtils;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.custom.ui.AdaptiveTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class StorageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEARCH = 0;
    private static final int TYPE_FILE_TYPES = 1;
    private static final int TYPE_DIR = 2;
    private static final int TYPE_FILE = 3;
    private static final int TYPE_SPACE = 4;

    private static final int SORT_NAME = 0;
    private static final int SORT_LAST_MODIFIED = 1;

    private final File internalStorage = Environment.getExternalStorageDirectory();
    private File currentDir = internalStorage;
    private File[] files;
    private File[] displayedFiles;

    private int lastDirIndex = -1;
    private final AddFilesActivity act;
    private final Stack<Parcelable> recyclerViewStates = new Stack<>();

    private Runnable onGoBack = null;

    public StorageAdapter(AddFilesActivity act) {
        this.act = act;
        viewDirectory(currentDir);

        act.getOnBackPressedDispatcher().addCallback(act, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_SEARCH;
        if (position == 1) return TYPE_FILE_TYPES;
        position -= 2;
        boolean hasSpaceView = (lastDirIndex & 1) == 0;
        if ((position - 1 == lastDirIndex) && hasSpaceView) {
            return TYPE_SPACE;
        }
        if (position > lastDirIndex && hasSpaceView) position--;
        return displayedFiles[position].isDirectory() ? TYPE_DIR : TYPE_FILE;
    }

    @NonNull
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_SEARCH:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_search, parent, false);
                return new SearchBarHolder(view, this);
            case TYPE_FILE_TYPES:
                HorizontalScrollView scrollView = new HorizontalScrollView(parent.getContext());
                scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                scrollView.setPadding(0, 24, 0, 24);
                scrollView.setVerticalScrollBarEnabled(false);
                scrollView.setHorizontalScrollBarEnabled(false);
                return new FileTypesHolder(scrollView, this);
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int positionR) {
        int type = getItemViewType(positionR);
        int position = positionR - 2;
        switch (type) {
            case TYPE_DIR:
                DirectoryViewHolder dirHolder = (DirectoryViewHolder) holder;
                dirHolder.setup(displayedFiles[position], (view) -> viewDirectory(displayedFiles[position]));
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
    }


    @Override
    public int getItemCount() {
        // if the last directory index is even add 1 to item count to put empty space in it
        // if no directories present then lastDirIndex will be -1 and (-1 & 1) is 1
        return ((lastDirIndex & 1) == 1 ? displayedFiles.length : displayedFiles.length + 1) + 2;
    }

    private void viewDirectory(File dir, boolean pop) {
        this.currentDir = dir;
        File[] listedFiles = currentDir.listFiles();
        if (listedFiles == null) { // permission denied
            Toast.makeText(act, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        this.files = listedFiles;
        this.displayedFiles = files;

        Animation anim;
        if (!pop) {
            recyclerViewStates.push(act.recyclerView.getLayoutManager().onSaveInstanceState());
            anim = AnimationUtils.loadAnimation(act, R.anim.to_up);
        } else {
            act.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewStates.pop());
            anim = AnimationUtils.loadAnimation(act, R.anim.to_down);
        }

        sortFiles(this.files, SORT_NAME);
        lastDirIndex = getLastDirectoryIndex();

        if (this.displayedFiles.length == 0) {
            act.folderEmptyTV.setVisibility(View.VISIBLE);
            act.folderEmptyTV.startAnimation(AnimationUtils.loadAnimation(act, R.anim.appear));
        } else act.folderEmptyTV.setVisibility(View.GONE);


        filesChanged();
        if (internalStorage.compareTo(dir) == 0) {
            act.appBar.setTitle(R.string.internal_storage);
        } else {
            act.appBar.setTitle(dir.getName());
        }

        act.recyclerView.startAnimation(anim);
    }

    private void viewFileType(int fileType) {
        ArrayList<File> filesList = new ArrayList<>();
        Utils.findFileTypeRecursively(Environment.getExternalStorageDirectory().getAbsolutePath(), filesList, fileType);
        currentDir = null;
        this.files = filesList.toArray(new File[0]);
        sortFiles(files, SORT_LAST_MODIFIED);
        this.displayedFiles = files;
        lastDirIndex = -1;
        filesChanged();
    }

    private void viewRecentFiles() {
        class RecentFile {
            final File file;
            final long lastSelected;

            public RecentFile(File file, long lastSelected) {
                this.file = file;
                this.lastSelected = lastSelected;
            }
        }

        new Thread(() -> {
            try {
                JSONArray jsonArray = new JSONArray(new String(FileUtils.readWholeFile(act.getRecentsFile())));

                RecentFile[] recentFiles = new RecentFile[jsonArray.length()];
                for( int i = 0 ; i < jsonArray.length() ; i++ ) {
                    JSONObject fileObject = jsonArray.getJSONObject(i);
                    recentFiles[i] = new RecentFile(
                            new File(fileObject.getString("path")),
                            fileObject.getLong("lastSelectedTime")
                    );
                }
                Arrays.sort(recentFiles, (l, r) -> Long.compare(r.lastSelected, l.lastSelected));

                this.files = new File[recentFiles.length];
                for( int i = 0 ; i < recentFiles.length ; i++ ) {
                    this.files[i] = recentFiles[i].file;
                }
                this.displayedFiles = files;

                currentDir = null;
                lastDirIndex = -1;

                act.runOnUiThread(this::filesChanged);
            } catch (JSONException e) {
                Utils.showErrorDialog("StorageAdapter.viewRecentFiles(). JSONException:", e.getMessage());
            } catch (IOException e) {
                Utils.showErrorDialog("StorageAdapter.viewRecentFiles(). IOException:", e.getMessage());
            }
        }).start();
    }

    private void viewDirectory(File dir) {
        viewDirectory(dir, false);
    }

    public void goBack() {
        if( onGoBack != null ) onGoBack.run();
        if( currentDir == null ) {
            viewDirectory(internalStorage);
        } else if (currentDir.compareTo(internalStorage) != 0) {
            viewDirectory(currentDir.getParentFile(), true);
        } else {
            act.setResult(Activity.RESULT_CANCELED);
            act.finish();
        }
    }

    // sort files but put directories first
    private void sortFiles(File[] files, int sortType) {
        Arrays.sort(files, (l, r) -> {
            if (l.isDirectory() && r.isFile()) return -1;
            if (l.isFile() && r.isDirectory()) return 1;
            else {
                switch (sortType) {
                    case SORT_NAME:
                        return l.compareTo(r);
                    case SORT_LAST_MODIFIED:
                        return Long.compare(r.lastModified(), l.lastModified());
                }
            }
            throw new IllegalArgumentException();
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
        int high = displayedFiles.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (displayedFiles[mid].isDirectory() && ((displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].isFile())
                    || displayedFiles.length <= mid + 1)) {
                return mid;
            }
            if (displayedFiles[mid].isFile() && (displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].isFile())) {
                high = mid - 1;
            } else if (displayedFiles[mid].isDirectory() && displayedFiles.length >= mid + 2 && displayedFiles[mid + 1].isDirectory()) {
                low = mid + 1;
            } else if (displayedFiles[mid].isFile() && displayedFiles.length <= mid + 1) { // only files
                return -1;
            }
        }
        return -1;  // dir empty
    }

    // use lowercase only
    public void searchDirectory(String searchInput) {
        if (searchInput.isEmpty()) {
            this.displayedFiles = this.files;
            lastDirIndex = getLastDirectoryIndex();
            filesChanged();
            return;
        }

        ArrayList<File> searchedFiles = new ArrayList<>(10);
        for (File i : this.files) {
            if (i.getName().toLowerCase().contains(searchInput)) {
                searchedFiles.add(i);
            }
        }
        this.displayedFiles = new File[searchedFiles.size()];
        searchedFiles.toArray(this.displayedFiles);
        lastDirIndex = getLastDirectoryIndex();
        filesChanged();
    }

    private void filesChanged() {
        notifyDataSetChanged();
    }
    public static class SearchBarHolder extends RecyclerView.ViewHolder {

        public SearchBarHolder(@NonNull View itemView, StorageAdapter adapter) {
            super(itemView);
            AppCompatEditText searchInput = itemView.findViewById(R.id.ET_SearchInput);
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.searchDirectory(s.toString().toLowerCase());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
    public void setOnGoBack(Runnable onGoBack) {
        this.onGoBack = onGoBack;
    }

    private static class FileTypesHolder extends RecyclerView.ViewHolder {
        public FileTypesHolder(@NonNull HorizontalScrollView itemView, StorageAdapter adapter) {
            super(itemView);
            LinearLayout layout = new LinearLayout(itemView.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);

            itemView.addView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int padding = Math.round(Utils.dpToPx(8));
            layout.setPadding(padding, 0, padding, 0);

            FileType recent = new FileType(R.string.recent, R.drawable.icon_recent);
            FileType images = new FileType(R.string.images, R.drawable.icon_image);
            FileType videos = new FileType(R.string.videos, R.drawable.icon_video);
            FileType audio = new FileType(R.string.audio, R.drawable.icon_audio);
            FileType docs = new FileType(R.string.documents, R.drawable.icon_document);

            recent.setOnClick((view) -> {
                recent.setSelected(true);
                images.setSelected(false);
                videos.setSelected(false);
                audio.setSelected(false);
                docs.setSelected(false);

                adapter.act.appBar.setTitle(R.string.recent);
                adapter.viewRecentFiles();
            });
            images.setOnClick((view) -> {
                recent.setSelected(false);
                images.setSelected(true);
                videos.setSelected(false);
                audio.setSelected(false);
                docs.setSelected(false);

                adapter.act.appBar.setTitle(R.string.images);
                adapter.viewFileType(Utils.FILE_TYPE_IMAGE);
            });
            videos.setOnClick((view) -> {
                recent.setSelected(false);
                images.setSelected(false);
                videos.setSelected(true);
                audio.setSelected(false);
                docs.setSelected(false);

                adapter.act.appBar.setTitle(R.string.videos);
                adapter.viewFileType(Utils.FILE_TYPE_VIDEO);
            });
            audio.setOnClick((view) -> {
                recent.setSelected(false);
                images.setSelected(false);
                videos.setSelected(false);
                audio.setSelected(true);
                docs.setSelected(false);

                adapter.act.appBar.setTitle(R.string.audio);
                adapter.viewFileType(Utils.FILE_TYPE_AUDIO);
            });
            docs.setOnClick((view) -> {
                recent.setSelected(false);
                images.setSelected(false);
                videos.setSelected(false);
                audio.setSelected(false);
                docs.setSelected(true);

                adapter.act.appBar.setTitle(R.string.documents);
                adapter.viewFileType(Utils.FILE_TYPE_DOCUMENT);
            });

            adapter.setOnGoBack(() -> {
                recent.setSelected(false);
                images.setSelected(false);
                videos.setSelected(false);
                audio.setSelected(false);
                docs.setSelected(false);
            });
            List<FileType> types = Arrays.asList(
                    recent,
                    images,
                    videos,
                    audio,
                    docs
            );

            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            ListIterator<FileType> iterator = types.listIterator();

            while (iterator.hasNext()) {
                iterator.next().setupView(inflater, layout, iterator.hasNext());
            }
        }

        private static class FileType {
            @StringRes
            public int text;

            @DrawableRes
            public int icon;

            public View.OnClickListener onClick;

            public FileType(int text, int icon) {
                this.text = text;
                this.icon = icon;
            }

            public void setOnClick(View.OnClickListener onClick) {
                this.onClick = onClick;
            }

            private CardView view;
            public void setupView(LayoutInflater inflater, ViewGroup layout, boolean withMarginEnd) {
                view = (CardView) inflater.inflate(R.layout.card_file_type, layout, false);
                AdaptiveTextView textView = view.findViewById(R.id.TV_FileTypeText);
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
                textView.setText(text);
                if( withMarginEnd ){ // is not last element
                    ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
                    layoutParams.setMarginEnd(Math.round(Utils.dpToPx(8)));
                    view.setLayoutParams(layoutParams);
                }
                view.setOnClickListener(onClick);
                layout.addView(view);
            }

            public void setSelected(boolean selected) {
                if( selected ) {
                    view.setCardBackgroundColor(view.getContext().getResources().getColor(R.color.checked_card));
                } else {
                    view.setCardBackgroundColor(0x77000000);
                }
            }
        }
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
        private final View lineV;
        private final TextView fileNameTV;
        private final TextView fileSizeTV;
        private final TextView fileTypeNameTV;
        private final CheckBox fileCB;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImageIV = itemView.findViewById(R.id.IV_FileImage);
            lineV = itemView.findViewById(R.id.V_Line);
            fileNameTV = itemView.findViewById(R.id.TV_FileName);
            fileSizeTV = itemView.findViewById(R.id.TV_FileSize);
            fileTypeNameTV = itemView.findViewById(R.id.TV_FileTypeName);
            fileCB = itemView.findViewById(R.id.CB_SelectFile);

        }

        public void setup(StorageAdapter adapter, int pos) {

            File file = adapter.displayedFiles[pos];

            if (FileUtils.setFileIcon(fileImageIV, file)) {
                lineV.setVisibility(View.INVISIBLE);
            } else {
                lineV.setVisibility(View.VISIBLE);
            }

            fileNameTV.setText(file.getName());
            fileSizeTV.setText(Utils.getFormattedSize(file.length()));
            fileCB.setChecked(Collections.binarySearch(adapter.act.selectedFilesPath, file.getPath()) >= 0);

            String typeName = FileUtils.getFileTypeName(file.getName());
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
