package com.tim.domi.remotecontrol;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Connection {
    private static final String TAG = "Connection";

    private final Listener listener;
    private final DatagramSocket socket;
    private final DatagramPacket sendPacket;
    private ConnectionState state;

    private Thread sender, receiver;

    public Connection(Listener listener) throws SocketException, UnknownHostException {
        this.listener = listener;
        sendPacket = new DatagramPacket(new byte[6], 0, 6, InetAddress.getByName("192.168.13.44"), 3842);
        socket = new DatagramSocket();
        socket.setSoTimeout(1000);
    }

    public void start() {
        cancel();
        state = ConnectionState.NOT_CONNECTED;
        receiver = new Receiver();
        sender = new Sender();
        receiver.start();
        sender.start();
    }

    public void newData(int speed, int steering) {
        sendPacket.getData()[0] = (byte) speed;
        sendPacket.getData()[1] = (byte) steering;
        if (sender != null) sender.interrupt();
    }

    private class Sender extends Thread {
        private boolean cancelled;

        @Override
        public void run() {
            while (!cancelled) {
                try {
                    Util.putInt((int) System.currentTimeMillis(), sendPacket.getData(), 2);
                    socket.send(sendPacket);
                } catch (IOException e) {
                    errorOccurred(e);
                }
                if (!interrupted() && !cancelled) Util.sleep(50);
            }
        }
    }

    private class Receiver extends Thread {
        private boolean cancelled;

        @Override
        public void run() {
            byte[] data = new byte[10];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            while (!cancelled) {
                try {
                    socket.receive(packet);
                    parse(packet.getData());
                } catch (IOException e) {
                    errorOccurred(e);
                    Util.sleep(25);
                }
            }
        }

        private void parse(byte[] data) {
            int pingToServer = ((int) System.currentTimeMillis()) - Util.readInt(data, 2);
            int pingServerRover = Util.readInt(data, 6);
            Log.d(TAG, "Ping to server: " + pingToServer + "; ping server rover: " + pingServerRover);

            if (pingServerRover == -1) {
                if (state != ConnectionState.WAITING_FOR_ROVER) {
                    Log.d(TAG, "Connected to server; waiting for rover");
                    state = ConnectionState.WAITING_FOR_ROVER;
                    listener.updateConnState(state);
                }
            } else {
                if (state != ConnectionState.CONNECTED) {
                    Log.d(TAG, "Connected to rover");
                    state = ConnectionState.CONNECTED;
                    listener.updateConnState(state);
                }
                listener.pingUpdate(pingToServer + pingServerRover);
            }
        }
    }

    private void errorOccurred(Exception e) {
        Log.d(TAG, "Not connected " + e.getMessage());
        state = ConnectionState.NOT_CONNECTED;
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
    }

    public enum ConnectionState {
        NOT_CONNECTED, WAITING_FOR_ROVER, CONNECTED
    }
}
