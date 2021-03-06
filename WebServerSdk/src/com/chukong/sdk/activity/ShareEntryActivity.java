package com.chukong.sdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import com.chukong.sdk.common.Log;

import com.chukong.sdk.GlobalInit;
import com.chukong.sdk.R;
import com.chukong.sdk.activity.HotpotHelper.OnHotpotStateListener;
import com.chukong.sdk.service.WSService;

public class ShareEntryActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnHotpotStateListener {

    private ToggleButton mShareType;
    private Button mAppSelfShare;
    private Button mAllAppsShare;
    private Button mServerSettings;
    private HotpotHelper mHotpotHelper;
    private boolean mBackPressed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackPressed = false;
        setContentView(R.layout.appshare_entrylayout);
        WSService.startWsService(this);
        mHotpotHelper = new HotpotHelper(this);
        mHotpotHelper.setOnHotpotStateListener(this);
        mShareType = (ToggleButton) findViewById(R.id.share_type);
        mShareType.setOnCheckedChangeListener(this);
        mShareType.setOnClickListener(this);
        mShareType.setChecked(false);

        mAppSelfShare = (Button) findViewById(R.id.app_self_share);
        mAppSelfShare.setOnClickListener(this);

        mAllAppsShare = (Button) findViewById(R.id.all_apps_share);
        mAllAppsShare.setOnClickListener(this);

        mServerSettings = (Button) findViewById(R.id.web_settings);
        mServerSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        String title = null;
        int id = v.getId();
        if (id == R.id.app_self_share ) {
            title = getResources().getString(R.string.app_self_share);
            startShare(true, title);
        } else if (id == R.id.all_apps_share) {
            title = getResources().getString(R.string.all_apps_share);
            startShare(false, title);
        } else if (id == R.id.web_settings) {
            intent = new Intent(this, PreferActivity.class);
            startActivity(intent);
        } else if (id == R.id.share_type) {
            boolean checked = mShareType.isChecked();
            mHotpotHelper.setWifiApEnabled(checked);
        }
    }

    public void startShare(boolean selfShare, String title) {
        Log.d(Log.TAG, "selfShare : " + selfShare);
        GlobalInit globalInit = GlobalInit.getInstance();
        globalInit.setLocalShare(selfShare);
        Intent intent = new Intent(this, WebServerDisplayActivity.class);
        intent.putExtra("hotpot", mHotpotHelper.hotpotEnabled());
        intent.putExtra("title", title);
        startActivity(intent);
    }

    
    @Override
    public void onBackPressed() {
        mBackPressed = false;
        if (mHotpotHelper.hotpotEnabled()) {
            mBackPressed = true;
            mHotpotHelper.setWifiApEnabled(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WSService.stopWsService(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int resId = checked ? R.string.hotpot_type : R.string.wlan_type;
        mShareType.setText(resId);
    }

    @Override
    public void onHotpotState(boolean enable) {
        if (mBackPressed) {
            finish();
        }
    }
}
