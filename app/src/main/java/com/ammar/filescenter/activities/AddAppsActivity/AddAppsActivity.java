package com.ammar.filescenter.activities.AddAppsActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity.adaptersR.AppsRecyclerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AddAppsActivity extends AppCompatActivity {
    public static final String EXTRA_INTENT_APPS = "com.ammar.filescenter.SELECTED_APPS";
    public static final String ACTION_ADD_APPS = "ACTION_ADD_APPS";
    private RecyclerView appsRecycler;
    private Toolbar appBar;
    public AppCompatEditText searchInputET;
    LinkedList<String> selectedApps = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if( darkMode ) {
            setTheme(R.style.AppThemeDark);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getWindow().setBackgroundDrawableResource(R.drawable.gradient_background_light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apps);
        appBar = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(appBar);
        appsRecycler = findViewById(R.id.RV_AppsRecycler);
        setTitle(R.string.select_apps);

        TextView loadingTV = findViewById(R.id.TV_AppsLoading);
        ProgressBar loadingPB = findViewById(R.id.PB_AppsLoading);
        searchInputET = findViewById(R.id.ET_SearchAppsInput);
        new Thread(() -> {
            ArrayList<ApplicationInfo> userApps = new ArrayList<>(30);
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
                appsRecycler.setLayoutManager(new GridLayoutManager(this, 3) {

                    @Override
                    public void onLayoutCompleted(RecyclerView.State state) {
                        super.onLayoutCompleted(state);
                        loadingTV.setVisibility(View.GONE);
                        loadingPB.setVisibility(View.GONE);
                        findViewById(R.id.CV_SearchAppsWrapper).setVisibility(View.VISIBLE);
                        findViewById(R.id.V_Border).setVisibility(View.VISIBLE);
                    }
                });
                appsRecycler.setAdapter(appsAdapter);
                appsRecycler.setHasFixedSize(true);
            });
        }).start();
    }

    public void setToolbarTitle(String title) {
        setTitle(title);
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
