package com.tim.domi.remotecontrol.activity;

import android.app.Activity;
import android.view.View;

public abstract class BaseActivity extends Activity {
    public int color(int id) {
        return getResources().getColor(id);
    }
}
