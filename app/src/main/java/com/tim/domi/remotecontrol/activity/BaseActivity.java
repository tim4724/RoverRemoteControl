package com.tim.domi.remotecontrol.activity;

import android.app.Activity;
import android.view.View;

public abstract class BaseActivity extends Activity {
    public int color(int id) {
        return getResources().getColor(id);
    }

    public void setEnable(boolean enable, View... views) {
        for (View v : views) {
            v.setEnabled(enable);
            if(enable) v.setVisibility(View.VISIBLE);
        }
    }

    public void setVisibility(int visibility, View... views) {
        for (View v : views) {
            v.setVisibility(visibility);
        }
    }
}
