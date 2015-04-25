package com.chukong.sdk.listener;

public interface OnNetworkListener {

    /**
     * @brief 网络连接了
     * @param isWifi 是否Wifi连接
     */
    void onConnected(boolean isWifi);

    /**
     * @brief 网络断开了
     */
    void onDisconnected();

}
