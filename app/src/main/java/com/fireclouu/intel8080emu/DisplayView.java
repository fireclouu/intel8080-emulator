package com.fireclouu.intel8080emu;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;
import java.util.*;

public class DisplayView extends View
{
	// TODO: Implement 8080 vram feed to canvas
	// 256 x 224 (w x h) = 57344 object 28 * 8; 32 * 8
	// 1 rect object per pixel (black = 0, white = 1)
	// 0x2400 - 0x3fff
	// spaceinvaders - 90° counter clockwise
	// rect pos for line e.g.
	// use of multidimentional array
	// rect[0][1].top - always equal to line num
	// rect[0][1].left - rect[0][0].right
	// rect[0][1].bottom - (pixel size)
	// rect[0][1].right - (pixel size)
	
	// plotdata using float
	// float are done by painting within defined x and y pos.
	// use of setstrokewidth may affect x, y pos (overlaps if var1x = 1, var2x = 2) so it needs to define its pixel size after plotting new one
	// x = 0 * pxWidth; y = 0 * pxwidth
	// if needs to down vertically , use for value to define how much it needs to go below
	
	CpuComponents cpu;

	Paint paintred;
	Paint paintwhite;
	Paint paintgreen;
	
	private final float PIXEL_SIZE = 2.90f;
	private final float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private final float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;
	
	private final int WIDTH = 256; // 256
	private final int HEIGHT = 224; // 224
	
	private boolean isUpdatingOnFunc = false;
	float[] pixelLoc;

	public DisplayView(Context context) {
		super(context);
		init(null);
	}
	public DisplayView(Context context, AttributeSet attrs){ 
		super(context, attrs);
		init(attrs);
	}

	public DisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	public DisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (isUpdatingOnFunc) {
			canvas.drawPoints(pixelLoc, paintwhite);
			isUpdatingOnFunc = false;
			
			if(canvas.isHardwareAccelerated()) {
				canvas.drawText("Hardware-accelerated: true", 0, getHeight(), paintwhite);
			} else {
				canvas.drawText("Hardware-accelerated: false", 0, getHeight(), paintwhite);
			}
			
		}
	}

	///  METHODS  ///
	private void init(AttributeSet attrs) {
		
		paintred = setPaint(Color.RED);
		paintwhite = setPaint(Color.WHITE);
		paintgreen = setPaint(Color.GREEN);
		
		paintwhite.setStrokeWidth(PIXEL_SIZE);
		
		pixelLoc = new float[(HEIGHT * WIDTH)];
		
	}

	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);
		
		return mPaint;
	}
	
	public void updateView(CpuComponents cpu) {
		// cpu object
		this.cpu = cpu;
		// initialize every draw
		pixelLoc = new float[(HEIGHT * WIDTH)];
		// vram starting point
		int vram = 0x2400;
		// array
		int arr = 0;
		
		for (int y = 0; y < HEIGHT; y++) { // 256
			
			for (int x = 0; x < WIDTH; x += 8) { // 224
				int pixel = cpu.memory[vram];
				
				for(int scan = 0; scan < 8; scan++) { // convert binary into pixel (on and off)
					if (((pixel >> scan) & 0x1) == 1) {
						pixelLoc[arr] = (scan + x) * PIXEL_SIZE_WIDTH;
						arr++;
						pixelLoc[arr] = y * PIXEL_SIZE_HEIGHT;
						arr++;
					}
				}
				
				vram++;
			}
		}

		isUpdatingOnFunc = true;
		postInvalidate();
		
	}

}
