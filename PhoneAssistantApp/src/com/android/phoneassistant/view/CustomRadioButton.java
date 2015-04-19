package com.android.phoneassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.android.phoneassistant.manager.FontManager;

public class CustomRadioButton extends RadioButton {

    public CustomRadioButton(Context context) {
        super(context);
        init(context);
    }
    public CustomRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setTypeface(FontManager.get(context).getTTF());
    }
}
