package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface DisplayAdapter
{
	int DISPLAY_WIDTH = (32 * 8); // 256 (0x2400 to 0x2407 = bit 0 to bit 7)
	int DISPLAY_HEIGHT = (224);   // 224
	int INIT_VRAM = 0x2400;
	
	byte ORIENTATION_DEFAULT = 0;
	byte ORIENTATION_COUNTERCLOCK = 1;
	
	void updateView(short[] memory);
	float[] getPos(int orientation);
	void startDisplay();
}
