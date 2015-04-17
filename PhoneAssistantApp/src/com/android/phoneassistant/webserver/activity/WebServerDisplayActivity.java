package com.android.phoneassistant.webserver.activity;

import java.io.File;
import java.io.IOException;

import org.join.zxing.Contents;
import org.join.zxing.Intents;
import org.join.zxing.encode.QRCodeEncoder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.phoneassistant.R;
import com.android.phoneassistant.manager.FontManager;
import com.android.phoneassistant.webserver.listener.OnWsListener;
import com.android.phoneassistant.webserver.recevier.WSReceiver;
import com.chukong.sdk.Constants;
import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.GlobalInit;
import com.chukong.sdk.common.CmdExecutor;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.receiver.WifiApStateReceiver;
import com.chukong.sdk.redirect.RedirectSwitch;
import com.chukong.sdk.serv.WebServer;
import com.chukong.sdk.util.CommonUtil;
import com.chukong.sdk.util.CopyUtil;
import com.chukong.sdk.wifiap.WifiApManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

/**
 * @brief 主活动界面
 * @details If you want a totally web server, <a href="https://code.google.com/p/i-jetty/">i-jetty</a> may be your choice.
 * @author join
 */
@SuppressWarnings("deprecation")
public class WebServerDisplayActivity extends WebServActivity implements OnWsListener, OnClickListener {

    static final String TAG = "WSActivity";
    static final boolean DEBUG = false || Config.DEV_MODE;

    private CommonUtil mCommonUtil;

    private TextView urlText;
	private ImageView qrCodeView;

    private String ipAddr;

    private boolean needResumeServer = true;
    private ToggleButton toggleBtnRedirect;

    private static final int W_START = 0x0101;
    private static final int W_STOP = 0x0102;
    private static final int W_ERROR = 0x0103;

    private static final int DLG_SERV_USELESS = 0x0201;
    private static final int DLG_PORT_IN_USE = 0x0202;
    private static final int DLG_TEMP_NOT_FOUND = 0x0203;
    private static final int DLG_SCAN_RESULT = 0x0204;

    private String lastResult;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case W_START: {
                setUrlText(ipAddr);
                qrCodeView.setVisibility(View.VISIBLE);
                break;
            }
            case W_STOP: {
                ipAddr = mCommonUtil.getLocalIpAddress();
                if (ipAddr == null) {
                    urlText.setText(R.string.info_net_off);
                } else {
                    urlText.setText("");
                }
                qrCodeView.setImageResource(0);
                qrCodeView.setVisibility(View.GONE);
                break;
            }
            case W_ERROR:
                switch (msg.arg1) {
                case WebServer.ERR_PORT_IN_USE: {
                    showDialog(DLG_PORT_IN_USE);
                    break;
                }
                case WebServer.ERR_TEMP_NOT_FOUND: {
                    showDialog(DLG_TEMP_NOT_FOUND);
                    break;
                }
                case WebServer.ERR_UNEXPECT:
                default:
                    Log.e(TAG, "ERR_UNEXPECT");
                    break;
                }
                doStopClick();
                return;
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean hotpot = false;
        if (intent != null) {
            hotpot = intent.getBooleanExtra("hotpot", false);
            String title = intent.getStringExtra("title");
            setTitle(title);
        }
        if (hotpot) {
            setContentView(R.layout.webserver_with_hotpot);
        } else {
            setContentView(R.layout.webserver_no_hotpot);
        }

        initObjs(savedInstanceState);
        initViews(savedInstanceState);
        WSReceiver.register(this, this);

