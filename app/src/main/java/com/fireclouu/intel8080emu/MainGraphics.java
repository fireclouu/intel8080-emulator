package com.fireclouu.intel8080emu;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainGraphics extends SurfaceView implements Runnable
{
	// get float value only
	// on emulation class devise array that can hold 0x2400 - 0x3fff and pass it here
	// do the loop here! instead of looping on another class
	// make isLoaded boolean
	
	private final byte ORIENTATION_DEFAULT = 0;
	private final byte ORIENTATION_COUNTER_CLOCKWISE = 1;
	
	private final float PIXEL_SIZE = 3.18f;
	private final float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private final float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;

	private final int DISPLAY_WIDTH = 32 * 8; // 256 (0x2400 to 0x2407 = bit 0 to bit 7)
	private final int DISPLAY_HEIGHT = 224;   // 224
	
	Paint paintred;
	Paint paintwhite;
	Paint paintgreen;
	
	private float[] display;
	private short[] memory;
	public boolean isMemLoaded = false;
	
	private boolean isStarting = true;
	
	int frameCount = 0;
	
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
		memory = new short[ProgramUtils.Machine.PROGRAM_LENGTH];
		
		Thread master = new Thread(this);
		
		master.start();
	}
	
	private void drawImage(Canvas canvas) {
		// rotate counter clockwise
		//           x, y
		// original: 0, 0
		// fixed:    0, 224

		// original: 1, 0
		// fixed:    0, 223

		// vram starting point
		int vram = ProgramUtils.Machine.VRAM_LOCATION;

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
	
	
//	private void drawImage(Canvas canvas) {
//		// rotate counter clockwise
//		//           x, y
//		// original: 0, 0
//		// fixed:    0, 224
//		
//		// original: 1, 0
//		// fixed:    0, 223
//		
//		// vram starting point
//		short vram = ProgramUtils.Machine.VRAM_LOCATION;
//		
//		for (int x = 0; x < DISPLAY_HEIGHT; x++) {          // x-axis 224px (H)
//			
//			for (int y = DISPLAY_WIDTH; y > 0; y -= 8) {    // y-axis 256px (V)
//				
//				short pixel = this.memory[vram++];            // read memory byte
//				if (pixel == 0) continue;                   // optimization
//			
//				for (byte scan = 0; scan < 8; scan++) {	    // read bits as pixel
//					
//					if (((pixel) & 0x1) == 1)
//						canvas.drawPoint(x * PIXEL_SIZE_WIDTH,
//							(y - scan) * PIXEL_SIZE_HEIGHT, paintwhite);
//					
//					pixel >>= 1;
//					
//					if(pixel == 0) break; // optimization
//				}
//			}
//			
//		}
//		
//
//	}
//	
	private void plotPixelDefault(Canvas canvas) {
		// vram starting point
		short vram = ProgramUtils.Machine.VRAM_LOCATION;
		
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
	
	public void setMemory(short[] memory) {
		this.memory = memory;
		isMemLoaded = true;
	}
	
	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);

		return mPaint;
	}
	
	@Override
	public void run() {
		while(true) {
			if ( (!holder.getSurface().isValid()) || (!isMemLoaded) ) continue;
			frameCount++;
			
			Canvas canvas = holder.lockCanvas();

			canvas.drawColor(Color.BLACK);
			
			drawImage(canvas); // primary updating method
			
			if (isStarting) {
				canvas.drawText(
					"System Ready...", 0, 
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


			canvas.drawText(
				"fireclouu", (int) (getWidth() / 1.1), 
				getHeight() - 10,
				paintwhite);

			holder.unlockCanvasAndPost(canvas);
			
			isMemLoaded = false;
		}
	}
	
}
