package com.ammar.filescenter.custom.ui;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;

public class CheckIndicator extends View implements Checkable {

    public CheckIndicator(Context context) {
        super(context);
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
