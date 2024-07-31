package com.ammar.filescenter.custom.ui;

import static com.ammar.filescenter.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentContainerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Utils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class AdaptiveDropDown extends PopupWindow {
    private static final LinkedList<WeakReference<AdaptiveDropDown>> menus = new LinkedList<>();

    public static void setDarkAll(boolean dark) {
        for (WeakReference<AdaptiveDropDown> i : menus) {
            AdaptiveDropDown dropDown = i.get();
            if (dropDown != null) {
                dropDown.setBackgroundDrawable(new ColorDrawable(dropDown.context.getResources().getColor(dark ? R.color.popupDarkerBG : R.color.popupLightBG)));
            }
        }
    }


    private Context context;
    public LinearLayout layout;

    public AdaptiveDropDown(Context act) {
        this.context = act;
        // default width/height
        setWidth((int) Utils.dpToPx(170));
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams((int) Utils.dpToPx(170), ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(darkMode ? R.color.popupDarkerBG : R.color.popupLightBG)));
        setElevation(24);
        setContentView(layout);

        AdaptiveDropDown.menus.addLast(new WeakReference<>(this));
    }

    public void setAnchorView(View anchor) {
        anchor.setOnClickListener(this::showAsDropDown);
    }

    public View addItem(@StringRes int stringRes) {
        return addItem(context.getResources().getString(stringRes));
    }

    public View addItem(String text) {
        AdaptiveTextView view = buildItem(text);
        layout.addView(view);
        return view;
    }

    public View addItem(@StringRes int stringRes, @DrawableRes int iconRes) {
        return addItem(context.getResources().getString(stringRes), ResourcesCompat.getDrawable(context.getResources(), iconRes, null));
    }
    public View addItem(@StringRes int stringRes, Drawable icon) {
        return addItem(context.getResources().getString(stringRes), icon);
    }

    public View addItem(String text, Drawable icon) {
        AdaptiveTextView view = buildItem(text);
        view.setCompoundDrawablesRelative(icon, null, null, null);
        view.setDark(darkMode);
        layout.addView(view);
        return view;
    }


    private AdaptiveTextView buildItem(String text) {
        AdaptiveTextView textView = new AdaptiveTextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setBackgroundResource(R.drawable.ripple_item);

        int paddingH = (int) Utils.dpToPx(10);
        int paddingV = (int) Utils.dpToPx(8);
        textView.setPadding(paddingH, paddingV, paddingH, paddingV);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setText(text);
        textView.setCompoundDrawablePadding((int) Utils.dpToPx(12)); // in case we wanted padding
        textView.setModifyDrawableColor(true);
        return textView;
    }
}
