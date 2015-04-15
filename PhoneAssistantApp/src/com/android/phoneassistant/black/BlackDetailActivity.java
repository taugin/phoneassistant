package com.android.phoneassistant.black;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;

import com.android.phoneassistant.R;
import com.android.phoneassistant.info.BlackDetail;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;

public class BlackDetailActivity extends Activity {

    private ListView mListView;
    private int mBlockId = -1;
    private BlackAdapter mBlackAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mBlockId = intent.getIntExtra("block_id", -1);
            String name = intent.getStringExtra("block_name");
            String number = intent.getStringExtra("block_number");
            if (TextUtils.isEmpty(name)) {
                setTitle(number);
            } else {
                setTitle(name);
            }
        }
        if (mBlockId == -1) {
            finish();
            return ;
        }
        setContentView(R.layout.black_detail_layout);
        mListView = (ListView) findViewById(R.id.black_detail_list);
        mBlackAdapter = new BlackAdapter(this);
        mListView.setAdapter(mBlackAdapter);
        new QueryBlackDetail().start();
    }

    class QueryBlackDetail extends Thread {
        public void run() {
            Cursor c = null;
            String selection = DBConstant.BLOCK_ID + "=" + mBlockId;
            String sortOrder = DBConstant.BLOCK_CALL_TYPE + " DESC , " + DBConstant.BLOCK_DETAIL_TIME + " DESC";
            try {
                c = getContentResolver().query(DBConstant.BLOCK_DETAIL_URI, null, selection, null, sortOrder);
                BlackDetail detail = null;
                if (c != null && c.moveToFirst()) {
                    do {
                        detail = new BlackDetail();
                        detail._id = c.getInt(c.getColumnIndex(DBConstant._ID));
                        detail.block_id = mBlockId;
                        detail.number = c.getString(c.getColumnIndex(DBConstant.BLOCK_DETAIL_NUMBER));
                        detail.time = c.getLong(c.getColumnIndex(DBConstant.BLOCK_DETAIL_TIME));
                        detail.sms = c.getString(c.getColumnIndex(DBConstant.BLOCK_DETAIL_SMS));
                        detail.callType = c.getInt(c.getColumnIndex(DBConstant.BLOCK_CALL_TYPE)) == 1;
                        detail.smsType = c.getInt(c.getColumnIndex(DBConstant.BLOCK_SMS_TYPE)) == 1;
                        runOnUiThread(detail);
                    } while(c.moveToNext());
                }
            } catch(Exception e) {
                Log.d(Log.TAG, "error : " + e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    private void runOnUiThread(final BlackDetail detail) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBlackAdapter.add(detail);
            }
        });
    }

    class BlackAdapter extends ArrayAdapter<BlackDetail>  {

        public BlackAdapter(Context context) {
            super(context, 0, new ArrayList<BlackDetail>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_2, null);
            } 
            TwoLineListItem twoItem = (TwoLineListItem)convertView;
            BlackDetail detail = getItem(position);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            twoItem.getText1().setText(sdf.format(new Date(detail.time)));
            twoItem.getText1().setTextColor(Color.BLACK);
            twoItem.getText2().setTextColor(Color.BLACK);
            if (detail.callType) {
                twoItem.getText2().setText(detail.number);
            } else {
                twoItem.getText2().setText(detail.sms);
            }
            return convertView;
        }
        
    }
}
