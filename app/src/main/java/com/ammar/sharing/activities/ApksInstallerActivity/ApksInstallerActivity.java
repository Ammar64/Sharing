package com.ammar.sharing.activities.ApksInstallerActivity;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.FileUtils;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.services.PackageInstallerService;

public class ApksInstallerActivity extends AppCompatActivity {

    private Button pickFileB;

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
        setContentView(R.layout.activity_apksinstaller);
        initItems();
        setListeners();
    }


    private void initItems() {
        Toolbar appBar = findViewById(R.id.TB_Toolbar);
        setSupportActionBar(appBar);
        appBar.setNavigationIcon(R.drawable.icon_back);

        pickFileB = findViewById(R.id.B_InstallerFilePick);
    }


    private void setListeners() {
        pickFileB.setOnClickListener((view) -> mGetContent.launch("*/*"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home ) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), (result) -> {
        if( result == null ) return;

        if (!FileUtils.getFileName(getContentResolver() ,result).endsWith(".apks")) {
            Toast.makeText(this, R.string.pls_choose_apks, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MYLOG", "PATH: " + result.getPath());


        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_installer, null, false);
        AlertDialog installerDialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .create();

        AdaptiveTextView titleTV = dialogLayout.findViewById(R.id.TV_InstallerDialogTitle);
        AdaptiveTextView operationTV = dialogLayout.findViewById(R.id.TV_InstallerDialogOperation);
        ProgressBar progress = dialogLayout.findViewById(R.id.PB_InstallerDialogProgress);
        Button installerButton = dialogLayout.findViewById(R.id.B_InstallerActionButton);

        installerDialog.setCancelable(false);
        installerDialog.setCanceledOnTouchOutside(false);

        Window window = installerDialog.getWindow();
        if (window != null)
            window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);

        titleTV.setDark(darkMode);
        operationTV.setDark(darkMode);


        progress.setVisibility(View.VISIBLE);
        installerButton.setText(android.R.string.cancel);
        installerButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, PackageInstallerService.class);
            intent.setAction(Consts.ACTION_STOP_INSTALLER);
            startService(intent);
            installerDialog.dismiss();
        });

        installerDialog.show();


        Intent intent = new Intent(this, PackageInstallerService.class);
        intent.setAction(Consts.ACTION_TRIGGER_APKS_INSTALL);
        intent.setData(result);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startService(intent);

        PackageInstallerService.installInfoNotifier.observe(this, (info) -> {
            String title = info.getString("title", "");
            String operation = info.getString("text", "");

            if (!title.isEmpty()) titleTV.setText(title);
            operationTV.setText(operation);

            if (info.getBoolean("stopProgress", false)) {
                progress.setVisibility(View.GONE);
            }

            if (info.getBoolean("buttonOk", false)) {
                installerButton.setText(android.R.string.ok);
                installerButton.setOnClickListener((view) -> installerDialog.dismiss());
            }
        });
    });

}
