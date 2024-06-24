package com.fireclouu.intel8080emu;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainActivity extends Activity implements Button.OnTouchListener, Button.OnClickListener
{
	Display mDisplay;
	Platform platform;
	
	// Buttons
	private Button
		mButtonCoin,
		mButtonP1Start,
		mButtonP1Left,
		mButtonP1Right,
		mButtonP1Fire,
		mButtonSetPlayer;

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
		byte playerPort = platform.getPlayerPort();
		int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
		boolean hasAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP;
		boolean isDown = action == MotionEvent.ACTION_DOWN;
		int buttonId = view.getId();
		byte key = 0;
		
		if (buttonId == R.id.btn_p1_coin) key = KeyInterrupts.KEY_COIN;
		if (buttonId == R.id.btn_p1_start) key = KeyInterrupts.KEY_P1_START;
		if (buttonId == R.id.btn_p1_fire) key = KeyInterrupts.KEY_FIRE;
		if (buttonId == R.id.btn_p1_left) key = KeyInterrupts.KEY_LEFT;
		if (buttonId == R.id.btn_p1_right) key = KeyInterrupts.KEY_RIGHT;
		
		if (hasAction) platform.sendInput(playerPort, key, isDown);
		return false;
	}
	
	@Override
	public void onClick(View view)
	{
		byte playerPort = platform.getPlayerPort();
		int buttonId = view.getId();
		if (buttonId == R.id.btn_change_player) {
			playerPort = playerPort == KeyInterrupts.INPUT_PORT_1 ? KeyInterrupts.INPUT_PORT_2 : KeyInterrupts.INPUT_PORT_1;
			platform.setPlayerPort(playerPort);
			mButtonSetPlayer.setText("P" + playerPort);
		}
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
		mButtonSetPlayer = findViewById(R.id.btn_change_player);
		
		mButtonCoin.setOnTouchListener(this);
		mButtonP1Start.setOnTouchListener(this);
		mButtonP1Left.setOnTouchListener(this);
		mButtonP1Fire.setOnTouchListener(this);
		mButtonP1Right.setOnTouchListener(this);
		mButtonSetPlayer.setOnClickListener(this);
	}
	
}
