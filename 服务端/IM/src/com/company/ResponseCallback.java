package com.company;

/**
 * Created by winsion on 2017/5/8.
 */
public interface ResponseCallback {

    void targetIsOffline(DataProtocol reciveMsg);

    void targetIsOnline(String clientIp);
}
