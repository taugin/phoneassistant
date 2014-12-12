package com.android.phoneassistant.webserver.listener;

public interface OnStorageListener {

    /**
     * 挂载
     */
    void onMounted();

    /**
     * 未挂载
     */
    void onUnmounted();

}
