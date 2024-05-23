package com.ammar.filescenter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.recyclers.AddFilesAdapter;

import java.util.LinkedList;

public class AddFilesActivity extends AppCompatActivity {
    public static final String EXTRA_INTENT_PATHS = "com.ammar.filescenter.EXTRA_INTENT_PATHS";
    LinkedList<String> selectedFiles = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_files);
        setSupportActionBar(findViewById(R.id.TB_AddFiles));
        DocumentFile internalStorage = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        DocumentFile[] files = internalStorage.listFiles();

        DocumentFile storageRoot = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        RecyclerView filesView = findViewById(R.id.RV_FilesList);
        AddFilesAdapter filesAdapter = new AddFilesAdapter(this, storageRoot, selectedFiles);
        filesView.setAdapter(filesAdapter);
        filesView.setLayoutManager(new LinearLayoutManager(this));


        OnBackPressedCallback onBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                filesAdapter.goBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBack);


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
            if (selectedFiles.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_INTENT_PATHS, selectedFiles);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
