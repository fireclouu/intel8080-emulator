package com.fireclouu.intel8080emu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fireclouu.spaceinvaders.intel8080.Guest;

import java.nio.ByteBuffer;

public class DisplaySurface extends SurfaceView implements SurfaceHolder.Callback {
    static {
        System.loadLibrary("ImGui");
    }

    public static final int DIMENSION_WIDTH = 0;
    public static final int DIMENSION_HEIGHT = 1;

    // this is intended as space invaders original scan is 90deg rotated to right
    public static final int GUEST_WIDTH = 224;
    public static final int GUEST_HEIGHT = 256;

    private int orientationWidth, orientationHeight;
    //    private SurfaceHolder holder;
    private Surface mSurface;
    private Bitmap bitmap;
    private Paint paint;

    private Handler handler;
    private Runnable renderRunnable;

    public DisplaySurface(Context context) {
        super(context);
        init();
    }

    public DisplaySurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        nativeShutdown();
        mSurface = holder.getSurface();
        nativeInit(mSurface);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        nativeShutdown();
        mSurface = holder.getSurface();
        nativeInit(mSurface);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        handler.removeCallbacks(renderRunnable);
        nativeShutdown();
    }

    private void init() {
        handler = new Handler();
        getHolder().addCallback(this);

        // this is intended, due to original game is rotated 90deg to right
        bitmap = Bitmap.createBitmap(GUEST_WIDTH, GUEST_HEIGHT, Bitmap.Config.ARGB_8888);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(5.0f);
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
        if (!mSurface.isValid()) return;

//        Canvas canvas = mSurface.lockHardwareCanvas();
        orientationWidth = GUEST_WIDTH;
        orientationHeight = GUEST_HEIGHT;

//        if (getHostMaxDimension() == DIMENSION_WIDTH) {
//            canvas.translate((canvas.getWidth() / 2.0f) - ((GUEST_WIDTH / 2.0f) * getScaleValueLogical()), 0);
//        }
//
//        canvas.scale(getScaleValueLogical(), getScaleValueLogical());
//        canvas.drawColor(Color.parseColor(Guest.Display.COLOR_BACKGROUND));
        bitmap.eraseColor(Color.parseColor(Guest.Display.COLOR_BACKGROUND));
        createGraphicsBitmapRotated(memoryVideoRam);

        // 22-07-2025
        ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        buffer.rewind();

        renderRunnable = () -> {
            if (mSurface != null && mSurface.isValid()) nativeMainLoopStep(buffer, bitmap.getWidth(), bitmap.getHeight());
//            handler.postDelayed(renderRunnable, 16);
        };
        handler.post(renderRunnable);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//
//        if (!mSurface.isValid()) return;
//        mSurface.unlockCanvasAndPost(canvas);
    }

    public native int nativeInit(Surface surface);
    public native void nativeMainLoopStep(ByteBuffer buffer, int width, int height);
    public native void nativeShutdown();

}
