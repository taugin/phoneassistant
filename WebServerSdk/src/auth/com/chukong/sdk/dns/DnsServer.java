package com.chukong.sdk.dns;


import com.chukong.sdk.common.Log;
import com.chukong.sdk.util.CommonUtil;

public class DnsServer extends Thread {
    private static boolean isShutDown = false;
    public void run() {
        String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
        Log.d(Log.TAG, "localAddress = " + localAddress);
        UDPSocketMonitor monitor = new UDPSocketMonitor(localAddress, 7755);
        monitor.start();
        
        while (!isShutDown) {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
