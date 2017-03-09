package com.tim.domi.remotecontrol;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class ConnectionThread extends Thread {
    private static final String TAG = "ConnectionThread";

    private final byte[] sendData;
    private final Listener listener;
    private int timeout;

    private boolean cancelled;
    private final Sender sender;

    public ConnectionThread(Listener listener, int timeout) {
        sendData = new byte[2];
        this.listener = listener;
        this.timeout = timeout;

        sender = new Sender();
    }

    @Override
    public void run() {
        while (!cancelled) {
            Log.d(TAG, "Try to connect to server");

            try (Socket socket = new Socket("tv_test.dd-dns.de", 3842)) {
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(timeout);
                if (socket.getInputStream().read() != 1) throw new IOException("read error");

                Log.d(TAG, "Connected");
                listener.onConnected();

                sender.start(socket);
                while (!interrupted() && !cancelled) {
                    if (socket.getInputStream().read() != 1) throw new IOException("read error");
                }
            } catch (Exception e) {
                Log.d(TAG, "Not connected " + e.getMessage());
                listener.onNotConnected(e);
                Util.sleep(250);
            }
        }
    }

    private class Sender extends Thread {
        private Socket socket;

        void start(Socket socket) {
            this.socket = socket;
            super.start();
        }

        @Override
        public void run() {
            try {
                while (!cancelled) {
                    socket.getOutputStream().write(sendData);
                    if (!interrupted() && !cancelled) Util.sleep(250);
                }
            } catch (Exception e) {
                Util.close(socket);
            }
        }
    }

    public void newData(int speed, int steering) {
        sendData[0] = (byte) speed;
        sendData[1] = (byte) steering;
        sender.interrupt();
    }

    public void cancel() {
        cancelled = true;
        this.interrupt();
        sender.interrupt();
    }

    public interface Listener {
        void onConnected();

        void onNotConnected(Exception e);
    }
}
