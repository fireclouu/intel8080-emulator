package com.fireclouu.intel8080emu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.fireclouu.spaceinvaders.intel8080.Guest;

public class Display extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap bitmap;
    public static final int DIMENSION_WIDTH = 0;
    public static final int DIMENSION_HEIGHT = 1;

    // this is intended as space invaders original scan is 90deg rotated to right
    public static final int GUEST_WIDTH = 224;
    public static final int GUEST_HEIGHT = 256;

    private int orientationWidth, orientationHeight;
    private SurfaceHolder holder;

    public Display(Context context) {
        super(context);
        init();
    }

    public Display(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.holder = holder;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        this.holder = holder;
    }

    private void init() {
        holder = getHolder();

        // this is intended, due to original game is rotated 90deg to right
        bitmap = Bitmap.createBitmap(GUEST_WIDTH, GUEST_HEIGHT, Bitmap.Config.ARGB_8888);
    }

    public int getHostMaxDimension() {
        return getWidth() > getHeight() ? DIMENSION_WIDTH : DIMENSION_HEIGHT;
    }

    public float getHostScalingValue(int orientation) {
        return orientation == DIMENSION_WIDTH ? ((float) getWidth() / (float) orientationWidth) : ((float) getHeight() / (float) orientationHeight);
    }

    public float getScaleValueLogical() {
        int maxDimension = getHostMaxDimension() == DIMENSION_WIDTH ? DIMENSION_HEIGHT : DIMENSION_WIDTH;
        return getHostScalingValue(maxDimension);
    }

    private void createGraphicsBitmapRotated(short[] memoryVideoRam) {
        int x = 0;
        int y = 31;
        for (int data : memoryVideoRam) {
            for (int bit = 0; bit < 8; bit++) {
                boolean isPixelOn = ((data >> bit) & 1) == 1;
                if (!isPixelOn) continue;

                // change color based on y region
                int color;
                if (y < 8) {
                    color = Color.parseColor(Guest.Display.COLOR_TOP);
                } else if (y < 24) {
                    color = Color.parseColor(Guest.Display.COLOR_MIDDLE);
                } else {
                    color = Color.parseColor(Guest.Display.COLOR_BELOW);
                }
                bitmap.setPixel(x, (y * 8) - bit, color);
            }
            y--;
            if (y < 0) {
                y = 31;
                x++;
            }
        }
    }

    public void draw(short[] memoryVideoRam) {
        if (holder == null) return;
        if (!holder.getSurface().isValid()) return;

        Canvas canvas = holder.getSurface().lockHardwareCanvas();
        orientationWidth = GUEST_WIDTH;
        orientationHeight = GUEST_HEIGHT;

        if (getHostMaxDimension() == DIMENSION_WIDTH) {
            canvas.translate((canvas.getWidth() / 2.0f) - ((GUEST_WIDTH / 2.0f) * getScaleValueLogical()), 0);
        }

        canvas.scale(getScaleValueLogical(), getScaleValueLogical());
        canvas.drawColor(Color.parseColor(Guest.Display.COLOR_BACKGROUND));
        bitmap.eraseColor(Color.parseColor(Guest.Display.COLOR_BACKGROUND));
        createGraphicsBitmapRotated(memoryVideoRam);
        canvas.drawBitmap(bitmap, 0, 0, null);

        if (holder == null) return;
        if (!holder.getSurface().isValid()) return;
        holder.getSurface().unlockCanvasAndPost(canvas);
    }
}
