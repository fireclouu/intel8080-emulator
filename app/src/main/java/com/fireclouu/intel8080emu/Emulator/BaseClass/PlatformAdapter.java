package com.fireclouu.intel8080emu.Emulator.BaseClass;

import com.fireclouu.intel8080emu.*;
import com.fireclouu.intel8080emu.Emulator.*;
import java.io.*;
import java.util.*;

public abstract class PlatformAdapter implements Runnable, ApiAdapter
{
	private Thread master;
	public static boolean stateMaster = true;
	
	protected Emulator emulator;
	protected CpuComponents cpu;
	protected DisplayAdapter display;
	protected StringUtils.File fileUtils;
	protected StringUtils.Component machineUtils;
	
	public static String OUT_MSG = "System OK!";
	public static String TEST_NAME;
	public static String BUILD_MSG[];
	public static int MSG_COUNT = 0;
	
	// Stream file
	public abstract InputStream openFile(String romName);
	
	@Override
	public void run() {
		while (isMasterRunning()) {
			emulator.startEmulation(cpu, display, this);
		}
	}
	
	public PlatformAdapter(DisplayAdapter display) {
		this.display = display;
	}
	
	// Main
	public void startOp() {
		// init master state flag
		setStateMaster(true);
		
		// initial file check
		if (!isTestFile()) {
				if(!isAllFileOK()) {
				OUT_MSG = "Some files could be corrupted, or no files specified";
				System.out.println(OUT_MSG);
				return;
			}	
			// Start emulation
			startMain();
			return;
		}
		// test file
		startTest();
	}

	public void init() {
		// initialize cpu components
		cpu = new CpuComponents();
		// initialize emulator
		emulator = new Emulator();
		// display draw signal
		display.isDrawing = false;
		// media
		setEffectShipHit(R.raw.ship_hit);
		setEffectAlienKilled(R.raw.alien_killed);
		setEffectAlienMove(
			R.raw.enemy_move_1,
			R.raw.enemy_move_2,
			R.raw.enemy_move_3,
			R.raw.enemy_move_4
		);
		setEffectFire(R.raw.fire);
		setEffectPlayerExploded(R.raw.explosion);
		setEffectShipIncoming(R.raw.ship_incoming);
	}

	private void startMain() {
		// reset objects
		init();
		// set view
		display.startDisplay();
		// load and start emulation
		if(loadSplitFiles() == 0) {
			master = new Thread(this);
			master.start();
		}
	}
	
	public static short[][] tf = new short[StringUtils.File.FILES.length][0x10_000];
	private void startTest() {
		// init
		init();
		// trigger debug
		StringUtils.Component.DEBUG = true;
		// testfiles container
		int counter = 0;
		for (String files : StringUtils.File.FILES)
		{
			tf[counter] = loadFile(files, 0x100, false);
			counter++;
		}
		
	
		// BUILD MSG
		BUILD_MSG = new String[65355];
		BUILD_MSG[0] = "";

		// reset objects
		init();
		display.startDisplay();
		display.isDrawing = true;

		// start emulation
		master = new Thread(this);
		master.start();
		
	}

	// check all files
	private boolean isAllFileOK() {
		for (String files : fileUtils.FILES) {
			if (!isAvailable(files)) return false;
		}
		
		return true;
	}
	// Read and buffer file
	public short[] loadFile(String filename, int addr, boolean sizeActual) {
		// holder
		short[] holder = null;
		short[] tmp = new short[0x10_000];
		// read file stream
		InputStream file = openFile(filename);
		// piece container
		short read;
		int counter = 0;
		try
		{
			while ((read = (short) file.read()) != -1) {
				tmp[addr++] = read;
				counter++;
			}
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
			return null;
		}
		
		holder = new short[(sizeActual ? counter : machineUtils.PROGRAM_LENGTH)];
		
		counter = 0;
		for (short tmp2 : tmp)
		{
			holder[counter++] = tmp2;
		}
		
		return holder;
	}
	
	// Read and buffer file
	public void loadFile(String filename, int addr) {
		// read file stream
		InputStream file = openFile(filename);
		// piece container
		short read;
		
		try
		{
			while ((read = (short) file.read()) != -1) {
				cpu.memory[addr++] = read;
			}
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
		}
	}
	private byte loadSplitFiles() {
		int counter = 0;
		for (String files : fileUtils.FILES) {
			if (isAvailable(files)) {
				loadFile(files, fileUtils.ROM_ADDRESS[counter++]);
			} else {
				display.isDrawing = true;
				System.out.println(OUT_MSG);
				return 1; // ERROR
			}
		}
		return 0; // SUCCESS
	}

	// File check
	private boolean isAvailable(String filename) {
		if (fileUtils.FILES.length == 0) {
			OUT_MSG = "No files specified.";
			return false;
		}
		if (fileUtils.ROM_ADDRESS.length == 0) {
			OUT_MSG = "File online, but no starting memory address specified.";
			return false;
		}
		if (fileUtils.ROM_ADDRESS.length != fileUtils.FILES.length) {
			OUT_MSG = "File online, but roms and memory address unaligned.";
			return false;
		}
		try
		{
			if (openFile(filename) == null) {
				OUT_MSG = "File \"" + filename + "\" could not be found.";
				return false;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		OUT_MSG = "File online , loaded successfully!";
		return true;
	}

	private boolean isTestFile() {
		for(String name : fileUtils.FILES) {
			switch (name) {
				case "cpudiag.bin":
				case "8080EX1.COM":
				case "8080EXER.COM":
				case "CPUTEST.COM":
				case "8080EXM.COM":
				case "8080PRE.COM":
				case "TST8080.COM":
					return true;
			}
		}
		return false;
	}
	
	// Machine scenarios
	public void appPause() {
		Emulator.stateMaster = false;
		
		// store highscore
		if ((cpu.memory[0x20f5] << 8 | cpu.memory[0x20f4])
				> (getPrefs("hsm") << 8 | getPrefs("hsl")))
			{
				putPrefs(StringUtils.ITEM_HISCORE_MSB, cpu.memory[0x20f5]);
				putPrefs(StringUtils.ITEM_HISCORE_LSB, cpu.memory[0x20f4]);
			}
	}

	public void appResume() {
		Emulator.stateMaster = true;
	}
	
	// Master control
	public static void setStateMaster(boolean state) {
		stateMaster = state;
	}
	
	public static boolean isMasterRunning() {
		return stateMaster;
	}
}
