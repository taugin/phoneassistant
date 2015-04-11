package com.android.phoneassistant.provider;

import android.net.Uri;

public class DBConstant {

    public static final String AUTHORITIES = "com.android.phoneassistant";
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "phoneassistant.db";
    public static final String _ID = "_id";
    public static final String FOO = "foo";

    public static final String TABLE_RECORD = "record_table";
    public static final String RECORD_CONTACT_ID = "record_contact_id";
    public static final String RECORD_NAME = "record_name";
    public static final String RECORD_FILE = "record_file";
    public static final String RECORD_NUMBER = "record_number";
    public static final String RECORD_FLAG = "record_flag";
    public static final String RECORD_SIZE = "record_size";
    public static final String RECORD_RING = "record_ring";
    public static final String RECORD_START = "record_start";
    public static final String RECORD_END = "record_end";

    public static final int FLAG_NONE = 0;
    public static final int FLAG_INCOMING = 1;
    public static final int FLAG_MISSCALL = 2;
    public static final int FLAG_BLOCKCALL = 3;
    public static final int FLAG_OUTGOING = 4;

    public static final String RECORD_CONTENT_TYPE = "vnd.android.cursor.item/vnd.record.items";
    public static final String RECORD_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.record.item";

    public static final Uri RECORD_URI = Uri.parse("content://" + AUTHORITIES + "/" + TABLE_RECORD);


    public static final String TABLE_CONTACTS = "table_contacts";
    public static final String CONTACT_NAME = "contact_name";
    public static final String CONTACT_SEX = "contact_sex";
    public static final String CONTACT_AGE = "contact_age";
    public static final String CONTACT_ADDRESS = "contact_address";
    public static final String CONTACT_NUMBER = "contact_number";
    public static final String CONTACT_CALL_LOG_COUNT = "contact_call_log_count";
    public static final String CONTACT_ALLOW_RECORD = "contact_allow_record";
    public static final String CONTACT_STATE = "contact_state";
    public static final String CONTACT_UPDATE = "contact_update";
    public static final String CONTACT_MODIFY_NAME = "contact_allow_modify";
    
    public static final int MODIFY_NAME_ALLOW = 0;
    public static final int MODIFY_NAME_FORBID = 1;

    public static final int ALLOW_RECORD = 1;
    public static final int FORBID_RECORD = 0;

    public static final String CONTACT_CONTENT_TYPE = "vnd.android.cursor.item/vnd.contact.items";
    public static final String CONTACT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.contact.item";

    public static final Uri CONTACT_URI = Uri.parse("content://" + AUTHORITIES + "/" + TABLE_CONTACTS);
    
    public static final int BLOCK = 1;
    public static final int NO_BLOCK = 0;
    public static final String TABLE_BLOCK = "block_table";
    public static final String BLOCK_NAME = "block_name";
    public static final String BLOCK_NUMBER = "block_number";
    public static final String BLOCK_COUNT = "block_count";
    public static final String BLOCK_TIME = "block_time";
    public static final String BLOCK_CALL = "block_call";
    public static final String BLOCK_SMS = "block_sms";
    public static final String BLOCK_CONTENT = "block_content";
    public static final int BLOCK_TYPE_CALL = 0;
    public static final int BLOCK_TYPE_MMS = 1;

    public static final String BLOCK_CONTENT_TYPE = "vnd.android.cursor.item/vnd.block.items";
    public static final String BLOCK_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.block.item";

    public static final Uri BLOCK_URI = Uri.parse("content://" + AUTHORITIES + "/" + TABLE_BLOCK);
}
