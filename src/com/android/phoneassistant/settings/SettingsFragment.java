package com.android.phoneassistant.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.android.phoneassistant.App;
import com.android.phoneassistant.R;
import com.android.phoneassistant.upgrade.UpgradeManager;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class SettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        findPreference(Constant.KEY_WARNING_TONE).setOnPreferenceChangeListener(this);
        findPreference(Constant.KEY_FLIP_MUTE).setOnPreferenceChangeListener(this);
        findPreference(Constant.KEY_BLOCK_ALL).setOnPreferenceChangeListener(this);
        getPreferenceScreen().removePreference(findPreference(Constant.KEY_BLOCK_ALL));
        findPreference(Constant.KEY_RECORD_CONTENT).setOnPreferenceChangeListener(this);
        findPreference(Constant.KEY_CHECK_LOG).setOnPreferenceClickListener(
                this);
        findPreference(Constant.KEY_CHECK_UPGRADE).setOnPreferenceClickListener(
                this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ListPreference preference = (ListPreference) findPreference(Constant.KEY_WARNING_TONE);
        int index = preference.findIndexOfValue(preference.getValue());
        if (index != -1) {
            preference.setSummary(preference.getEntries()[index]);
        }
        
        preference = (ListPreference) findPreference(Constant.KEY_RECORD_CONTENT);
        index = preference.findIndexOfValue(preference.getValue());
        if (index != -1) {
            preference.setSummary(preference.getEntries()[index]);
        }
        String versionLabel = getResources().getString(R.string.version);
        findPreference(Constant.KEY_CHECK_UPGRADE).setSummary(
                versionLabel + getAppVerName());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(Constant.KEY_WARNING_TONE)) {
            String value = (String) newValue;
            Log.d(Log.TAG, "value = " + value);
            ListPreference list = (ListPreference) preference;
            list.setValue(value);
            int index = list.findIndexOfValue(value);
            if (index != -1) {
                preference.setSummary(list.getEntries()[index]);
            }
            setCallForward(value);
            Log.getLog(getActivity()).recordOperation("Set ringtone tip to " + list.getEntries()[index]);
            return true;
        } else if (preference.getKey().equals(Constant.KEY_FLIP_MUTE)) {
            Boolean value = (Boolean) newValue;
            String operation = value ? "Open flip mute" : "Close flip mute";
            Log.getLog(getActivity()).recordOperation(operation);
            return true;
        } else if (preference.getKey().equals(Constant.KEY_BLOCK_ALL)) {
            Boolean value = (Boolean) newValue;
            String operation = value ? "Open block all calls" : "Close block all calls";
            Log.getLog(getActivity()).recordOperation(operation);
            return true;
        } else if (preference.getKey().equals(Constant.KEY_RECORD_CONTENT)) {
            String value = (String) newValue;
            Log.d(Log.TAG, "value = " + value);
            ListPreference list = (ListPreference) preference;
            list.setValue(value);
            int index = list.findIndexOfValue(value);
            if (index != -1) {
                preference.setSummary(list.getEntries()[index]);
            }
            setCallForward(value);
            Log.getLog(getActivity()).recordOperation("Set record content to " + list.getEntries()[index]);
            return true;
        }

        return false;
    }

    private void setCallForward(String value) {
        String forwordNumber = null;
        if ("empty".equals(value)) {
            forwordNumber = Constant.ENABLE_SERVICE;
        } else if ("stop".equals(value)) {
            forwordNumber = Constant.ENABLE_STOP_SERVICE;
        } else if ("shutdown".equals(value)) {
            forwordNumber = Constant.ENABLE_POWEROFF_SERVICE;
        } else if ("busy".equals(value)) {
            forwordNumber = Constant.DISABLE_SERVICE;
        } else {
            return ;
        }
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse(forwordNumber));
        startActivityCatchException(i);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(Constant.KEY_CHECK_LOG)) {
            App app = (App) getActivity().getApplication();
            String logFile = app.getLogFile();
            if (logFile != null) {
                Uri uri = Uri.parse("file://" + logFile);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClassName("com.android.htmlviewer",
                        "com.android.htmlviewer.HTMLViewerActivity");
                startActivityCatchException(intent);
            }
            return true;
        } else if (preference.getKey().equals(Constant.KEY_CHECK_UPGRADE)) {
            UpgradeManager manager = new UpgradeManager(getActivity());
            manager.checkUpgrade();
            return true;
        }
        return false;
    }

    private void startActivityCatchException(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private String getAppVerName() {
        try {
            PackageManager pm = getActivity().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return "";
    }

    private int getAppVerCode() {
        try {
            PackageManager pm = getActivity().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getActivity().getPackageName(),
                    0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return -1;
    }
}
