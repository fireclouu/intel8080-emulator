package com.fireclouu.intel8080emu;

import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.util.*;
import android.view.SurfaceHolder.*;

public class AppDisplay extends SurfaceView implements SurfaceHolder.Callback, DisplayAdapter
{
	// get float value only
	// on emulation class devise array that can hold 0x2400 - 0x3fff and pass it here
	// do the loop here! instead of looping on another class
	ArrayList<Float> pixelHolder;
	private float[] pixels;
	
	Thread master;
	
	Canvas canvas;
	
	private final float PIXEL_SIZE = 3.18f;
	private final float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private final float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;

	private final int DISPLAY_WIDTH = 32 * 8; // 256 (0x2400 to 0x2407 = bit 0 to bit 7)
	private final int DISPLAY_HEIGHT = 224;   // 224
	
	private final int vramloc = 0x2400;
	
	Paint paintred;
	Paint paintwhite;
	Paint paintgreen;
	Paint textPaint;
	
	private float[] display;
	private short[] memory;
	
	private boolean isStarting = true;
	long frameCount = 0;
	SurfaceHolder holder;
	
	// TOUCH
	String touchSample = "";
	
	public AppDisplay(Context context) {
		super(context); 
		init();
	}
	
	public AppDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		holder = getHolder();

		paintred = setPaint(Color.RED);
		paintwhite = setPaint(Color.WHITE);
		paintgreen = setPaint(Color.GREEN);

		paintwhite.setStrokeWidth(PIXEL_SIZE);
		textPaint = setPaint(Color.WHITE);
		textPaint.setTextSize(16);
		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		memory = new short[AppUtils.Component.PROGRAM_LENGTH];
		
		pixelHolder = new ArrayList<Float>();
		
		runState = true;
	}
	
	@Override
	public void updateView(short[] memory) {
		this.memory = memory;
		isDrawing = true;
	}

	@Override
	public void startView() {
		if(AppUtils.Component.DEBUG) {
			master = new Thread(new DebugThread());
		} else {
			master = new Thread(new DrawThread());
		}
		
		master.start();
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

	private void drawImage(Canvas canvas) {
		// rotate counter clockwise
		//           x, y
		// original: 0, 0
		// fixed:    0, 224

		// original: 1, 0
		// fixed:    0, 223

		// vram starting point
		int vram = vramloc;

		for (int x = 0; x < DISPLAY_HEIGHT; x++) {          // x-axis 224px (H)

			for (int y = DISPLAY_WIDTH; y > 0; y -= 8) {    // y-axis 256px (V)

				short pixel = this.memory[vram++];            // read memory byte
				if (pixel == 0) continue;                   // optimization

				for (byte scan = 0; scan < 8; scan++) {	    // read bits as pixel

					if (((pixel) & 0x1) == 1)
						canvas.drawPoint(x * PIXEL_SIZE_WIDTH,
										 (y - scan) * PIXEL_SIZE_HEIGHT, paintwhite);

					pixel >>= 1;

					if(pixel == 0) break; // optimization
				}
				
				isStarting = false;
			}

		}
		
	}
	
	private void plotPixelDefault(Canvas canvas) {
		// vram starting point
		short vram = vramloc;
		
		for (int y = 0; y < DISPLAY_HEIGHT; y++) {       // y-axis 224px (V)

			for (int x = 0; x < DISPLAY_WIDTH; x += 8) { // x-axis 256px (H)
				
				short pixel = memory[vram++];        // read memory byte
				if (pixel == 0) continue;                // optimization
				
				for(byte scan = 0; scan < 8; scan++) {   // read bits as pixel
				
					if (((pixel >> scan) & 0x1) == 1)		
						canvas.drawPoint((scan + x) * PIXEL_SIZE_WIDTH,
							y * PIXEL_SIZE_HEIGHT, paintwhite);
							
					pixel >>= 1;

					if(pixel == 0) break; // optimization
				}
			}
		}
		
		isStarting = false;
	}
	
	
	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);

		return mPaint;
	}
	
	private void cDrawText(String text, int x, int y) {
		canvas.drawText(text, x, y, paintwhite);
	}
	
	class DrawThread implements Runnable {
	@Override
	public void run() {
		while(PlatformAdapter.getStateMaster() && runState) {
			if (!holder.getSurface().isValid() || !isDrawing ) continue;

			frameCount++;

			canvas = holder.lockCanvas();
			canvas.drawColor(Color.BLACK);
			drawImage(canvas); // primary updating method
			/*drawImage();
			canvas.drawPoints(pixels, paintwhite);*/
			if (isStarting) {
				canvas.drawText(
					Platform.OUT_MSG, 0, 
					10,
					paintwhite);
			}
			
			canvas.drawText(
				"Hardware accelerated: " + isHardwareAccelerated(), 0, 
				getHeight() - 10,
				paintwhite);

			canvas.drawText(
				"frames: " + frameCount, 0, 
				getHeight() - 25,
				paintwhite);
				
			if (Emulator.isCycleCorrect()) {
				canvas.drawText(
					Emulator.cycleInfo, 0,
					getHeight() - 40, paintgreen);
			} else {
				canvas.drawText(
					Emulator.cycleInfo, 0,
					getHeight() - 40, paintred);
			}
				
			canvas.drawText(
				"Machine speed: " + Emulator.actual_cycle, 0,
				getHeight() - 55, paintwhite);
			
			canvas.drawText(
				"fireclouu", (int) (getWidth() / 1.1), 
				getHeight() - 10,
				paintwhite);
				
			canvas.drawText(touchSample, 0, 70, paintwhite);
			
			holder.unlockCanvasAndPost(canvas);

			isDrawing = false;
		}
	}
	
}
class DebugThread implements Runnable {
		@Override
		public void run() {
			while(PlatformAdapter.getStateMaster() && runState) {
				if (!holder.getSurface().isValid() || !isDrawing ) continue;
				
				canvas = holder.lockCanvas();
				
				canvas.drawColor(Color.BLACK);
				if (isStarting) {
					canvas.drawText(
						Platform.OUT_MSG, 0, 
						10,
						paintwhite);
				}
				
				canvas.drawText(
					AppUtils.getTime(), getWidth() - 60, 15, paintwhite);
				
				long expected = 23_803_381_171_L; // 24 billion
				try {
				int startingpoint = 20;
				for (String msg : PlatformAdapter.BUILD_MSG) {
					canvas.drawText(msg, 0, startingpoint += 20, textPaint);
				} 
					
				}catch (NullPointerException e) {
					e.printStackTrace();
				}
				
				
				canvas.drawText(
					"Hardware accelerated: " + isHardwareAccelerated(), 0, 
					getHeight() - 10,
					paintwhite);

				canvas.drawText(
					"Expected Cpu Cycle: " + expected, 0, 
					getHeight() - 25,
					paintwhite);
					
				canvas.drawText(
					"Remaining Cpu Cycle: " + (expected - Interpreter.cycle), 0, 
					getHeight() - 40,
					paintwhite);
					
				canvas.drawText(
					"Current Cpu Cycle: " + Interpreter.cycle, 0, 
					getHeight() - 55,
					paintwhite);
					
				canvas.drawText(
					"fireclouu", (int) (getWidth() / 1.1), 
					getHeight() - 10,
					paintwhite);

					
				cDrawText("NAN: " + System.nanoTime(), 0, getHeight() - 70);
				cDrawText("NAN: " + System.currentTimeMillis(), 0, getHeight() - 85);
				holder.unlockCanvasAndPost(canvas);
			}
		}
}
}
