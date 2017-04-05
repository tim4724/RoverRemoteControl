package com.tim.domi.remotecontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class RemoteControl {
    private static final String TAG = "RemoteControl";

    private final Listener listener;
    private DatagramSocket socket;

    private Thread sender, receiver;
    private boolean connected;

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
        ((Sender) sender).newData(speed, steering);
    }

    private class Sender extends Thread {
        private boolean cancelled;
        private byte[] data = new byte[6];

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName("192.168.13.38"), 5005);
                while (!cancelled) {
                    try {
                        Util.putInt((int) System.currentTimeMillis(), data, 0);
                        socket.send(packet);
                    } catch (IOException e) {
                        errorOccurred(e);
                    }
                    if (!interrupted() && !cancelled) Util.sleep(250);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                listener.failed(e);
                cancel();
            }
        }

        void newData(int speed, int steering) {
            data[4] = (byte) speed;
            data[5] = (byte) steering;
            this.interrupt();
        }
    }

    private class Receiver extends Thread {
        private boolean cancelled;

        @Override
        public void run() {
            byte[] data = new byte[6];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            long connectedAt = System.currentTimeMillis();
            while (!cancelled) {
                try {
                    //receive
                    socket.receive(packet);
                    setConnected(true);
                    connectedAt = System.currentTimeMillis();
                    listener.pingUpdate(((int) connectedAt) - Util.readInt(packet.getData(), 0));
                } catch (SocketTimeoutException e) {
                    if (System.currentTimeMillis() - connectedAt > 5000) errorOccurred(e);
                    setConnected(false);
                    Util.sleep(25);
                } catch (IOException e) {
                    errorOccurred(e);
                    Util.sleep(25);
                }
            }
        }
    }

    private void errorOccurred(Exception e) {
        cancel();
        listener.updateConnState(false);
        listener.failed(e);
    }

    private void setConnected(boolean connected) {
        if (this.connected != connected) {
            this.connected = connected;
            listener.updateConnState(connected);
        }
    }

    public void cancel() {
        if (sender != null) {
            ((Sender) sender).cancelled = true;
            sender.interrupt();
        }
        if (receiver != null) {
            ((Receiver) receiver).cancelled = true;
            receiver.interrupt();
        }
        connected = false;
    }

    public interface Listener {
        void updateConnState(boolean connected);

        void pingUpdate(int ping);

        void failed(Exception e);
    }
}
