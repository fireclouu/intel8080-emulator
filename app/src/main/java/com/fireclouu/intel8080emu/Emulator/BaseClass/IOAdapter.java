package com.fireclouu.intel8080emu.Emulator.BaseClass;

import com.fireclouu.intel8080emu.Emulator.*;

public interface IOAdapter
{
	public static final int PORT_KEY_LEFT  = 0;
	public static final int PORT_KEY_UP    = 1;
	public static final int PORT_KEY_RIGHT = 2;
	public static final int PORT_KEY_DOWN  = 3;
	public static final int PORT_KEY_FIRE  = 4;
	public static final int PORT_COIN      = 5;
	
	public short handleIN(CpuComponents cpu, short port);
	public void handleOUT(CpuComponents cpu, short port, short value);
}
