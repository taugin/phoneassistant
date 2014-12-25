package com.android.phoneassistant.customer;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.info.ContactInfo;
import com.android.phoneassistant.info.RecordInfo;
import com.android.phoneassistant.manager.RecordFileManager;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;

public class CustomerDetailFragment extends ListFragment implements OnClickListener, TextWatcher {

    private int mContactId;
    private ContactInfo mContact;
    private ArrayList<RecordInfo> mCallLogList;
    private TextView mPhoneNumberView;
    private EditText mCustomerNameView;
    private View mEditSave;
    private DetailAdapter mDetailAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View headerView = inflater.inflate(R.layout.custom_detail_layout, null);
        mPhoneNumberView = (TextView) headerView.findViewById(R.id.customer_number);
        mCustomerNameView = (EditText) headerView.findViewById(R.id.customer_name);
        mCustomerNameView.addTextChangedListener(this);
        View dialNumber = headerView.findViewById(R.id.dial_number);
        dialNumber.setOnClickListener(this);
        mEditSave = headerView.findViewById(R.id.edit_save);
        mEditSave.setOnClickListener(this);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.addHeaderView(headerView);
        mCallLogList = new ArrayList<RecordInfo>();
        mDetailAdapter = new DetailAdapter(getActivity(), mCallLogList);
        listView.setAdapter(mDetailAdapter);
        return view;
    }

    public void setContactId(int id) {
        mContactId = id;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(Log.TAG, "onActivityCreated mBaseId = " + mContactId);
        setListShown(true);
        updateUI();
        getActivity().getContentResolver().registerContentObserver(DBConstant.RECORD_URI, true, mRecordObserver);
    }

    private void updateUI() {
        mCallLogList = RecordFileManager.getInstance(getActivity()).getRecordsFromDB(mCallLogList, mContactId);
        mDetailAdapter.notifyDataSetChanged();
        mContact = RecordFileManager.getInstance(getActivity()).getSingleContact(mContactId);
        if (mContact == null) {
            return ;
        }
        mPhoneNumberView.setText(mContact.contactNumber);
        mCustomerNameView.setText(mContact.contactName);
        int len = 0;
        if (!TextUtils.isEmpty(mContact.contactName)) {
            len = mContact.contactName.length();
        }
        mCustomerNameView.setEnabled(!mContact.contactModifyName);
        mCustomerNameView.setSelection(len);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getContentResolver().unregisterContentObserver(mRecordObserver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.edit_save) {
            String newName = mCustomerNameView.getText().toString();
            Log.d(Log.TAG, "newName = " + newName);
            if (!TextUtils.isEmpty(newName)) {
                ContentValues values = new ContentValues();
                values.put(DBConstant.CONTACT_NAME, newName);
                Uri uri = ContentUris.withAppendedId(DBConstant.CONTACT_URI, mContactId);
                int ret = getActivity().getContentResolver().update(uri, values, null, null);
                String message = null;
                if (ret > 0) {
                    message = getActivity().getResources().getString(R.string.save_success);
                } else {
                    message = getActivity().getResources().getString(R.string.save_failure);
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                Log.getLog(getActivity()).recordOperation("Save record name : " + newName);
            }
        } else if (v.getId() == R.id.dial_number) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + mContact.contactNumber));
            getActivity().startActivity(intent);
        } else if (v.getId() == R.id.delete_file) {
            int position = (Integer) v.getTag();
            deleteFileConfirm(position);
        } else if (v.getId() == R.id.media_control) {
            int position = (Integer) v.getTag();
            Log.d(Log.TAG, "position = " + position);
            RecordInfo info = mCallLogList.get(position);
            playAudio(info.recordFile);
        }
    }

    private void deleteFileConfirm(final int position) {
        if (position < 0) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_warning);
        builder.setMessage(R.string.confirm_message);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(Log.TAG, "position = " + position);
                RecordInfo info = mCallLogList.get(position);
                ArrayList<RecordInfo> list = new ArrayList<RecordInfo>();
                list.add(info);
                int ret = RecordFileManager.getInstance(getActivity())
                        .deleteRecordFiles(list);
                if (ret > 0) {
                    mCallLogList.remove(info);
                    mDetailAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mContact == null) {
            return ;
        }
        String newName = mCustomerNameView.getText().toString();
        if (newName != null && newName.equals(mContact.contactName == null ? "" : mContact.contactName)) {
            mEditSave.setEnabled(false);
        } else {
            mEditSave.setEnabled(true);
        }
    }
    
    private RecordObserver mRecordObserver = new RecordObserver();
    private class RecordObserver extends ContentObserver {
        public RecordObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(Log.TAG, "onChange selfChange = " + selfChange + " , uri = " + uri);
            updateUI();
        }
        
    }
    class ViewHolder {
        ImageView callFlag;
        TextView callDate;
        TextView callDuration;
        View deleteFile;
        View mediaControl;
    }
    class DetailAdapter extends ArrayAdapter<RecordInfo> {

        private Context mContext;
        public DetailAdapter(Context context, ArrayList<RecordInfo> list) {
            super(context, 0, list);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.call_log_item, null);
                holder = new ViewHolder();
                holder.callFlag = (ImageView) convertView.findViewById(R.id.call_log_flag);
                holder.callDate = (TextView) convertView.findViewById(R.id.call_date);
                holder.callDuration = (TextView) convertView.findViewById(R.id.call_duration);
                holder.deleteFile = convertView.findViewById(R.id.delete_file);
                holder.deleteFile.setOnClickListener(CustomerDetailFragment.this);
                holder.mediaControl = convertView.findViewById(R.id.media_control);
                holder.mediaControl.setOnClickListener(CustomerDetailFragment.this);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.deleteFile.setTag(position);
            holder.mediaControl.setTag(position);
            RecordInfo info = getItem(position);
            if (info != null) {
                int resId = 0;
                if (info.callFlag == DBConstant.FLAG_INCOMING) {
                    resId = R.drawable.ic_incoming;
                } else if (info.callFlag == DBConstant.FLAG_OUTGOING) {
                    resId = R.drawable.ic_outgoing;
                } else if (info.callFlag == DBConstant.FLAG_MISSCALL){
                    resId = R.drawable.ic_missed_call;
                } else {
                    resId = R.drawable.ic_block_call;
                }
                holder.callFlag.setImageResource(resId);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long updateTime = 0;
                if (info.callFlag >= DBConstant.FLAG_OUTGOING) {
                    updateTime = info.recordStart;
                } else {
                    updateTime = info.recordRing;
                }
                holder.callDate.setText(sdf.format(new Date(updateTime)));
                
                holder.callDuration.setText(getTimeExperence(info.recordStart == 0 ? 0 : info.recordEnd - info.recordStart));
                
                if (info.recordFile == null) {
                    holder.mediaControl.setVisibility(View.INVISIBLE);
                } else {
                    holder.mediaControl.setVisibility(View.VISIBLE);
                }
            }
            return convertView;
        }
        private String getTimeExperence(long timeExperence) {
            int allsec = Math.round(timeExperence / (float)1000);
            int min = allsec / 60;
            int sec = allsec % 60;
            int hour = min / 60;
            if (hour > 0) {
                min = min % 60;
            }
            String hTag = getResources().getText(R.string.hour_tag).toString();
            String mTag = getResources().getText(R.string.min_tag).toString();
            String sTag = getResources().getText(R.string.sec_tag).toString();
            String sHour = hour > 0 ? hour + hTag : "";
            String sMin = min > 0 ? min + mTag : "";
            String sSec = sec + sTag;
            return String.valueOf(sHour + sMin + sSec);
        }
    }

    private void playAudio(String audioPath) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + audioPath), "audio/amr");
        startActivity(intent);
    }

}
