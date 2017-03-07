package com.tim.domi.remotecontrol;

import android.widget.SeekBar;

import java.io.IOException;
import java.net.Socket;

class SendDataThread extends Thread {
    private boolean cancelled;
    private byte speed, steering;
    private final RoverControlActivity controlActivity;

    SendDataThread(RoverControlActivity controlActivity) {
        this.controlActivity = controlActivity;

        //init speed and steering values
        steering = (byte) ((SeekBar) controlActivity.findViewById(R.id.speed_seekbar)).getProgress();
        steering = (byte) ((SeekBar) controlActivity.findViewById(R.id.steeringSeekBar)).getProgress();
    }

    @Override
    public void run() {
        while (!cancelled) {
            Socket socket = null;
            try {
                System.out.println("Try to connect");
                socket = new Socket("asdf", 4586);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(500);

                socket.getOutputStream().write(new byte[]{speed, steering});
                socket.getInputStream().read();

                connected();

                //send data
                sendLoop(socket);
            } catch (IOException e) {
                System.out.println("connection lost " + e.getMessage());
                connectionLost(e);
                //sleep for a short intervall before trying to reconnect
                sleep(200);
            } finally {
                try {
                    socket.close();
                } catch (IOException | NullPointerException ignore) {
                }
            }
        }
    }

    private void sendLoop(Socket socket) throws IOException {
        byte[] readBuffer = new byte[20], sendData = new byte[2];

        while (!cancelled) {
            sendData[0] = speed;
            sendData[1] = steering;
            socket.getOutputStream().write(sendData);
            System.out.println("Sende speed: " + sendData[0] + " steering: " + sendData[1] + " connected: " + socket.isConnected() + " closed: " + socket.isClosed());
            int bytesReceived = socket.getInputStream().read(readBuffer);
            if(bytesReceived <= 0) {
                throw new IOException("no response");
            }

            if (!interrupted() && !cancelled) {
                sleep(200);
            }
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    private void connected() {
        controlActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                controlActivity.connected();
            }
        });
    }

    private void connectionLost(final IOException e) {
        controlActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                controlActivity.connectionLost(e);
            }
        });
    }

    void cancel() {
        cancelled = true;
        interrupt();
    }

    void setSpeed(byte speed) {
        this.speed = speed;
    }

    void setSteering(byte steering) {
        this.steering = steering;
    }
}
