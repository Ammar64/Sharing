package com.ammar.filescenter.activities.ApksInstallerActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.custom.ui.AdaptiveTextView;
import com.ammar.filescenter.services.PackageInstallerService;

public class ApksInstallerActivity extends AppCompatActivity {

    private LinearLayout logLayout;
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), (result) -> {
        Intent intent = new Intent(this, PackageInstallerService.class);
        intent.setAction(Consts.ACTION_TRIGGER_APKS_INSTALL);
        intent.setData(result);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startService(intent);

        PackageInstallerService.logNotifier.observe(this, (log) -> {
            logLayout.addView(makeLogTextView(log));
        });
    });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if( darkMode ) {
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
        logLayout = findViewById(R.id.LL_InstallerLogLayout);
        mGetContent.launch("application/*");
    }


    private TextView makeLogTextView(String text) {
        AdaptiveTextView logText = new AdaptiveTextView(this);
        logText.setTextSize(14);
        //logText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        logText.setText(text);
        return logText;
    }
}
