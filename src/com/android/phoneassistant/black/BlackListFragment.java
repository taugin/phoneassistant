package com.android.phoneassistant.black;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.info.BlackInfo;
import com.android.phoneassistant.manager.RecordFileManager;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.FragmentListener;
import com.android.phoneassistant.util.Log;

public class BlackListFragment extends ListFragment implements OnClickListener, OnLongClickListener, Callback, FragmentListener{

    private static final int VIEW_STATE_NORMAL = 0;
    private static final int VIEW_STATE_DELETE = 1;
    private BlackListAdapter mListAdapter;
    private ArrayList<BlackInfo> mBlackList;
    private int mViewState;
    private AlertDialog mAlertDialog;
    private AlertDialog mSelectionDialog;
    private AlertDialog mAddBlackDialog;
    private MenuItem mMenuItem;
    private ActionMode mActionMode;
    private EditText mPhoneNumber;
    private PopupWindow mPopupWindow;
    private CheckBox mCheckBox;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.black_name);
        mViewState = VIEW_STATE_NORMAL;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBlackList = new ArrayList<BlackInfo>();
        mListAdapter = new BlackListAdapter(getActivity(), mBlackList);
        getListView().setAdapter(mListAdapter);
        setListShown(true);
        setEmptyText(getResources().getText(R.string.empty_black_name));
        getActivity().getContentResolver().registerContentObserver(DBConstant.BLOCK_URI, true, mBlockObserver);
        if (mHandler.hasMessages(UPDATE_LIST)) {
            mHandler.removeMessages(UPDATE_LIST);
        }
        mHandler.sendEmptyMessageDelayed(UPDATE_LIST, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Log.TAG, "");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onBackPressed() {
        if (mViewState == VIEW_STATE_DELETE) {
            mViewState = VIEW_STATE_NORMAL;
            mListAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(mBlockObserver);
        super.onDestroy();
    }

    private void updateUI() {
        Log.d(Log.TAG, "");
        mBlackList = RecordFileManager.getInstance(getActivity()).getBlackListFromDB(mBlackList);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.black_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_add: {
            showSelectionDialog();
        }
            break;
        }
        return true;
    }
    private void showSelectionDialog() {
        if (mSelectionDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.black_add_selection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(Log.TAG, "which = " + which);
                    if (which == 1) {
                        Intent intent = new Intent(getActivity(), SelectBlackList.class);
                        getActivity().startActivity(intent);
                    } else if (which == 0) {
                        showAddBlackDialog();
                    }
                }
            });
            mSelectionDialog = builder.create();
            mSelectionDialog.setCanceledOnTouchOutside(false);
        }
        mSelectionDialog.show();
    }
    private void showConfirmDialog() {
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirm_message);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecordFileManager.getInstance(getActivity()).deleteBlackInfoFromDB(mBlackList);
                    mViewState = VIEW_STATE_NORMAL;
                    mListAdapter.notifyDataSetChanged();
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mViewState = VIEW_STATE_NORMAL;
                    mListAdapter.notifyDataSetChanged();
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        mAlertDialog.show();
    }

    private void showAddBlackDialog() {
        if (mAddBlackDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            mPhoneNumber = new EditText(getActivity());
            mPhoneNumber.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            builder.setView(mPhoneNumber);
            builder.setTitle(R.string.input_number);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstant.BLOCK_NUMBER, mPhoneNumber.getText().toString());
                    getActivity().getContentResolver().insert(DBConstant.BLOCK_URI, values);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            mAddBlackDialog = builder.create();
            mAddBlackDialog.setCanceledOnTouchOutside(true);
        }
        if (mPhoneNumber != null) {
            mPhoneNumber.setText("");
        }
        mAddBlackDialog.show();
    }
    
    class ViewHolder {
        LinearLayout itemContainer;
        TextView displayName;
        TextView blockCount;
        TextView blockDate;
        LinearLayout checkBoxContainer;
        CheckBox checkBox;
        View deleteBlack;
    }
    private class BlackListAdapter extends ArrayAdapter<BlackInfo>{

        private Context mContext;
        public BlackListAdapter(Context context, ArrayList<BlackInfo> listInfos) {
            super(context, 0, listInfos);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.black_item_layout, null);
                viewHolder.itemContainer = (LinearLayout) convertView.findViewById(R.id.item_container);
                viewHolder.itemContainer.setOnClickListener(BlackListFragment.this);
                viewHolder.itemContainer.setOnLongClickListener(BlackListFragment.this);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.display_name);
                viewHolder.blockCount = (TextView) convertView.findViewById(R.id.block_count);
                viewHolder.blockDate = (TextView) convertView.findViewById(R.id.block_date);
                viewHolder.deleteBlack = convertView.findViewById(R.id.delete_black);
                viewHolder.deleteBlack.setOnClickListener(BlackListFragment.this);
                viewHolder.checkBoxContainer = (LinearLayout) convertView.findViewById(R.id.check_box_container);
                viewHolder.checkBoxContainer.setOnClickListener(BlackListFragment.this);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.itemContainer.setTag(position);
            viewHolder.deleteBlack.setTag(position);
            viewHolder.checkBoxContainer.setTag(position);

            BlackInfo info = getItem(position);
            if (info != null) {
                String displayName = info.blackNumber;
                if (!TextUtils.isEmpty(info.blackName)) {
                    displayName += "-" + info.blackName;
                }
                viewHolder.displayName.setText(displayName);
                viewHolder.blockCount.setText(getResources().getString(R.string.block_times_args, info.blockCount));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                /**
                if (info.blockHisTimes != null) {
                    String dates[] = info.blockHisTimes.split(",");
                    if (dates != null) {
                        String lastDate = dates[dates.length - 1];
                        long lastTime = 0;
                        try {
                            lastTime = Long.parseLong(lastDate);
                        } catch(NumberFormatException e) {
                            lastTime = System.currentTimeMillis();
                        }
                        viewHolder.blockDate.setText(sdf.format(new Date(lastTime)));
                    } else {
                        viewHolder.blockDate.setText("");
                    }
                } else {
                    viewHolder.blockDate.setText("");
                }*/
                if (info.blockTime != 0) {
                    viewHolder.blockDate.setText(sdf.format(new Date(info.blockTime)));
                } else {
                    viewHolder.blockDate.setText("");
                }
                viewHolder.checkBox.setChecked(info.checked);
            }
            if (mViewState == VIEW_STATE_NORMAL) {
                viewHolder.deleteBlack.setVisibility(View.VISIBLE);
                viewHolder.checkBox.setVisibility(View.INVISIBLE);
            } else if (mViewState == VIEW_STATE_DELETE) {
                viewHolder.deleteBlack.setVisibility(View.INVISIBLE);
                viewHolder.checkBox.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_container) {
            if (mActionMode != null) {
                return ;
            }
            int position = (Integer) v.getTag();
            BlackInfo info = mListAdapter.getItem(position);
            //Intent intent = new Intent(getActivity(), CustomerDetailActivity.class);
            //intent.putExtra(DBConstant._ID, info._id);
            //startActivity(intent);
            Log.d(Log.TAG, "position = " + position);
            Log.d(Log.TAG, "info = " + info.blackNumber);
        } else if (v.getId() == R.id.check_box_container) {
            int position = (Integer) v.getTag();
            BlackInfo info = mListAdapter.getItem(position);
            info.checked = !info.checked;
            if (mMenuItem == null) {
                return ;
            }
            int count = mListAdapter.getCount();
            if (count == getCheckedCount()) {
                mMenuItem.setTitle(android.R.string.cancel);
            } else {
                mMenuItem.setTitle(android.R.string.selectAll);
            }
        } else if (v.getId() == R.id.delete_black) {
            v.setEnabled(false);
            synchronized(mListAdapter) {
                int position = (Integer) v.getTag();
                    if (position < mListAdapter.getCount()) {
                    BlackInfo info = mListAdapter.getItem(position);
                    Uri uri = ContentUris.withAppendedId(DBConstant.BLOCK_URI, info._id);
                    int ret = getActivity().getContentResolver().delete(uri, null, null);
                    if (ret > 0) {
                        mListAdapter.remove(info);
                    }
                }
            }
            v.setEnabled(true);
        }
    }

    private int getCheckedCount() {
        int count = 0;
        for (BlackInfo info : mBlackList) {
            if (info.checked) {
                count ++;
            }
        }
        return count;
    }
    
    private RecordObserver mBlockObserver = new RecordObserver();
    private class RecordObserver extends ContentObserver {
        public RecordObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(Log.TAG, "selfChange = " + selfChange + " , uri = " + uri);
            if (mHandler.hasMessages(UPDATE_LIST)) {
                mHandler.removeMessages(UPDATE_LIST);
            }
            mHandler.sendEmptyMessageDelayed(UPDATE_LIST, 500);
        }
    }
    
    private static final int UPDATE_LIST = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case UPDATE_LIST:
                updateUI();
                break;
            }
        }
    };


    @Override
    public boolean onLongClick(View v) {
        if (mActionMode != null) {
            return true;
        }
        getActivity().startActionMode(this);
        int position = (Integer) v.getTag();
        Log.d(Log.TAG, "position = " + position);
        BlackInfo info = mListAdapter.getItem(position);
        info.checked = true;
        mListAdapter.notifyDataSetChanged();
        return true;
    }

    private void selectAll(boolean select) {
        int count = mListAdapter.getCount();
        for (int position = 0; position < count; position++) {
            mListAdapter.getItem(position).checked = select;
        }
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActionMode = mode;
        mode.setTitle(R.string.action_delete);
        mode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
        mMenuItem = menu.findItem(R.id.action_selectall);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mViewState = VIEW_STATE_DELETE;
        mListAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_selectall:
            int count = mListAdapter.getCount();
            if (count == getCheckedCount()) {
                selectAll(false);
                item.setTitle(android.R.string.selectAll);
            } else {
                selectAll(true);
                item.setTitle(android.R.string.cancel);
            }
            break;
        case R.id.action_ok:
            if (getCheckedCount() > 0) {
                showConfirmDialog();
            } else {
                mode.finish();
            }
            break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectAll(false);
        mViewState = VIEW_STATE_NORMAL;
        mListAdapter.notifyDataSetChanged();
        mActionMode = null;
    }
    public void finishActionModeIfNeed() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void onFragmentSelected(int pos) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isSearching() {
        return false;
    }
}
