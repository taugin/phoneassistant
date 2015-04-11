package com.android.phoneassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class CustomCheckBox extends LinearLayout {

    private CheckBox mCheckBox;
    public CustomCheckBox(Context context) {
        super(context, null);
    }
    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    public CustomCheckBox(Context context, AttributeSet attrs, int defStyle) {
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
        mCheckBox.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        mCheckBox.setPressed(pressed);
    }

    public void setChecked(boolean checked) {
        mCheckBox.setChecked(checked);
    }

    public boolean isChecked(){
        return mCheckBox.isChecked();
    }
}
