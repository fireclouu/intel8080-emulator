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
	ProgramInfo info;
	
	Context context;
	CpuComponents cpu;
	Handler mainHandler, emuHandler, dispHandler;
	Runnable mainRunnable, emuRunnable, dispRunnable;
	DisplayView mDisplay;
	
	private short[] memory;
	EmulationThread emulator;
	
	
	public PlatformPort(Context context, DisplayView mDisplay) {
		this.mDisplay = mDisplay;
		this.context = context;
		
		init();
	}
	
	public void startEmulator() {
		
		// DISPLAY THREAD
		dispHandler = new Handler();
		dispRunnable = new Runnable() {

			@Override
			public void run() {
				mDisplay.updateView(cpu);
				dispHandler.post(dispRunnable);
			}
			
			
		};
		
		dispHandler.post(dispRunnable);
		emulator.start();	
	}
	
	private boolean loadRom(String[] fileName) {
		InputStream file;
		short read;
		short[] holder = new short[info.PROGRAM_LENGTH];
		
		if (fileName.length > 1) {
			// for split files
			for(int i = 0; i < info.romName.length; i++) {
				file = openFile(info.romName[i]);
				int currentAddr = info.romAddr[i];
				
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
					holder[info.romAddr[0] + a] = read;
				}
			} catch (IOException e) {
				return false;
			}
		}
		
		// pass buffered rom
		this.memory = holder;
		return true;
	}
	
	// asset file reader
	private InputStream openFile(String romName) {
		try {
			return ((Activity)context).getAssets().open(romName);
		} catch (IOException e) {
			return null;
		}
	}
	
	// init
	private void init() {
		// main cpu object
		if (loadRom(info.romName)) {
			cpu = new CpuComponents( memory );
		} else {
			Toast.makeText(context, R.string.warn_file_error, Toast.LENGTH_LONG).show();
		}
		
		emulator = new EmulationThread(cpu, mDisplay);
	}
	
}

class EmulationThread extends Thread
{
	CpuComponents cpu;
	DisplayView mDisplay;
	
	public EmulationThread(CpuComponents cpu, DisplayView mDisplay) {
		this.cpu = cpu;
		this.mDisplay = mDisplay;
	}
	
	@Override
	public void run() {
		super.run();
		new Emulation(cpu, mDisplay);
	}
	
}
