package com.android.phoneassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.android.phoneassistant.R;
import com.android.phoneassistant.manager.FontManager;

public class CustomCheckBox extends LinearLayout {

    private CheckBox mCheckBox;
    private int mButtonDrawableId = -1;
    private int mTextId = -1;

    public CustomCheckBox(Context context) {
        super(context, null);
        init(context, null);
    }

    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomCheckBox);
            mButtonDrawableId = a.getResourceId(R.styleable.CustomCheckBox_button, -1);
            mTextId = a.getResourceId(R.styleable.CustomCheckBox_text, -1);
            // Log.d(Log.TAG, "mTextId : " + mTextId);
            a.recycle();
        }
        mCheckBox = new CheckBox(context);
        mCheckBox.setTypeface(FontManager.get(getContext()).getTTF());
        mCheckBox.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mButtonDrawableId = mButtonDrawableId != -1 ? mButtonDrawableId : R.drawable.btn_check;
        mCheckBox.setButtonDrawable(mButtonDrawableId);
        if (mTextId != -1) {
            mCheckBox.setText(mTextId);
        }
        addView(mCheckBox);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (mCheckBox != null) {
            mCheckBox.setPressed(pressed);
        }
    }

    public void setChecked(boolean checked) {
        if (mCheckBox != null) {
            mCheckBox.setChecked(checked);
        }
    }

    public boolean isChecked() {
        if (mCheckBox != null) {
            return mCheckBox.isChecked();
        }
        return false;
    }
}
