package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.util.*;

public class Emulator
{
	CpuComponents cpu;
	Interpreter interpreter;
	PrintTrace print;

	private final long USEC = 1_000_000; // template
	private final long REFRESH_RATE = (int) ((1 / 60) * USEC);

	private long timerNow = 0;
	private long lastTime = 0;
	private byte whichInterrupt;
	private long nextInterrupt;
	
	private long timerHz;

	// CONSTRUCTOR
	public Emulator() {
		init();
	}
	
	// INIT
	private void init() {
		interpreter = new Interpreter();
		print = new PrintTrace();
	}
	
	// MAIN
	public void startEmulation(CpuComponents cpu, DisplayAdapter display) {
		
		while(true) {	
			// 2MHz = execute every 5e-7 secs
			// System.nanoTime() = billionth of a sec. (epoch)
			timerNow = getMicroSec();
			
			// 60hz
			
			if((timerHz + REFRESH_RATE < timerNow) & !display.isMemLoaded) {
				display.setDraws(cpu.memory);
				timerHz = timerNow;
			}
			
			// first run
			if (lastTime == 0) {
				lastTime = timerNow;
				nextInterrupt = lastTime + 16000;
				whichInterrupt = 1;
				timerHz = timerNow;
			}
			
			// INTERRUPT
			if (cpu.int_enable && (timerNow > nextInterrupt)){
				if (whichInterrupt == 1) {
					interpreter.GenerateInterrupt(cpu, (byte) 1);
					whichInterrupt = 2;
				} else {
					interpreter.GenerateInterrupt(cpu, (byte) 2);
					whichInterrupt = 1;
				}
				
				nextInterrupt = timerNow + (8000);
			}
			
			// emulation
			interpreter.emulate8080(cpu);
			
			// 2 MHz
			// continue execute instructions if lastTime < lastTime + usec & cycle < 2million
			//long elapse = timerNow - lastTime;
			//long cycle_needed = (elapse * 2);

			//int cycles = 0; // reset every succeeding usec passed

			//while((cycles < cycle_needed)) {
			// print instruction
			// pTrace.printInstruction(cpu, cpu.PC, true); // enabling this slows (?) emulation

			
			//}
			
			lastTime = timerNow;
		}
	}
	
	private long getMicroSec() {
		long microSec = System.nanoTime(); // static variable!
		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (USEC * (microSec / USEC))) / 1000);
	}
	

	// CPU OVERRIDE
	private void AUTO_TEST(CpuComponents cpu) {
		switch (AppUtils.File.FILE_NAME[0]) {
			case "cpudiag.bin":
			case "8080EX1.COM":
			case "8080EXER.COM":
			case "CPUTEST.COM":
			case "8080EXM.COM":
			case "8080PRE.COM":
			case "TST8080.COM":
				TEST_OVERRIDE_GENERIC(cpu);
				dialog();
				break;
		}
	}
	
	private void dialog() {
		if (!AppUtils.Machine.DEBUG) System.out.println("debug is off!");
		
		System.out.println("CPU EXERCISER \nSTART:  " + new Date().toString());
		System.out.println();
		PAUSE_THREAD(1000);
	}
	
	private void TEST_OVERRIDE_GENERIC(CpuComponents cpu) {
		cpu.PC = AppUtils.File.ROM_ADDRESS[0];
		// do port injections here
	}
	
	public static void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
