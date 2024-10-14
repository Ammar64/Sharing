package com.ammar.sharing.activities.ChangeLogActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ammar.sharing.BuildConfig;
import com.ammar.sharing.R;
import com.ammar.sharing.custom.ui.AdaptiveActivity;
import com.ammar.sharing.custom.ui.AdaptiveTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChangeLogActivity extends AdaptiveActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_log);
        Toolbar toolbar = findViewById(R.id.TB_Toolbar);

        Resources res = getResources();
        toolbar.setTitle(res.getString(R.string.changelog) + " (" + res.getString(R.string.new_in_version, BuildConfig.VERSION_NAME) + ")");

        LinearLayout changeLogLL = findViewById(R.id.LL_ChangeLogs);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TextView text = rawLineToTextView(line);
                if (text != null) {
                    changeLogLL.addView(text);
                }
            }
        } catch (IOException ignore) {
        }

        Button continueB = findViewById(R.id.B_Continue);
        continueB.setOnClickListener((v) -> {
            finish();
        });
    }

    private TextView rawLineToTextView(String line) {
        if (line.startsWith("//")) return null;
        AdaptiveTextView textView = new AdaptiveTextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (line.startsWith("#")) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            textView.setText(line.substring(1).trim());
        } else {
            textView.setText(line.trim());
        }

        return textView;
    }
}


