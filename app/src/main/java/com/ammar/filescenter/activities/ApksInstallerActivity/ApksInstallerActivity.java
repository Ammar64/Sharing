package com.ammar.filescenter.activities.ApksInstallerActivity;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        String filename = getFileName(result);
        if( !filename.endsWith(".apks") ) {
            Toast.makeText(this, "Please choose apks file", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
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

        if(getIntent().getData() == null) {
            mGetContent.launch("*/*");
        } else {
            Intent intent = new Intent(this, PackageInstallerService.class);
            intent.setData(getIntent().getData());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startService(intent);
        }
    }


    private TextView makeLogTextView(String text) {
        AdaptiveTextView logText = new AdaptiveTextView(this);
        logText.setTextSize(14);
        //logText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        logText.setText(text);
        return logText;
    }

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
