package com.ammar.filescenter.activities.TutorialActivity;

import android.os.Bundle;
import android.telecom.Call;
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
        WebView wv = findViewById(R.id.WV_Tutorial);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(this, "AndroidNativeInterface");
        wv.loadUrl("file:///android_asset/onboarding.html");
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
        overridePendingTransition(R.anim.fragment_enter_left, R.anim.fragment_exit_left);
    }
}
