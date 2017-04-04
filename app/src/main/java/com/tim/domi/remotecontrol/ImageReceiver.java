package com.tim.domi.remotecontrol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ImageReceiver extends Thread {
    private static final String TAG = "ImageReceiver";
    private Listener listener;
    private boolean cancelled;
    private Bitmap bmp;
    private int lastSequenceReceived = -1;

    public ImageReceiver(Listener listener) {
        this.listener = listener;
    }

    public void run() {
        cancelled = false;
        byte[] data = new byte[1024];

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(100);
            DatagramPacket sendPacket = new DatagramPacket(data, 0, 8, InetAddress.getByName("tv_test.dd-dns.de"), 3843);
            DatagramPacket receivePacket = new DatagramPacket(data, 0, data.length);

            while (!cancelled) {
                try {
                    socket.send(sendPacket);
                    socket.receive(receivePacket);

                    int sequenceNr = Util.readInt(data, 0);
                    if (sequenceNr > lastSequenceReceived && !cancelled) {
                        lastSequenceReceived = sequenceNr;
                        bmp = BitmapFactory.decodeByteArray(data, 8, receivePacket.getLength());
                        listener.updateImage();
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            listener.failed(e);
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public Bitmap getBitmap() {
        return bmp;
    }

    public interface Listener {
        void updateImage();

        void failed(Exception e);
    }
}
