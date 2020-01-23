package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import android.widget.*;
import android.widget.FrameLayout.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainActivity extends Activity implements OnTouchListener
{
	AppDisplay mDisplay;
	Platform ms; // machine specific
	WakelockApplication wl;
	public static final int INTENT_FLAG_SINGLE_ACTIVITY = Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP;
	
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		// set window first
		
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_emulation);
		
		mDisplay = findViewById(R.id.mainDisplay);
		
		initAndStart();
		wl.startWakelock();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ms.setStateMaster(true);
		
		if (firstCall) {
			firstCall = false;
			return;
		} else if (!firstCall & !PlatformAdapter.master.isAlive()) {
			initAndStart();
		} else {
			DisplayAdapter.runState = true;
			mDisplay.startView();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		DisplayAdapter.runState = false;
	}
	
	// ontouch
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		switch(p1.getId())
		{
			case R.id.btn_p1_coin:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0000_0001;
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1111_1110;
						break;
				}
				
				break;
			case R.id.btn_p1_start:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0000_0100;
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1111_1011;
						break;
				}

				break;
			case R.id.bt_p1_left:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0010_0000;
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1101_1111;
						break;
				}

				break;
			case R.id.btn_p1_fire:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0001_0000;
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1110_1111;
						break;
				}

				break;
			case R.id.btn_p1_right:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0100_0000;
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1011_1111;
						break;
				}
		}
		return false;
	}
	
	private void initAndStart() {
		ms = new Platform(this, mDisplay);
		wl = new WakelockApplication(this);
		
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
		ms.startOp();
	}
}
