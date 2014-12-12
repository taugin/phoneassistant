package com.chukong.sdk;

import com.chukong.sdk.util.CommonUtil;

import android.os.Environment;

/**
 * @brief 应用设置常量
 * @author join
 */
public final class Constants {

    public static String APP_DIR_NAME = "/.ws/";
    public static String APP_DIR = null;
    static {
        APP_DIR = CommonUtil.getSingleton().getAppDir() + APP_DIR_NAME;
    }

    public static class Config {
        public static final boolean DEV_MODE = false;

        public static int PORT = 7766;
        //public static String WEBROOT = "/";
        public static String WEBROOT = CommonUtil.getSingleton().getGameDir();

        /** 服务资源文件 */
        public static final String SERV_ROOT_DIR = APP_DIR + "root/";

        /** 渲染模板目录 */
        public static final String SERV_TEMP_DIR = SERV_ROOT_DIR + "temp/";

        /** 统一编码 */
        public static final String ENCODING = "UTF-8";

        /** 是否允许下载 */
        public static boolean ALLOW_DOWNLOAD = true;
        /** 是否允许删除 */
        public static boolean ALLOW_DELETE = true;
        /** 是否允许上传 */
        public static boolean ALLOW_UPLOAD = true;

        /** The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file. */
        public static final int THRESHOLD_UPLOAD = 1024 * 1024; // 1MB

        /** 是否使用GZip */
        public static boolean USE_GZIP = false;
        /** GZip扩展名 */
        public static final String EXT_GZIP = ".gz"; // used in cache

        /** 是否使用文件缓存 */
        public static boolean USE_FILE_CACHE = false;
        /** 文件缓存目录 */
        public static final String FILE_CACHE_DIR = APP_DIR + "cache/";

        public static boolean SHOW_INSTALLED_APP = true;

        /** 缓冲字节长度=1024*4B */
        public static final int BUFFER_LENGTH = 4096;
    }

    public static final int OP_START_WEBSERVER = 0;
    public static final int OP_STOP_WEBSERVER = 1;
    public static final int OP_START_DNSSERVER = 2;
    public static final int OP_STOP_DNSSERVER = 3;
    public static final int OP_QUERY_WEBSERVER = 4;
    public static final String REDIRECT_STATUS = "redirect_started";

    public static final String KEY_SAVED_SSID = "key_saved_ssid";
    public static final String KEY_SAVED_PASS = "key_saved_pass";
    public static final String KEY_SECURITY_TYPE = "key_security_type";

    public static final String IPTABLES_SUCCESS = "iptables_execute_successfully";
}
