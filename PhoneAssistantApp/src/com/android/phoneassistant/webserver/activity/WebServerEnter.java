package com.android.phoneassistant.webserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.phoneassistant.R;
import com.android.phoneassistant.webserver.service.WSService;
import com.chukong.sdk.GlobalInit;

public class WebServerEnter extends Activity implements OnClickListener {

    private static final String LOCALSHARE_ACTIVITY = "com.android.phoneassistant.activity.ShareActivity";
    private Button mWebServerNoHotpot;
    private Button mWebServerWithHotpot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webserver_enter);
        WSService.startWsService(this);
        mWebServerNoHotpot = (Button) findViewById(R.id.webserver_no_hotpot);
        mWebServerNoHotpot.setOnClickListener(this);
        mWebServerWithHotpot = (Button) findViewById(R.id.webserver_with_hotpot);
        mWebServerWithHotpot.setOnClickListener(this);

        boolean localshare = LOCALSHARE_ACTIVITY.equals(getComponentName()
                .getClassName());
        GlobalInit globalInit = GlobalInit.getInstance();
        globalInit.setLocalShare(localshare);
    }
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
        case R.id.webserver_no_hotpot:
            intent = new Intent(this, WebServerNoHotpot.class);
            startActivity(intent);
            break;
        case R.id.webserver_with_hotpot:
            intent = new Intent(this, WebServerWithHotpot.class);
            startActivity(intent);
            break;
        default:
            break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WSService.stopWsService(this);
    }
}