        GlobalInit globalInit = GlobalInit.getInstance();
        new GlobalThread().execute(globalInit);
        doStartClick();
    }

    private void initObjs(Bundle state) {
        mCommonUtil = CommonUtil.getSingleton();
    }

    private void initViews(Bundle state) {
        toggleBtnRedirect = (ToggleButton) findViewById(R.id.toggleBtnRedirect);
        if (toggleBtnRedirect != null) {
            boolean redirect = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.REDIRECT_STATUS, false);
            toggleBtnRedirect.setChecked(redirect);
            toggleBtnRedirect.setOnClickListener(this);
            toggleBtnRedirect.setTypeface(FontManager.get(this).getTTF());
        }
        urlText = (TextView) findViewById(R.id.urlText);
        qrCodeView = (ImageView) findViewById(R.id.qrCodeView);
        if (state != null) {
            ipAddr = state.getString("ipAddr");
            needResumeServer = state.getBoolean("needResumeServer", false);
            boolean isRunning = state.getBoolean("isRunning", false);
            if (isRunning) {
                setUrlText(ipAddr);
                doBindService();
            }
        }
    }

    private void setUrlText(String ipAddr) {
        String url = "http://" + ipAddr + ":" + Config.PORT + "/";
        urlText.setText(url);
        generateQRCode(url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ipAddr", ipAddr);
        outState.putBoolean("needResumeServer", needResumeServer);
        boolean isRunning = webService != null && webService.isRunning();
        outState.putBoolean("isRunning", isRunning);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG)
            Log.d(TAG,
                    newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "ORIENTATION_LANDSCAPE"
                            : "ORIENTATION_PORTRAIT");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiApStateReceiver.unregister(this);
        WSReceiver.unregister(this);
        doStopClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_preferences:
            toPreferActivity();
            break;
        case R.id.action_show_coninfos:
            //showConnInfoDlg();
            break;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    private void doStartClick() {
        ipAddr = mCommonUtil.getLocalIpAddress();
        if (ipAddr == null) {
            urlText.setText(R.string.info_net_off);
            //toast(getString(R.string.info_net_off));
            return;
        }
        doBindService();
    }

    private void doStopClick() {
        doUnbindService();
        ipAddr = null;
    }

    @Override
    public void onStarted() {
        Log.d(Log.TAG, "W_START");
        mHandler.sendEmptyMessage(W_START);
    }

    @Override
    public void onStopped() {
        Log.d(Log.TAG, "W_STOP");
        mHandler.sendEmptyMessage(W_STOP);
    }

    @Override
    public void onError(int code) {
        Message msg = mHandler.obtainMessage(W_ERROR);
        msg.arg1 = code;
        mHandler.sendMessage(msg);
    }
    private boolean isWebServAvailable() {
        return mCommonUtil.isNetworkAvailable() && mCommonUtil.isExternalStorageMounted();
    }

    @Override
    public void onServAvailable() {
        if (needResumeServer) {
            doStartClick();
            needResumeServer = false;
        }
    }

    @Override
    public void onServUnavailable() {
        if (webService != null && webService.isRunning()) {
            doStopClick();
            needResumeServer = true;
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // DialogFragment needs android-support.jar in API-8.
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_SERV_USELESS:
            return createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_serv_useless, R.string.msg_serv_useless, null);
        case DLG_PORT_IN_USE:
            return createConfirmDialog(android.R.drawable.ic_dialog_info, R.string.tit_port_in_use,
                    R.string.msg_port_in_use, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toPreferActivity();
                        }
                    });
        case DLG_TEMP_NOT_FOUND:
            return createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_temp_not_found, R.string.tit_temp_not_found,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toPreferActivity();
                        }
                    });
        case DLG_SCAN_RESULT:
            AlertDialog dialog = createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_scan_result, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copy2Clipboard(lastResult);
                        }
                    });
            dialog.setMessage(lastResult);
            return dialog;
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case DLG_SCAN_RESULT:
            ((AlertDialog) dialog).setMessage(lastResult);
            break;
        }
        super.onPrepareDialog(id, dialog, args);
    }

    private AlertDialog createConfirmDialog(int iconId, int titleId, int messageId,
            DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (iconId > 0)
            builder.setIcon(iconId);
        if (titleId > 0)
            builder.setTitle(titleId);
        if (messageId > 0)
            builder.setMessage(messageId);
        builder.setPositiveButton(android.R.string.ok, positiveListener);
        return builder.create();
    }

    private void toPreferActivity() {
        try {
            Intent intent = new Intent(this, PreferActivity.class);
            intent.putExtra("isRunning", webService == null ? false : webService.isRunning());
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void copy2Clipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    @SuppressLint("NewApi")
    private void setWifiApEnabled(boolean enabled) {
        if (enabled) {
            WifiConfiguration oldConfig = WifiApManager.getInstance(this).getWifiApConfiguration();
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++oldConfig.SSID = " + oldConfig.SSID + " , oldConfig.preShareKey = " + oldConfig.preSharedKey);
            editor.putString(Constants.KEY_SAVED_SSID, oldConfig.SSID);
            editor.putString(Constants.KEY_SAVED_PASS, oldConfig.preSharedKey);
            editor.putInt(Constants.KEY_SECURITY_TYPE, WifiApManager.getSecurityTypeIndex(oldConfig));
            editor.apply();
            String SSID = "Chukong-Share";
            WifiConfiguration config = WifiApManager.getInstance(this).getConfig(SSID, null, WifiApManager.OPEN_INDEX);
            Log.d(Log.TAG, "config =  " + config.SSID);
            WifiApManager.getInstance(this).setWifiApConfiguration(config);
            WifiApManager.getInstance(this).setSoftApEnabled(null, enabled);
        } else {
            String SSID = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.KEY_SAVED_SSID, null);
            String pass = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.KEY_SAVED_PASS, null);
            int securityType = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.KEY_SECURITY_TYPE, WifiApManager.OPEN_INDEX);
            WifiConfiguration config = WifiApManager.getInstance(this).getConfig(SSID, pass, securityType);
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++ssid = " + SSID + " , preSharedKey = " + pass + ", securityType = " + securityType);
            // 还原原来的SSID会导致重启
            WifiApManager.getInstance(this).setSoftApEnabled(null, enabled);
            WifiApManager.getInstance(this).setWifiApConfiguration(config);
        }
    }

    private void generateQRCode(String text) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, text);
        try {
            int dimension = getDimension();
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, intent, dimension, false);
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            qrCodeEncoder.setLogoBmp(bmp);
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                Log.d(TAG, "Could not encode barcode");
            } else {
                qrCodeView.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
        }
    }

    private int getDimension() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int dimension = width < height ? width : height;
        dimension = dimension * 3 / 4;
        return dimension;
    }

    private void missRootPermissions() {
        boolean hasRootAccess = CmdExecutor.hasRootAccess(this);
        if (hasRootAccess) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.root_tips);
            builder.setMessage(R.string.root_msg);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.root_tool_title);
            builder.setMessage(R.string.root_tool_msg);
            builder.setPositiveButton(R.string.install_apk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installRootTool();
                }
            });
            builder.setNegativeButton(R.string.quit, null);
            builder.create().show();
        }
    }

    private void installRootTool() {
        CopyUtil copyUtil = new CopyUtil(WebServerDisplayActivity.this);
        try {
            copyUtil.assetsCopy("tools", getFilesDir().getAbsolutePath(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final String filePath = getFilesDir().getAbsolutePath() + "/PermRoot.apk";
        Log.d(Log.TAG, "filePath = " + filePath);
        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                       "application/vnd.android.package-archive");
        startActivity(intent);
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(Log.TAG, "intent = " + intent.getDataString());
                File file = new File(filePath);
                file.delete();
                unregisterReceiver(this);
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.toggleBtnRedirect:
            setRedirect(toggleBtnRedirect.isChecked());
            break;
        default:
            break;
        }
    }

    private void setRedirect(boolean redirect) {
        Log.d(Log.TAG, "CommonUtil.isRooted() = " + CommonUtil.isRooted());
        boolean wifiApEnabled = WifiApManager.getInstance(this)
                .isWifiApEnabled();
        if (redirect && !wifiApEnabled) {
            Toast.makeText(this, "建议先开启WifiAp", Toast.LENGTH_SHORT).show();
            toggleBtnRedirect.setChecked(false);
            return;
        }
        boolean result = RedirectSwitch.getInstance(this).setRedirectState(
                redirect);
        boolean preState = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constants.REDIRECT_STATUS, false);
        toggleBtnRedirect.setChecked(result ? redirect : preState);
        if (!result) {
            // missRootPermissions();
            Toast.makeText(this, R.string.miss_root, Toast.LENGTH_SHORT).show();
        }
    }

    private class GlobalThread extends AsyncTask<GlobalInit, Void, Void> {
        @Override
        protected Void doInBackground(GlobalInit... params) {
            GlobalInit globalInit = params[0];
            globalInit.init();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            doStartClick();
        }
    }
}