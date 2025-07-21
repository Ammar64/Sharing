package com.ammar.sharing.activities.StreamingActivity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.StreamingActivity.fragments.NavFragment;
import com.ammar.sharing.custom.ui.DefaultActivity;

public class StreamingActivity extends DefaultActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        setupActivity();
    }

    private void setupActivity() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.FCV_StreamingActivityFCV ,NavFragment.class, null)
                .setReorderingAllowed(true)
                .commit();
    }
}
