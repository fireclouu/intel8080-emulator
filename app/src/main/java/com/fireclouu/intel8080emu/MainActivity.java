package com.fireclouu.intel8080emu;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainActivity extends Activity implements Button.OnTouchListener
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
	private final String KEY_SINGLE_INSTANCE = "singleInstance";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_emulation);
		
		init();
		startEmulation();
	}
	
	@Override
	protected void onResume() {
		platform.resume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		platform.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Toast.makeText(getApplicationContext(), "Emulation terminated", 
					   Toast.LENGTH_SHORT).show();
		platform.stop();
		platform.releaseResource();
		platform = null;
		super.onDestroy();
	}
	
	// separate interrupt from platform-specific data
	// move in implementation
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent)
	{
		view.performClick();
		if (view.getId() == R.id.btn_p1_coin) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_COIN;
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_COIN;
					break;
			}
		}
		if (view.getId() == R.id.btn_p1_start) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_START;
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_START;
					break;
			}
		}
		if (view.getId() == R.id.btn_p1_left) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_LEFT;
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_LEFT;
					break;
			}
		}
		if (view.getId() == R.id.btn_p1_fire) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_FIRE;
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_FIRE;
					break;
			}
		}
		if (view.getId() == R.id.btn_p1_right) {
			switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN:
					Emulator.port[1] |= Interrupts.KEY_RIGHT;
					break;
				case MotionEvent.ACTION_UP:
					Emulator.port[1] &= ~Interrupts.KEY_RIGHT;
					break;
			}
		}
		return false;
	}
	
	private void startEmulation() {
		if (platform == null) {
			platform = new Platform(this, mDisplay);
			Mmu.platform = platform;
		}
		platform.start();
		Toast.makeText(getApplicationContext(), "Emulation started", 
					   Toast.LENGTH_SHORT).show();
	}
	
	private void init() {
		mDisplay = findViewById(R.id.mainDisplay);
		
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
	}
	
}
