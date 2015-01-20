package com.android.phoneassistant.backup;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.util.Log;
import com.android.phoneassistant.util.Utils;

public class ImportExportActivity extends Activity implements OnClickListener,
        OnShowListener, Runnable, OnImportExportListener,
        OnItemLongClickListener {

    private ImportExportDialog mImportExportDialog;
    private boolean mExport;
    private ExportHelper mExportHelper;
    private ImportHelper mImportHelper;
    private String mImportingFile;
    private ListView mListView;
    private String[] mExportZipFiles;
    private CheckedTextView mCheckedTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_export);
        mCheckedTextView = (CheckedTextView) findViewById(android.R.id.text1);
        mCheckedTextView.setText(R.string.export_file_tip);
        mCheckedTextView.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.export_file_list);
        mListView.setOnItemLongClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listExportFiles();
        Button button = null;
        button = (Button) findViewById(R.id.export);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.import_);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.delete_file);
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
        case R.id.delete_file:
            deleteExportFile();
            break;
        case android.R.id.text1:
            mCheckedTextView.setChecked(!mCheckedTextView.isChecked());
            selectUnselectAll(mCheckedTextView.isChecked());
            break;
        }
    }

    private void selectUnselectAll(boolean select) {
        int count = mListView.getCount();
        for (int index = 0; index < count; index++) {
            mListView.setItemChecked(index, select);
        }
    }
    private void listExportFiles() {
        mExportZipFiles = queryImportFiles();
        mListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice,
                mExportZipFiles));
    }
    private void deleteExportFile() {
        SparseBooleanArray array = mListView.getCheckedItemPositions();
        int count = mListView.getCount();
        for (int index = 0; index < count; index++) {
            if (array.get(index)) {
                deleteFile(index);
            }
        }
        listExportFiles();
    }

    private void deleteFile(int position) {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return;
        }
        File exportFile = new File(recordDir + File.separator
                + mExportZipFiles[position]);
        if (exportFile.exists()) {
            exportFile.delete();
        }
    }

    public String[] queryImportFiles() {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return null;
        }
        String backupFiles[] = recorderFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".zip")) {
                    return true;
                }
                return false;
            }
        });
        return backupFiles;
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
                mImportHelper.unzipFile(mImportingFile);
                mImportHelper.importCallInfo();
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listExportFiles();
            }
        });
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
                String recordDir = Utils.getRecorderFolder();
                File recorderFile = new File(recordDir);
                if (!recorderFile.exists()) {
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

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        Intent intent = getShareIntent(mExportZipFiles[arg2]);
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return false;
    }

    private Intent getShareIntent(String zipFile) {
        String recordDir = Utils.getRecorderFolder();
        File recorderFile = new File(recordDir);
        if (!recorderFile.exists()) {
            return null;
        }

        File file = new File(recordDir + File.separator + zipFile);
        if (!file.exists()) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        Intent shareIntent = Intent.createChooser(intent, "分享");
        return shareIntent;
    }
}
