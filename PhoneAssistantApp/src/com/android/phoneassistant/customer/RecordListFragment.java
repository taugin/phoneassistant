package com.android.phoneassistant.customer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.info.ContactInfo;
import com.android.phoneassistant.manager.BlackNameManager;
import com.android.phoneassistant.manager.RecordFileManager;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.FragmentListener;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.view.CustomCheckBox;

public class RecordListFragment extends ListFragment implements OnCheckedChangeListener, OnClickListener,
                                    OnLongClickListener, Callback, FragmentListener, OnQueryTextListener {

    private static final int VIEW_STATE_NORMAL = 0;
    private static final int VIEW_STATE_DELETE = 1;
    private RecordListAdapter mListAdapter;
    private ArrayList<ContactInfo> mRecordList;
    private int mViewState;
    private AlertDialog mAlertDialog;
    private ActionMode mActionMode;
    private PopupWindow mPopupWindow;
    private CheckBox mCheckBox;
    private MenuItem mMenuItem;
    private SearchView mSearchView;
    private int mExpandPos = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.call_log);
        mViewState = VIEW_STATE_NORMAL;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecordList = new ArrayList<ContactInfo>();
        mListAdapter = new RecordListAdapter(getActivity(), mRecordList);
        getListView().setAdapter(mListAdapter);
        getListView().setTextFilterEnabled(true);
        setListShown(true);
        setEmptyText(getResources().getText(R.string.empty_call_log));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);
            return true;
        }
        if (mViewState == VIEW_STATE_DELETE) {
            mViewState = VIEW_STATE_NORMAL;
            mListAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateUI() {
        mRecordList = RecordFileManager.getInstance(getActivity()).getContactFromDB(mRecordList);
        mListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.record_menu, menu);
        mSearchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showConfirmDialog() {
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.confirm_message);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecordFileManager.getInstance(getActivity()).deleteContactFromDB(mRecordList);
                    mViewState = VIEW_STATE_NORMAL;
                    mListAdapter.notifyDataSetChanged();
                    mExpandPos = -1;
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mActionMode != null) {
                        mViewState = VIEW_STATE_NORMAL;
                        mListAdapter.notifyDataSetChanged();
                        mActionMode.finish();
                    }
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        mAlertDialog.show();
    }

    private ContactInfo lastContactInfo = null;
    class ViewHolder {
        LinearLayout dialNumber;
        LinearLayout itemContainer;
        TextView displayName;
        TextView displayNumber;
        TextView callState;
        TextView callLogDate;
        CustomCheckBox deleteCheckBox;
        CustomCheckBox functionMenu;
        View moreFunction;
        CustomCheckBox deleteItem;
        CustomCheckBox blackName;
        CustomCheckBox sendSms;
        View blackStateIcon;
    }
    private class RecordListAdapter extends ArrayAdapter<ContactInfo> {

        private Context mContext;
        public RecordListAdapter(Context context, ArrayList<ContactInfo> listInfos) {
            super(context, 0, listInfos);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item_layout, null);
                viewHolder.dialNumber = (LinearLayout) convertView.findViewById(R.id.dial_number);
                viewHolder.dialNumber.setOnClickListener(RecordListFragment.this);
                viewHolder.itemContainer = (LinearLayout) convertView.findViewById(R.id.item_container);
                viewHolder.itemContainer.setOnClickListener(RecordListFragment.this);
                viewHolder.itemContainer.setOnLongClickListener(RecordListFragment.this);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.display_name);
                viewHolder.displayNumber = (TextView) convertView.findViewById(R.id.display_number);
                viewHolder.blackStateIcon = convertView.findViewById(R.id.black_state_icon);
                viewHolder.callState = (TextView) convertView.findViewById(R.id.call_state);
                viewHolder.callLogDate = (TextView) convertView.findViewById(R.id.call_log_date);
                viewHolder.functionMenu = (CustomCheckBox) convertView.findViewById(R.id.function_menu);
                viewHolder.functionMenu.setOnClickListener(RecordListFragment.this);

                viewHolder.deleteCheckBox = (CustomCheckBox) convertView.findViewById(R.id.delete_checkbox);
                viewHolder.deleteCheckBox.setOnClickListener(RecordListFragment.this);

                viewHolder.moreFunction = convertView.findViewById(R.id.more_function);
                viewHolder.deleteItem = (CustomCheckBox) convertView.findViewById(R.id.delete_item);
                viewHolder.deleteItem.setOnClickListener(RecordListFragment.this);
                viewHolder.blackName = (CustomCheckBox) convertView.findViewById(R.id.black_name);
                viewHolder.blackName.setOnClickListener(RecordListFragment.this);
                viewHolder.sendSms = (CustomCheckBox) convertView.findViewById(R.id.send_sms);
                viewHolder.sendSms.setOnClickListener(RecordListFragment.this);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.itemContainer.setTag(position);
            viewHolder.dialNumber.setTag(position);
            viewHolder.deleteCheckBox.setTag(position);
            viewHolder.functionMenu.setTag(position);

            viewHolder.deleteItem.setTag(position);
            viewHolder.blackName.setTag(position);
            viewHolder.sendSms.setTag(position);

            ContactInfo info = getItem(position);

            if (info != null) {
                CharSequence chars = getListView().getTextFilter();
                String filter = null;
                int len = 0;
                if (chars != null) {
                    filter = chars.toString();
                }

                String contactName = info.contactNumber != null ? info.contactNumber : "";
                SpannableString span = new SpannableString(contactName);
                if (filter != null) {
                    len = filter.length();
                }
                span.setSpan(new ForegroundColorSpan(Color.RED), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (!TextUtils.isEmpty(info.contactName)) {
                    viewHolder.displayName.setText(info.contactName);
                    viewHolder.displayNumber.setText(span);
                } else {
                    viewHolder.displayNumber.setText("");
                    viewHolder.displayName.setText(span);
                }
                String callLog = String.format("%d%s", info.contactLogCount, RecordListFragment.this.getResources().getString(R.string.call_log_count));
                viewHolder.callState.setText(callLog);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                viewHolder.callLogDate.setText(sdf.format(new Date(info.contactUpdate)));
                viewHolder.deleteCheckBox.setChecked(info.checked);
                if (mViewState == VIEW_STATE_NORMAL) {
                    viewHolder.functionMenu.setVisibility(View.VISIBLE);
                    viewHolder.deleteCheckBox.setVisibility(View.INVISIBLE);
                } else if (mViewState == VIEW_STATE_DELETE) {
                    viewHolder.functionMenu.setVisibility(View.INVISIBLE);
                    viewHolder.deleteCheckBox.setVisibility(View.VISIBLE);
                }

                if (mExpandPos != -1) {
                    info.expand = mExpandPos == position;
                }
                viewHolder.moreFunction.setVisibility(info.expand ? View.VISIBLE : View.GONE);
                viewHolder.functionMenu.setChecked(info.expand);
                viewHolder.blackName.setChecked(info.blocked);
                viewHolder.blackStateIcon.setVisibility(info.blocked ? View.VISIBLE : View.INVISIBLE);
            }
            return convertView;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
        if (buttonView.getId() == R.id.check_box) {
            int position = (Integer) buttonView.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            info.checked = isChecked;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.item_container) {
            if (mActionMode != null) {
                return ;
            }
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), CustomerDetailActivity.class);
            intent.putExtra(DBConstant._ID, info._id);
            startActivity(intent);
        } if (v.getId() == R.id.dial_number) {
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + info.contactNumber));
            getActivity().startActivity(intent);
        } else if (v.getId() == R.id.function_menu) {
            /*
            if (mPopupWindow == null) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.pop_menu, null);
                mCheckBox = (CheckBox) view.findViewById(R.id.add_black_name);
                mPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mPopupWindow.setContentView(view);
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mPopupWindow.setFocusable(true);
            }
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            mCheckBox.setTag(position);
            mCheckBox.setOnClickListener(this);
            Log.d(Log.TAG, "info.blocked = " + info.blocked + " , position = "
                    + position);
            mCheckBox.setChecked(info.blocked);

            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAsDropDown(v);
            }*/
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            int visiblePos = position - getListView().getFirstVisiblePosition();
            View view = getListView().getChildAt(visiblePos);
            ViewHolder holder = (ViewHolder) view.getTag();
            if (lastContactInfo != null && (lastContactInfo != info)) {
                lastContactInfo.expand = false;
            }
            info.expand = !info.expand;
            mExpandPos = info.expand ? position : -1;
            lastContactInfo = info;
            mListAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.black_name) {
            int position = (Integer) v.getTag();
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }

            ContactInfo info = mListAdapter.getItem(position);
            Log.d(Log.TAG, "info blocked = " + info.blocked);
            updateBlockCall(info);
            mListAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.delete_checkbox) {
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
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
            mListAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.delete_item) {
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            info.checked = true;
            showConfirmDialog();
        } else if (v.getId() == R.id.send_sms) {
            int position = (Integer) v.getTag();
            ContactInfo info = mListAdapter.getItem(position);
            Uri smsToUri = Uri.parse("smsto:" + info.contactNumber);
            Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
            try {
                startActivity(intent);
            } catch(ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.sms_activity_notfound, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getBlockCallId(ContactInfo info) {
        Cursor c = null;
        String selection = DBConstant.BLOCK_NUMBER + " LIKE '%" + info.contactNumber + "'";
        String projection[] = new String[]{DBConstant._ID};
        try {
            c = getActivity().getContentResolver().query(DBConstant.BLOCK_URI, projection, selection, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndex(DBConstant._ID));
            }
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return -1;
    }
    private void updateBlockCall(ContactInfo info) {
        int _id = getBlockCallId(info);
        Log.d(Log.TAG, "_id = " + _id);
        if (_id == -1) {
            ContentValues values = new ContentValues();
            values.put(DBConstant.BLOCK_NAME, info.contactName);
            values.put(DBConstant.BLOCK_NUMBER, info.contactNumber);
            values.put(DBConstant.BLOCK_CALL, DBConstant.BLOCK);
            if (getActivity().getContentResolver().insert(DBConstant.BLOCK_URI, values) != null) {
                info.blocked = true;
            }
        } else {
            String where = DBConstant._ID + "=" + _id;
            ContentValues values = new ContentValues();
            values.put(DBConstant.BLOCK_CALL, info.blocked ? DBConstant.NO_BLOCK : DBConstant.BLOCK);
            if (getActivity().getContentResolver().update(DBConstant.BLOCK_URI, values, where, null) > 0) {
                info.blocked = !info.blocked;
            }
        }
    }
    private int getCheckedCount() {
        int count = 0;
        for (ContactInfo info : mRecordList) {
            if (info.checked) {
                count ++;
            }
        }
        return count;
    }
    

    @Override
    public boolean onLongClick(View v) {
        if (mActionMode != null) {
            return true;
        }
        getActivity().startActionMode(this);
        int position = (Integer) v.getTag();
        Log.d(Log.TAG, "position = " + position);
        ContactInfo info = mListAdapter.getItem(position);
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
        Log.d(Log.TAG, "mode : " + mode);
        mActionMode = mode;
        mode.setTitle(R.string.action_delete);
        mode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
        mMenuItem = menu.findItem(R.id.action_selectall);
        Intent intent = new Intent(Constant.ACTION_RADIOGROUP_ENABLE);
        intent.putExtra("radiogroup_enable", false);
        getActivity().sendBroadcast(intent);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.d(Log.TAG, "mode : " + mode);
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
        Log.d(Log.TAG, "mode : " + mode);
        selectAll(false);
        mViewState = VIEW_STATE_NORMAL;
        mListAdapter.notifyDataSetChanged();
        mActionMode = null;
        Intent intent = new Intent(Constant.ACTION_RADIOGROUP_ENABLE);
        intent.putExtra("radiogroup_enable", true);
        getActivity().sendBroadcast(intent);
    }

    public void finishActionModeIfNeed() {
        Log.d(Log.TAG, "mActionMode = " + mActionMode);
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
        if (!mSearchView.isIconified()) {
            mSearchView.setQuery("", false);
            mSearchView.setIconified(true);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(Log.TAG, "newText = " + newText);
        if (TextUtils.isEmpty(newText)) {
            getListView().clearTextFilter();
        } else {
            getListView().setFilterText(newText);
        }
        return true;
    }

    @Override
    public void onFragmentSelected(int pos) {
        
    }

    @Override
    public boolean isSearching() {
        if (mSearchView != null) {
            return !mSearchView.isIconified();
        }
        return false;
    }
}
