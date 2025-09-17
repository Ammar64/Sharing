package com.ammar.sharing.custom.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;

public class PermissionRequester {

    private final ActivityResultCaller resultCaller;
    private final ComponentActivity activity;
    private final String permission;
    private Runnable onGranted;
    private ActivityResultLauncher<String> permissionRequestLauncher;
    private String title = "";
    private String explanation = "";

    public PermissionRequester(ComponentActivity activity, String permission) {
        this.activity = activity;
        this.resultCaller = activity;
        this.permission = permission;
        this.title = "Title hasn't been set";
        this.explanation = "Explanation hasn't been set";
        this.permissionRequestLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::resultCallback);
    }

    public PermissionRequester(Fragment fragment, String permission) {
        this.activity = fragment.requireActivity();
        this.resultCaller = fragment;
        this.permission = permission;
        this.title = "Title hasn't been set";
        this.explanation = "Explanation hasn't been set";
        this.permissionRequestLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::resultCallback);
    }

    private void resultCallback(boolean granted) {
        if (granted) {
            onGranted.run();
        } else {
            showExplanationDialog(!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission));
        }
    }

    public PermissionRequester setTitle(@StringRes int title) {
        setTitle(activity.getResources().getString(title));
        return this;
    }

    public PermissionRequester setTitle(String title) {
        this.title = title;
        return this;
    }

    public PermissionRequester setExplanation(@StringRes int hint) {
        setExplanation(activity.getResources().getString(hint));
        return this;
    }

    public PermissionRequester setExplanation(String hint) {
        this.explanation = hint;
        return this;
    }

    public void request(Runnable onGranted) {
        this.onGranted = onGranted;
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            this.onGranted.run();
        } else {
            permissionRequestLauncher.launch(permission);
        }
    }


    private void showExplanationDialog(boolean request_in_settings) {
        RoundDialog permissionExplanationDialog = new RoundDialog(activity);
        permissionExplanationDialog.setView(R.layout.dialog_permission_explanation);
        permissionExplanationDialog.setCornerRadius((int) Utils.dpToPx(18));
        View v = permissionExplanationDialog.getView();

        TextView titleTV = v.findViewById(R.id.TV_PermissionExplanationDialogTitle);
        TextView explanationTV = v.findViewById(R.id.TV_PermissionExplanation);
        Button okButton = v.findViewById(R.id.B_PermissionExplanationDialogOk);
        Button cancelButton = v.findViewById(R.id.B_PermissionExplanationDialogCancel);

        titleTV.setText(title);
        explanationTV.setText(explanation);

        okButton.setOnClickListener((buttonView) -> {
            if (request_in_settings) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            } else {
                this.permissionRequestLauncher.launch(permission);
            }
        });

        cancelButton.setOnClickListener((buttonView) -> {
            permissionExplanationDialog.dismiss();
        });

        permissionExplanationDialog.show();
    }
}
