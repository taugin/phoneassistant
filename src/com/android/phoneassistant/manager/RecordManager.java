package com.android.phoneassistant.manager;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import com.android.phoneassistant.util.Constant;
import com.android.phoneassistant.util.Log;

public class RecordManager {
    private MediaRecorder mMediaRecorder;
    private boolean mRecording;
    private Context mContext;
    private static RecordManager sRecordManager;
    public static RecordManager getInstance(Context context) {
        if (sRecordManager == null) {
            sRecordManager = new RecordManager(context);
        }
        return sRecordManager;
    }
    private RecordManager(Context c) {
        mContext = c;
        mMediaRecorder = new MediaRecorder();
    }
    public synchronized void initRecorder(String fileName) {
        mRecording = false;
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File recordDir = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FILE_RECORD_FOLDER);
        if (!recordDir.exists()) {
            recordDir.mkdirs();
            File noMedia = new File(recordDir + "/.nomedia");
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                logv("create .nomedia file failure");
            }
        }
        mMediaRecorder.setOutputFile(fileName);
        logv("RecordManager prepare recorder file : " + fileName);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            logd("error : " + e);
            stopRecorder();
        } catch (IOException e) {
            logd("error : " + e);
            stopRecorder();
        }
    }
    public synchronized void startRecorder() {
        if (mMediaRecorder != null) {
            logv("RecordManager startRecorder");
            mMediaRecorder.start();
            mRecording = true;
        }
    }
    
    public synchronized void stopRecorder() {
        if (mMediaRecorder != null) {
            logv("RecordManager stopRecorder");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
//            mMediaRecorder.release();
            mRecording = false;
        }
    }

    public boolean recording() {
        return mRecording;
    }
    
    private void logd(String msg) {
        Log.d(Log.TAG, msg);
    }
    private void logv(String msg) {
        Log.v(Log.TAG, msg);
    }
    private void logw(String msg) {
        Log.w(Log.TAG, msg);
    }
}
