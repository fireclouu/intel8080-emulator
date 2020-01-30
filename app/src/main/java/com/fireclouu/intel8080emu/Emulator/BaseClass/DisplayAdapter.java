package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface DisplayAdapter
{
	public boolean runState;
	
	public final int DISPLAY_WIDTH = (32 * 8); // 256 (0x2400 to 0x2407 = bit 0 to bit 7)
	public final int DISPLAY_HEIGHT = (224);   // 224
	public final int INIT_VRAM = 0x2400;
	
	public final byte ORIENTATION_DEFAULT = 0;
	public final byte ORIENTATION_COUNTERCLOCK = 1;
	
	public boolean isDrawing;
	
	public void updateView(short[] memory);
	public float[] getPos(int orientation);
	public void startDisplay();
}
