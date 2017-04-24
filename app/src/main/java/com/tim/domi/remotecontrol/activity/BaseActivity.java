package com.tim.domi.remotecontrol.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tim.domi.remotecontrol.FotoStream;
import com.tim.domi.remotecontrol.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class BaseActivity extends Activity {
    public int color(int id) {
        return getResources().getColor(id);
    }
    /*public void recieveFoto(View view){
        System.out.println("Button!");
        try {
            FotoStream foto = new FotoStream(this);
            foto.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveByte(byte[] bytes) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "FotosRover");
            if (!file.mkdirs()) {
                System.out.println("Directory not created");
            }
            File file2 = new File(file, "abc.png");
            FileOutputStream fos = new FileOutputStream(file2);
            fos.write(bytes);
            fos.flush();
            fos.close();

            //setContentView(R.layout.activity_fullscreen);
            //ImageView jpgView = (ImageView)findViewById(R.id.ivPreview);
            //Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/FotosRover/abc.png");
            //jpgView.setImageBitmap(bitmap);

            //Intent intent = new Intent();
            //intent.setAction(Intent.ACTION_VIEW);
            //intent.setDataAndType(Uri.decode("content:///storage/emulated/0/Pictures/FotosRover"),"abc.png");
            //intent.setDataAndType(Uri.fromFile(file), "*");
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intent);
        /*} catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("erfolgreich geschrieben");

    }*/
}
