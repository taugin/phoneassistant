package com.android.phoneassistant.manager;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.phoneassistant.util.GlobalConfig;
import com.android.phoneassistant.util.Log;

public class FontManager {

    private static FontManager sFontManager = null;

    private Context mContext;
    private Typeface mTypeface;
    private String mFontPath = null;

    private FontManager(Context context) {
        mContext = context;
        initFontPath();
        mTypeface = createCustomFont();
    }

    private void initFontPath() {
        mFontPath = GlobalConfig.get().fontpath;
    }

    private Typeface createCustomFont() {
        Typeface typeface = null;
        if (!TextUtils.isEmpty(mFontPath)) {
            try {
                typeface = Typeface.createFromFile(mFontPath);
            } catch (Exception e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(mContext.getAssets(),
                        "fonts/kaiti.ttf");
            } catch (Exception e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
        return typeface;
    }

    public static FontManager get(Context context) {
        if (sFontManager == null) {
            sFontManager = new FontManager(context);
        }
        return sFontManager;
    }

    public Typeface getTTF() {
        if (mTypeface == null) {
            mTypeface = createCustomFont();
        }
        mTypeface = mTypeface != null ? mTypeface : Typeface.DEFAULT;
        return mTypeface;
    }

    public void changeFont(View view) {
        if (view == null) {
            return ;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            int count = viewGroup.getChildCount();
            View v = null;
            for (int index = 0; index < count; index++) {
                v = viewGroup.getChildAt(index);
                if (v instanceof TextView) {
                    ((TextView)v).setTypeface(mTypeface);
                } else {
                    changeFont(v);
                }
            }
        } else if (view instanceof TextView) {
            ((TextView)view).setTypeface(mTypeface);
        }
    }
}
