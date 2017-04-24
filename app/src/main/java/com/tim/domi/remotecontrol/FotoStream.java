package com.tim.domi.remotecontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.tim.domi.remotecontrol.activity.BaseActivity;
import com.tim.domi.remotecontrol.activity.RoverControlActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kernd on 22.04.2017.
 */

public class FotoStream extends Thread{
    RoverControlActivity base;
    public FotoStream(RoverControlActivity base)throws Exception
    {
        this.base = base;
    }
    public void run() {
        System.out.println("Thread leuft");
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(9788);
        while(true) {
            Socket socket = welcomeSocket.accept();
            InputStream in = socket.getInputStream();

            DataInputStream data = new DataInputStream(in);
            int len = data.readInt();
            byte[] imgBytes = new byte[len];
            data.readFully(imgBytes);

            System.out.println("Received bits: " + imgBytes.length);
            base.saveByte(imgBytes);

        }
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
