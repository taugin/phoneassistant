package com.android.phoneassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.android.phoneassistant.manager.FontManager;

public class CustomToggleButton extends ToggleButton {
    public CustomToggleButton(Context context) {
        super(context);
        init(context);
    }
    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setTypeface(FontManager.get(context).getTTF());
    }
}
