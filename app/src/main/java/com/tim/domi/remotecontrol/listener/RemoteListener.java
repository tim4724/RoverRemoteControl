package com.tim.domi.remotecontrol.listener;

public interface RemoteListener {
    void onConnected();

    void onNotConnected();

    void pingUpdate(int ping);

    void failed(Exception e);
}
