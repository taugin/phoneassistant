package com.android.phoneassistant.info;


public class ContactInfo {
    public int _id;
    public String contactName;
    public int contactSex;
    public int contactAge;
    public String contactAddress;
    public String contactNumber;
    public int contactLogCount;
    public long contactUpdate;
    public int contactAllowRecord;
    public int contactState;
    public boolean contactModifyName;
    public boolean checked;
    public boolean blocked;
    
    public String toString() {
        return contactNumber;
    }
}
