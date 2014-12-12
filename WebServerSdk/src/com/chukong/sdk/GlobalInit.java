package com.chukong.sdk;

import java.io.IOException;

import net.asfun.jangod.lib.TagLibrary;
import net.asfun.jangod.lib.tag.ResColorTag;
import net.asfun.jangod.lib.tag.ResStrTag;
import net.asfun.jangod.lib.tag.UUIDTag;
import android.content.Context;
import android.util.AndroidRuntimeException;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.serv.TempCacheFilter;
import com.chukong.sdk.util.CopyUtil;

public class GlobalInit {

    private Context mContext;
    private static GlobalInit sGlobalInit = null;
    private boolean mLocalShare = false;

    public static GlobalInit getInstance() {
        if (sGlobalInit == null) {
            throw new AndroidRuntimeException("GlobalInit should be called first");
        }
        return sGlobalInit;
    }
    public GlobalInit(Context context) {
        sGlobalInit = this;
        mContext = context;
    }

    public Context getBaseContext() {
        return mContext;
    }

    public void setLocalShare(boolean local) {
        mLocalShare = local;
    }

    public boolean getLocalShare() {
        return mLocalShare;
    }

    public void init() {
        initAppDir();
        initJangod();
        initAppFilter();
    }
    /**
     * @brief 初始化应用目录
     */
    private void initAppDir() {
        CopyUtil mCopyUtil = new CopyUtil(mContext);
        // mCopyUtil.deleteFile(new File(Config.SERV_ROOT_DIR)); // 清理服务文件目录
        try {
            // 重新复制到SDCard，仅当文件不存在时
            mCopyUtil.assetsCopy("ws", Config.SERV_ROOT_DIR, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @brief 初始化Jangod，添加自定义内容
     */
    private void initJangod() {
        /* custom tags */
        TagLibrary.addTag(new ResStrTag());
        TagLibrary.addTag(new ResColorTag());
        TagLibrary.addTag(new UUIDTag());
        /* custom filters */
    }

    /**
     * @brief 初始化应用过滤器
     */
    private void initAppFilter() {
        /* TempCacheFilter */
        TempCacheFilter.addCacheTemps("403.html", "404.html", "503.html");
        /* GzipFilter */
    }
}
