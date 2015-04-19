package com.android.phoneassistant.view;

import com.android.phoneassistant.manager.FontManager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context, null);
        init(context);
    }
    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }
    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return ;
        }
        setTypeface(FontManager.get(context).getTTF());
    }
}
