package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.fireclouu.intel8080emu.Emulator.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class MainActivity extends Activity implements OnTouchListener
{
	AppDisplay mDisplay;
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
		// Hacky avoidance to bugs
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
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
		super.onResume();
		platform.setStateMaster(true);
		
		if (firstCall) {
			firstCall = false;
			return;
		} else if (!firstCall & !PlatformAdapter.isMasterRunning()) {
			PlatformAdapter.setStateMaster(true);
			DisplayAdapter.runState = false;
			initAndStart();
		} else if (!firstCall) {
			DisplayAdapter.runState = true;
			mDisplay.startDisplay();
			platform.appResume();
		}
	}

	@Override
	protected void onPause() {
		platform.appPause();
		
		super.onPause();
		DisplayAdapter.runState = false;
	}

	@Override
	protected void onDestroy() {
		platform.appPause();
		platform.setStateMaster(false);
		platform.releaseResource();
		super.onDestroy();
	}

  // separate interrupt from platform-specific data
  // move in implementation
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		switch(p1.getId())
		{
			case R.id.btn_p1_coin:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
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
				break;
			case R.id.btn_p1_start:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
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

				break;
			case R.id.bt_p1_left:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
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
				break;
			case R.id.btn_p1_fire:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
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
				break;
			case R.id.btn_p1_right:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
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
		}
		return false;
	}


	private void initAndStart() {
		platform = new Platform(this, mDisplay);
		
		// Buttons
		mButtonCoin = findViewById(R.id.btn_p1_coin);
		mButtonP1Start = findViewById(R.id.btn_p1_start);
		mButtonP1Left = findViewById(R.id.bt_p1_left);
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
