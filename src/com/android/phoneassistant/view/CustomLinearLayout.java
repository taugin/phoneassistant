package com.android.phoneassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class CustomLinearLayout extends LinearLayout {

    private CheckBox mCheckBox;
    public CustomLinearLayout(Context context) {
        super(context, null);
    }
    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheckBox = (CheckBox) findViewWithTag("CustomLinearLayoutTag");
    }
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            mCheckBox.setChecked(!mCheckBox.isChecked());
        }
    }
}
