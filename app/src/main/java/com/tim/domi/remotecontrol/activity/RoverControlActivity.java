package com.tim.domi.remotecontrol.activity;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;

import com.tim.domi.remotecontrol.R;
import com.tim.domi.remotecontrol.ConnectionThread;
import com.tim.domi.remotecontrol.widget.ConnStateView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.SeekBarTouchStart;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@Fullscreen
@EActivity(R.layout.activity_fullscreen)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class RoverControlActivity extends BaseActivity implements ConnectionThread.Listener {

    @ViewById(R.id.speed_control_view)
    SeekBar speedControl;
    @ViewById(R.id.steering_control_view)
    SeekBar steeringControl;
    @ViewById(R.id.connect_button)
    Button connectButton;
    @ViewById(R.id.conn_state_view)
    ConnStateView connStateView;

    private ConnectionThread connThread;

    @Override
    protected void onResume() {
        super.onResume();
        connStateView.newState(R.string.conn_none, color(R.color.text_nomal_color), false);
        if (connThread != null) connect();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // make the steering slider the same size as the speed slider
        steeringControl.getLayoutParams().width = speedControl.getMeasuredHeight();
        steeringControl.setLayoutParams(steeringControl.getLayoutParams());
    }

    @Click(R.id.connect_button)
    public void connect() {
        connectButton.setEnabled(false);
        connStateView.newState(R.string.conn_none, color(R.color.text_loading_color), true);

        connThread = new ConnectionThread(this, 1000);
        connThread.newData(speedControl.getProgress(), steeringControl.getProgress());
        connThread.start();
    }

    @SeekBarProgressChange({R.id.speed_control_view, R.id.steering_control_view})
    void onProgressChanged(SeekBar seekBar, int i) {
        if (Math.abs(i - seekBar.getMax() / 2) < 5)
            seekBar.setProgress(seekBar.getMax() / 2);
        connThread.newData(speedControl.getProgress(), steeringControl.getProgress());
    }

    @SeekBarTouchStart({R.id.speed_control_view, R.id.steering_control_view})
    void onStartTrackingTouch(SeekBar seekBar) {
        seekBar.setAlpha(1);
    }

    @SeekBarTouchStop({R.id.speed_control_view, R.id.steering_control_view})
    void onStopTrackingTouch(SeekBar seekBar) {
        seekBar.setAlpha(0.4f);
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connStateView.newState(R.string.conn_success, color(R.color.text_success_color), false);
                setVisibility(View.GONE, connectButton);
                setEnable(true, speedControl, steeringControl);
            }
        });
    }

    @Override
    public void onNotConnected(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connStateView.newState(R.string.conn_none, color(R.color.text_error_color), true);
                setEnable(false, speedControl, steeringControl);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connThread != null) {
            connThread.cancel();
        }
    }
}
