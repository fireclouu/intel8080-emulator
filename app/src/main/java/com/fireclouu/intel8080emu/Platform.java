package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import com.fireclouu.intel8080emu.Emulator.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.io.*;

public class Platform extends PlatformAdapter
{
	Context context;
	CpuComponents cpu;
	AppDisplay mDisplay;
	
	// CONSTRUCTOR
	public Platform(Context context, AppDisplay mDisplay) {
		this.mDisplay = mDisplay; // set AppDisplay variable
		this.context = context;
	}
	
	@Override
	public InputStream openFile(String romName) {
		try
		{
			return ((Activity)context).getAssets().open(romName);
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public void makeDisplay() {
		this.display = mDisplay; // set DisplayAdapter variable
	}
}
