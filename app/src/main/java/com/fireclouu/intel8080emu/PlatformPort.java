package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import com.fireclouu.intel8080emu.Emulator.*;
import java.io.*;
import android.widget.*;

public class PlatformPort
{
	Context context;
	CpuComponents cpu;
	Handler dispHandler;
	Runnable dispRunnable;
	MainGraphics mGraphics;
	
	private short[] memory;
	EmulationThread emulator;
	Thread displayThread;
	
	// CONSTRUCTOR
	public PlatformPort(Context context, MainGraphics mGraphics) {
		this.mGraphics = mGraphics;
		this.context = context;
		
		init();
	}
	
	// INIT
	private void init() {
		// main cpu object
		if (loadRom(ProgramUtils.Rom.FILE_NAME)) {
			cpu = new CpuComponents( memory );
			emulator = new EmulationThread(cpu, mGraphics);
		} else {
			Toast.makeText(
				context,
				R.string.warn_file_error,
				Toast.LENGTH_LONG).show();
		}
	}
	
	// FUNCTION
	public void startEmulator() {
		emulator.start(); // emulation
	}
	
	private boolean loadRom(String[] fileName) {
		InputStream file;
		short read;
		short[] holder = new short[ProgramUtils.Machine.PROGRAM_LENGTH];
		
		if (fileName.length > 1) {
			// for split files
			for(int i = 0; i < ProgramUtils.Rom.FILE_NAME.length; i++) {
				file = openFile(ProgramUtils.Rom.FILE_NAME[i]);
				int currentAddr = ProgramUtils.Rom.ROM_ADDRESS[i];
				
				try {	
					for(int a = 0; (read = (short) file.read()) != -1; a++) {
						holder[currentAddr + a] = read;
					}
				} catch (IOException e) {
					return false;
				}
			}		
		} else {
			// single file
			try {
				file = openFile(fileName[0]);
				
				for(int a = 0; (read = (short) file.read()) != -1; a++) {
					holder[ProgramUtils.Rom.ROM_ADDRESS[0] + a] = read;
				}
			} catch (IOException e) {
				return false;
			}
		}
		
		// pass buffered rom
		this.memory = holder;
		return true;
	}
	
	private InputStream openFile(String romName) {
		try {
			return ((Activity)context).getAssets().open(romName);
		} catch (IOException e) {
			return null;
		}
	}
}

class EmulationThread extends Thread
{
	CpuComponents cpu;
	MainGraphics mGraphics;
	
	public EmulationThread(CpuComponents cpu, MainGraphics mGraphics) {
		this.cpu = cpu;
		this.mGraphics = mGraphics;
	}
	
	@Override
	public void run() {
		super.run();
		new Emulation(cpu, mGraphics); // can update display over emulation thread
	}
	
}
