package com.fireclouu.intel8080emu.Emulator.BaseClass;

import com.fireclouu.intel8080emu.Emulator.*;
import java.io.*;

public abstract class PlatformAdapter implements Runnable
{
	public static Thread master;
	private static boolean isRunning = true;
	
	protected Emulator emulator;
	protected CpuComponents cpu;
	protected DisplayAdapter display;
	protected ApiAdapter media;
	protected StringUtils.File fileUtils;
	protected StringUtils.Component machineUtils;
	
	public static String OUT_MSG = "System OK!";
	public static String TEST_NAME;
	public static String BUILD_MSG[];
	public static int MSG_COUNT = 0;
	
	public Object pauseLock;
	
	// Stream file
	public abstract InputStream openFile(String romName);
	
	@Override
	public void run() {
		emulator.startEmulation(cpu, display, media);
	}
	
	public PlatformAdapter(DisplayAdapter display, ApiAdapter media) {
		this.display = display;
		this.media = media;
	}
	
	// Main
	public void startOp() {
		// set master state
		setStateMaster(true);
		
		// initial file check
		/*if(!isAllFileOK()) {
			OUT_MSG = "Some files could be corrupted, or no files specified";
			display.setView();
			display.readyToDraw = true;
			
			System.out.println(OUT_MSG);
			return;
		}*/

		// check if file is test file
		if (isTestFile()) {
			startTest();
			return;
		}
		// Start emulation
		startMain();
	}

	public void init() {
		// initialize cpu components
		cpu = new CpuComponents();
		// initialize emulator
		emulator = new Emulator();
		// display draw signal
		display.isDrawing = false;
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

	private void startTest() {
		// init
		init();
		// trigger debug
		StringUtils.Component.DEBUG = true;
		// view
		//display.readyToDraw = true;
		// dummy
		String name = "8080EXM.COM";
		
		//for (String name : fileUtils.FILES) {
			// BUILD MSG
			BUILD_MSG = new String[5000];
			BUILD_MSG[0] = "";
			// get test filename
			TEST_NAME = name;
			// reset objects
			init();
			display.startDisplay();
			display.isDrawing = true;
			
			// load file and injects
			// SOURCE: superzazu — intel 8080 c99
			// inject "out 1,a" at 0x0000 (signal to stop the test)
			cpu.memory[0x0000] = 0xD3;
			cpu.memory[0x0001] = 0x00;
			// inject "in a,0" at 0x0005 (signal to output some characters)
			cpu.memory[0x0005] = 0xDB;
			cpu.memory[0x0006] = 0x00;
			cpu.memory[0x0007] = 0xC9;
			// jump pc to 0x100 (to avoid executing test_finished to true);
			cpu.PC = 0x0100;
			loadFile(name, 0x100);
			// start emulation
			master = new Thread(this);
			master.start();
			// avoid overlaps
			/*try {
				mainThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/

			System.out.println(BUILD_MSG);
		//}
	}

	// check all files
	private boolean isAllFileOK() {
		for (String files : fileUtils.FILES) {
			if (!isAvailable(files)) return false;
		}
		try
		{
			isAvailable(fileUtils.FILES[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}

		return true;
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
		/*/try {
			pauseLock.wait();
		} catch (InterruptedException e) {}*/
	}

	public void appResume() {
		//pauseLock.notify();
	}
	
	// Master control
	public static void setStateMaster(boolean state) {
		isRunning = state;
	}
	
	public static boolean getStateMaster() {
		return isRunning;
	}
}
