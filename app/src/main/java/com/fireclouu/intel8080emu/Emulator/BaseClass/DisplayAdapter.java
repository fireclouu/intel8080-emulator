package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface DisplayAdapter
{
	public final byte ORIENTATION_DEFAULT = 0;
	public final byte ORIENTATION_COUNTERCLOCK = 1;
	
	public boolean isMemLoaded;
	
	public void setDraws(short[] memory);
}
