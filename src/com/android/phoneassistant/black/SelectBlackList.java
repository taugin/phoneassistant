package com.android.phoneassistant.black;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.phoneassistant.R;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;

public class SelectBlackList extends ListActivity implements OnItemClickListener {

    private ArrayList<HashMap<String, ContactHolder>> mContactList;
    private ContactLoaderThread mContactLoaderThread;
    private Handler mHandler;
    private SimpleAdapter mContactAdapter;
    private MenuItem mMenuItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_black_name);
        mContactList = new ArrayList<HashMap<String, ContactHolder>>();
        mHandler = new Handler();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mContactAdapter = new SimpleAdapter(this, mContactList,
                android.R.layout.simple_list_item_multiple_choice, new String[]{Phone.DISPLAY_NAME}, new int[]{android.R.id.text1});
        listView.setAdapter(mContactAdapter);
        listView.setOnItemClickListener(this);
        mContactLoaderThread = new ContactLoaderThread(this);
        mContactLoaderThread.start();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_menu, menu);
        mMenuItem = menu.findItem(R.id.action_selectall);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_selectall:
            int count = getListView().getCheckedItemCount();
            if (count == getListView().getCount()) {
                selectAll(false);
                item.setTitle(android.R.string.selectAll);
            } else {
                selectAll(true);
                item.setTitle(android.R.string.cancel);
            }
            break;
        case R.id.action_ok:
            importBlackList();
            finish();
            break;
        case android.R.id.home:
            finish();
            break;
        }
        return true;
    }

    private void selectAll(boolean select) {
        int count = getListView().getCount();
        for (int position = 0; position < count; position++) {
            getListView().setItemChecked(position, select);
        }
    }

    @SuppressWarnings("unchecked")
    private void importBlackList() {
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        SparseBooleanArray array = getListView().getCheckedItemPositions();
        int size = getListView().getCount();
        HashMap<String, ContactHolder> hashMap;
        ContactHolder holder = null;
        for (int index = 0; index < size; index++) {
            if (array.get(index)) {
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DBConstant.BLOCK_URI);
                hashMap = (HashMap<String, ContactHolder>) mContactAdapter.getItem(index);
                holder = hashMap.get(Phone.DISPLAY_NAME);
                builder.withValue(DBConstant.BLOCK_NAME, holder.displayName);
                builder.withValue(DBConstant.BLOCK_NUMBER, holder.phoneNumber);
                ContentProviderOperation operater = builder.build();
                operationList.add(operater);
                Log.d(Log.TAG, holder.phoneNumber);
            }
        }
        try {
            getContentResolver().applyBatch(DBConstant.AUTHORITIES, operationList);
        } catch (RemoteException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (OperationApplicationException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    class ItemAdder implements Runnable {
        private ContactHolder mContactHolder;
        public ItemAdder(ContactHolder holder) {
            mContactHolder = holder;
        }
        public void run() {
            HashMap<String, ContactHolder> map = new HashMap<String, ContactHolder>();
            map.put(Phone.DISPLAY_NAME, mContactHolder);
            mContactList.add(map);
            mContactAdapter.notifyDataSetChanged();
        }
    }
    class ContactLoaderThread extends Thread {
        private Context mContext;
        public ContactLoaderThread(Context context) {
            mContext = context;
        }
        @Override
        public void run() {
            queryContact();
        }
        
        private void queryContact() {
            String[] PHONES_PROJECTION = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER };
            Cursor c = null;
            try {
                c = mContext.getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, 
                        null, null, null);
                if (c != null) {
                    if (c.moveToFirst()) {
                        do {
                            ContactHolder holder = new ContactHolder();
                            holder.phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                            holder.displayName = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
                            if (holder.phoneNumber != null && holder.phoneNumber.equals(holder.displayName)) {
                                holder.displayName = null;
                            }
                            Log.d(Log.TAG, "phoneNumber = " + holder.phoneNumber);
                            if (TextUtils.isEmpty(holder.phoneNumber))
                                continue; 
                            if (holder.phoneNumber.startsWith("+86")) {
                                holder.phoneNumber = holder.phoneNumber.substring("+86".length());
                            }
                            holder.phoneNumber = holder.phoneNumber.replaceAll("-", "");
                            holder.phoneNumber = holder.phoneNumber.replaceAll("\\s+", "");
                            mHandler.post(new ItemAdder(holder));
                        } while (c.moveToNext());
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    class ContactHolder {
        public String displayName;
        public String phoneNumber;
        public String toString() {
            if (TextUtils.isEmpty(displayName)) {
                return phoneNumber;
            }
            return phoneNumber + "-" + displayName;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (mMenuItem == null) {
            return ;
        }
        int count = getListView().getCheckedItemCount();
        if (count == getListView().getCount()) {
            mMenuItem.setTitle(android.R.string.cancel);
        } else {
            mMenuItem.setTitle(android.R.string.selectAll);
        }
    }
}
