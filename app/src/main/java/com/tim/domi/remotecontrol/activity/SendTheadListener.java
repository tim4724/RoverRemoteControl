package com.tim.domi.remotecontrol.activity;

import com.tim.domi.remotecontrol.ConnectionThread;

/**
 * Created by Tim on 08.03.2017.
 */

class SendTheadListener implements ConnectionThread.Listener {
    private final RoverControlActivity roverControlActivity;

    SendTheadListener(RoverControlActivity roverControlActivity) {
        this.roverControlActivity = roverControlActivity;
    }

    @Override
    public void onConnected() {
        roverControlActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roverControlActivity.connected();
            }
        });
    }

    @Override
    public void onConnectionLost(final Exception e) {
        roverControlActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roverControlActivity.connectionLost(e);
            }
        });
    }

    @Override
    public void onFailed(final Exception e) {
        roverControlActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roverControlActivity.connectionFailed(e);
            }
        });
    }
}
