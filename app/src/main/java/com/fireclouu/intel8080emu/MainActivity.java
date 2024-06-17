package com.fireclouu.intel8080emu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.fireclouu.intel8080emu.Emulator.Emulator;
import com.fireclouu.intel8080emu.Emulator.Interrupts;

public class MainActivity extends Activity implements View.OnTouchListener
{
	Display mDisplay;
	Platform platform;
	
	// Buttons
	private Button
		mButtonCoin,
		mButtonP1Start,
		mButtonP1Left,
		mButtonP1Right,
		mButtonP1Fire;

	boolean firstCall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		getWindow().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// render to hardware
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		// set window first
        super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_emulation);
		mDisplay = findViewById(R.id.mainDisplay);
		initAndStart();
	}
	
	@Override
	protected void onResume() {
//		platform.appResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
//		platform.appPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		platform.releaseResource();
		super.onDestroy();
	}

	// separate interrupt from platform-specific data
	// move in implementation
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent)
	{
		if (view.getId() == R.id.btn_p1_coin) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_COIN;
					mButtonCoin.setTextSize(8);
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_COIN;
					mButtonCoin.setTextSize(10);
					break;
			}
			view.performClick();
			return true;
		}
		if (view.getId() == R.id.btn_p1_start) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_START;
					mButtonP1Start.setTextSize(8);
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_START;
					mButtonP1Start.setTextSize(10);
					break;
			}
			view.performClick();
			return true;
		}
		if (view.getId() == R.id.btn_p1_left) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_LEFT;
					mButtonP1Left.setTextSize(8);
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_LEFT;
					mButtonP1Left.setTextSize(10);
					break;
			}
			view.performClick();
			return true;
		}
		if (view.getId() == R.id.btn_p1_fire) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_FIRE;
					mButtonP1Fire.setTextSize(8);
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_FIRE;
					mButtonP1Fire.setTextSize(10);
					break;
			}
			view.performClick();
			return true;
		}
		if (view.getId() == R.id.btn_p1_right) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_RIGHT;
					mButtonP1Right.setTextSize(8);
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_RIGHT;
					mButtonP1Right.setTextSize(10);
					break;
			}
			view.performClick();
			return true;
		}
		return false;
	}

	private void initAndStart() {
		platform = new Platform(this, mDisplay);
		
		// Buttons
		mButtonCoin = findViewById(R.id.btn_p1_coin);
		mButtonP1Start = findViewById(R.id.btn_p1_start);
		mButtonP1Left = findViewById(R.id.btn_p1_left);
		mButtonP1Fire = findViewById(R.id.btn_p1_fire);
		mButtonP1Right = findViewById(R.id.btn_p1_right);
		
		mButtonCoin.setOnTouchListener(this);
		mButtonP1Start.setOnTouchListener(this);
		mButtonP1Left.setOnTouchListener(this);
		mButtonP1Fire.setOnTouchListener(this);
		mButtonP1Right.setOnTouchListener(this);
		
		// Run
		platform.startOp();
	}
	
}
