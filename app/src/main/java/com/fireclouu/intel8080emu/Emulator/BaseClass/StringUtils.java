package com.fireclouu.intel8080emu.Emulator.BaseClass;

import java.util.*;

public final class StringUtils
{
	private StringUtils() { } // prevent instatiation

	public static final String getTime() {
		Date date = new Date();
		return String.format("%02d", date.getHours()) + ":" +
			String.format("%02d", date.getMinutes()) + ":" +
			String.format("%02d", date.getSeconds());
	}

	public static final class File {
		private File() { }

		public static final String[] FILES = {
			"invaders.h", "invaders.g",
			"invaders.f", "invaders.e",

			///  TESTS  ///
			//"TST8080.COM",
			//"8080PRE.COM",
			//"CPUTEST.COM",
			//"8080EXM.COM",
			//"8080EX1.COM",
			//"8080EXER.COM",
		};

		public static final int[] ROM_ADDRESS = {
			0x0000, 0x0800,
			0x1000, 0x1800,
			//0x0100
		};
	}

	public static final class Component {
		private Component() { }

		// DO NOT MODIFY
		public static boolean DEBUG = false;

		public static final boolean PRINT_LESS = true;
		public static final int PROGRAM_LENGTH = 0x10_000;
		public static final String STORAGE_LOCATION = "/sdcard/Download/";
		public static final long MAX_CYCLE_SPEED_PER_SECOND = 2_000_000;
	}
}
