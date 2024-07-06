package com.fireclouu.intel8080emu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fireclouu.intel8080emu.emulator.baseclass.DisplayAdapter;
import com.fireclouu.intel8080emu.emulator.Machine;

import java.util.ArrayList;
import java.util.List;

public class Display extends SurfaceView implements SurfaceHolder.Callback, DisplayAdapter {

    // get float value only
    // on emulation class devise array that can hold 0x2400 - 0x3fff and pass it here
    // do the loop here! instead of looping on another class

    public static final int DIMENSION_WIDTH = 0;
    public static final int DIMENSION_HEIGHT = 1;
    public static final int GUEST_WIDTH = 256;
    public static final int GUEST_HEIGHT = 224;
    // make tests for display
    Canvas canvas;
    long expected = 23803381171L; // 24 billion (long crc32)
    private float pixelHostSize = 3.18f;
    private final int DRAW_ORIENTATION = DRAW_ORIENTATION_PORTRAIT;
    private int orientationWidth, orientationHeight;
    private Paint paintRed, paintWhite, paintGreen, paintText;
    private SurfaceHolder holder;
    private boolean enableOffset = false;
    private boolean inDrawing = false;

    public Display(Context context) {
        super(context);
        init(DRAW_ORIENTATION_PORTRAIT);
    }

    public Display(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(DRAW_ORIENTATION_PORTRAIT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder p1) {
        // TODO: Implement this method
    }

    @Override
    public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4) {
        // TODO: Implement this method
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder p1) {
        // TODO: Implement this method
    }

    private void init(int orientation) {
        holder = getHolder();

        paintRed = setPaint(Color.RED);
        paintWhite = setPaint(Color.WHITE);
        paintGreen = setPaint(Color.GREEN);

        paintText = setPaint(Color.WHITE);
        paintText.setTextSize(12);

        if (orientation == DRAW_ORIENTATION_PORTRAIT) {
            orientationWidth = GUEST_HEIGHT;
            orientationHeight = GUEST_WIDTH;
        } else {
            orientationWidth = GUEST_WIDTH;
            orientationHeight = GUEST_HEIGHT;
        }
    }

    public int getHostMaxDimension() {
        return getWidth() > getHeight() ? DIMENSION_WIDTH : DIMENSION_HEIGHT;
    }

    public float getHostScalingValue(int orientation) {
        return orientation == DIMENSION_WIDTH ? ((float) getWidth() / (float) orientationWidth) : ((float) getHeight() / (float) orientationHeight);
    }

    public boolean hasWidthSpace(float scale) {
        boolean returnValue = false;
        float newWidth = scale * GUEST_WIDTH;
        returnValue = (newWidth <= getWidth());
        return returnValue;
    }

    private float getCenterOffset(float maxValue) {
        float offset = 0;
        int hostWidth = getWidth();
        float centerPointHost = (float) hostWidth / 2;
        float centerPointGuest = maxValue / 2;
        offset = Math.abs(centerPointHost - centerPointGuest);
        return offset;
    }

    public float getScaleValueLogical() {
        int maxDimension = getHostMaxDimension() == DIMENSION_WIDTH ? DIMENSION_HEIGHT : DIMENSION_WIDTH;
        float scaleValue = getHostScalingValue(maxDimension);
        enableOffset = hasWidthSpace(scaleValue);
        return scaleValue;
    }

    public float[] convertVramToFloatPoints(int drawOrientation, short[] memory) {
        float centerOffset = enableOffset ? getCenterOffset(orientationWidth * pixelHostSize) : 0;
        final float spacing = pixelHostSize;
        List<Float> plotList = new ArrayList<>();
        float[] returnValue;
        int counter = 0;
        float mapX = 0;
        float mapY = 0;
        float translateX = 0;
        float translateY = 0;

        int vramNormalized;
        int data;
        if (drawOrientation == DRAW_ORIENTATION_PORTRAIT) {
            orientationWidth = GUEST_HEIGHT;
            orientationHeight = GUEST_WIDTH;
        } else {
            orientationWidth = GUEST_WIDTH;
            orientationHeight = GUEST_HEIGHT;
        }

        // TODO: needs testing , if screen glitches
        // change GUEST_WIDTH to orientationWidth
        final int guestLinearDataLength = GUEST_WIDTH / 8;

        for (int vramPc = Machine.VRAM_START; vramPc <= Machine.VRAM_END; vramPc++) {
            data = memory[vramPc];
            vramNormalized = vramPc - Machine.VRAM_START;

            // draws
            mapY = vramNormalized == 0 ? 0 : (float) vramNormalized / guestLinearDataLength;
            for (int bit = 0; bit < 8; bit++) {
                int pixel = ((data >> bit) & 1);
                if (pixel == 0) continue;

                mapX = bit + (8 * (vramNormalized % guestLinearDataLength));

                // translate pixel
                if (drawOrientation == DRAW_ORIENTATION_PORTRAIT) {
                    translateX = mapY;
                    translateY = Math.abs(mapX - orientationHeight);
                } else {
                    translateX = mapX;
                    translateY = mapY;
                }

                plotList.add((translateX * spacing) + centerOffset);
                plotList.add((translateY * spacing));
            }
        }

        returnValue = new float[plotList.size()];
        for (float pos : plotList) returnValue[counter++] = pos;
        return returnValue;
    }

    private Paint setPaint(int color) {
        Paint mPaint;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);

        return mPaint;
    }

    private double fps() {
        return 0;
    }

    private String parseFps(double fps) {
        return String.format("fps: %.2f", fps);
    }

    @Override
    public void draw(short[] memory) {
        inDrawing = true;
        while (!holder.getSurface().isValid()) continue;

        pixelHostSize = getScaleValueLogical();
        paintWhite.setStrokeWidth(pixelHostSize + 0.5f);

        while (!holder.getSurface().isValid() && !holder.isCreating()) continue;

        // canvas
        canvas = holder.getSurface().lockHardwareCanvas();
        canvas.drawColor(Color.BLACK);
        canvas.drawPoints(convertVramToFloatPoints(DRAW_ORIENTATION, memory), paintWhite);

        canvas.drawText("fireclouu", (int) (getWidth() / 1.1), getHeight() - 10, paintWhite);
        // release
        holder.getSurface().unlockCanvasAndPost(canvas);
        inDrawing = false;
    }

    @Override
    public boolean isDrawing() {
        return inDrawing;
    }
}
