package com.ammar.filescenter.custom.ui;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ammar.filescenter.R;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class TrackedTextView extends androidx.appcompat.widget.AppCompatTextView {

    public static final LinkedList<WeakReference<TrackedTextView>> textViews = new LinkedList<>();

    private void addTextView() {
        TrackedTextView.textViews.addLast(new WeakReference<>(this));
        int textColor = getResources().getColor(darkMode ? R.color.text_color_light : R.color.text_color_dark);
        setTextColor( textColor );
    }

    public TrackedTextView(@NonNull Context context) {
        super(context);
        addTextView();

    }

    public TrackedTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addTextView();

    }

    public TrackedTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addTextView();
    }

}