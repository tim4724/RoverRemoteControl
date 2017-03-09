package com.tim.domi.remotecontrol;

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
}
