package com.fireclouu.intel8080emu;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.content.pm.*;
import android.view.*;

public class MainActivity extends Activity 
{
	DisplayView mDisplay;
	PlatformPort ms; // machine specific
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// render to hardware
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		mDisplay = new DisplayView(this);
        super.onCreate(savedInstanceState);
        setContentView(this.mDisplay);
		
		init();
		ms.startEmulator();
	}
	
	private void init() {
		ms = new PlatformPort(this, mDisplay);
	}
	
}
