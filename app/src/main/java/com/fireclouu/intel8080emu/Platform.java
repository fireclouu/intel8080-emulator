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
	
	public Platform(Context context, DisplayAdapter display, ApiAdapter media) {
		super(display, media);
		this.context = context;
	}
	
	@Override
	public InputStream openFile(String romName) {
		try
		{
			return context.getAssets().open(romName);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
