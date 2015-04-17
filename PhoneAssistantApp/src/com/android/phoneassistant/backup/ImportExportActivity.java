package com.android.phoneassistant.backup;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import com.android.phoneassistant.R;
import com.android.phoneassistant.backup.ImportExportManager.WorkingState;
import com.android.phoneassistant.manager.FontManager;
import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Utils;

public class ImportExportActivity extends Activity implements OnClickListener,
        OnItemLongClickListener {

    private Button mExportButton;
    private Button mImportButton;
    private Button mDeleteButton;
    private ListView mListView;
    private String[] mExportZipFiles;
    private CheckedTextView mCheckedTextView;
    private ImportExportManager mImportExportManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_export);
        mCheckedTextView = (CheckedTextView) findViewById(android.R.id.text1);
        mCheckedTextView.setText(R.string.export_file_tip);
        mCheckedTextView.setTypeface(FontManager.get(this).getTTF());
        mCheckedTextView.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.export_file_list);
        mListView.setOnItemLongClickListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listExportFiles();
        mExportButton = (Button) findViewById(R.id.export);
        mExportButton.setOnClickListener(this);
        mExportButton.setTypeface(FontManager.get(this).getTTF());

        mImportButton = (Button) findViewById(R.id.import_);
        mImportButton.setOnClickListener(this);
        mImportButton.setTypeface(FontManager.get(this).getTTF());

        mDeleteButton = (Button) findViewById(R.id.delete_file);
        mDeleteButton.setOnClickListener(this);
        mDeleteButton.setTypeface(FontManager.get(this).getTTF());

        mImportExportManager = ImportExportManager.get(this);
        boolean working = mImportExportManager.isWorking();
        mExportButton.setEnabled(!working);
        mImportButton.setEnabled(!working);
        mDeleteButton.setEnabled(!working);
    }

    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Constant.ACTION_IMPORTING_EXPORING);
        registerReceiver(mBroadcastReceiver, filter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.export: {
            mImportExportManager.startExport();
        }
            break;
        case R.id.import_:
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

    private void importCallFile() {
        final String importFiles[] = queryImportFiles();
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
                String importingFile = recordDir + "/" + importFiles[which];
                mImportExportManager.startImport(importingFile);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return ;
            }
            int state = intent.getIntExtra("workingstate", WorkingState.NOWORKING.ordinal());
            WorkingState workingState = WorkingState.values()[state];
            // Log.d(Log.TAG, "workingState : " + workingState + " , state : " + state);
            if (workingState != WorkingState.NOWORKING) {
                mExportButton.setEnabled(false);
                mImportButton.setEnabled(false);
                mDeleteButton.setEnabled(false);
            } else {
                mExportButton.setEnabled(true);
                mImportButton.setEnabled(true);
                mDeleteButton.setEnabled(true);
            }
        }
    };
}
