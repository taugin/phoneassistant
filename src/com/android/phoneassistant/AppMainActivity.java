package com.android.phoneassistant;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.phoneassistant.black.BlackListFragment;
import com.android.phoneassistant.customer.RecordListFragment;
import com.android.phoneassistant.util.FragmentListener;

public class AppMainActivity extends Activity implements
        OnCheckedChangeListener {

    // private TabContainer mTabContainer;
    private RecordListFragment mRecordListFragment;
    private BlackListFragment mBlackListFragment;
    private HashMap<Integer, Fragment> mFragmentMap;

    private FragmentManager mFragmentManager;
    private RadioGroup mRadioGroup;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mRecordListFragment = new RecordListFragment();
        mBlackListFragment = new BlackListFragment();
        mFragmentMap = new HashMap<Integer, Fragment>();
        mFragmentMap.put(R.id.call_log_radio, mRecordListFragment);
        mFragmentMap.put(R.id.black_radio, mBlackListFragment);
        mFragmentManager = getFragmentManager();

        mRadioGroup = (RadioGroup) findViewById(R.id.tab_group);
        mRadioGroup.setOnCheckedChangeListener(this);
        mRadioGroup.check(R.id.call_log_radio);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_content, mFragmentMap.get(checkedId));
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        int checkedId = mRadioGroup.getCheckedRadioButtonId();
        FragmentListener fragment = (FragmentListener) mFragmentMap.get(checkedId);
        if (fragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

}
