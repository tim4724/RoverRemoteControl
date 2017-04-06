package com.tim.domi.remotecontrol;

public class Util {
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleepUninterruptibly(long millis) {
        boolean interrupted = false;
        try {
            long end = System.currentTimeMillis() + millis;
            while (true) {
                try {
                    if (millis > 0) Thread.sleep(millis);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    millis = end - System.currentTimeMillis();
                }
            }
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    public static void putInt(int value, byte[] array, int offset) {
        array[offset] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte) (value & 255);
    }

    public static int readInt(byte[] value, int offset) {
        return value[offset] << 24 |
                (value[1 + offset] & 255) << 16 |
                (value[2 + offset] & 255) << 8 |
                value[3 + offset] & 255;
    }
}
