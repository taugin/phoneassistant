package com.android.phoneassistant.backup;

public interface OnImportExportListener {
    public void onStart(int totalCount);
    public void onProcessing(int index, String statusText);
    public void onEnd();
}
