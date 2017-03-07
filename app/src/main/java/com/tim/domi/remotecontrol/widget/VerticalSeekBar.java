package com.tim.domi.remotecontrol.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {

    private OnSeekBarChangeListener l;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (l != null) l.onStartTrackingTouch(this);
                trackTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                if (l != null) l.onStopTrackingTouch(this);
            case MotionEvent.ACTION_MOVE:
                trackTouchEvent(event);
                break;
        }
        return true;
    }

    private void trackTouchEvent(MotionEvent event) {
        final float available = getHeight() - getPaddingLeft() - getPaddingRight();
        int progress = getMax() - (int) (getMax() * (event.getY() - getPaddingLeft()) / available);
        setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
        this.l = l;
    }
}
