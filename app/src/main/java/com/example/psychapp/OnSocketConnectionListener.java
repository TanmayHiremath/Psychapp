package com.example.psychapp;

public interface OnSocketConnectionListener {
    void onSocketEventFailed();
    void onSocketConnectionStateChange(int socketState);
    void onInternetConnectionStateChange(int socketState);
}
