package com.android.phoneassistant.upgrade;

public class UpgradeInfo {
    public String app_url;
    public String app_name;
    public int version_code;
    public String version_name;
    public String release_time;
    public String instructions;

    public String toString() {
        String str = "\n";
        str += "app_url       : " + app_url + "\n";
        str += "app_name      : " + app_name + "\n";
        str += "version_code  : " + version_code + "\n";
        str += "version_name  : " + version_name + "\n";
        str += "release_time  : " + release_time + "\n";
        str += "instructions  : " + instructions + "\n";
        return str;
    }
}
