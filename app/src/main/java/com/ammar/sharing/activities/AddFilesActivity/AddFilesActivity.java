package com.ammar.sharing.activities.AddFilesActivity;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddFilesActivity.adaptersR.StorageAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.FileUtils;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.custom.ui.AdaptiveDropDown;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class AddFilesActivity extends AppCompatActivity {
    public Toolbar appBar;
    private View selectV;
    private View sortByV;
    public final ArrayList<String> selectedFilesPath = new ArrayList<>();
    public RecyclerView recyclerView;
    public ScrollControllerGridLayoutManager layoutManager;
    private StorageAdapter storageAdapter;
    public AppCompatTextView folderEmptyTV;
    private AdaptiveDropDown dropDownMenu;

    // file where we store recently selected files
    private File recentsFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (darkMode) {
            setTheme(R.style.AppThemeDark);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setTheme(R.style.AppTheme);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_files);
        initItems();
        setListeners();

    }

    private void initItems() {
        recentsFile = new File(getFilesDir(), "recent_files.json");

        appBar = findViewById(R.id.TB_Toolbar);
        appBar.setNavigationIcon(R.drawable.icon_back);
        appBar.setNavigationContentDescription(R.string.back);
        appBar.setTitle(R.string.select_files);

        selectV = findViewById(R.id.MI_Select);
        // disabled for now sort by will be implemented in next releases
//        dropDownMenu = new AdaptiveDropDown(this);
//        sortByV = dropDownMenu.addItem(R.string.sort_by);
//        dropDownMenu.setAnchorView(findViewById(R.id.MI_DropDown));

        // this line must be before initializing the adapter
        folderEmptyTV = findViewById(R.id.TV_FolderEmpty);

        recyclerView = findViewById(R.id.RV_FilesRecycler);
        layoutManager = new ScrollControllerGridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 || position == 1 ? 2 : 1;
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        storageAdapter = new StorageAdapter(this);
        recyclerView.setAdapter(storageAdapter);

    }

    private void setListeners() {
        appBar.setNavigationOnClickListener(v -> storageAdapter.goBack());
        selectV.setOnClickListener(v -> {
            if (selectedFilesPath.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            new Thread(() -> saveFilesToRecent(selectedFilesPath.toArray(new String[0]))).start();

            Intent intent = new Intent(Consts.ACTION_ADD_FILES);
            intent.putStringArrayListExtra(Consts.EXTRA_FILES_PATH, selectedFilesPath);
            setResult(RESULT_OK, intent);
            finish();
        });


//        sortByV.setOnClickListener(v -> {
//
//        });

    }

    public File getRecentsFile() {
        return recentsFile;
    }

    private void saveFilesToRecent(String[] newRecentFiles) {
        try {
            JSONArray selectedFilesJson;
            // check if the file exists then read already stored recent files info
            if (recentsFile.exists()) {
                byte[] data = FileUtils.readWholeFile(recentsFile);
                selectedFilesJson = new JSONArray(new String(data));

                // remove files paths that already exists
                for (int i = 0; i < selectedFilesJson.length(); i++) {
                    JSONObject fileObject = selectedFilesJson.getJSONObject(i);
                    if (fileObject.isNull("path")) continue;
                    String path = fileObject.getString("path");
                    for (int j = 0; j < newRecentFiles.length; j++) {
                        if (path.equals(newRecentFiles[j])) {
                            newRecentFiles[j] = null;
                            fileObject.put("lastSelectedTime", System.currentTimeMillis());
                        }
                    }
                }
            } else {
                selectedFilesJson = new JSONArray();
            }


            for (String i : newRecentFiles) {
                if (i != null) {
                    JSONObject selectedFileJson = new JSONObject();
                    selectedFileJson.put("path", i);
                    selectedFileJson.put("lastSelectedTime", System.currentTimeMillis());
                    selectedFilesJson.put(selectedFileJson);
                }
            }

            String logJ = selectedFilesJson.toString();
            byte[] recentFilesJsonBytes = logJ.getBytes(StandardCharsets.UTF_8);

            FileUtils.overwriteFile(recentsFile, recentFilesJsonBytes);
        } catch (JSONException e) {
            Utils.showErrorDialog("saveFilesToRecent(). JSONException:", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class ScrollControllerGridLayoutManager extends GridLayoutManager {

        public ScrollControllerGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public ScrollControllerGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        public ScrollControllerGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }


        private boolean canScroll = true;

        public void setCanScroll(boolean canScroll) {
            this.canScroll = canScroll;
        }

        @Override
        public boolean canScrollVertically() {
            return canScroll;
        }
    }

}
