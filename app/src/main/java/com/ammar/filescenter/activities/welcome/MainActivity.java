package com.ammar.filescenter.activities.welcome;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.ammar.filescenter.R; // Import R class here

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure R.layout.activity_main is resolved correctly

        webView = findViewById(R.id.webview);

        // Enable JavaScript (if your HTML requires it)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load HTML content
        webView.loadUrl("assets/welcome page/welcome.html");

        // Optional: Handle links in WebView
        webView.setWebViewClient(new WebViewClient());
    }
}
