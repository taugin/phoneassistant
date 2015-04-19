package com.android.phoneassistant.settings;

import android.os.Bundle;

import com.android.phoneassistant.BaseActivity;
import com.android.phoneassistant.R;

public class PhoneAssistantSettings extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        setTitleMiddle(R.string.action_settings);
    }
}
