package com.ammar.sharing.activities.StreamingActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.custom.ui.DefaultActivity;

public class StreamingActivity extends DefaultActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
    }



    private Runnable touchUpListener;
    public void setTouchUpListener(Runnable l) {
        touchUpListener = l;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(touchUpListener != null && event.getAction() == MotionEvent.ACTION_UP) {
            touchUpListener.run();
            return true;
        }
        return false;
    }
}
