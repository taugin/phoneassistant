package com.android.phoneassistant.webserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.webserver.activity.HotpotHelper.OnHotpotStateListener;
import com.android.phoneassistant.webserver.service.WSService;
import com.chukong.sdk.GlobalInit;

public class ShareEntryActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnHotpotStateListener {

    private CheckBox mShareType;
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
        mShareType = (CheckBox) findViewById(R.id.share_type);
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
        switch(v.getId()) {
        case R.id.app_self_share:
            startShare(true);
            break;
        case R.id.all_apps_share:
            startShare(false);
            break;
        case R.id.web_settings:
            intent = new Intent(this, PreferActivity.class);
            startActivity(intent);
            break;
        case R.id.share_type:
            boolean checked = mShareType.isChecked();
            mHotpotHelper.setWifiApEnabled(checked);
            break;
        default:
            break;
        }
    }

    public void startShare(boolean selfShare) {
        Log.d(Log.TAG, "selfShare : " + selfShare);
        GlobalInit globalInit = GlobalInit.getInstance();
        globalInit.setLocalShare(selfShare);
        Intent intent = new Intent(this, WebServerDisplayActivity.class);
        intent.putExtra("hotpot", mHotpotHelper.hotpotEnabled());
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
