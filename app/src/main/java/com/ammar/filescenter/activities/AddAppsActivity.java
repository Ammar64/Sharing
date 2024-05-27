package com.ammar.filescenter.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.recyclers.AppsRecyclerAdapter;

import java.util.LinkedList;
import java.util.List;

public class AddAppsActivity extends AppCompatActivity {
    public static final String EXTRA_INTENT_APPS = "com.ammar.filescenter.SELECTED_APPS";
    private RecyclerView appsRecycler;
    LinkedList<String> selectedApps = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apps);
        setSupportActionBar(findViewById(R.id.TB_AddApps));
        appsRecycler = findViewById(R.id.RV_AppsRecycler);

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> appsInfo = pm.getInstalledApplications(0);



        AppsRecyclerAdapter appsAdapter = new AppsRecyclerAdapter(this, appsInfo, selectedApps);
        appsRecycler.setAdapter(appsAdapter);
        appsRecycler.setLayoutManager(new GridLayoutManager(this, 3));


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
            if (selectedApps.isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_INTENT_APPS, selectedApps);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
