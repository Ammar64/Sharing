package com.ammar.sharing.custom.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.filter.InputFilterMax;
import com.ammar.sharing.custom.lambda.LambdaNoReturn;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class NumberDialog {

    private final Context context;
    private String title;
    private String hint;
    private Integer max = null;
    private Integer min = null;
    private LambdaNoReturn<Integer> onResult;

    private RoundDialog roundNumberDialog;
    private Integer defaultValue = null;
    private TextInputEditText setNumberTEIT;
    private TextView errorTV;
    private TextInputLayout setNumberTIL;


    private int _default_box_stroke_color;
    public NumberDialog(Context context) {
        this.context = context;
        this.title = context.getResources().getString(R.string.set_number); // default title
        this.hint = ""; // default is no hint;
        this.onResult = (i) -> {};
        roundNumberDialog = null;
    }

    public NumberDialog setTitle(@StringRes int title) {
        setTitle(context.getResources().getString(title));
        return this;
    }
    public NumberDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public NumberDialog setHint(@StringRes int hint) {
        setHint(context.getResources().getString(hint));
        return this;
    }
    public NumberDialog setHint(String hint) {
        this.hint = hint;
        return this;
    }


    public NumberDialog setMinValue(Integer min) {
        this.min = min;
        return this;
    }
    public NumberDialog setMaxValue(Integer max) {
        this.max = max;
        return this;
    }

    public NumberDialog setDefaultValue(Integer defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public NumberDialog setOnResult(LambdaNoReturn<Integer> onResult) {
        this.onResult = onResult;
        return this;
    }

    public NumberDialog create() {
        roundNumberDialog = new RoundDialog(context);
        roundNumberDialog.setView(R.layout.dialog_set_number);
        roundNumberDialog.setCornerRadius((int)Utils.dpToPx(18));
        View v = roundNumberDialog.getView();

        TextView titleTV = v.findViewById(R.id.TV_TitleDialogSetNumber);
        setNumberTIL = v.findViewById(R.id.TIL_SetNumber);
        setNumberTEIT = v.findViewById(R.id.TIET_SetNumber);
        errorTV = v.findViewById(R.id.TV_InputError);
        Button okButton = v.findViewById(R.id.B_DialogSetNumberOk);

        _default_box_stroke_color = setNumberTIL.getBoxStrokeColor();

        titleTV.setText(title);
        setNumberTEIT.setText(defaultValue == null ? "" : String.valueOf(defaultValue));
        setNumberTEIT.setHint(hint);

        if( max != null )
            setNumberTEIT.setFilters(new InputFilter[]{new InputFilterMax(this.max)});
        setNumberTEIT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearError();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        okButton.setOnClickListener((buttonView) -> {
            Editable resultE = setNumberTEIT.getText();
            if( resultE != null && resultE.length() != 0) {
                Integer resultI = Integer.parseInt(resultE.toString());
                if( this.min != null && resultI < this.min ) {
                    showError(context.getResources().getString(R.string.min_value_error, this.min));
                    return;
                }
                if( this.max != null && resultI > this.max ) {
                    showError(context.getResources().getString(R.string.max_value_error, this.max));
                    return;
                }
                onResult.apply(resultI);
            }
            roundNumberDialog.dismiss();
        });
        return this;
    }

    public void show() {
        setNumberTEIT.setText(defaultValue == null ? "" : String.valueOf(defaultValue));
        if( roundNumberDialog == null ) return;
        roundNumberDialog.show();
    }



    private void showError(String e) {
        setNumberTIL.setBoxStrokeColor(context.getResources().getColor(R.color.red));
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(e);
    }
    private void clearError() {
        setNumberTIL.setBoxStrokeColor(_default_box_stroke_color);
        errorTV.setVisibility(View.GONE);
        errorTV.setText("");
    }
}
