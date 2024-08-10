package com.ammar.sharing.activities.TutorialActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.ammar.sharing.R;

public class TutorialActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        webView.setLayoutParams( new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) );
        webView.loadUrl("https://ammar64.github.io/Sharing/Tutorial");
        setContentView(webView);
    }
}