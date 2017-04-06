package com.tim.domi.remotecontrol;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RemoteControl {
    private static final String TAG = "RemoteControl";

    private final Listener listener;
    private DatagramSocket socket;
    private Sender sender;
    private Receiver receiver;

    public RemoteControl(Listener listener) throws SocketException {
        this.listener = listener;
        socket = new DatagramSocket();
        socket.setSoTimeout(1000);
    }

    public void start() {
        cancel();
        receiver = new Receiver();
        sender = new Sender();
        receiver.start();
        sender.start();
    }

    public void newData(int speed, int steering) {
        sender.newData(speed, steering);
    }

    private class Sender extends Thread {
        private boolean cancelled;
        private byte[] data = new byte[6];

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        new InetSocketAddress("192.168.13.38", 5005));
                Log.d(TAG, "try to connect to " + packet.getSocketAddress());
                while (!cancelled) {
                    Util.putInt((int) System.currentTimeMillis(), data, 0);
                    socket.send(packet);

                    if (!cancelled) Util.sleepUninterruptibly(100);//max send 10 packets a second
                    if (!interrupted() && !cancelled) Util.sleep(250);
                }
            } catch (Exception e) {
                errorOccurred(e);
            }
        }

        void newData(int speed, int steering) {
            data[4] = (byte) speed;
            data[5] = (byte) steering;
            this.interrupt();
        }
    }

    private class Receiver extends Thread {
        private boolean cancelled, connected;

        @Override
        public void run() {
            byte[] data = new byte[6];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            long connectedAt = System.currentTimeMillis();

            try {
                while (!cancelled) {
                    try {
                        socket.receive(packet);

                        updateConnState(true);
                        connectedAt = System.currentTimeMillis();
                        listener.pingUpdate(((int) connectedAt) - Util.readInt(packet.getData(), 0));
                    } catch (SocketTimeoutException e) {
                        updateConnState(false);
                        if (System.currentTimeMillis() - connectedAt > 4000) throw e;
                    }
                }
            } catch (Exception e) {
                errorOccurred(e);
            }
        }

        private void updateConnState(boolean connected) {
            if (this.connected != connected) {
                this.connected = connected;
                listener.updateConnState(connected);
                Log.d(TAG, (!connected ? "not " : "") + "connected to rover");
            }
        }
    }

    private void errorOccurred(Exception e) {
        Log.e(TAG, "error occurred", e);
        listener.failed(e);
        cancel();
    }

    public void cancel() {
        if (sender != null && receiver != null) {
            Log.d(TAG, "cancel");
            sender.cancelled = true;
            receiver.cancelled = true;
            sender = null;
            receiver = null;
        }
    }

    public interface Listener {
        void updateConnState(boolean connected);

        void pingUpdate(int ping);

        void failed(Exception e);
    }
}
