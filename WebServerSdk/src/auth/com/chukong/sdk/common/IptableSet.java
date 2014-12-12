package com.chukong.sdk.common;

import android.util.Log;

public class IptableSet {

    // 清除所有规则
    public static final String NAT_RULES_CLEAR_ALL =
            "iptables -t nat -F";

    public static final String NAT_RULES_CLEAR_IP_CHECK =
            "iptables -t nat -F IP_CHECK";

    public static final String NAT_RULES_CLEAR_IP_CHAIN =
            "iptables -t nat -X IP_CHECK";

    public static final String NAT_RULES_DELETE_IP_CHECK_CHAIN =
            "iptables -t nat -D PREROUTING -j IP_CHECK";

    /*
    public static final String NAT_RULES_DELETE_REDIRECT =
            "iptables -t nat -D IP_CHECK 1";

    public static final String NAT_RULE_DELETE_DNS =
            "iptables -t nat -D IP_CHECK 2";
    */
    // 自定义规则链
    public static final String NAT_RULE_CREATE_IP_CHECK_CHAIN =
            "iptables -t nat -N IP_CHECK";

    // 将来自192.168.43.249：80的数据包重定向到本机的7766端口
    // -i ap0
    public static final String NAT_RULE_REDIRECT_HTTP_80 =
            //String.format("iptables -t nat -A IP_CHECK -p tcp -s %s --dport 80 -j REDIRECT --to-port 7766", "0.0.0.0/0");
            "iptables -t nat -A IP_CHECK -p tcp --dport 80 -j REDIRECT --to-port 7766";

    // 引用自定义规则到PREROUTING
    public static final String NAT_RULE_PREROUTING_IP_CHECK =
            "iptables -t nat -A PREROUTING -j IP_CHECK";

    // 列出所有的NAT规则
    public static final String NAT_RULE_LIST =
             "iptables -t nat -L -n";
    
    public static final String NAT_RULE_DNS_REDIRECT =
            "iptables -t nat -A IP_CHECK -p udp --dport 53 -j REDIRECT --to-port 7755";

    public static String generateClearIpRule() {
        String script = "";
        script += IptableSet.NAT_RULES_DELETE_IP_CHECK_CHAIN + "\n";
        script += IptableSet.NAT_RULES_CLEAR_IP_CHECK + "\n";
        script += IptableSet.NAT_RULES_CLEAR_IP_CHAIN + "\n";
        return script;
    }
    public static String generateIpCheckRule(String srcAddr) {
        String script = "";
        script += IptableSet.NAT_RULE_CREATE_IP_CHECK_CHAIN + "\n";
        //script += NAT_RULE_REDIRECT_HTTP_80 + "\n";
        script += String.format("iptables -t nat -A IP_CHECK -p tcp -s %s --dport 80 -j REDIRECT --to-port 7766\n", srcAddr);
        //script += NAT_RULE_DNS_REDIRECT + "\n";
        script += String.format("iptables -t nat -A IP_CHECK -p udp -s %s --dport 53 -j REDIRECT --to-port 7755\n", srcAddr);
        script += IptableSet.NAT_RULE_PREROUTING_IP_CHECK + "\n";
        return script;
    }
    private static String redirectHttp(String ipAddrMask) {
        String subAddress = null;
        int maskNumber = 0;
        try {
            String [] ipMask = ipAddrMask.split("\\/");
            int ipInt = bytesToInt(ipAddrToByte(ipMask[0]));
            maskNumber = Integer.valueOf(ipMask[1]);
            int mask = maskNumberToInt(maskNumber);
            int subNet = ipInt & mask;
            subAddress = intToIp(subNet);
        } catch(Exception e) {
            subAddress = null;
            maskNumber = 0;
            Log.d("taugin", "e = " + e);
        }
        if (subAddress != null && maskNumber != 0) {
            subAddress += "/";
            subAddress += maskNumber;
            return String.format("iptables -t nat -A IP_CHECK -p tcp -d \'!\' %s --dport 80 -j REDIRECT --to-port 7766", subAddress);
        }
        //  -i ap0
        return String.format("iptables -t nat -A IP_CHECK -p tcp -s %s --dport 80 -j REDIRECT --to-port 7766", ipAddrMask);
    }

    private static byte[] ipAddrToByte(String ipAddr) {
        byte[] ret = new byte[4];
        try {
            String[] ipArr = ipAddr.split("\\.");
            ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
            return ret;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }
    private static int maskNumberToInt(int mask) {
        int ipInt = 0xFFFFFFFF;
        ipInt = ipInt << (32 - mask);
        return ipInt;
    }
    public static String intToIp(int ipInt) {
        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.')
                .append((ipInt >> 16) & 0xff).append('.').append(
                        (ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff))
                .toString();
    }
}
