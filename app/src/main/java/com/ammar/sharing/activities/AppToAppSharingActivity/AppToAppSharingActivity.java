package com.ammar.sharing.activities.AppToAppSharingActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.custom.ui.DefaultActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class AppToAppSharingActivity extends DefaultActivity {
    public static final String ACTION_SEND_TO_DEVICES = "ACTION_SEND_TO_DEVICES";
    public static final String ACTION_RECEIVE_FROM_DEVICES = "ACTION_RECEIVE_FROM_DEVICES";

    private NavHostFragment mNavHostFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apptoappsharing);

        mNavHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.FCV_AppToAppSharingNavHost);
        assert mNavHostFragment != null;
        NavInflater navInflater = mNavHostFragment.getNavController().getNavInflater();
        NavGraph navGraph = navInflater.inflate(R.navigation.apptoappsharing);


        String action = getIntent().getAction();
        if (ACTION_SEND_TO_DEVICES.equals(action)) {
            launcher.launch(new Intent(this, AddAppsAndFilesActivity.class));
            navGraph.setStartDestination(R.id.discoverDevicesFragment);
        } else if (ACTION_RECEIVE_FROM_DEVICES.equals(action)) {
            navGraph.setStartDestination(R.id.listenerFragment);
        } else {
            Toast.makeText(this, "Invalid action.", Toast.LENGTH_SHORT).show();
            finish();
        }
        mNavHostFragment.getNavController().setGraph(navGraph);

        Toolbar appBar = findViewById(R.id.TB_Toolbar);
        appBar.setNavigationIcon(R.drawable.ic_back);

        mNavHostFragment.getNavController().addOnDestinationChangedListener((nc, nd, b) -> {
            appBar.setTitle(nd.getLabel());
        });

        appBar.setNavigationOnClickListener((v) -> {
            NavController navController = mNavHostFragment.getNavController();
            if (navController.getGraph().getStartDestinationId() == navController.getCurrentDestination().getId()) {
                finish();
                return;
            }
            navController.navigateUp();
        });
    }

    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if (data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_URIS_SHARED, false)) {
                ArrayList<Uri> uris = data.getParcelableArrayListExtra(AddAppsAndFilesActivity.EXTRA_URIS);
            }
            if (data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_APPS_SHARED, false)) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_PACKAGES_NAMES);
            }
            if (data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_FILE_PATHS_SHARED, false)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_FILES_PATHS);
            }

            //TODO: Send selected files to the fragment
        } else {
            finish();
        }
    });
}
