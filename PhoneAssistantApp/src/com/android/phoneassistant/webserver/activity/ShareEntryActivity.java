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
import com.android.phoneassistant.webserver.service.WSService;
import com.chukong.sdk.GlobalInit;

public class ShareEntryActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

    private CheckBox mShareType;
    private Button mAppSelfShare;
    private Button mAllAppsShare;
    private Button mServerSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appshare_entrylayout);
        WSService.startWsService(this);
        mShareType = (CheckBox) findViewById(R.id.share_type);
        mShareType.setOnCheckedChangeListener(this);
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
        default:
            break;
        }
    }

    public void startShare(boolean selfShare) {
        Log.d(Log.TAG, "selfShare : " + selfShare);
        boolean checked = mShareType.isChecked();
        GlobalInit globalInit = GlobalInit.getInstance();
        globalInit.setLocalShare(selfShare);
        Intent intent = new Intent(this, checked ? WebServerWithHotpot.class : WebServerNoHotpot.class);
        startActivity(intent);
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
}
