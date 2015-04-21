package com.android.phoneassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.phoneassistant.util.Constant;

public abstract class BaseActivity extends Activity {

    private FrameLayout mActivityContent;
    private LayoutInflater mInflater;
    protected TextView mTitleLeft;
    protected TextView mTitleMiddle;
    protected TextView mTitleRight;
    protected ImageView mTitleIconRight;
    
    private View mTitleLayout1 = null;
    private View mTitleLayout2 = null;
    private View mTitleLayout3 = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.common_activity_layout);
        mInflater = LayoutInflater.from(this);
        mActivityContent = (FrameLayout) findViewById(R.id.activity_content);
        mTitleLeft = (TextView) findViewById(R.id.title_text1);
        mTitleMiddle = (TextView) findViewById(R.id.title_text2);
        mTitleRight = (TextView) findViewById(R.id.title_text3);
        mTitleIconRight = (ImageView) findViewById(R.id.title_icon3);

        mTitleLayout1 = findViewById(R.id.title_layout1);
        mTitleLayout1.setOnClickListener(mOnClickListener);
        mTitleLayout2 = findViewById(R.id.title_layout2);
        mTitleLayout2.setOnClickListener(mOnClickListener);
        mTitleLayout3 = findViewById(R.id.title_layout3);
        mTitleLayout3.setOnClickListener(mOnClickListener);
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            setTitleMiddle(title);
        }

        String preTitle = null;
        Intent intent = getIntent();
        if (intent != null) {
            preTitle = intent.getStringExtra(Constant.EXTRA_PRE_TITLE);
        }
        if (!TextUtils.isEmpty(preTitle)) {
            setTitleLeft(preTitle);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = mInflater.inflate(layoutResID, null);
        mActivityContent.addView(view);
    }

    @Override
    public void setContentView(View view) {
        mActivityContent.addView(view);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        mActivityContent.addView(view, params);
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(Constant.EXTRA_PRE_TITLE, getTitleMiddle());
        super.startActivity(intent);
    }

    @SuppressLint("NewApi")
    @Override
    public void startActivity(Intent intent, Bundle options) {
        intent.putExtra(Constant.EXTRA_PRE_TITLE, getTitleMiddle());
        super.startActivity(intent, options);
    }

    protected void setTitleLeft(CharSequence text) {
        mTitleLeft.setText(text);
        mTitleLeft.setVisibility(View.VISIBLE);
    }

    protected void setTitleMiddle(CharSequence text) {
        mTitleMiddle.setText(text);
        mTitleMiddle.setVisibility(View.VISIBLE);
    }

    protected void setTitleRight(CharSequence text) {
        mTitleRight.setText(text);
        mTitleRight.setVisibility(View.VISIBLE);
    }

    protected void setTitleLeft(int resId) {
        mTitleLeft.setText(resId);
        mTitleLeft.setVisibility(View.VISIBLE);
    }

    protected void setTitleMiddle(int resId) {
        mTitleMiddle.setText(resId);
        mTitleMiddle.setVisibility(View.VISIBLE);
    }

    protected void setTitleRight(int resId) {
        mTitleRight.setText(resId);
        mTitleRight.setVisibility(View.VISIBLE);
    }

    protected void setTitleIconRight(Drawable drawable) {
        mTitleIconRight.setImageDrawable(drawable);
        mTitleIconRight.setVisibility(View.VISIBLE);
    }

    protected void setTitleIconRight(int resId) {
        mTitleIconRight.setImageResource(resId);
        mTitleIconRight.setVisibility(View.VISIBLE);
    }

    protected CharSequence getTitleLeft() {
        return mTitleLeft.getText().toString();
    }

    protected CharSequence getTitleMiddle() {
        return mTitleMiddle.getText().toString();
    }

    protected CharSequence getTitleRight() {
        return mTitleRight.getText().toString();
    }

    OnClickListener mOnClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
            case R.id.title_layout1:
                onTitleLeftClick();
                break;
            case R.id.title_layout2:
                onTitleMiddleClick();
                break;
            case R.id.title_layout3:
                onTitleRightClick();
                break;
            }
        }
    };

    protected void onTitleLeftClick() {
        finish();
    }

    protected void onTitleMiddleClick() {
    }

    protected void onTitleRightClick() {
    }
}
