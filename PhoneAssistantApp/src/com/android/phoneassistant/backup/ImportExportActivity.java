package com.android.phoneassistant.backup;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class ImportExportActivity extends Activity implements OnClickListener,
        OnShowListener, Runnable, OnImportExportListener {

    private ImportExportDialog mImportExportDialog;
    private boolean mExport;
    private ExportHelper mExportHelper;
    private ImportHelper mImportHelper;
    private String mImportingFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_export);
        Button button = null;
        button = (Button) findViewById(R.id.export);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.import_);
        button.setOnClickListener(this);
        mImportHelper = new ImportHelper(this);
        mImportHelper.setOnImportExportListener(this);

        mExportHelper = new ExportHelper(this);
        mExportHelper.setOnImportExportListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.export: {
            mExport = true;
            mImportExportDialog = new ImportExportDialog(this, true);
            mImportExportDialog.setOnShowListener(this);
            mImportExportDialog.show();
        }
            break;
        case R.id.import_:
            mExport = false;
            importCallFile();
            break;
        }
    }


    @Override
    public void onShow(DialogInterface dialog) {
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (mExport) {
            mExportHelper.exportCallInfo();
        } else {
            Log.d(Log.TAG, "mImportingFile : " + mImportingFile);
            if (mImportingFile == null) {
                return;
            }
            File file = new File(mImportingFile);
            if (file.exists()) {
                mImportHelper.importCallInfo(mImportingFile);
            }
        }
    }

    @Override
    public void onStart(int totalCount) {
        mImportExportDialog.setMax(totalCount);
        mImportExportDialog.setProgress(0);
    }

    @Override
    public void onProcessing(String statusText) {
        mImportExportDialog.incrementProgress(statusText);
    }

    @Override
    public void onEnd() {
        mImportExportDialog.dismiss();
    }

    private void importCallFile() {
        final String importFiles[] = mImportHelper.queryImportFiles();
        if (importFiles == null || importFiles.length == 0) {
            Toast.makeText(this, R.string.no_import_file, Toast.LENGTH_SHORT).show();
            return ;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.imported_file);
        builder.setItems(importFiles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ImportExportActivity.this, importFiles[which],
                        Toast.LENGTH_SHORT).show();
                File recordDir = new File(Environment.getExternalStorageDirectory()
                        + "/" + Constant.FILE_RECORD_FOLDER);
                if (!recordDir.exists()) {
                    return;
                }
                mImportingFile = recordDir + "/" + importFiles[which];
                mImportExportDialog = new ImportExportDialog(
                        ImportExportActivity.this, false);
                mImportExportDialog
                        .setOnShowListener(ImportExportActivity.this);
                mImportExportDialog.show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    class ImportExportDialog extends Dialog {

        private TextView mIndexState;
        private TextView mStatusText;
        private ProgressBar mProgressBar;
        private boolean mBackup;

        public ImportExportDialog(Context context, boolean backup) {
            super(context);
            setCancelable(false);
            setCanceledOnTouchOutside(false);
            mBackup = backup;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.import_export_dlg);
            mIndexState = (TextView) findViewById(R.id.index_state);
            mStatusText = (TextView) findViewById(R.id.status_text);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            setTitle(mBackup ? R.string.export : R.string.import_);
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
                            mBackup ? R.string.exporting : R.string.importing,
                            text);
                    mStatusText.setText(statusText);
                    mIndexState.setText(String.valueOf(cur + "/" + max));
                }
            });
        }
    }
}
