package com.ammar.filescenter.activities.TutorialActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ammar.filescenter.R;

public class TutorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        WebView wv = findViewById(R.id.WV_Tutorial);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(this, "AndroidNativeInterface");

        wv.loadUrl("file:///android_asset/tutorial/onboarding.html");
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // override so pressing back doesn't take you back to MainActivity.
            }
        });

    }

    @JavascriptInterface
    public void endTutorial() {
        finish();
        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }
}
