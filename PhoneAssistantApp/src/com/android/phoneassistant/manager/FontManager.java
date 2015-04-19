package com.android.phoneassistant.manager;

import com.android.phoneassistant.util.GlobalConfig;
import com.android.phoneassistant.util.Log;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

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
}
