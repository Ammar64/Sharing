package com.ammar.filescenter.activities.AddFilesActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddFilesActivity.adaptersR.StorageAdapter;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.custom.ui.AdaptiveDropDown;

import java.io.File;
import java.util.ArrayList;

public class AddFilesActivity extends AppCompatActivity {
    public Toolbar appBar;
    private View selectV;
    private View sortByV;
    public final ArrayList<String> selectedFilesPath = new ArrayList<>();
    public RecyclerView recyclerView;
    private StorageAdapter storageAdapter;
    public AppCompatTextView folderEmptyTV;
    private AdaptiveDropDown dropDownMenu;

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
        ArrayList<File> filesList = new ArrayList<>();
        Utils.findImagesRecursively(Environment.getExternalStorageDirectory().getAbsolutePath(), filesList);

        appBar = findViewById(R.id.TB_Toolbar);
        appBar.setNavigationIcon(R.drawable.icon_back);
        appBar.setNavigationContentDescription(R.string.back);
        appBar.setTitle(R.string.select_files);

        selectV = findViewById(R.id.MI_Select);
        dropDownMenu = new AdaptiveDropDown(this);
        sortByV = dropDownMenu.addItem(R.string.sort_by);
        dropDownMenu.setAnchorView(findViewById(R.id.MI_DropDown));

        // this line must be before initializing the adapter
        folderEmptyTV = findViewById(R.id.TV_FolderEmpty);

        recyclerView = findViewById(R.id.RV_FilesRecycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
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
            }
            Intent intent = new Intent(Consts.ACTION_ADD_FILES);
            intent.putStringArrayListExtra(Consts.EXTRA_FILES_PATH, selectedFilesPath);
            setResult(RESULT_OK, intent);
            finish();
        });
        sortByV.setOnClickListener(v -> {
            
        });

    }

}
