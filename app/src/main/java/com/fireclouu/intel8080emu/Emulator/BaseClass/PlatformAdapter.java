package com.fireclouu.intel8080emu.Emulator.BaseClass;

import com.fireclouu.intel8080emu.Emulator.CpuComponents;
import com.fireclouu.intel8080emu.Emulator.Emulator;
import com.fireclouu.intel8080emu.Emulator.Mmu;
import com.fireclouu.intel8080emu.R;

import java.io.IOException;
import java.io.InputStream;
import android.os.*;
import java.util.concurrent.*;
import com.fireclouu.intel8080emu.Emulator.*;

public abstract class PlatformAdapter implements Runnable, ResourceAdapter {
	private Emulator emulator;
	private Mmu mmu;
	private CpuComponents cpu;
	private DisplayAdapter display;
	private Handler handler;
	private KeyInterrupts keyInterrupts;
	private ExecutorService executor;
	private boolean isLogging;
	
	public static String OUT_MSG = "System OK!";
	public static String[] BUILD_MSG;
	public static int MSG_COUNT = 0;
	
	public abstract InputStream openFile(String romName);
	public abstract void writeLog(String message);
	public abstract boolean isDrawing();
	
	@Override
	public void run() {
		emulator.start(cpu, display, this);
	}
	
	public PlatformAdapter(DisplayAdapter display) {
		this.display = display;
	}
	
	public void setDisplay (DisplayAdapter display) {
		this.display = display;
	}
	
	// Main
	public void start() {
		// mmu inject
		mmu = new Mmu();
		Mmu.resource = this;
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
		cpu = new CpuComponents();
		emulator = new Emulator(this, mmu);
		keyInterrupts = new KeyInterrupts(emulator);
		executor = Executors.newSingleThreadExecutor();
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
		handler = new Handler(Looper.getMainLooper());
		init();
		
		if(loadSplitFiles() == 0) {
			executor.execute(this);
		}
	}
	
	private void startTest() {
		// init
		init();
		// trigger debug
		StringUtils.Component.DEBUG = true;
		// testfiles container
		int counter = 0;
		for (String files : StringUtils.File.FILES)
		{
			// tf[counter] = loadFile(files, 0x100, false);
			counter++;
		}
		
		// BUILD MSG
		BUILD_MSG = new String[65355];
		BUILD_MSG[0] = "";

		// reset objects
		init();

		// start emulation
		executor.execute(this);
	}

	// check all files
	private boolean isAllFileOK() {
		for (String files : StringUtils.File.FILES) {
			if (!isAvailable(files)) return false;
		}
		
		return true;
	}

	public short[] loadFile(String filename, int addr, boolean sizeActual) {
		short[] holder = null;
		short[] tmp = new short[0x10_000];
		InputStream file = openFile(filename);
		short read;
		int counter = 0;
		try
		{
			while ((read = (short) file.read()) != -1) {
				tmp[addr++] = read;
				counter++;
			}
			file.close();
			file = null;
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
			return null;
		}
		
		holder = new short[(sizeActual ? counter : StringUtils.Component.PROGRAM_LENGTH)];
		
		counter = 0;
		for (short tmp2 : tmp)
		{
			holder[counter++] = tmp2;
		}
		
		return holder;
	}
	
	public void loadFile(String filename, int addr) {
		InputStream file = openFile(filename);
		short read;
		
		try
		{
			while ((read = (short) file.read()) != -1) {
				mmu.writeMemory(addr++, read);
			}
			file.close();
			file = null;
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
		}
	}
	
	private byte loadSplitFiles() {
		int counter = 0;
		for (String files : StringUtils.File.FILES) {
			if (isAvailable(files)) {
				loadFile(files, StringUtils.File.ROM_ADDRESS[counter++]);
			} else {
				System.out.println(OUT_MSG);
				return 1;
			}
		}
		return 0;
	}

	private boolean isAvailable(String filename) {
		if (StringUtils.File.FILES.length == 0) {
			OUT_MSG = "No files specified.";
			return false;
		}
		if (StringUtils.File.ROM_ADDRESS.length == 0) {
			OUT_MSG = "File is empty.";
			return false;
		}
		if (StringUtils.File.ROM_ADDRESS.length != StringUtils.File.FILES.length) {
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
		for(String name : StringUtils.File.FILES) {
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
	
	public void sendInput(int port, byte key, boolean isDown) {
		keyInterrupts.sendInput(port, key, isDown);
	}
	
	public byte getPlayerPort() {
		return keyInterrupts.getPlayerPort();
	}
	public void setPlayerPort(byte playerPort) {
		keyInterrupts.setPlayerPort(playerPort);
	}
	
	public void setPause(boolean value) {
		emulator.setPause(value);
	}
	
	public void setHighscore(int data) {
		
		int storedHiscore = getPrefs(StringUtils.ITEM_HISCORE);
		
		if (data > storedHiscore)
		{
			putPrefs(StringUtils.ITEM_HISCORE, data);
		}
	}

	public boolean isPaused() {
		return emulator.isPaused();
	}
	
	public void stop() {
		emulator.stop();
		executor.shutdown();
	}
	
	public boolean isLogging() {
		return isLogging;
	};
	
	public void toggleLog(boolean value) {
		this.isLogging = value;
	};
	
	public void togglePause() {
		boolean pause = !emulator.isPaused();
		emulator.setPause(pause);
	}
}
