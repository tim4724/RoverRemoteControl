package com.tim.domi.remotecontrol.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tim.domi.remotecontrol.R;

public class ConnStateView extends LinearLayout {
    private TextView textView;
    private ProgressBar progressBar;

    public ConnStateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StateView, 0, 0);
        String text = a.getString(R.styleable.StateView_text);
        int color = a.getColor(R.styleable.StateView_color, getResources().getColor(R.color.normal_color));
        boolean showProgress = a.getBoolean(R.styleable.StateView_showProgress, false);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.state_view, this, true);

        textView = (TextView) getChildAt(0);
        progressBar = (ProgressBar) getChildAt(1);

        newState(text, color, showProgress);
    }

    public void newState(int text, int color, boolean showProgress) {
        newState(getResources().getString(text), color, showProgress);
    }

    public void newState(String text, int color, boolean showProgress) {
        textView.setText(text);
        textView.setTextColor(color);
        progressBar.getIndeterminateDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(showProgress ? VISIBLE : GONE);
    }
}
