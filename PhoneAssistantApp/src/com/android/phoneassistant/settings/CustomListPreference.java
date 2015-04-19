package com.android.phoneassistant.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.phoneassistant.manager.FontManager;

public class CustomListPreference extends ListPreference {

    public CustomListPreference(Context context) {
        super(context);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        FontManager.get(getContext()).changeFont(view);
        return view;
    }
}
