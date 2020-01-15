package com.fireclouu.intel8080emu.Emulator;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import com.fireclouu.intel8080emu.*;

public class Emulation
{
	///  TIMER and INTERRUPT  ///
	long nowTime  = 0; // init
	long lastTime = 0; // init
	long usec_template = 1_000_000; // microsec template
	byte whichInterrupt;
	long nextInterrupt;
	
	long timerHz;
	long refreshRate = ((1 / 60) * usec_template); // 16ms
	
	PrintTrace pTrace;
	public Interpreter interpreter;

	// CONSTRUCTOR
	public Emulation(CpuComponents cpu, MainGraphics mGraphics) {
		System.out.println("Start emulator...\n");
		
		init(cpu);
		startEmulation(cpu, mGraphics);
		
		System.out.println("\nEnd emulator...");
	}
	
	// INIT
	private void init(CpuComponents cpu) {
		interpreter = new Interpreter(cpu);
		pTrace = new PrintTrace();
	}
	
	// MAIN
	public void startEmulation(CpuComponents cpu, MainGraphics mGraphics) {
		
		while(ProgramUtils.Machine.isRunning) {	
			// 2MHz = execute every 5e-7 secs
			// System.nanoTime() = billionth of a sec. (epoch)
			nowTime = getMicroSec();
			
			// 60hz
			if ( (timerHz + refreshRate) < nowTime) {
				cpu.updateScreen = true;
				timerHz = nowTime;
			}
			
			// emulation
			interpreter.emulate8080(cpu);
			
			// first run
			if (lastTime == 0) {
				lastTime = nowTime;
				nextInterrupt = lastTime + refreshRate;
				whichInterrupt = 1;
				timerHz = nowTime;
			}
			
			if (cpu.int_enable && (nowTime > nextInterrupt)){
				if (whichInterrupt == 1) {
					interpreter.GenerateInterrupt(cpu, (byte) 1);
					whichInterrupt = 2;
				} else {
					interpreter.GenerateInterrupt(cpu, (byte) 2);
					whichInterrupt = 1;
				}
				
				nextInterrupt = nowTime + (8000);
			}
			
			// 2 MHz
			// continue execute instructions if lastTime < lastTime + usec & cycle < 2million
			long elapse = nowTime - lastTime;
			long cycle_needed = (elapse * 2);

			int cycles = 0; // reset every succeeding usec passed

			//while((cycles < cycle_needed)) {
			// print instruction
			// pTrace.printInstruction(cpu, cpu.PC, true); // enabling this slows (?) emulation

			
			//}
			
			lastTime = nowTime;
		}
	}
	
	private long getMicroSec() {
		long microSec = System.nanoTime(); // static variable!
		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (usec_template * (microSec / usec_template))) / 1000);
	}
}
