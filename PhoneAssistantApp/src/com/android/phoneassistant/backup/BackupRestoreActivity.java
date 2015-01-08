package com.android.phoneassistant.backup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.phoneassistant.R;
import com.android.phoneassistant.backup.BackupHelper.OnBackupListener;

public class BackupRestoreActivity extends Activity implements OnClickListener,
        OnShowListener, Runnable, OnBackupListener {

    private BackupRestoreDialog mBackupRestoreDialog;
    private boolean mBackup;
    private BackupHelper mBackupHelper;
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
        case R.id.backup: {
            mBackup = true;
            mBackupRestoreDialog = new BackupRestoreDialog(this, true);
            mBackupRestoreDialog.setOnShowListener(this);
            mBackupRestoreDialog.show();
        }
            break;
        case R.id.restore:
            restore();
            break;
        }
    }


    @Override
    public void onShow(DialogInterface dialog) {
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (mBackup) {
            mBackupHelper = new BackupHelper(this);
            mBackupHelper.setOnBackupListener(this);
            mBackupHelper.backup();
        }
    }

    @Override
    public void onBackupStart(int totalCount) {
        mBackupRestoreDialog.setMax(totalCount);
        mBackupRestoreDialog.setProgress(0);
    }

    @Override
    public void onBackupProcessing(String statusText) {
        mBackupRestoreDialog.incrementProgress(statusText);
    }

    @Override
    public void onBackupEnd() {
        mBackupRestoreDialog.dismiss();
    }

    private void restore() {
    }
    
    class BackupRestoreDialog extends Dialog {

        private TextView mIndexState;
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
            mIndexState = (TextView) findViewById(R.id.index_state);
            mStatusText = (TextView) findViewById(R.id.status_text);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            setTitle(mBackup ? R.string.backup : R.string.restore);
        }
        
        public void setMax(final int max) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setMax(max);
                }
            });
        }

        public void setProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(progress);
                }
            });
        }

        public void incrementProgress(final String text) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgressBar.incrementProgressBy(1);
                    int max = mProgressBar.getMax();
                    int cur = mProgressBar.getProgress();
                    String statusText = getResources().getString(
                            mBackup ? R.string.backuping : R.string.restoring,
                            text);
                    mStatusText.setText(statusText);
                    mIndexState.setText(String.valueOf(cur + "/" + max));
                }
            });
        }
    }
}
