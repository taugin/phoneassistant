package org.join.zxing.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chukong.sdk.R;
import com.chukong.sdk.common.Log;
import com.google.zxing.client.result.ParsedResultType;

public class BarCodeScanActivity extends Activity {

    private static final int REQ_CAPTURE = 0x0001;

    private String lastResult;
    private ParsedResultType mResultType;
    private String mType;
    private String mResult;

    private TextView mScanType;
    private TextView mScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_layout);
        mScanType = (TextView) findViewById(R.id.type);
        mScanResult = (TextView) findViewById(R.id.result);
        toCaptureActivity();
    }

    protected void onTitleRightClick() {
        Log.d(Log.TAG, "");
        toCaptureActivity();
    }

    private void toCaptureActivity() {
        try {
            Intent intent = new Intent(this, CaptureActivity.class);
            intent.setAction(Intents.Scan.ACTION);
            startActivityForResult(intent, REQ_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onCopy(View view) {
        copy2Clipboard(mResult);
    }

    public void onOpen(View view) {
        if (!TextUtils.isEmpty(mResult)) {
            toBrowserActivity(mResult);
        }
    }
    private void toBrowserActivity(String text) {
        Uri uri = null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (mResultType == ParsedResultType.TEL) {
            uri = Uri.parse("tel:" + text);
        } else if (mResultType == ParsedResultType.URI) {
            uri = Uri.parse(text);
            // intent.setPackage(getBrowserPackage("http://www.baidu.com"));
        } else if (mResultType == ParsedResultType.SMS) {
            int index = text.indexOf("\n");
            String phone = text.substring(0, index);
            String body = text.substring(index);
            uri = Uri.parse("smsto:" + phone);
            intent.putExtra("sms_body", body);
        } else {
            uri = Uri.parse("http://www.baidu.com/s?wd=" + Uri.encode(text));
            // intent.setPackage(getBrowserPackage("http://www.baidu.com"));
        }
        if (uri != null) {
            intent.setData(uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
    }

    @SuppressLint("NewApi")
    private void copy2Clipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setText(text);
        Toast.makeText(this, "Copy Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CAPTURE) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(Intents.Scan.RESULT);
                ParsedResultType type = ParsedResultType.values()[data
                        .getIntExtra(Intents.Scan.RESULT_TYPE,
                                ParsedResultType.TEXT.ordinal())];
                mResultType = type;
                mResult = result;
                mType = type.name();
                mScanType.setText(type.name());
                mScanResult.setText(result);
                Log.d(Log.TAG, "result : " + result);
                boolean isShow = false;
                toBrowserActivity(result);
                try {
                    if (type == ParsedResultType.URI) {
                        // toBrowserActivity(result);
                    } else {
                        isShow = true;
                    }
                } catch (ActivityNotFoundException e) {
                    isShow = true;
                } finally {
                    lastResult = result;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getBrowserPackage(String reportUrl) {
        PackageManager pm = getPackageManager();
        Intent queryIntent = new Intent();
        queryIntent.setAction(Intent.ACTION_VIEW);
        queryIntent.setData(Uri.parse(reportUrl));
        List<ResolveInfo> lists = pm.queryIntentActivities(queryIntent, 0);
        if (lists == null) {
            return "";
        }
        if (lists.size() > 0) {
            return lists.get(lists.size() - 1).activityInfo.packageName;
        }
        return "";
    }
}
