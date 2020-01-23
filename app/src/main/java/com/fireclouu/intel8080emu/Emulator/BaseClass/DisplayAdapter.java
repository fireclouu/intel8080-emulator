package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface DisplayAdapter
{
	public boolean runState;
	
	public final byte ORIENTATION_DEFAULT = 0;
	public final byte ORIENTATION_COUNTERCLOCK = 1;
	
	public boolean isDrawing;
	
	public void updateView(short[] memory);
	public void startView();
}
