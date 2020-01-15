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
		plotPixelCounterClock(cpu);
		
		if (!holder.getSurface().isValid()) {
			return;
		}

		c = holder.lockCanvas();
		
		c.drawColor(Color.BLACK);
		c.drawPoints(display, paintwhite);
		
		holder.unlockCanvasAndPost(c);
	}
	
	private void plotPixelCounterClock(CpuComponents cpu) {
		// rotate counter clockwise
		//           x, y
		// original: 0, 0
		// fixed:    0, 224
		
		// original: 1, 0
		// fixed:    0, 223
		
		// initialize every draw
		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		// vram starting point
		short vram = 0x2400;
		// array
		short loc = 0; // drawPoints feeds two values of float
		
		for (int x = 0; x < DISPLAY_HEIGHT; x++) {          // x-axis 224px (H)
			
			for (int y = DISPLAY_WIDTH; y > 0; y -= 8) {    // y-axis 256px (V)
				
				short pixel = cpu.memory[vram++];           // read memory byte
				if (pixel == 0) continue;                   // optimization
			
				for (byte scan = 0; scan < 8; scan++) {	    // read bits as pixel
					
					if (((pixel >> scan) & 0x1) == 1) {
						
						display[loc] = x * PIXEL_SIZE_WIDTH;                 // x
						display[loc + 1] = (y - scan) * PIXEL_SIZE_HEIGHT;   // y
						loc += 2; // point to empty stack
					
					}
					
				}
			}
			
		}
		

	}
	
	private void plotPixelDefault(CpuComponents cpu) {
		// initialize every draw
		display = new float[(DISPLAY_HEIGHT * DISPLAY_WIDTH)];
		// vram starting point
		short vram = 0x2400;
		// array
		short loc = 0; // drawPoints feeds two values of float
		
		for (int y = 0; y < DISPLAY_HEIGHT; y++) {       // y-axis 224px (V)

			for (int x = 0; x < DISPLAY_WIDTH; x += 8) { // x-axis 256px (H)
				
				short pixel = cpu.memory[vram++];        // read memory byte
				if (pixel == 0) continue;                // optimization
				
				for(byte scan = 0; scan < 8; scan++) {   // read bits as pixel
				
					if (((pixel >> scan) & 0x1) == 1) {
					
						display[loc] = (scan + x) * PIXEL_SIZE_WIDTH;   // x
						display[loc + 1] = y * PIXEL_SIZE_HEIGHT;       // y
						loc += 2; // point to empty stack
						
					}
				}
			}
		}
		
	}
	
	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);

		return mPaint;
	}
	
}
