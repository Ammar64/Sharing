package com.ammar.sharing.activities.MessagesActivity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MessagesActivity.adaptersR.MessageAdapter.MessagesAdapter;
import com.ammar.sharing.custom.ui.AdaptiveActivity;

public class MessagesActivity extends AdaptiveActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        initItems();
    }

    private void initItems() {
        Toolbar toolbar = findViewById(R.id.TB_Toolbar);
        toolbar.setTitle(R.string.message_title);

        RecyclerView recyclerView = findViewById(R.id.RV_MessagesRecyclerView);
        recyclerView.setAdapter(new MessagesAdapter());
    }
}
