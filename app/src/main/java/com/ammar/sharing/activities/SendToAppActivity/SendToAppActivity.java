package com.ammar.sharing.activities.SendToAppActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.custom.ui.DefaultActivity;
import com.ammar.sharing.services.ServerService;

import java.util.ArrayList;

public class SendToAppActivity extends DefaultActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendtoapp);
        launcher.launch(new Intent(this, AddAppsAndFilesActivity.class));
    }

    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if( data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_URIS_SHARED, false) ) {
                ArrayList<Uri> uris = data.getParcelableArrayListExtra(AddAppsAndFilesActivity.EXTRA_URIS);
            }
            if( data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_APPS_SHARED, false) ) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_PACKAGES_NAMES);
            }
            if (data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_FILE_PATHS_SHARED, false)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_FILES_PATHS);
            }

            //TODO: Send selected files to the fragment
        }
    });



}
