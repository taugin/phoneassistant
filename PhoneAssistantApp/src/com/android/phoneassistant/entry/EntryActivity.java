package com.android.phoneassistant.entry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;

public class EntryActivity extends Activity implements
        OnItemClickListener {
    /** Called when the activity is first created. */
    private static final int ITEM_WIDTH_HEIGHT = 270;
    private final String TAG = "EntryActivity";
    private final String ACTION = "com.android.phoneassistant.intent.action.PHONEASSISTANT";
    private final String CATEGORY = "com.android.phoneassistant.intent.category.PHONEASSISTANT";
    private PackageManager mPm = null;
    private ActivityAdapter mActivityAdapter;
    private int mWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_layout);
        mWidth = getResources().getDisplayMetrics().widthPixels;
        int itemCount = mWidth / ITEM_WIDTH_HEIGHT;
        Log.d(Log.TAG, "itemCount = " + itemCount);
        // setTitle(getLocalClassName());
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);
        gridView.setNumColumns(itemCount);
        gridView.setOnItemClickListener(this);
        mPm = getPackageManager();
        ArrayList<EntryInfo> activitiesList = new ArrayList<EntryInfo>();
        if (mPm != null) {
            queryActivities(activitiesList);
            // Collections.sort(activitiesList);
            mActivityAdapter = new ActivityAdapter(this, activitiesList);
            gridView.setAdapter(mActivityAdapter);
        }
    }

    private void queryActivities(List<EntryInfo> list) {
        Intent queryIntent = new Intent();
        queryIntent.setAction(ACTION);
        queryIntent.addCategory(CATEGORY);
        // queryIntent.setAction(Intent.ACTION_MAIN);
        // queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> lists = mPm.queryIntentActivities(queryIntent, 0);
        EntryInfo entryInfo = null;
        // Log.d(TAG, "lists = " + lists);
        if (lists != null) {
            int size = lists.size();
            // Log.d(TAG, "size = " + size);
            int i = 0;
            ResolveInfo info = null;
            while (i < size) {
                entryInfo = new EntryInfo();
                info = lists.get(i);
                Intent intent = activityIntent(info.activityInfo.packageName,
                        info.activityInfo.name);
                String name = info.activityInfo.name;
                if (name.startsWith(info.activityInfo.packageName)) {
                    name = name.substring(info.activityInfo.packageName
                            .length() + 1);
                }
                String label = info.activityInfo.loadLabel(mPm).toString();
                if (label != null && !label.equals("")) {
                    name = label;
                }
                entryInfo.name = name;
                entryInfo.intent = intent;
                entryInfo.logo = info.loadIcon(mPm);
                list.add(entryInfo);
                i++;
            }
        }
    }

    protected Intent activityIntent(String pkg, String className) {
        // Log.d(TAG, "pkg = " + pkg + " , className = " + className);
        Intent result = new Intent();
        result.setClassName(pkg, className);
        return result;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        EntryInfo info = mActivityAdapter.getItem(arg2);
        if (info != null) {
            Intent intent = info.intent;
            startActivity(intent);
        }
    }

    class ActivityAdapter extends ArrayAdapter<EntryInfo> {

        private Context mContext;
        private LayoutInflater mInflater;

        public ActivityAdapter(Context context, ArrayList<EntryInfo> list) {
            super(context, 0, list);
            mContext = context;
            mInflater = (LayoutInflater) mContext
                    .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = null;
            if (convertView == null) {
                view = (TextView) mInflater.inflate(
                        android.R.layout.simple_list_item_1, null);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        ITEM_WIDTH_HEIGHT, ITEM_WIDTH_HEIGHT);
                view.setLayoutParams(params);
            } else {
                view = (TextView) convertView;
            }
            view.setGravity(Gravity.CENTER);
            view.setBackgroundResource(R.drawable.griditem_background);
            view.setPadding(0, 20, 0, 20);
            view.setCompoundDrawablePadding(40);
            view.setSingleLine();
            view.setEllipsize(TruncateAt.END);
            EntryInfo info = getItem(position);
            view.setText(info.name);
            info.logo.setBounds(0, 0, 56, 56);
            view.setCompoundDrawables(null, info.logo, null, null);
            return view;
        }
    }

    class EntryInfo implements Comparable<EntryInfo> {
        String name;
        Intent intent;
        Drawable logo;

        @Override
        public int compareTo(EntryInfo another) {
            return Collator.getInstance().compare(name, another.name);
        }
    }
}