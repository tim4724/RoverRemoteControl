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
    private boolean connectedOnce;
    private final Sender sender;

    public ConnectionThread(byte speed, byte steering, Listener listener, int timeout) {
        sendData = new byte[2];
        sendData[0] = speed;
        sendData[1] = steering;
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
                if(socket.getInputStream().read() != 1) throw new IOException();
                connected();

                sender.start(socket);
                while (!interrupted() && !cancelled) {
                    if(socket.getInputStream().read() != 1) throw new IOException();
                }
            } catch (Exception e) {
                if (connectedOnce) {
                    connectionLost(e);
                    Util.sleep(250);
                } else {
                    connectionFailed(e);
                    cancel();
                }
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

    private void connected() {
        connectedOnce = true;
        Log.d(TAG, "Connected");
        listener.onConnected();
    }

    private void connectionLost(Exception e) {
        Log.d(TAG, "Connection lost " + e.getMessage());
        listener.onConnectionLost(e);
    }

    private void connectionFailed(Exception e) {
        Log.d(TAG, "Failed to connect " + e.getMessage());
        listener.onFailed(e);
    }

    public void newData(byte speed, byte steering) {
        sendData[0] = speed;
        sendData[1] = steering;
        sender.interrupt();
    }

    public void cancel() {
        cancelled = true;
        this.interrupt();
        sender.interrupt();
    }

    public interface Listener {
        void onConnected();

        void onConnectionLost(Exception e);

        void onFailed(Exception e);
    }
}
