package com.ammar.sharing.custom.ui;

import static com.ammar.sharing.activities.MainActivity.MainActivity.sDarkMode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;

public class RoundDialog {
    private final AlertDialog alertDialog;
    private final CardView cardView;
    private final Context context;
    private View view;
    private int maxHeight;
    private int maxWidth;
    public RoundDialog(Context context) {
        this.context = context;
        cardView = new CardView(context);
        alertDialog = new AlertDialog.Builder(context)
                .setView(cardView)
                .create();
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

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        maxWidth = displayMetrics.widthPixels - MainActivity.systemBarsPaddings.right - MainActivity.systemBarsPaddings.left;
        maxHeight = displayMetrics.heightPixels - MainActivity.systemBarsPaddings.top - MainActivity.systemBarsPaddings.bottom;


        Resources res = context.getResources();
        int color = ResourcesCompat.getColor(res, sDarkMode ? R.color.dialogColorDark : R.color.dialogColorLight, null);
        setBackgroundColor(color);
        alertDialog.show();
        cardView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            DisplayMetrics displayMetrics1 = context.getResources().getDisplayMetrics();
            maxWidth = displayMetrics1.widthPixels - MainActivity.systemBarsPaddings.right - MainActivity.systemBarsPaddings.left;
            maxHeight = displayMetrics1.heightPixels - MainActivity.systemBarsPaddings.top - MainActivity.systemBarsPaddings.bottom;
            setupDialogSize();
        });
    }

    public void dismiss() {
        alertDialog.dismiss();
    }
    public CardView getInternalRootCardView() {
        return cardView;
    }
    public AlertDialog getInternalAlertDialog() {
        return alertDialog;
    }
    private void setupDialogSize() {
        View child = cardView.getChildAt(0);
        ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
        int width = childLayoutParams.width;
        int height = childLayoutParams.height;
        if (height > maxHeight) {
            height = maxHeight;
            childLayoutParams.height = maxHeight;
            child.setLayoutParams(childLayoutParams);
        }
        if (width > maxWidth) {
            width = maxWidth;
            childLayoutParams.width = maxWidth;
            child.setLayoutParams(childLayoutParams);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.CENTER;
        cardView.setLayoutParams(layoutParams);
    }
}
