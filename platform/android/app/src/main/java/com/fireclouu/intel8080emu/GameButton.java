package com.fireclouu.intel8080emu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.fireclouu.spaceinvaders.intel8080.Platform;

public class GameButton extends Button {
    private byte keyCode;
    private Platform platform;

    public GameButton(Context context) {
        super(context);
    }

    public GameButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setKeyCode(byte keyCode) { this.keyCode = keyCode; }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        performClick();
        byte playerPort = platform.getPlayerPort();
        int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
        boolean hasAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP;
        boolean isDown = action == MotionEvent.ACTION_DOWN;

        // clear touch state
        if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            setPressed(false);
            platform.sendInput(playerPort, keyCode, false);
        } else if (hasAction) {
            platform.sendInput(playerPort, keyCode, isDown);
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}