package com.ammar.filescenter.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.recyclers.AppsRecyclerAdapter;

import java.util.LinkedList;
import java.util.List;

public class AddAppsActivity extends AppCompatActivity {
    public static final String EXTRA_INTENT_APPS = "com.ammar.filescenter.SELECTED_APPS";
    public static final String ACTION_ADD_APPS = "ACTION_ADD_APPS";
    private RecyclerView appsRecycler;
    private Toolbar appBar;
    LinkedList<String> selectedApps = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apps);
        appBar = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(appBar);
        appsRecycler = findViewById(R.id.RV_AppsRecycler);

        TextView loadingTV = findViewById(R.id.TV_AppsLoading);
        ProgressBar loadingPB = findViewById(R.id.PB_AppsLoading);
        new Thread(() -> {
            List<ApplicationInfo> userApps = new LinkedList<>();
            int flags = PackageManager.GET_META_DATA |
                    PackageManager.GET_SHARED_LIBRARY_FILES |
                    PackageManager.GET_UNINSTALLED_PACKAGES;

            PackageManager pm = getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(flags);
            for (ApplicationInfo appInfo : applications) {
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                    userApps.add(appInfo);
                }
            }
            AppsRecyclerAdapter appsAdapter = new AppsRecyclerAdapter(this, userApps, selectedApps);
            runOnUiThread(() -> {
                appBar.setTitle(R.string.select_apps);
                appsRecycler.setAdapter(appsAdapter);
                appsRecycler.setHasFixedSize(true);

                appsRecycler.setLayoutManager(new GridLayoutManager(this, 3) {
                    @Override
                    public void onLayoutCompleted(RecyclerView.State state) {
                        super.onLayoutCompleted(state);
                        loadingTV.setVisibility(View.GONE);
                        loadingPB.setVisibility(View.GONE);
                    }
                });


            });
        }).start();
    }

    public void setToolbarTitle(String title) {
        appBar.setTitle(title);
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
            Intent intent = new Intent(ACTION_ADD_APPS);
            intent.putExtra(EXTRA_INTENT_APPS, selectedApps);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
