package com.fireclouu.intel8080emu;

import android.app.*;
import android.os.*;
import android.view.*;

public class MainActivity extends Activity 
{
	AppDisplay mDisplay;
	Platform ms; // machine specific
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// render to hardware
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		// set window first
		mDisplay = new AppDisplay(this);
        super.onCreate(savedInstanceState);
        setContentView(mDisplay);
		
		init();
	 	ms.startOp();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Program notifier
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// Program notifier
	}
	
	
	private void init() {
		ms = new Platform(this, mDisplay);
	}
}
