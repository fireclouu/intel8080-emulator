package com.fireclouu.intel8080emu;

public class ProgramInfo
{
	///   ROM META   ///
	public  static final int PROGRAM_LENGTH = 0xffff;

	///   SPLIT ROMS LIST   ///
	public static final String[] romName = {
		"invaders.h",
		"invaders.g",
		"invaders.f",
		"invaders.e"
		//"cpudiag.bin"
		//"8080EX1.COM"
	};

	///   LOAD ADDRESS   ///
	public static final int[] romAddr = {
		0x0000,
		0x0800,
		0x1000,
		0x1800
		//0x0100
	};
}
