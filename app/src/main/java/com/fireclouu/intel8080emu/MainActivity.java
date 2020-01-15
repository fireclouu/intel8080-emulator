package com.fireclouu.intel8080emu;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.content.pm.*;
import android.view.*;
import com.fireclouu.intel8080emu.Emulator.*;

public class MainActivity extends Activity 
{
	MainGraphics mGraphics;
	PlatformPort ms; // machine specific
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// render to hardware
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		mGraphics = new MainGraphics(this);
		
        super.onCreate(savedInstanceState);
        setContentView(mGraphics);
		
		
		init();
		ms.startEmulator();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Program notifier
		ProgramUtils.Machine.isRunning = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// Program notifier
		ProgramUtils.Machine.isRunning = false;
	}
	
	
	private void init() {
		ms = new PlatformPort(this, mGraphics);
		
		ProgramUtils.Machine.isRunning = true;
	}
	
}
