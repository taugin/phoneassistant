package com.android.phoneassistant.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

import com.android.phoneassistant.provider.DBConstant;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class AttributionQuery implements Listener<String>, ErrorListener{

    private Context mContext;
    private RequestQueue mQueue;
    private int mId = -1;
    public AttributionQuery(Context context) {
        mContext = context;
        mQueue = Volley.newRequestQueue(mContext);
        mQueue.start();
    }

    public void query(int _id, String phoneNumber) {
        mId = _id;
        String url = "http://life.tenpay.com/cgi-bin/mobile/MobileQueryAttribution.cgi?chgmobile=" + phoneNumber;
        StringRequest request = new StringRequest(url, this, this){

            @Override
            protected Response<String> parseNetworkResponse(
                    NetworkResponse response) {
                String parsed;
                try {
                    String charset = HttpHeaderParser.parseCharset(response.headers);
                    Log.d(Log.TAG, "charset : " + charset);
                    parsed = new String(response.data, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
            
        };
        mQueue.add(request);
    }

    @Override
    public void onResponse(String response) {
        if (TextUtils.isEmpty(response)) {
            return ;
        }
        Log.d(Log.TAG, "response : " + response);
        String attri = xmlParser(response);
        Log.d(Log.TAG, "attri : " + attri);
        Uri uri = ContentUris.withAppendedId(DBConstant.CONTACT_URI, mId);
        ContentValues values = new ContentValues();
        values.put(DBConstant.CONTACT_ATTRIBUTION, attri);
        mContext.getContentResolver().update(uri, values, null, null);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(Log.TAG, "error : " + error);
    }

    private String xmlParser(String document) {
        XmlPullParser xmlPullparser = Xml.newPullParser();
        String province = "";
        String city = "";
        String supplier = "";
        try {
            xmlPullparser.setInput(new StringReader(document));
            int eventType = xmlPullparser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = xmlPullparser.getName();
                    if (name.equals("province")) {
                        province = xmlPullparser.nextText();
                    } else if (name.equals("city")) {
                        city = xmlPullparser.nextText();
                    } else if (name.equals("supplier")) {
                        supplier = xmlPullparser.nextText();
                    }
                }
                eventType = xmlPullparser.next();
            }
        } catch (XmlPullParserException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return province + city + supplier;
    }
}
