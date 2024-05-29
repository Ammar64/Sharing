package com.ammar.filescenter.activities.SendActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.ammar.filescenter.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SendActivity extends AppCompatActivity {

    private FloatingActionButton serverButton;
    boolean server_on = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        initItems();
        setItemsListener();
    }

    private void initItems() {
        serverButton = findViewById(R.id.FAB_ServerButton);
    }

    private void setItemsListener() {
        serverButton.setOnClickListener(( button ) -> {
            if(server_on) {
                server_on = false;
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_off)));
            } else {
                server_on = true;
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_on)));
            }
        });
    }
}