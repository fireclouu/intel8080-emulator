package com.fireclouu.intel8080emu;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainGraphics extends SurfaceView
{
	Canvas c;

	Paint paintred;
	Paint paintwhite;
	Paint paintgreen;

	private final float PIXEL_SIZE = 2.90f;
	private final float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private final float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;

	private final int DISPLAY_WIDTH = 32 * 8; // 32 = 0xXXX0 - 0xXXYF (X = num ; Y = num + 1) ... ; 8 = bit 0 - 7
	private final int DISPLAY_HEIGHT = 224; // 224

	float[] display;
	
	SurfaceHolder holder;
	
	public MainGraphics(Context context) {
		super(context);
		init();
	}
	
	public MainGraphics(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public MainGraphics(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		holder = getHolder();
		
		paintred = setPaint(Color.RED);
		paintwhite = setPaint(Color.WHITE);
		paintgreen = setPaint(Color.GREEN);

		paintwhite.setStrokeWidth(PIXEL_SIZE);

		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		
	}
	
	public void updateView(CpuComponents cpu) {
		plotImage(cpu);
		
		if (!holder.getSurface().isValid()) {
			return;
		}

		c = holder.lockCanvas();
		
		c.drawColor(Color.BLACK);
		c.drawPoints(display, paintwhite);
		
		holder.unlockCanvasAndPost(c);
	}
	
	private void plotImageCC(CpuComponents cpu) {
		// initialize every draw
		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		// vram starting point
		int vram = 0x2400;
		// array
		int loc = 0; // drawPoints feeds two values of float
		
		
		

	}
	
	private void plotImage(CpuComponents cpu) {
		// initialize every draw
		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		// vram starting point
		int vram = 0x2400;
		// array
		int arr = 0;
		
		for (int y = 0; y < DISPLAY_HEIGHT; y++) { // 256

			for (int x = 0; x < DISPLAY_WIDTH; x += 8) { // 224
				int pixel = cpu.memory[vram];

				for(int scan = 0; scan < 8; scan++) { // convert binary into pixel (on and off)
					if (((pixel >> scan) & 0x1) == 1) {
						display[arr] = (scan + x) * PIXEL_SIZE_WIDTH;
						arr++;
						display[arr] = y * PIXEL_SIZE_HEIGHT;
						arr++;
					}
				}

				vram++;
			}
		}
		
	}
	
	// rotate vid counterclockwise
	// suppose x, y = 0, 0 , next 1, 0 after rotate, 0, 256, next 0, 255
	// y constantly updated from bottom to up, while x, increments every y > 256
	
	// original 255, 0 in fixed on is 0, 1
	
	// update will turn bottom to up, left to right
	
	
	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);

		return mPaint;
	}
	
}
