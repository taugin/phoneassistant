package com.android.phoneassistant.manager;

import android.content.Context;
import android.graphics.Typeface;

public class FontManager {

    private static FontManager sFontManager = null;

    private Context mContext;
    private Typeface mTypeface;

    private FontManager(Context context) {
        mContext = context;
        mTypeface = Typeface.createFromAsset(context.getAssets(),
                "fonts/kaiti.ttf");
    }

    public static FontManager get(Context context) {
        if (sFontManager == null) {
            sFontManager = new FontManager(context);
        }
        return sFontManager;
    }

    public Typeface getTTF() {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(mContext.getAssets(),
                    "fonts/kaiti.ttf");
        }
        mTypeface = mTypeface != null ? mTypeface : Typeface.DEFAULT;
        return mTypeface;
    }
}
