package com.android.phoneassistant.wifi;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;

public class WifiFragment extends ListFragment implements OnCheckedChangeListener, OnItemClickListener, OnLongClickListener, OnClickListener {

    private WifiManager mWifiManager;
    private CheckBox mWifiSwitcher;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mScanList;
    private ScanResult mScanResult;
    private ApListAdapter mApListAdapter;
    private EditText mPassword;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getActivity().getSystemService(Activity.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.startScan();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mWifiStateReceiver, filter);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.wifi_actionbar);
        View view = actionBar.getCustomView();
        mWifiSwitcher = (CheckBox) view.findViewById(R.id.wifi_switcher);
        mWifiSwitcher.setChecked(mWifiManager.isWifiEnabled());
        mWifiSwitcher.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mWifiStateReceiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        mScanList = new ArrayList<ScanResult>();
        mApListAdapter = new ApListAdapter(getActivity(), mScanList);
        listView.setAdapter(mApListAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListShown(true);
        getListView().setOnItemClickListener(this);
        getListView().setOnLongClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
            }
        } else {
            if (mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(false);
            }
        }
    }

    
    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return ;
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                Log.d(Log.TAG, "wifiState = " + wifiState);
                if (wifiState == WifiManager.WIFI_STATE_ENABLING 
                        || wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    mWifiSwitcher.setEnabled(false);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED 
                        || wifiState == WifiManager.WIFI_STATE_DISABLED
                        || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
                    mWifiSwitcher.setEnabled(true);
                }
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    mWifiManager.startScan();
                }
                if (wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    mScanList.clear();
                    mApListAdapter.notifyDataSetChanged();
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> list = mWifiManager.getScanResults();
                if (list != null) {
                    Log.d(Log.TAG, "SCAN_RESULTS_AVAILABLE_ACTION list size = " + list.size());
                    mScanList.clear();
                    mScanList.addAll(list);
                    mApListAdapter.notifyDataSetChanged();
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                State state = info.getState();
                Log.d(Log.TAG, "state = " + state);
                mWifiInfo = mWifiManager.getConnectionInfo();
                if (state == NetworkInfo.State.DISCONNECTED) {
                } else if (state == NetworkInfo.State.CONNECTING) {
                    
                } else if (state == NetworkInfo.State.DISCONNECTING) {
                    
                } else if (state == NetworkInfo.State.CONNECTED) {
                    if (mWifiInfo != null) {
                        int ip = mWifiInfo.getIpAddress();
                        Log.d(Log.TAG, "ip = " + longToIP(ip));
                    }
                } else if (state == NetworkInfo.State.SUSPENDED) {
                    
                } else if (state == NetworkInfo.State.UNKNOWN) {
                    
                }
            }
        }
    };
    public static String longToIP(long longIp){
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((longIp & 0x000000FF)));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((longIp >>> 24)));
        return sb.toString();
    }
    class ViewHolder {
        TextView ssid;
        TextView extra;
        ImageView signal;
    }
    class ApListAdapter extends ArrayAdapter<ScanResult> {

        private Context mContext;
        public ApListAdapter(Context context, List<ScanResult> list) {
            super(context, 0, list);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_access_item, null);
                holder  = new ViewHolder();
                holder.ssid = (TextView) convertView.findViewById(R.id.ssid);
                holder.extra = (TextView) convertView.findViewById(R.id.extra);
                holder.signal = (ImageView) convertView.findViewById(R.id.signal_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ScanResult result = getItem(position);
            if (result != null) {
                holder.ssid.setText(result.SSID != null ? result.SSID.trim() : "");
                // holder.extra.setText(result.capabilities != null ? result.capabilities.trim() : "");
                String extra;
                if (mWifiInfo != null) {
                    String ssid = mWifiInfo.getSSID();
                    ssid = ssid.replaceAll("\"", "");
                    Log.d(Log.TAG, "ssid ================= " + ssid + " , result.SSID ============ " + result.SSID);
                    if (result.SSID.equals(ssid)) {
                        extra = "Connected";
                    } else {
                        extra = getSecury(result.capabilities);
                    }
                } else {
                    extra = getSecury(result.capabilities);
                }
                holder.extra.setText(toString(result));
                int strenghLen = WifiManager.calculateSignalLevel(result.level, 5);
                holder.signal.getDrawable().setLevel(strenghLen);
            }
            return convertView;
        }
        private String toString(ScanResult result) {
            StringBuffer sb = new StringBuffer();
            sb.append("SSID : ");
            sb.append(result.SSID);
            sb.append("\n");
            sb.append("BSSID : ");
            sb.append(result.BSSID);
            return sb.toString();
        }
    }
    private String getSecury(String capabilities) {
        StringBuilder builder = new StringBuilder();
        if (capabilities.contains("WPA") && capabilities.contains("WPA2")) {
            builder.append("WPA/WPA2 - PSK");
        } else if (capabilities.contains("WPA")) {
            builder.append("WPA - PSK");
        } else if (capabilities.contains("WPA2")) {
            builder.append("WPA2 - PSK");
        } else if (capabilities.contains("WEP")) {
            builder.append("WEP");
        }
        if (builder.length() != 0) {
            
        }
        if (capabilities.contains("WPS")) {
            builder.append(" Use WPS");
        }
        return builder.toString();
    }
    @Override
    public boolean onLongClick(View v) {
        Log.d(Log.TAG, "v = " + v);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Log.d(Log.TAG, "itemClick position = " + position);
        ScanResult result = mScanList.get(position);
        showConfigDlg(result);
        
    }
    private void showConfigDlg(ScanResult result) {
        mScanResult = result;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.wifi_config, null);
        TextView tv = (TextView) view.findViewById(R.id.ssid);
        tv.setText(result.SSID);
        mPassword = (EditText) view.findViewById(R.id.password);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        mPassword.setText("");
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        
    }
}
