package com.ammar.filescenter.activities.welcome;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.ammar.filescenter.R; // Import R class here

public class welcomeActivity extends AppCompatActivity {

    private WebView welcomePage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_main); // Set the main layout

        // Initialize WebView from welcome_main.xml
        welcomePage = findViewById(R.id.welcomePage);

        // Enable JavaScript (if your HTML requires it)
        WebSettings webSettings = welcomePage.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load HTML content initially
        welcomePage.loadUrl("file:///android_asset/welcome page/welcome.html");

        // Optional: Handle links in WebView
        welcomePage.setWebViewClient(new WebViewClient());

        // Inflate menu_main.xml to access its elements
        View menuMainView = getLayoutInflater().inflate(R.layout.menu_main, null);

        // Find your button in menu_main.xml and set OnClickListener
        View button = menuMainView.findViewById(R.id.TV_MenuMainTutorial);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load WebView with HTML content
                welcomePage.loadUrl("file:///android_asset/welcome page/welcome.html");
            }
        });
    }
}
