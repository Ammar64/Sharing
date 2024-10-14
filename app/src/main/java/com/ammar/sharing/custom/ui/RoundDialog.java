package com.ammar.sharing.custom.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;

import com.ammar.sharing.activities.MainActivity.MainActivity;

public class RoundDialog {


    // used for round corners
    private final AlertDialog alertDialog;
    private final CardView cardView;
    private View view;
    private final int maxHeight;
    public RoundDialog(Context context) {
        cardView = new CardView(context);
        alertDialog = new AlertDialog.Builder(context)
                .setView(cardView)
                .create();
        maxHeight = context.getResources().getDisplayMetrics().heightPixels - MainActivity.systemBarsPaddings.top - MainActivity.systemBarsPaddings.bottom;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }

    public void setView(View view) {
        this.view = view;
        cardView.addView(view, 0);
    }

    public void setView(@LayoutRes int resId) {
        view = LayoutInflater.from(cardView.getContext()).inflate(resId, cardView, false);
        cardView.addView(view, 0);
    }

    public CardView getRootCardView() {
        return cardView;
    }

    public View getView() {
        return view;
    }

    public void setCornerRadius(int radius) {
        cardView.setRadius(radius);
    }

    public void setBackgroundColor(@ColorInt int color) {
        cardView.setCardBackgroundColor(color);
    }

    public void show() {
        alertDialog.show();
        View child = cardView.getChildAt(0);
        ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
        int width = childLayoutParams.width;
        int height = childLayoutParams.height;
        Log.d("HEIGHT", "Height: " + height + "     Max Height: " + maxHeight);
        if( height > maxHeight ) {
            height = maxHeight;
            childLayoutParams.height = maxHeight;
            child.setLayoutParams(childLayoutParams);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        cardView.setLayoutParams(layoutParams);
    }

    public void dismiss() {
        alertDialog.dismiss();
    }
}
