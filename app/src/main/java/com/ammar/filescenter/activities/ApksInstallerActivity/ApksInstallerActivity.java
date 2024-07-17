package com.ammar.filescenter.activities.ApksInstallerActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.custom.ui.AdaptiveTextView;
import com.ammar.filescenter.services.PackageInstallerService;

public class ApksInstallerActivity extends AppCompatActivity {

    private LinearLayout pickFileLL;
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
        pickFileLL = findViewById(R.id.LL_InstallerFilePick);
        pickFileB = findViewById(R.id.B_InstallerFilePick);
    }

    private void setListeners() {
        pickFileB.setOnClickListener((view) -> {
            mGetContent.launch("*/*");
        });
    }


    private LinearLayout wrapper = null;
    private ProgressBar bar = null;
    private boolean hadProgress = false;

    private View makeLogText(Bundle log) {

        if (hadProgress) {
            wrapper.removeViewAt(wrapper.getChildCount() - 1);
            hadProgress = false;
        }

        wrapper = new LinearLayout(this);

        AdaptiveTextView logText = new AdaptiveTextView(this);
        logText.setTextSize(14);
        logText.setText(log.getString("L"));

        wrapper.addView(logText);

        if (log.getBoolean("P", false)) {
            bar = new ProgressBar(this);
            int size = (int) Utils.dpToPx(20);
            bar.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            logText.setPaddingRelative(0, 0, 24, 0);
            wrapper.addView(bar);
            hadProgress = true;
        }
        return wrapper;
    }


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), (result) -> {
        if(!getFileName(result).endsWith(".apks")) {
            Toast.makeText(this, R.string.pls_choose_apks, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MYLOG", "PATH: " + result.getPath());


        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_installer, null, false);
        AlertDialog installerDialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    Intent intent = new Intent(this, PackageInstallerService.class);
                    intent.setAction(Consts.ACTION_STOP_INSTALLER);
                    startService(intent);
                })
                .create();
        installerDialog.setCancelable(false);
        installerDialog.setCanceledOnTouchOutside(false);

        Window window = installerDialog.getWindow();
        if (window != null)
            window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);

        LinearLayout logLayout = dialogLayout.findViewById(R.id.LL_InstallerLogLayout);
        installerDialog.show();


        Intent intent = new Intent(this, PackageInstallerService.class);
        intent.setAction(Consts.ACTION_TRIGGER_APKS_INSTALL);
        intent.setData(result);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startService(intent);

        PackageInstallerService.logNotifier.observe(this, (log) -> {
            if (log.getBoolean("D", false)) {
                logLayout.postDelayed(installerDialog::dismiss, log.getInt("T"));
            }
            logLayout.addView(makeLogText(log));
        });
    });

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME} , null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
