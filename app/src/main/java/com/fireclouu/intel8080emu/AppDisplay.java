package com.fireclouu.intel8080emu;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.util.*;

public class AppDisplay extends SurfaceView implements SurfaceHolder.Callback, DisplayAdapter
{
	// get float value only
	// on emulation class devise array that can hold 0x2400 - 0x3fff and pass it here
	// do the loop here! instead of looping on another class
	
	// make tests for display
	Thread master;
	Canvas canvas;
	
	// make adaptive
	private float PIXEL_SIZE = 3.18f;
	private float PIXEL_SIZE_WIDTH = PIXEL_SIZE;
	private float PIXEL_SIZE_HEIGHT = PIXEL_SIZE;

	Paint paintred;
	Paint paintwhite;
	Paint paintgreen;
	Paint textPaint;
	
	private short[] memory;
	ArrayList<Float> plotList;
	
	private boolean isStarting = true;
	SurfaceHolder holder;
	
	private LinkedList<Long> times = new LinkedList<Long>(){{
			add(System.nanoTime());
		}};
	
	public AppDisplay(Context context) {
		super(context); 
		init();
	}
	
	public AppDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	public void surfaceCreated(SurfaceHolder p1) {
		// TODO
	}

	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4) {
		// TODO: Implement this method
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder p1) {
		// TODO: Implement this method
	}
	private void init() {
		holder = getHolder();
		
		//holder.setFormat(holder.SURFACE_TYPE_GPU);
		//holder.setFixedSize(480, 858);
		//holder.setFixedSize(240, 352);
		
		paintred = setPaint(Color.RED);
		paintwhite = setPaint(Color.WHITE);
		paintgreen = setPaint(Color.GREEN);

		textPaint = setPaint(Color.WHITE);
		textPaint.setTextSize(12);
		
		runState = true;
	}
	
	@Override
	public void updateView(short[] memory) {
		this.memory = memory;
	}
	
	@Override
	public float[] getPos(int orientation) {
		
		plotList = new ArrayList<Float>();

		short vram = INIT_VRAM;
		float[] plot;
		int counter = 0;
		
		int x = 0, y = 0;
		int cond_x;
		int cond_y;
		
		boolean swap = false;
		int setBit;
		
		switch (orientation) {
			case ORIENTATION_COUNTERCLOCK:
				x = -DISPLAY_WIDTH;
				y = 0;
				setBit = -1;
				
				// swap
				swap = true;
				break;
			default:
				x = 0;
				y = 0;
				setBit = 1;
		}
		
		// automate
		cond_x = (x == 0) ? DISPLAY_WIDTH  : 0;
		cond_y = (y == 0) ? DISPLAY_HEIGHT : 0;
		
		for (int ty = y; ty < cond_y; ty += 1)
		{
			for (int tx = x; tx < cond_x; tx += 8)
			{
				short data = this.memory[vram++];	// increment location of vram to decode
				for (int bit = 0; bit < 8; bit++)
				{
					if (( (data >> bit) & 1) == 1)
					{
						if (swap) {
							plotList.add((Math.abs(ty)) * PIXEL_SIZE_HEIGHT);
							plotList.add((Math.abs(tx)  + (bit * setBit)) * PIXEL_SIZE_WIDTH);
						} else {
							plotList.add((Math.abs(tx)  + (bit * setBit)) * PIXEL_SIZE_WIDTH);
							plotList.add((Math.abs(ty)) * PIXEL_SIZE_HEIGHT);
						}
					}
				}
			}	
		}
		
		plot = new float[plotList.size()];
		for (float pos : plotList) plot[counter++] = pos;
		
		isStarting = false;	// unraise flag
		
		return plot;
	}
	
	@Override
	public void startDisplay() {
		if(StringUtils.Component.DEBUG) {
			master = new Thread(new DebugThread());
		} else {
			master = new Thread(new DrawThread());
		}
		
		master.start();
	}
	
	private Paint setPaint(int color) {
		Paint mPaint;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);
		
		return mPaint;
	}
	
	private final int MAX_SIZE = 100;
	private final double NANOS = 1000000000.0;
	
	// https://stackoverflow.com/questions/10210439/how-to-count-the-framerate-with-which-a-surfaceview-refreshes
	/** Calculates and returns frames per second */
	private double fps() {
		long lastTime = System.nanoTime();
		double difference = (lastTime - times.getFirst()) / NANOS;
		times.addLast(lastTime);
		int size = times.size();
		if (size > MAX_SIZE) {
			times.removeFirst();
		}
		
		return difference > 0 ? (int) ((times.size() / difference) * 100) / 100.0 : 0.0;
	}
	
	private String parseFps(double fps) {
		return String.format("fps: %.2f", fps);
	}
	
	private float getAdaptiveSize() {
		float adapt_h = ( ((float) getHeight()) / 100.0f) / 5.0f;
		float adapt_w = ( ((float)  getWidth()) / 100.0f) / 5.0f;
		return (adapt_h + adapt_w) / 1.25f;
	}
	
	class DrawThread implements Runnable {
	@Override
	public void run() {
		while (!holder.getSurface().isValid()) {
			// container
			PIXEL_SIZE = getAdaptiveSize();
			PIXEL_SIZE_WIDTH = PIXEL_SIZE;
			PIXEL_SIZE_HEIGHT = PIXEL_SIZE;
			paintwhite.setStrokeWidth(PIXEL_SIZE);
		}
		
		while (memory == null) continue;
		
		while(PlatformAdapter.isMasterRunning() && runState) {
			
			if (!holder.getSurface().isValid() & !holder.isCreating()) continue;
			
			// canvas
			canvas = holder.lockCanvas();
			canvas.drawColor(Color.BLACK);		
			canvas.drawPoints(getPos(ORIENTATION_COUNTERCLOCK), paintwhite);
			
			if (isStarting) canvas.drawText(Platform.OUT_MSG, 0, 10, paintwhite);
			
			// fps
			canvas.drawText(parseFps(fps()), 0, getHeight() - 10, paintwhite);
			// Emulation thread speed
			canvas.drawText("Thread speed: " + Emulator.actual_cycle, 0, getHeight() - 25, paintwhite);
			//cycle
			if (Emulator.isCycleCorrect()) {
				canvas.drawText(Emulator.cycleInfo, 0, getHeight() - 40, paintgreen);
			} else {
				canvas.drawText(Emulator.cycleInfo, 0, getHeight() - 40, paintred);
			}
			
			canvas.drawText("fireclouu", (int) (getWidth() / 1.1), getHeight() - 10, paintwhite);
			
			// release
			holder.unlockCanvasAndPost(canvas);
		}
	}
	
}
class DebugThread implements Runnable {
		@Override
		public void run() {
			while(PlatformAdapter.isMasterRunning() && runState) {
				if (!holder.getSurface().isValid()) continue;
				
				canvas = holder.lockCanvas();
				
				canvas.drawColor(Color.BLACK);
				if (isStarting) {
					canvas.drawText(
						Platform.OUT_MSG, 0, 
						10,
						paintwhite);
				}
				
				canvas.drawText(
					StringUtils.getTime(), getWidth() - 60, 15, paintwhite);
				
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
				
				holder.unlockCanvasAndPost(canvas);
			}
		}
}
}
