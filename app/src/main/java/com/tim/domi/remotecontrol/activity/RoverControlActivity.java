package com.tim.domi.remotecontrol.activity;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tim.domi.remotecontrol.RemoteControl;
import com.tim.domi.remotecontrol.R;
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
public class RoverControlActivity extends BaseActivity implements RemoteControl.Listener {

    @ViewById(R.id.speed_control_view) SeekBar speedView;
    @ViewById(R.id.steering_control_view) SeekBar steeringView;
    @ViewById(R.id.connect_button) Button connectButton;
    @ViewById(R.id.conn_state_view) ConnStateView connStateView;
    @ViewById(R.id.ping_view) TextView pingTextView;

    private RemoteControl remote;

    @Override
    protected void onResume() {
        super.onResume();
        connStateView.newState(R.string.conn_none, color(R.color.text_nomal_color), false);
        if (remote != null) connect();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // make the steering slider the same size as the speed slider
        steeringView.getLayoutParams().width = speedView.getMeasuredHeight();
        steeringView.setLayoutParams(steeringView.getLayoutParams());
    }

    @Click(R.id.connect_button)
    public void connect() {
        try {
            if (remote == null) remote = new RemoteControl(this);
            remote.start();
            remote.newData(speedView.getProgress(), steeringView.getProgress());

            connectButton.setEnabled(false);
            connStateView.newState(R.string.conn_load, color(R.color.text_loading_color), true);
        } catch (Exception e) {
            failed(e);
        }
    }

    @SeekBarProgressChange({R.id.speed_control_view, R.id.steering_control_view})
    void onProgressChanged(SeekBar seekBar, int i) {
        if (Math.abs(i - seekBar.getMax() / 2) < 5) seekBar.setProgress(seekBar.getMax() / 2);
        if (remote != null) remote.newData(speedView.getProgress(), steeringView.getProgress());
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
    public void updateConnState(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    speedView.setVisibility(View.VISIBLE);
                    steeringView.setVisibility(View.VISIBLE);
                    connectButton.setVisibility(View.GONE);
                }
                connStateView.newState(connected ? R.string.conn_success : R.string.conn_load,
                        color(connected ? R.color.text_success_color : R.color.text_error_color), !connected);
                pingTextView.setVisibility(connected ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    public void pingUpdate(final int ping) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pingTextView.setText(String.valueOf(Math.round(ping / 2f)));
            }
        });
    }

    @Override
    public void failed(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recreate();
                Toast.makeText(RoverControlActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (remote != null) remote.cancel();
    }
}
