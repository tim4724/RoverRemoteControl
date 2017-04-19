package com.tim.domi.remotecontrol;

import android.util.Log;

import com.tim.domi.remotecontrol.listener.RemoteListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static com.tim.domi.remotecontrol.Util.putInt;
import static com.tim.domi.remotecontrol.Util.readInt;
import static com.tim.domi.remotecontrol.Util.sleepUninterruptibly;

public class RemoteControl {
    private static final String TAG = "RemoteControl";

    private final RemoteListener listener;
    private DatagramSocket socket;
    private Sender sender;
    private Receiver receiver;

    public RemoteControl(RemoteListener listener) throws SocketException {
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
        private byte[] data = new byte[10];
        private int seqenceNr = 0;

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        new InetSocketAddress("192.168.13.38", 5005));
                Log.d(TAG, "try to connect to " + packet.getSocketAddress());
                while (!cancelled) {
                    putInt(seqenceNr++, data, 0);
                    putInt((int) System.currentTimeMillis(), data, 4);
                    socket.send(packet);

                    if (!cancelled) sleepUninterruptibly(100);//max send 10 packets a second
                    if (!interrupted() && !cancelled) Util.sleep(250);
                }
            } catch (Exception e) {
                errorOccurred(e);
            }
        }

        void newData(int speed, int steering) {
            data[8] = (byte) speed;
            data[9] = (byte) steering;
            this.interrupt();
        }
    }

    private class Receiver extends Thread {
        private boolean cancelled, connected;

        @Override
        public void run() {
            byte[] data = new byte[10];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            long connectedAt = System.currentTimeMillis();

            try {
                while (!cancelled) {
                    try {
                        socket.receive(packet);
                        connectedAt = System.currentTimeMillis();
                        updateConnState(true);
                        listener.pingUpdate(((int) connectedAt) - readInt(data, 4));
                    } catch (SocketTimeoutException e) {
                        updateConnState(false);
                        if (System.currentTimeMillis() - connectedAt > 4000) {
                            //no packet received for 4 seconds ->  rethrow sockettimeout exception
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                errorOccurred(e);
            }
        }

        private void updateConnState(boolean connected) {
            if (this.connected != connected) {
                this.connected = connected;
                if (connected) {
                    Log.d(TAG, "connected to rover");
                    listener.onConnected();
                } else {
                    Log.d(TAG, "not connected to rover");
                    listener.onNotConnected();
                }
            }
        }
    }

    private void errorOccurred(Exception e) {
        Log.e(TAG, "error occurred", e);
        listener.failed(e);
        cancel();
    }

    public void cancel() {
        Log.d(TAG, "cancel");
        if (sender != null) sender.cancelled = true;
        if (receiver != null) receiver.cancelled = true;
    }
}
