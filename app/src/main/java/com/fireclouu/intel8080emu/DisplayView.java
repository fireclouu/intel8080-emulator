package com.fireclouu.intel8080emu;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;
import java.util.*;

public class DisplayView extends View
{
	// Canvas[][] canvasGlobal;
	
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
	
	
	private final float PIXEL_SIZE = 2.90f;
	private final float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private final float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;
	
	private final float ASPECT = 1.00f;
	private final int WIDTH = 256; // 256
	private final int HEIGHT = 224; // 224
	
	private float cur_width = 0.00f;
	private float cur_height = 0.00f;
	
	private int vram = 0x2400;
	
	private boolean isUpdatingOnFunc = false;
	
	CpuComponents cpu;
	
	RectF[][] rectF = new RectF[HEIGHT][WIDTH];
	float[] plotWhite, plotBlack;
	
	Paint paintblack;
	Paint paintwhite;
	Paint paintgreen;

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
	
	public void setCpu(CpuComponents cpu) {
		this.cpu = cpu;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// suspend emu
		// canvas.drawRect(0, 0, getWidth(), getHeight(), paintblack);
		
		if (isUpdatingOnFunc) {
			// put on other thread!
			vram = 0x2400;
			int pxPos = 0;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x+=8) {
					int flipbit = cpu.memory[vram];
					int hscan = 0;
					for(int scan = 0; scan < 8; scan++) { // Little endian

						if (((flipbit >> scan) & 0x1) == 1) {
							// canvas.drawRect(rectF[y][x + hscan], paintwhite);
							plotWhite[pxPos] = (scan + x) * PIXEL_SIZE_WIDTH;
							pxPos++;
							plotWhite[pxPos] = (y * PIXEL_SIZE_HEIGHT);
							pxPos++;
						}

						hscan++;
					}

					vram++;
				}
			}

			
			canvas.drawPoints(plotWhite, paintwhite);
			
			isUpdatingOnFunc = false;
			
			if(canvas.isHardwareAccelerated()) {
				canvas.drawText("Hardware-accelerated: true", 0, getHeight(), paintwhite);
			} else {
				canvas.drawText("Hardware-accelerated: false", 0, getHeight(), paintwhite);
			}
			
			
			// canvas.drawPoints(plot, paintwhite);
			// unsuspend emu
			
		}
	}

	///  METHODS  ///
	private void init(AttributeSet attrs) {
		//this.canvasGlobal = new Canvas[HEIGHT][WIDTH];
		
		paintblack = setPaint(Color.BLACK);
		paintwhite = setPaint(Color.WHITE);
		paintgreen = setPaint(Color.GREEN);
		
		paintwhite.setStrokeWidth(PIXEL_SIZE);
		
		//rectF = new RectF[HEIGHT][WIDTH];
		plotWhite = new float[(HEIGHT * WIDTH)];
		
		/*
		for(int y = 0; y < HEIGHT; y++) {
			cur_width = 0;

			for(int x = 0; x < WIDTH; x++) {
				rectF[y][x] = new RectF(cur_width, cur_height, cur_width + (PIXEL_SIZE_WIDTH * ASPECT), cur_height + (PIXEL_SIZE_HEIGHT * ASPECT)); // left, top, right, bottom
				cur_width += (PIXEL_SIZE_WIDTH * ASPECT);
			}

			cur_height += (PIXEL_SIZE_HEIGHT * ASPECT);
		}
		*/
	}

	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);
		
		return mPaint;
	}
	
	public void updateView(CpuComponents cpu) {
		this.cpu = cpu;
		isUpdatingOnFunc = true;
		postInvalidate();
		cpu.updateScreen = false;
	}

}
