package com.android.phonerecorder;

public class RecordInfo implements Comparable<RecordInfo> {
    public static int checkedNumber = 0;
    public boolean play;
    public String recordFile;
    public String recordName;
    public long recordSize;
    public long recordStart;
    public long recordEnd;
    public boolean incoming;
    public boolean checked;

    @Override
    public int compareTo(RecordInfo another) {
        return Long.valueOf(another.recordStart - recordStart).intValue();
    }
}
