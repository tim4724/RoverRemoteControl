package com.tim.domi.remotecontrol;

import android.view.View;

import java.io.Closeable;
import java.io.IOException;

public class Util {
    static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static void setVisibility(int visibility, View... views) {
        for (View v : views) {
            v.setVisibility(visibility);
        }
    }

    public static void setEnabled(boolean enable, View... views) {
        for (View v : views) {
            v.setEnabled(enable);
            if (enable) {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
}
