package com.fireclouu.intel8080emu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fireclouu.intel8080emu.emulator.Guest.Display.Orientation;

import java.util.ArrayList;
import java.util.List;

public class Display extends SurfaceView implements SurfaceHolder.Callback {
    public static final int DIMENSION_WIDTH = 0;
    public static final int DIMENSION_HEIGHT = 1;
    public static final int GUEST_WIDTH = 256;
    public static final int GUEST_HEIGHT = 224;
    private Canvas canvas;
    private float pixelHostSize = 3.18f;
    private final int DRAW_ORIENTATION = Orientation.PORTRAIT;
    private int orientationWidth, orientationHeight;
    private Paint paintRed, paintWhite, paintGreen, paintText;
    private SurfaceHolder holder;
    private boolean enableOffset = false;

    public Display(Context context) {
        super(context);
        init();
    }

    public Display(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder p1) {
        holder = p1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4) {
        holder = p1;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder p1) {
        holder = p1;
    }

    private void init() {
        holder = getHolder();
        paintRed = initPaintProperty(Color.RED);
        paintWhite = initPaintProperty(Color.WHITE);
        paintGreen = initPaintProperty(Color.GREEN);

        paintText = initPaintProperty(Color.WHITE);
        paintText.setTextSize(12);
    }

    public int getHostMaxDimension() {
        return getWidth() > getHeight() ? DIMENSION_WIDTH : DIMENSION_HEIGHT;
    }

    public float getHostScalingValue(int orientation) {
        return orientation == DIMENSION_WIDTH ? ((float) getWidth() / (float) orientationWidth) : ((float) getHeight() / (float) orientationHeight);
    }

    public boolean hasWidthSpace(float scale) {
        boolean returnValue;
        float newWidth = scale * GUEST_WIDTH;
        returnValue = (newWidth <= getWidth());
        return returnValue;
    }

    private float getCenterOffset(float maxValue) {
        float offset;
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
        float mapX;
        float mapY;
        float translateX;
        float translateY;

        int data;
        if (drawOrientation == Orientation.PORTRAIT) {
            orientationWidth = GUEST_HEIGHT;
            orientationHeight = GUEST_WIDTH;
        } else {
            orientationWidth = GUEST_WIDTH;
            orientationHeight = GUEST_HEIGHT;
        }

        // TODO: needs testing , if screen glitches
        // change GUEST_WIDTH to orientationWidth
        final int guestLinearDataLength = GUEST_WIDTH / 8;

        for (int map = 0; map < memory.length; map++) {
            data = memory[map];

            // draws
            mapY = map == 0 ? 0 : (float) map / guestLinearDataLength;
            for (int bit = 0; bit < 8; bit++) {
                int pixel = ((data >> bit) & 1);
                if (pixel == 0) continue;

                mapX = bit + (8 * (map % guestLinearDataLength));

                if (drawOrientation == Orientation.PORTRAIT) {
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

    private Paint initPaintProperty(int color) {
        Paint mPaint;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        return mPaint;
    }

    public void draw(short[] memory) {
        if (holder == null) return;
		if (!holder.getSurface().isValid()) return;

        pixelHostSize = getScaleValueLogical();
        paintWhite.setStrokeWidth(pixelHostSize + 0.5f);

        canvas = holder.getSurface().lockHardwareCanvas();

        canvas.drawColor(Color.BLACK);
        canvas.drawPoints(convertVramToFloatPoints(DRAW_ORIENTATION, memory), paintWhite);

        holder.getSurface().unlockCanvasAndPost(canvas);
    }
}
