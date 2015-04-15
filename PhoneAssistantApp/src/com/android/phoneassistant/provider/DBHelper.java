package com.android.phoneassistant.provider;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.phoneassistant.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String CREATE_CONTACT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DBConstant.TABLE_CONTACTS
          + "("
          + DBConstant._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
          + DBConstant.CONTACT_NAME + " TEXT,"
          + DBConstant.CONTACT_SEX + " INTEGER,"
          + DBConstant.CONTACT_AGE + " INTEGER,"
          + DBConstant.CONTACT_ADDRESS + " TEXT,"
          + DBConstant.CONTACT_NUMBER + " TEXT UNIQUE,"
          + DBConstant.CONTACT_CALLLOG_COUNT + " INTEGER DEFAULT 0,"
          + DBConstant.CONTACT_ALLOW_RECORD + " INTEGER DEFAULT 1,"
          + DBConstant.CONTACT_STATE + " TEXT,"
          + DBConstant.CONTACT_UPDATE + " LONG DEFAULT 0,"
          + DBConstant.CONTACT_MODIFY_NAME + " INTEGER DEFAULT 0,"
          + DBConstant.FOO + " text"
          + ")";
    private static final String DROP_CONTACT_TABLE = "DROP TABLE " + DBConstant.TABLE_CONTACTS + " IF EXISTS";

    private static final String TRIGGER_ON_DELETE_CONTACT =
            "CREATE TRIGGER DELETE_CONTACT_TRIGGER AFTER DELETE ON " + DBConstant.TABLE_CONTACTS
          + " FOR EACH ROW "
          + " BEGIN "
          + " DELETE FROM " + DBConstant.TABLE_RECORD
          + " WHERE " + DBConstant.TABLE_RECORD + "." + DBConstant.RECORD_CONTACT_ID + "=" + "OLD." + DBConstant._ID + ";"
          + " END;";
    private static final String DROP_TRIGGER_CONTACT = "DROP TRIGGER DELETE_CONTACT_TRIGGER";

    private static final String CREATE_RECORD_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DBConstant.TABLE_RECORD
          + "("
          + DBConstant._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
          + DBConstant.RECORD_CONTACT_ID + " INTEGER REFERENCES " + DBConstant.TABLE_CONTACTS + "(" + DBConstant._ID + "),"
          + DBConstant.RECORD_NAME + " TEXT UNIQUE,"
          + DBConstant.RECORD_FILE + " TEXT,"
          + DBConstant.RECORD_NUMBER + " TEXT,"
          + DBConstant.RECORD_FLAG + " INTEGER DEFAULT 0,"
          + DBConstant.RECORD_SIZE + " LONG DEFAULT 0,"
          + DBConstant.RECORD_RING + " LONG DEFAULT 0,"
          + DBConstant.RECORD_START + " LONG DEFAULT 0,"
          + DBConstant.RECORD_END + " LONG DEFAULT 0,"
          + DBConstant.FOO + " text"
          + ")";
    private static final String DROP_RECORD_TABLE = "DROP TABLE " + DBConstant.TABLE_RECORD + " IF EXISTS";

    private static final String CREATE_BLOCK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DBConstant.TABLE_BLOCK
          + "("
          + DBConstant._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
          + DBConstant.BLOCK_NAME + " TEXT,"
          + DBConstant.BLOCK_NUMBER + " TEXT UNIQUE,"
          + DBConstant.BLOCK_CALL_COUNT + " INTEGER DEFAULT 0,"
          + DBConstant.BLOCK_SMS_COUNT + " INTEGER DEFAULT 0,"
          + DBConstant.BLOCK_CALL + " INTEGER DEFAULT 0,"
          + DBConstant.BLOCK_SMS + " INTEGER DEFAULT 0,"
          + DBConstant.FOO + " text"
          + ")";
    private static final String DROP_BLOCK_TABLE = "DROP TABLE " + DBConstant.TABLE_BLOCK + " IF EXISTS";

    private static final String TRIGGER_ON_DELETE_RECORD =
            "CREATE TRIGGER DELETE_RECORD_TRIGGER AFTER DELETE ON " + DBConstant.TABLE_RECORD
          + " FOR EACH ROW "
          + " BEGIN "
          + " UPDATE " + DBConstant.TABLE_CONTACTS + " SET " + DBConstant.CONTACT_CALLLOG_COUNT + "=" + DBConstant.CONTACT_CALLLOG_COUNT + "-1 "
          + " WHERE " + DBConstant.TABLE_CONTACTS + "." + DBConstant._ID + "=" + "OLD." + DBConstant.RECORD_CONTACT_ID + ";"
          + " END;";
    private static final String DROP_TRIGGER_RECORD = "DROP TRIGGER DELETE_RECORD_TRIGGER";

    private static final String CREATE_BLOCK_DETAIL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DBConstant.TABLE_BLOCK_DETAIL
          + "("
          + DBConstant._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
          + DBConstant.BLOCK_ID + " INTEGER DEFAULT 0,"
          + DBConstant.BLOCK_DETAIL_NUMBER + " TEXT,"
          + DBConstant.BLOCK_DETAIL_TIME + " LONG DEFAULT 0,"
          + DBConstant.BLOCK_DETAIL_SMS + " TEXT,"
          + DBConstant.BLOCK_CALL_TYPE + " INTEGER DEFAULT 0,"
          + DBConstant.BLOCK_SMS_TYPE + " INTEGER DEFAULT 0,"
          + DBConstant.FOO + " text"
          + ")";

    private static final String DROP_BLOCK_DETAIL_TABLE = "DROP TABLE " + DBConstant.TABLE_BLOCK_DETAIL + " IF EXISTS";

    private static final String TRIGGER_ON_INSERT_BLOCKCALL =
            "CREATE TRIGGER INSERT_BLOCKCALL_TRIGGER AFTER UPDATE OF " + DBConstant.BLOCK_CALL_TYPE + " ON " + DBConstant.TABLE_BLOCK_DETAIL
          + " FOR EACH ROW "
          + " BEGIN "
          + " UPDATE " + DBConstant.TABLE_BLOCK + " SET " + DBConstant.BLOCK_CALL_COUNT + "=" + DBConstant.BLOCK_CALL_COUNT + "+1 "
          + " WHERE " + DBConstant.TABLE_BLOCK + "." + DBConstant._ID + "=" + "NEW." + DBConstant.BLOCK_ID
          + " AND " + "NEW." + DBConstant.BLOCK_CALL_TYPE + "=" + DBConstant.BLOCK_FLAG + ";"
          + " END;";
    private static final String DROP_TRIGGER_BLOCKCALL = "DROP TRIGGER INSERT_BLOCKCALL_TRIGGER";

    private static final String TRIGGER_ON_INSERT_BLOCKSMS =
            "CREATE TRIGGER INSERT_BLOCKSMS_TRIGGER AFTER UPDATE OF " + DBConstant.BLOCK_SMS_TYPE + " ON " + DBConstant.TABLE_BLOCK_DETAIL
          + " FOR EACH ROW "
          + " BEGIN "
          + " UPDATE " + DBConstant.TABLE_BLOCK + " SET " + DBConstant.BLOCK_SMS_COUNT + "=" + DBConstant.BLOCK_SMS_COUNT + "+1 "
          + " WHERE " + DBConstant.TABLE_BLOCK + "." + DBConstant._ID + "=" + "NEW." + DBConstant.BLOCK_ID
          + " AND " + "NEW." + DBConstant.BLOCK_SMS_TYPE + "=" + DBConstant.BLOCK_FLAG + ";"
          + " END;";
    private static final String DROP_TRIGGER_BLOCKSMS = "DROP TRIGGER INSERT_BLOCKSMS_TRIGGER";

    private static final String TRIGGER_ON_DELETE_BLOCK =
            "CREATE TRIGGER DELETE_BLOCK_TRIGGER AFTER DELETE ON " + DBConstant.TABLE_BLOCK
          + " FOR EACH ROW "
          + " BEGIN "
          + " DELETE FROM " + DBConstant.TABLE_BLOCK_DETAIL
          + " WHERE " + DBConstant.TABLE_BLOCK_DETAIL + "." + DBConstant.BLOCK_ID + "=" + "OLD." + DBConstant._ID + ";"
          + " END;";
    private static final String DROP_TRIGGER_BLOCK = "DROP TRIGGER DELETE_BLOCK_TRIGGER";

    /**
     * sqlite do not support if else trigger
    private static final String TRIGGER_ON_INSERT_BLOCK =
            "CREATE TRIGGER INSERT_BLOCK_TRIGGER AFTER INSERT ON " + DBConstant.TABLE_BLOCK_DETAIL
          + " FOR EACH ROW "
          + " BEGIN "
          + " IF :NEW." + DBConstant.BLOCK_DETAIL_TYPE + "=" + DBConstant.BLOCK_TYPE_SMS + " THEN "
          + " UPDATE " + DBConstant.TABLE_BLOCK + " SET " + DBConstant.BLOCK_SMS_COUNT + "=" + DBConstant.BLOCK_SMS_COUNT + "+1 "
          + " WHERE " + DBConstant.TABLE_BLOCK + "." + DBConstant._ID + "=" + "NEW." + DBConstant.BLOCK_ID
          + " AND " + "NEW." + DBConstant.BLOCK_DETAIL_TYPE + "=" + DBConstant.BLOCK_TYPE_SMS + ";"
          + " ELSE "
          + " UPDATE " + DBConstant.TABLE_BLOCK + " SET " + DBConstant.BLOCK_CALL_COUNT + "=" + DBConstant.BLOCK_CALL_COUNT + "+1 "
          + " WHERE " + DBConstant.TABLE_BLOCK + "." + DBConstant._ID + "=" + "NEW." + DBConstant.BLOCK_ID
          + " AND " + "NEW." + DBConstant.BLOCK_DETAIL_TYPE + "=" + DBConstant.BLOCK_TYPE_CALL + ";"
          + " ENDIF; "
          + " END;";
    private static final String DROP_TRIGGER_BLOCK = "DROP TRIGGER INSERT_BLOCK_TRIGGER";
    */
    private Context mContext;

    public DBHelper(Context context) {
        super(context, DBConstant.DB_NAME, null, DBConstant.DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(CREATE_CONTACT_TABLE);
            db.execSQL(CREATE_RECORD_TABLE);
            db.execSQL(CREATE_BLOCK_TABLE);
            db.execSQL(CREATE_BLOCK_DETAIL_TABLE);

            db.execSQL(TRIGGER_ON_DELETE_RECORD);
            db.execSQL(TRIGGER_ON_INSERT_BLOCKCALL);
            db.execSQL(TRIGGER_ON_INSERT_BLOCKSMS);
            db.execSQL(TRIGGER_ON_DELETE_BLOCK);
            db.execSQL(TRIGGER_ON_DELETE_CONTACT);
        }catch(SQLException e){
            Log.d(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion){
            try{
                db.execSQL(DROP_RECORD_TABLE);
                db.execSQL(DROP_CONTACT_TABLE);
                db.execSQL(DROP_BLOCK_TABLE);
                db.execSQL(DROP_BLOCK_DETAIL_TABLE);
                db.execSQL(DROP_TRIGGER_RECORD);
                db.execSQL(DROP_TRIGGER_BLOCKCALL);
                db.execSQL(DROP_TRIGGER_BLOCKSMS);
                db.execSQL(DROP_TRIGGER_BLOCK);
                db.execSQL(DROP_TRIGGER_CONTACT);
            } catch(SQLException e){
                e.printStackTrace();
            } finally{
                onCreate(db);
            }
        }
    }

}
