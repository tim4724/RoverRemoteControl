package com.tim.domi.remotecontrol.activity;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tim.domi.remotecontrol.R;
import com.tim.domi.remotecontrol.ConnectionThread;
import com.tim.domi.remotecontrol.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.SeekBarTouchStart;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_fullscreen)
public class RoverControlActivity extends Activity {

    @ViewById(R.id.speed_seekbar)
    SeekBar speedSlider;
    @ViewById(R.id.steeringSeekBar)
    SeekBar steeringSlider;
    @ViewById(R.id.connect_button)
    Button connectButton;
    @ViewById(R.id.connStateView)
    TextView connStateView;
    @ViewById(R.id.progressBar)
    ProgressBar progressView;

    private ConnectionThread connThread;

    @Override
    protected void onResume() {
        super.onResume();
        connStateView.setTextColor(getResources().getColor(R.color.text_nomal_color));
        connStateView.setText(R.string.conn_none);
        if (connThread != null) {
            connectClick();
        }
    }

    @Click(R.id.connect_button)
    public void connectClick() {
        connectButton.setEnabled(false);
        progressView.setVisibility(View.VISIBLE);

        byte speed = (byte) speedSlider.getProgress();
        byte steering = (byte) speedSlider.getProgress();
        connThread = new ConnectionThread(speed, steering, new SendTheadListener(this), 1000);
        connThread.start();
    }

    //<editor-fold desc="Seekbar listeners">
    @SeekBarProgressChange({R.id.speed_seekbar, R.id.steeringSeekBar})
    void onProgressChanged(SeekBar seekBar, int i) {
        if (Math.abs(i - seekBar.getMax() / 2) < 5) seekBar.setProgress(seekBar.getMax() / 2);
        connThread.newData((byte) speedSlider.getProgress(), (byte) steeringSlider.getProgress());
    }

    @SeekBarTouchStart({R.id.speed_seekbar, R.id.steeringSeekBar})
    void onStartTrackingTouch(SeekBar seekBar) {
        seekBar.setAlpha(1);
    }

    @SeekBarTouchStop({R.id.speed_seekbar, R.id.steeringSeekBar})
    void onStopTrackingTouch(SeekBar seekBar) {
        seekBar.setAlpha(0.4f);
    }
    //</editor-fold>

    //<editor-fold desc="SendDataThead listeners">
    @SupposeUiThread
    void connected() {
        Util.setEnabled(true, speedSlider, steeringSlider);
        Util.setVisibility(View.GONE, progressView, connectButton);

        //set width of the horizontal seekbar
        ViewGroup.LayoutParams layoutParams = steeringSlider.getLayoutParams();
        layoutParams.width = speedSlider.getMeasuredHeight();
        steeringSlider.setLayoutParams(layoutParams);

        connStateView.setTextColor(getResources().getColor(R.color.text_success_color));
        connStateView.setText(R.string.conn_success);
    }

    @SupposeUiThread
    void connectionLost(Exception e) {
        Util.setEnabled(false, speedSlider, steeringSlider);
        progressView.setVisibility(View.VISIBLE);

        connStateView.setTextColor(getResources().getColor(R.color.text_error_color));
        connStateView.setText(R.string.conn_lost);
    }

    @SupposeUiThread
    void connectionFailed(Exception e) {
        Util.setEnabled(true, connectButton);
        Util.setEnabled(false, speedSlider, steeringSlider);
        Util.setVisibility(View.GONE, speedSlider, steeringSlider, progressView);

        connStateView.setTextColor(getResources().getColor(R.color.text_error_color));
        connStateView.setText(R.string.conn_failed);
    }
    //</editor-fold>

    @Override
    protected void onPause() {
        super.onPause();
        if (connThread != null) {
            connThread.cancel();
        }
    }
}
