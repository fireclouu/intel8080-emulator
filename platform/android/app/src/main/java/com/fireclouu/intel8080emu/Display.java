package com.fireclouu.intel8080emu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fireclouu.spaceinvaders.intel8080.Guest.Display.Orientation;

import java.util.ArrayList;
import java.util.List;

public class Display extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap bitmap;
    public static final int DIMENSION_WIDTH = 0;
    public static final int DIMENSION_HEIGHT = 1;

    // this is intended as space invaders original scan is 90deg rotated to right
    public static final int GUEST_WIDTH = 224;
    public static final int GUEST_HEIGHT = 256;

    private final float pixelHostSize = 3.18f;
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

        // this is intended, due to original game is rotated 90deg to right
        bitmap = Bitmap.createBitmap(GUEST_WIDTH, GUEST_HEIGHT, Bitmap.Config.ARGB_8888);
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

    // legacy
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

    private void createGraphicsBitmapRaw(short[] memoryVideoRam) {
        int x = 0;
        int y = 0;
        for (int data : memoryVideoRam) {
            if (x == 32) {
                x = 0;
                y++;
            }

            for (int bit = 0; bit < 8; bit++) {
                boolean isPixelOn = ((data >> bit) & 1) == 1;
                if (!isPixelOn) continue;
                bitmap.setPixel((x * 8) + bit, y, Color.WHITE);
            }
            x++;
        }
    }

    private void createGraphicsBitmapRotated(short[] memoryVideoRam) {
        int x = 0;
        int y = 31;
        for (int index = 0; index < memoryVideoRam.length; index++) {
            int data = memoryVideoRam[index];
            for (int bit = 0; bit < 8; bit++) {
                boolean isPixelOn = ((data >> bit) & 1) == 1;
                if (!isPixelOn) continue;

                // change color based on y region
                int color = Color.WHITE;
                if (y < 8) {
                    color = Color.RED;
                } else if (y < 24) {
                    color = Color.WHITE;
                } else {
                    color = Color.GREEN;
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

    private Paint initPaintProperty(int color) {
        Paint mPaint;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        return mPaint;
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
        canvas.drawColor(Color.BLACK);
        bitmap.eraseColor(Color.BLACK);
        createGraphicsBitmapRotated(memoryVideoRam);
        canvas.drawBitmap(bitmap, 0, 0, null);
        holder.getSurface().unlockCanvasAndPost(canvas);
    }
}
