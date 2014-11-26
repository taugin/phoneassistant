package com.android.phoneassistant.info;

import com.android.phoneassistant.util.Log;

public class RecordInfo implements Comparable<RecordInfo> {
    public static int checkedNumber = 0;
    public int recordId;
    public boolean play;
    public String recordFile;
    public String recordName;
    public long recordSize;
    public long recordRing;
    public long recordStart;
    public long recordEnd;
    public int callFlag;
    public boolean checked;

    @Override
    public int compareTo(RecordInfo another) {
        Log.d(Log.TAG, "another.recordStart = " + another.recordStart + " , recordStart = " + recordStart);
        //return Long.valueOf(another.recordStart - recordStart).intValue();
        if (another.recordStart - recordStart > 0) {
            return 1;
        } else if (another.recordStart - recordStart < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
