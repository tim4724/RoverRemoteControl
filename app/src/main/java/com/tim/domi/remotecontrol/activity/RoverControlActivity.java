package com.tim.domi.remotecontrol.activity;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tim.domi.remotecontrol.RemoteControl;
import com.tim.domi.remotecontrol.R;
import com.tim.domi.remotecontrol.listener.RemoteListener;
import com.tim.domi.remotecontrol.widget.ConnStateView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.SeekBarTouchStart;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@Fullscreen
@EActivity(R.layout.activity_fullscreen)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class RoverControlActivity extends BaseActivity implements RemoteListener {

    @ViewById(R.id.speed_control_view) SeekBar speedView;
    @ViewById(R.id.steering_control_view) SeekBar steeringView;
    @ViewById(R.id.connect_button) Button connectButton;
    @ViewById(R.id.conn_state_view) ConnStateView connStateView;
    @ViewById(R.id.ping_view) TextView pingTextView;

    private RemoteControl remote;

    @Override
    protected void onResume() {
        super.onResume();
        connStateView.newState(R.string.conn_none, color(R.color.normal_color), false);
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
            connStateView.newState(R.string.conn_load, color(R.color.loading_color), true);
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
    @UiThread
    public void onConnected() {
        connectButton.setVisibility(View.GONE);
        speedView.setVisibility(View.VISIBLE);
        steeringView.setVisibility(View.VISIBLE);
        connStateView.newState(R.string.conn_success, color(R.color.success_color), false);
        pingTextView.setVisibility(View.VISIBLE);
    }

    @Override
    @UiThread
    public void onNotConnected() {
        connStateView.newState(R.string.conn_load, color(R.color.error_color), true);
        pingTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    @UiThread
    public void pingUpdate(final int ping) {
        pingTextView.setText(String.valueOf(Math.round(ping / 2f)));
    }

    @Override
    @UiThread
    public void failed(final Exception e) {
        recreate();
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (remote != null) remote.cancel();
    }
}
