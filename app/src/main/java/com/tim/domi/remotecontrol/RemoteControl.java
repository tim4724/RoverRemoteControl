package com.tim.domi.remotecontrol;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class RemoteControl {
    private static final String TAG = "RemoteControl";

    private final Listener listener;
    private DatagramSocket socket;
    private ConnectionState state;

    private Thread sender, receiver;

    public RemoteControl(Listener listener) throws SocketException {
        this.listener = listener;
        socket = new DatagramSocket();
        socket.setSoTimeout(1000);
    }

    public void start() {
        cancel();
        state = ConnectionState.NOT_CONNECTED;
        receiver = new Receiver(5000);
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
                DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName("tv_test.dd-dns.de"), 3842);
                while (!cancelled) {
                    try {
                        Util.putInt((int) System.currentTimeMillis(), data, 2);
                        socket.send(packet);
                    } catch (IOException e) {
                        errorOccurred(e);
                    }
                    if (!interrupted() && !cancelled) Util.sleep(150);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                listener.failed(e);
                cancel();
            }
        }

        void newData(int speed, int steering) {
            data[0] = (byte) speed;
            data[1] = (byte) steering;
            this.interrupt();
        }
    }

    private class Receiver extends Thread {
        private boolean cancelled;
        private final int connectTimeout;

        Receiver(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        @Override
        public void run() {
            byte[] data = new byte[10];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            long connectedAt = System.currentTimeMillis();
            while (!cancelled) {
                try {
                    //check for connection timeout
                    if (connectedAt + connectTimeout < System.currentTimeMillis()) {
                        listener.failed(new IOException("Timout while trying to connect"));
                        cancel();
                    }
                    //receive
                    socket.receive(packet);
                    state = parse(packet.getData());
                    if (state == ConnectionState.CONNECTED)
                        connectedAt = System.currentTimeMillis();
                } catch (IOException e) {
                    errorOccurred(e);
                    Util.sleep(25);
                }
            }
        }

        private ConnectionState parse(byte[] data) {
            int pingToServer = ((int) System.currentTimeMillis()) - Util.readInt(data, 2);
            int pingServerRover = Util.readInt(data, 6);
            Log.d(TAG, "Ping to server: " + pingToServer + ";\t ping server rover: " + pingServerRover);

            ConnectionState newState = (pingServerRover == -1) ? ConnectionState.WAITING_FOR_ROVER : ConnectionState.CONNECTED;
            if (newState != state) {
                Log.d(TAG, newState.name());
                listener.updateConnState(newState);
            }
            if (newState == ConnectionState.CONNECTED)
                listener.pingUpdate(pingToServer + pingServerRover);
            return newState;
        }
    }

    private void errorOccurred(Exception e) {
        state = ConnectionState.NOT_CONNECTED;
        Log.d(TAG, state.name() + ' ' + e.getMessage());
        listener.updateConnState(state);
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
    }

    public interface Listener {
        void updateConnState(ConnectionState state);

        void pingUpdate(int ping);

        void failed(Exception e);
    }

    public enum ConnectionState {
        NOT_CONNECTED, WAITING_FOR_ROVER, CONNECTED
    }
}
