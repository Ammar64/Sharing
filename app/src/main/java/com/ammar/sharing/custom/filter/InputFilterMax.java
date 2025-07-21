package com.ammar.sharing.custom.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMax implements InputFilter {

    private int max;

    public InputFilterMax(int max) {
        this.max = max;
    }

    public InputFilterMax(String max) {
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String startString = dest.toString().substring(0, dstart);
            String insert = source.toString();
            String endString = dest.toString().substring(dend);
            String parseThis = startString + insert + endString;
            int input = Integer.parseInt(parseThis);
            if (input <= max)
                return null;
        } catch (NumberFormatException ignore) {}
        return "";
    }
}
