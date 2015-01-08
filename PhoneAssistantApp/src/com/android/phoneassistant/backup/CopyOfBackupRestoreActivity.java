package com.android.phoneassistant.backup;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.provider.DBConstant;
import com.android.phoneassistant.util.Log;

public class CopyOfBackupRestoreActivity extends Activity implements OnClickListener {

    private static final String NAMESPACE = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_restore);
        Button button = null;
        button = (Button) findViewById(R.id.backup);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.restore);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.backup:
            backup();
            break;
        case R.id.restore:
            restore();
            break;
        }
    }

    private void backup() {
        Cursor c = null;
        int count = 0;
        try {
            c = getContentResolver().query(DBConstant.CONTACT_URI, null, null, null, DBConstant.CONTACT_UPDATE + " DESC");
            if (c == null) {
                return;
            }
            count = c.getCount();
            if (count <= 0) {
                return;
            }
            backupContacts(c);
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void backupContacts(Cursor c) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("utf-8",null);
            serializer.startTag(NAMESPACE, "phoneassistant");

            serializer.startTag(NAMESPACE, "count");
            serializer.text(String.valueOf(c.getCount()));
            serializer.endTag(NAMESPACE, "count");

            serializer.startTag(NAMESPACE, "contacts");
            c.moveToFirst();
            do {
                serializer.startTag(NAMESPACE, "contact");
                String name = c.getString(c.getColumnIndex(DBConstant.CONTACT_NAME));
                serializer.startTag(NAMESPACE, "name");
                serializer.text(String.valueOf(name));
                serializer.endTag(NAMESPACE, "name");

                int sex = c.getInt(c.getColumnIndex(DBConstant.CONTACT_SEX));
                serializer.startTag(NAMESPACE, "sex");
                serializer.text(String.valueOf(sex));
                serializer.endTag(NAMESPACE, "sex");

                int age = c.getInt(c.getColumnIndex(DBConstant.CONTACT_AGE));
                serializer.startTag(NAMESPACE, "age");
                serializer.text(String.valueOf(age));
                serializer.endTag(NAMESPACE, "name");

                String address = c.getString(c.getColumnIndex(DBConstant.CONTACT_ADDRESS));
                serializer.startTag(NAMESPACE, "address");
                serializer.text(String.valueOf(address));
                serializer.endTag(NAMESPACE, "address");

                String number = c.getString(c.getColumnIndex(DBConstant.CONTACT_NUMBER));
                serializer.startTag(NAMESPACE, "number");
                serializer.text(String.valueOf(number));
                serializer.endTag(NAMESPACE, "number");

                int call_log_count = c.getInt(c.getColumnIndex(DBConstant.CONTACT_CALL_LOG_COUNT));
                serializer.startTag(NAMESPACE, "name");
                serializer.text(String.valueOf(call_log_count));
                serializer.endTag(NAMESPACE, "call_log_count");

                int allow_record = c.getInt(c.getColumnIndex(DBConstant.CONTACT_ALLOW_RECORD));
                serializer.startTag(NAMESPACE, "allow_record");
                serializer.text(String.valueOf(allow_record));
                serializer.endTag(NAMESPACE, "allow_record");

                String state = c.getString(c.getColumnIndex(DBConstant.CONTACT_STATE));
                serializer.startTag(NAMESPACE, "state");
                serializer.text(String.valueOf(state));
                serializer.endTag(NAMESPACE, "state");

                long update = c.getLong(c.getColumnIndex(DBConstant.CONTACT_UPDATE));
                serializer.startTag(NAMESPACE, "update");
                serializer.text(String.valueOf(update));
                serializer.endTag(NAMESPACE, "update");

                int allow_modify = c.getInt(c.getColumnIndex(DBConstant.CONTACT_MODIFY_NAME));
                serializer.startTag(NAMESPACE, "allow_modify");
                serializer.text(String.valueOf(allow_modify));
                serializer.endTag(NAMESPACE, "allow_modify");
                serializer.endTag(NAMESPACE, "contact");
            } while (c.moveToNext());
            serializer.endTag(NAMESPACE, "contacts");
            serializer.endTag(NAMESPACE, "phoneassistant");
        } catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }
    
    private void backupRecord(XmlSerializer serializer, int id) {
        Cursor c = null;
        int count = 0;
        String selection = DBConstant.RECORD_CONTACT_ID + "=" + id;
        try {
            c = getContentResolver().query(DBConstant.RECORD_URI, null,
                    selection, null, DBConstant.RECORD_START + " DESC");
            if (c == null) {
                return;
            }
            count = c.getCount();
            if (count <= 0) {
                return;
            }
            serializer.startTag(NAMESPACE, "records");
            serializer.startTag(NAMESPACE, "count");
            serializer.text(String.valueOf(c.getCount()));
            serializer.endTag(NAMESPACE, "count");
            serializer.endTag(NAMESPACE, "phoneassistant");
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void restore() {

    }
    
    class BackupRestoreDialog extends Dialog {

        private TextView mDialogTitle;
        private TextView mStatusText;
        private ProgressBar mProgressBar;
        private boolean mBackup;

        public BackupRestoreDialog(Context context, boolean backup) {
            super(context);
            setCancelable(false);
            setCanceledOnTouchOutside(false);
            mBackup = backup;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.backup_restore_dlg);
            mDialogTitle = (TextView) findViewById(R.id.index_state);
            mDialogTitle = (TextView) findViewById(R.id.status_text);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mDialogTitle.setText(mBackup ? R.string.backup : R.string.restore);
        }
        
        public void setMax(int max) {
            mProgressBar.setMax(max);
        }

        public void mProgressBar(int progress) {
            mProgressBar.setProgress(progress);
        }
    }
}
