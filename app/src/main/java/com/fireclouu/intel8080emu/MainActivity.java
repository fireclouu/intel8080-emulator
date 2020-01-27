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
import android.media.*;

public class MainActivity extends Activity implements OnTouchListener
{
	AppDisplay mDisplay;
	public static SoundManager media;
	
	// need to optimize / on separate class
	public static Vibrator vibrator;
	
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
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		// set window first
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
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
		} else if (!firstCall) {
			DisplayAdapter.runState = true;
			mDisplay.startDisplay();
			ms.appResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		DisplayAdapter.runState = false;
		ms.appPause();
	}

	@Override
	protected void onDestroy() {
		ms.setStateMaster(false);
		media.releaseResource();
		super.onDestroy();
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
						mButtonCoin.setTextSize(8);
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1111_1110;
						mButtonCoin.setTextSize(10);
						break;
				}
				
				break;
			case R.id.btn_p1_start:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0000_0100;
						mButtonP1Start.setTextSize(8);
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1111_1011;
						mButtonP1Start.setTextSize(10);
						break;
				}

				break;
			case R.id.bt_p1_left:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0010_0000;
						mButtonP1Left.setTextSize(8);
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1101_1111;
						mButtonP1Left.setTextSize(10);
						break;
				}

				break;
			case R.id.btn_p1_fire:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0001_0000;
						mButtonP1Fire.setTextSize(8);
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1110_1111;
						mButtonP1Fire.setTextSize(10);
						break;
				}

				break;
			case R.id.btn_p1_right:
				switch(p2.getAction() & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
						Emulator.port[1] |= 0b_0100_0000;
						mButtonP1Right.setTextSize(8);
						break;
					case MotionEvent.ACTION_UP:
						Emulator.port[1] &= 0b_1011_1111;
						mButtonP1Right.setTextSize(10);
						break;
				}
		}
		return false;
	}
	
	private void initAndStart() {
		
		// Display and media
		media = new SoundManager(getApplicationContext());
		
		// Media
		media.setEffectShipHit(R.raw.ship_hit);
		media.setEffectAlienKilled(R.raw.alien_killed);
		media.setEffectAlienMove(
			R.raw.enemy_move_1,
			R.raw.enemy_move_2,
			R.raw.enemy_move_3,
			R.raw.enemy_move_4
		);
		media.setEffectFire(R.raw.fire);
		media.setEffectPlayerExploded(R.raw.explosion);
		media.setEffectShipIncoming(R.raw.ship_incoming);
		loadResources();
		
		ms = new Platform(this, mDisplay, media);
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
	
	public static void loadResources() {
		media.setEffectShipIncoming(R.raw.ship_incoming);
	}
	
}
