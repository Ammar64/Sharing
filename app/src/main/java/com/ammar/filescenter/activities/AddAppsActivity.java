package com.ammar.filescenter.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.recyclers.AppsRecyclerAdapter;

import java.util.List;

public class AddAppsActivity extends AppCompatActivity {

    private RecyclerView appsRecycler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apps);
        appsRecycler = findViewById(R.id.RV_AppsRecycler);

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> appsInfo = pm.getInstalledApplications(0);



        AppsRecyclerAdapter appsAdapter = new AppsRecyclerAdapter(this, appsInfo);
        appsRecycler.setAdapter(appsAdapter);
        appsRecycler.setLayoutManager(new GridLayoutManager(this, 3));


    }
}
