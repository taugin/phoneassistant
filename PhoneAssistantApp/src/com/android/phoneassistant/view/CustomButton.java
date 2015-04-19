package com.android.phoneassistant.view;

import com.android.phoneassistant.manager.FontManager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class CustomButton extends Button {

    public CustomButton(Context context) {
        super(context);
        init(context);
    }
    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setTypeface(FontManager.get(context).getTTF());
    }
}
