package com.ammar.filescenter.activities.AddFilesActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddFilesActivity.adaptersR.StorageAdapter;
import com.ammar.filescenter.common.Consts;

import java.util.ArrayList;

public class AddFilesActivity extends AppCompatActivity {
    private Toolbar appBar;
    public final ArrayList<String> selectedFilesPath = new ArrayList<>();
    public RecyclerView recyclerView;
    private StorageAdapter storageAdapter;
    public AppCompatTextView folderEmptyTV;
    public AppCompatEditText searchInputET;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if( darkMode ) {
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
        appBar = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(appBar);
        setTitle(R.string.select_files);
        searchInputET = findViewById(R.id.ET_SearchFilesInput);

        // this line must be before initializing the adapter
        folderEmptyTV = findViewById(R.id.TV_FolderEmpty);

        recyclerView = findViewById(R.id.RV_FilesRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        storageAdapter = new StorageAdapter(this);
        recyclerView.setAdapter(storageAdapter);
    }

    private void setListeners() {
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MI_AddFilesDone) {
            if (selectedFilesPath.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
            }
            Intent intent = new Intent(Consts.ACTION_ADD_FILES);
            intent.putStringArrayListExtra(Consts.EXTRA_FILES_PATH, selectedFilesPath);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
