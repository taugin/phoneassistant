package com.chukong.sdk;

import java.io.IOException;

import org.join.zxing.Contents;
import org.join.zxing.Intents;
import org.join.zxing.encode.QRCodeEncoder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.receiver.OnWifiApStateChangeListener;
import com.chukong.sdk.receiver.WifiApStateReceiver;
import com.chukong.sdk.serv.WebServer.OnWebServListener;
import com.chukong.sdk.service.WebService;
import com.chukong.sdk.util.CommonUtil;
import com.chukong.sdk.wifiap.WifiApManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class ShareDialog extends Dialog implements OnWifiApStateChangeListener,
        OnWebServListener, OnDismissListener {

    private static final int W_START = 0x0101;
    private static final int W_STOP = 0x0102;
    private static final int W_ERROR = 0x0103;
    private static final int THEME_LIGHT_FULLSCREEN = android.R.style.Theme_Light_NoTitleBar_Fullscreen;
    private static final int THEME_LIGHT = android.R.style.Theme_Light_NoTitleBar;

    private static String tips = "请好友连接Chukong-Share热点\n并输入一下地址或扫描二维码\nhttp://192.168.43.1:7766";

    protected Intent webServIntent;
    protected WebService webService;
    private boolean isBound = false;
    private String ipAddr = null;
    private CommonUtil mCommonUtil;
    private Bitmap mLogoBmp;

    private ImageView mQRImage = null;

    public ShareDialog(Context context) {
        super(context, THEME_LIGHT);
        setCanceledOnTouchOutside(false);
        setOnDismissListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        GlobalInit globalInit = GlobalInit.getInstance();
        new GlobalThread().execute(globalInit);
        webServIntent = new Intent(getContext(), WebService.class);
        mCommonUtil = CommonUtil.getSingleton();

        setContentView(initView2());
    }

    private View initView() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView textview = new TextView(getContext());
        textview.setGravity(Gravity.CENTER_HORIZONTAL);
        textview.setText(tips);
        layout.addView(textview);
        mQRImage = new ImageView(getContext());
        layout.addView(mQRImage);
        return layout;
    }

    private View initView2() {
        AssetManager assetManager = getContext().getAssets();
        AssetFileDescriptor afd = null;
        Bitmap wifiBmp = null;
        Bitmap nextBmp = null;
        Drawable wifiDrawable = null;
        try {
            afd = assetManager.openFd("next_down.png");
            Log.d(Log.TAG, "afd2 = " + afd);
            if (afd != null) {
                nextBmp = BitmapFactory.decodeStream(afd.createInputStream());
            }
            Log.d(Log.TAG, "afd3 = " + afd);
            if (afd != null) {
                afd = assetManager.openFd("wifi.png");
                wifiBmp = BitmapFactory.decodeStream(afd.createInputStream());
                if (wifiBmp != null) {
                    wifiDrawable = new BitmapDrawable(wifiBmp);
                }
            }
        } catch (IOException e) {
            Log.e(Log.TAG, e.getMessage());
        }
        Log.d(Log.TAG, "wifiBmp = " + wifiBmp + " , nextBmp = " + nextBmp
                + " , guideDrawable = " + wifiDrawable);
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        scrollView.addView(layout);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = dip2px(getContext(), 48);// LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                width, height);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView stepOne = new TextView(getContext());
        stepOne.setTextSize(20.0f);
        stepOne.setText("第一步");
        layout.addView(stepOne, params2);
        TextView stepOneTips = new TextView(getContext());
        stepOneTips.setText("请好友打开手机Wifi，连接到无线网络");
        layout.addView(stepOneTips, params2);

        TextView SSID = new TextView(getContext());
        SSID.setGravity(Gravity.CENTER_VERTICAL);
        SSID.setTextSize(15.0f);
        SSID.setText("Chukong-Share");
        if (wifiDrawable != null) {
            wifiDrawable.setBounds(new Rect(0, 0, 48, 48));
            SSID.setCompoundDrawables(null, null, wifiDrawable, null);
            SSID.setCompoundDrawablePadding(50);
        }
        layout.addView(SSID, params1);
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setBounds(new Rect(0, 0, SSID.getWidth(), SSID
                .getHeight()));
        shapeDrawable.getPaint().setColor(0xFF11C2EE);
        shapeDrawable.getPaint().setStrokeWidth(5);
        shapeDrawable.getPaint().setStyle(Style.STROKE);
        SSID.setBackgroundDrawable(shapeDrawable);

        ImageView imageview = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(48, 48);
        if (nextBmp != null) {
            imageview.setImageBitmap(nextBmp);
        }
        layout.addView(imageview, params);

        TextView stepTwo = new TextView(getContext());
        stepTwo.setTextSize(20.0f);
        stepTwo.setText("第二步");
        layout.addView(stepTwo, params2);
        TextView stepTwoTips = new TextView(getContext());
        stepTwoTips.setText("打开浏览器输入网址 或 扫描二维码");
        layout.addView(stepTwoTips, params2);

        TextView webSite = new TextView(getContext());
        webSite.setGravity(Gravity.CENTER_VERTICAL);
        webSite.setPadding(5, 0, 5, 0);
        webSite.setText("http://192.168.43.1:7766");
        layout.addView(webSite, params1);
        shapeDrawable = new ShapeDrawable();
        shapeDrawable.setBounds(new Rect(0, 0, SSID.getWidth(), SSID
                .getHeight()));
        shapeDrawable.getPaint().setColor(0xFF11C2EE);
        shapeDrawable.getPaint().setStrokeWidth(5);
        shapeDrawable.getPaint().setStyle(Style.STROKE);
        webSite.setBackgroundDrawable(shapeDrawable);

        mQRImage = new ImageView(getContext());
        layout.addView(mQRImage);
        return scrollView;
    }
    @Override
    protected void onStart() {
        super.onStart();
        WifiApStateReceiver.register(getContext(), this);
        // setWifiApEnabled(true);
    }

    @Override
    protected void onStop() {
        doUnbindService();
        setWifiApEnabled(false);
        WifiApStateReceiver.unregister(getContext());
        super.onStop();
    }

    @Override
    public void onWifiApStateChanged(int state) {
        if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLING || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLING) {
        } else if(state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
        }
        if (state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getWifiApConfiguration();
            if (config != null) {
                //wifiApText.setText("ssid : " + config.SSID + "\n" + config.preSharedKey);
                doBindService();
            }
        } else if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED) {
            //wifiApText.setText("");
            doUnbindService();
        }
    }
    
    @SuppressLint("NewApi")
    private void setWifiApEnabled(boolean enabled) {
        boolean apEnable = WifiApManager.getInstance(getContext()).isWifiApEnabled();
        if (apEnable == enabled) {
            return ;
        }
        if (enabled) {
            WifiConfiguration oldConfig = WifiApManager.getInstance(getContext()).getWifiApConfiguration();
            Log.d(Log.TAG, "----------------------oldConfig = " + oldConfig);
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++oldConfig.SSID = " + oldConfig.SSID + " , oldConfig.preShareKey = " + oldConfig.preSharedKey);
            editor.putString(Constants.KEY_SAVED_SSID, oldConfig.SSID);
            editor.putString(Constants.KEY_SAVED_PASS, oldConfig.preSharedKey);
            editor.putInt(Constants.KEY_SECURITY_TYPE, WifiApManager.getSecurityTypeIndex(oldConfig));
            editor.apply();
            String SSID = "Chukong-Share";
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getConfig(SSID, null, WifiApManager.OPEN_INDEX);
            Log.d(Log.TAG, "config =  " + config.SSID);
            WifiApManager.getInstance(getContext()).setWifiApConfiguration(config);
            WifiApManager.getInstance(getContext()).setSoftApEnabled(null, enabled);
        } else {
            String SSID = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.KEY_SAVED_SSID, null);
            String pass = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.KEY_SAVED_PASS, null);
            int securityType = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Constants.KEY_SECURITY_TYPE, WifiApManager.OPEN_INDEX);
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getConfig(SSID, pass, securityType);
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++ssid = " + SSID + " , preSharedKey = " + pass + ", securityType = " + securityType);
            // 还原原来的SSID会导致重启
            WifiApManager.getInstance(getContext()).setSoftApEnabled(null, enabled);
            WifiApManager.getInstance(getContext()).setWifiApConfiguration(config);
        }
    }
    
    
    private ServiceConnection servConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Log.TAG, "");
            webService = ((WebService.LocalBinder) service).getService();
            webService.setOnWebServListener(ShareDialog.this);
            webService.openServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Log.TAG, "");
            webService = null;
        }
    };
    protected void doBindService() {
        // Restore configs of port and root here.
        //PreferActivity.restore(PreferActivity.KEY_SERV_PORT, PreferActivity.KEY_SERV_ROOT);
        boolean ret = getContext().bindService(webServIntent, servConnection,
                Context.BIND_AUTO_CREATE);
        Log.d(Log.TAG, "ret = " + ret);
        isBound = true;
    }

    protected void doUnbindService() {
        Log.d(Log.TAG, "isBound = " + isBound);
        if (isBound) {
            getContext().unbindService(servConnection);
            isBound = false;
        }
    }

    @Override
    public void onStarted() {
        mHandler.sendEmptyMessage(W_START);
    }

    @Override
    public void onStopped() {
        mHandler.sendEmptyMessage(W_STOP);
    }

    @Override
    public void onError(int code) {
        
    }
    
    private void setUrlText(String ipAddr) {
        ipAddr = mCommonUtil.getLocalIpAddress();
        String url = "http://" + ipAddr + ":" + Config.PORT + "/";
        setTitle(url);
        generateQRCode(url);
    }
    private void generateQRCode(String text) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, text);
        try {
            int dimension = getDimension();
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(getContext(), intent, dimension, false);
            qrCodeEncoder.setLogoBmp(mLogoBmp);
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                Log.d(Log.TAG, "Could not encode barcode");
            } else {
                mQRImage.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
        }
    }

    private int getDimension() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int dimension = width < height ? width : height;
        dimension = dimension * 3 / 4;
        return dimension;
    }
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case W_START: {
                setUrlText(ipAddr);
                mQRImage.setVisibility(View.VISIBLE);
                break;
            }
            case W_STOP: {
                //urlText.setText("");
                mQRImage.setImageResource(0);
                mQRImage.setVisibility(View.GONE);
                break;
            }
            case W_ERROR:
                doUnbindService();
                return;
            }
        }

    };
    public void setLogoBmp(Bitmap bmp) {
        mLogoBmp = bmp;
    }

    private class GlobalThread extends AsyncTask<GlobalInit, Void, Void> {
        @Override
        protected Void doInBackground(GlobalInit... params) {
            GlobalInit globalInit = params[0];
            globalInit.init();
            globalInit.setLocalShare(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setWifiApEnabled(true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(Log.TAG, "dialog = " + dialog);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
