package com.ammar.filescenter.activities.FastShareActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.FileUtils;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.ServerService;

import java.util.Locale;

public class FastShareActivity extends AppCompatActivity {

    FastShareServer server;
    boolean isDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isDarkMode = getSharedPreferences(Consts.PREF_SETTINGS, MODE_PRIVATE).getBoolean(Consts.PREF_FIELD_IS_DARK, true);
        if (isDarkMode) {
            setTheme(R.style.AppThemeDark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setTheme(R.style.AppTheme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_share);
        setupUIElements();

        Intent intent = getIntent();
        if( Intent.ACTION_SEND.equals(intent.getAction()) ) {
            Uri uri = intent.getData();
            if( uri == null )
                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            ((TextView) findViewById(R.id.TV_FastShareFileName)).setText(FileUtils.getFileName(getContentResolver(), uri));
            server = new FastShareServer(this, uri);
        } else finish();
    }

    private void finishActivityWithMessage(@StringRes int noDataAttached) {
        Toast.makeText(this, noDataAttached, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupUIElements() {

        // setup colors
        CardView cv = findViewById(R.id.CV_FastShareWrapperView);
        if( isDarkMode ) {
            cv.setCardBackgroundColor(0xFF454545);
        } else {
            cv.setCardBackgroundColor(0xFFDDDDDD);
        }

        // setup values
        TextView ipTV = findViewById(R.id.TV_FastShareServerLink);
        ImageView qrCodeIV = findViewById(R.id.IV_FastShareQRCodeImage);

        String ip = ServerService.getIpAddress();
        if(ip == null) {
            ipTV.setMaxLines(3);
            ipTV.setText(R.string.connect_to_wifi_or_hotspot);

            // keep testing every 2 seconds if user has enabled wifi or hotspot
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String ipToCheck = ServerService.getIpAddress();
                    if( ipToCheck == null )
                        handler.postDelayed(this, 2000);
                    else
                        setupQrCode(ipTV, qrCodeIV, ipToCheck);
                }
            }, 2000);
        } else {
            setupQrCode(ipTV, qrCodeIV, ip);
        }

    }

    private void setupQrCode(TextView ipTV, ImageView qrCodeIV, String ip) {
        ipTV.setMaxLines(1);

        String link = String.format(Locale.ENGLISH, "http://%s:3000", ip);
        ipTV.setText(link);
        byte[] qrCodeBytes = Utils.encodeTextToQR(link);
        Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes, isDarkMode);
        qrCodeIV.setImageBitmap(qrCodeBitmap);
    }

    @Override
    protected void onDestroy() {
        if( server != null ) server.stopServing();
        super.onDestroy();
    }
}
