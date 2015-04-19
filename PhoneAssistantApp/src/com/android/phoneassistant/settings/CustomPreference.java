package com.android.phoneassistant.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.phoneassistant.manager.FontManager;

public class CustomPreference extends Preference {

    public CustomPreference(Context context) {
        super(context);
    }
    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        FontManager.get(getContext()).changeFont(view);
        return view;
    }
    
}
