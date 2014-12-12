package com.android.phoneassistant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RadioLogMatcher {
    private static final String CMDLINE = "logcat -d -b radio -v time";
    private static final int MAX_AGE = 7000;
    private static final String TAG = "ForwardedCall.RLMatcher";

    private static String[] MATCHES = new String[3];
    static {
        MATCHES[0] = "com.android.internal.telephony.gsm.SuppServiceNotification@";
        MATCHES[1] = "{mt,code=";
        MATCHES[2] = "+CSSU: 0";
    }
    
    public static long getLogAge(Date paramDate, String paramString)
            throws ParseException {
        Date localDate = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").parse(paramString);
        int year = paramDate.getYear();
        localDate.setYear(year);
        long time1 = paramDate.getTime();
        long time2 = localDate.getTime();
        return time1 - time2;
    }
    
    public static boolean isDivertedCall() {
        try {
            Date localDate = new Date();
            InputStream localInputStream = Runtime.getRuntime().exec("logcat -d -b radio -v time").getInputStream();
            InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream);
            BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);
            String str1 = null;
            while ((str1 = localBufferedReader.readLine()) != null) {
                int k = 0;
                if (str1 != null) {
                    String[] arrayOfString = MATCHES;
                    for (k = 0; k < arrayOfString.length; k++) {
                        if (str1.contains(arrayOfString[k])) {
                            long l = getLogAge(localDate, str1);
                            String str3 = "" + l + "ms: " + str1;
                            Log.d(Log.TAG, "str3 = " + str3);
                            if (l > 7000L) {
                                k++;
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String readRadioLog() {
      try {
        ArrayList<String> commandLine = new ArrayList<String>();
        commandLine.add("logcat");
        commandLine.add("-d");
        //commandLine.add("-b");
        //commandLine.add("radio");
        commandLine.add("-v");
        commandLine.add("time");
        InputStream inputStream = Runtime.getRuntime().exec(commandLine.toArray( new String[commandLine.size()])).getInputStream();
        //InputStream inputStream = Runtime.getRuntime().exec("logcat -d -v time -b radio").getInputStream();
        Log.d(Log.TAG, "localInputStream = " + inputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("------------- radio log -------------\n");
        String str1 = null;
        str1 = bufferedReader.readLine();
        Log.d(Log.TAG, "readRadioLog --------------------------------------------------------" + str1);
        while ((str1 = bufferedReader.readLine()) != null) {
          String str2 = str1.replaceAll("([0-9]{4})[0-9]{7}\\b", "\\1*******");
          stringBuilder.append(str2);
          stringBuilder.append("\n");
          Log.d(Log.TAG, stringBuilder.toString());
        }
        File file = new File("/sdcard/radio.log");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(stringBuilder.toString().getBytes());
        fos.close();
        return stringBuilder.toString();
      } catch (Exception e) {
          Log.d(Log.TAG, "error : " + e);
      }
      return null;
    }
}
