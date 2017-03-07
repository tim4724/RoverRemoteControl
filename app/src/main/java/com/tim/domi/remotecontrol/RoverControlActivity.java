package com.tim.domi.remotecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

public class RoverControlActivity extends Activity {

    private SeekBar speedSlider, steeringSlider;
    private Button connectButton;
    private SendDataThread sendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        speedSlider = (SeekBar) findViewById(R.id.speed_seekbar);
        steeringSlider = (SeekBar) findViewById(R.id.steeringSeekBar);
        connectButton = (Button) findViewById(R.id.connect_button);

        speedSlider.setOnSeekBarChangeListener(seekBarListener);
        steeringSlider.setOnSeekBarChangeListener(seekBarListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sendThread != null) {
            sendThread = new SendDataThread(this);
            sendThread.start();
        }
    }

    public void connectClick(View v) {
        connectButton.setEnabled(false);

        sendThread = new SendDataThread(this);
        sendThread.start();
    }

    final SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (Math.abs(i - seekBar.getMax() / 2) < 5) {
                i = seekBar.getMax() / 2;
                seekBar.setProgress(i);
            }

            if (seekBar == speedSlider) {
                sendThread.setSpeed((byte) i);
                sendThread.interrupt();
            } else if (seekBar == steeringSlider) {
                sendThread.setSteering((byte) i);
                sendThread.interrupt();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setAlpha(1);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setAlpha(0.4f);
        }
    };

    public void connectionLost(IOException e) {
        Toast.makeText(this, getString(R.string.connection_lost_message) + "\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void connected() {
        connectButton.setVisibility(View.GONE);
        steeringSlider.setVisibility(View.VISIBLE);
        speedSlider.setVisibility(View.VISIBLE);

        //set width of the horizontal seekbar
        ViewGroup.LayoutParams layoutParams = steeringSlider.getLayoutParams();
        layoutParams.width = speedSlider.getMeasuredHeight();
        steeringSlider.setLayoutParams(layoutParams);

        Toast.makeText(this, getString(R.string.connected_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sendThread != null) {
            sendThread.cancel();
        }
    }
}
