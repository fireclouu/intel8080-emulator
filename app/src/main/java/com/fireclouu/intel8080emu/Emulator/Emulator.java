package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.util.*;

public class Emulator implements IOAdapter
{
	private Interpreter interpreter;
	private PrintTrace print;
	
	AppUtils.Component machineUtils;
	
	private final double NANO_SEC = 1_000_000.0; // template
	private final double VBLANK_START = (1.0 / 60.0) * (NANO_SEC);
	private final double MIDFRAME = VBLANK_START / 2.0;
	private byte whichInterrupt;
	private double nextInterrupt;
	
	private double timerHz;

	private boolean test_finished = false;
	private boolean done = false;
	private short
		shift0 = 0,
		shift1 = 0;
	private byte shift_offset = 0;
	
	private final double WHEN_TO_RUN_CYCLE = 1.0/2_000_000.0;
	private double timerNow = 0;
	private double timerLast = 0;
	private double customMhz = (WHEN_TO_RUN_CYCLE * (NANO_SEC * 10.0)) ;
	
	public static long getCycle = 0;
	
	// Interrupt
	private final byte INTERRUPT_MID  = 1;
	private final byte INTERRUPT_FULL = 2;
	
	private byte fireInterrupt = INTERRUPT_MID;
	
	short readPort;
	public static volatile short[] port = new short[8];
	
	
	// can be removed
	double checkNow = 0;
	double checkLast = 0;
	int sys_cycle = 0;
	public static int ac = 0, actual_cycle = 0;
	
	public static String cycleInfo = ""; // avoid null
	
	public Emulator() {
		init();
	}
	
	private void init() {
		interpreter = new Interpreter();
		print = new PrintTrace();
	}
	
	@Override
	public short handleIN(CpuComponents cpu, short port) {
		short a = 0;
		switch(port) {
			case 0: // ?
				return 0;
			case 1: // INPUTS
				return this.port[1];
			case 2: // INPUTS
				return 0;
			case 3: // SHIFT REGISTER DATA (ROTATE)
				int v = (shift1 << 8) | shift0;    
				a = (short) ((v >> (8-shift_offset)) & 0xff);  
				break;
		}
		return a;
	}

	@Override
	public void handleOUT(CpuComponents cpu, short port, short value) {
		switch(port) {
			case 2: // SHIFT AMOUNT 
				shift_offset = (byte) (value & 0x7);    
				break;  
			case 3: // SOUND 1
				break;
			case 4:    
				shift0 = shift1;    
				shift1 = value;    
				break;    
		}
	}
	
	public void ioHandler(CpuComponents cpu, int opcode) {
		readPort = 0;
		switch(cpu.memory[opcode])  {
			case 0xdb: // IN
				readPort = cpu.memory[opcode + 1];
				cpu.A = handleIN(cpu, readPort);
				break;
			case 0xd3: // OUT
				readPort = cpu.memory[opcode + 1];
				handleOUT(cpu, readPort, cpu.A);
				break;
		}
	}
	
	public void startEmulation(final CpuComponents cpu, final DisplayAdapter display) {
		
		if (AppUtils.Component.DEBUG) {
			runTests(cpu);
			return;
		}
		
		// Display
		new Thread() {
			@Override
			public void run() {
				
			}
		}.start();
		
		
		while(PlatformAdapter.getStateMaster()) {	

			timerNow = getNano();
			checkNow = timerNow;
			
			// 60hz
			if((timerHz + VBLANK_START < timerNow)/* && !display.isDrawing*/) {
				display.updateView(cpu.memory);
				timerHz = timerNow;
			}
			
			
			// interrupt for space invaders
			// Check midframe interrupt and rst 1 if true
			// check final frame interrupt and rst 2 if true
			if ((cpu.int_enable == 1) && (timerNow > nextInterrupt)) {
				interpreter.GenerateInterrupt(cpu, fireInterrupt);
				fireInterrupt = (fireInterrupt == INTERRUPT_MID) ? INTERRUPT_FULL : INTERRUPT_MID;
				nextInterrupt = timerNow + (MIDFRAME);
			}
			
			
				// run at 2 MHz
				
				// cycle catch-up
				while((checkNow > checkLast + (NANO_SEC)) && interpreter.cycle <= machineUtils.MAX_CYCLE_SPEED_PER_SECOND) {
					// IO
					ioHandler(cpu, cpu.PC);
					
					interpreter.cycle += interpreter.emulate8080(cpu);
					sys_cycle++;
				}
				// cycle reset
				if (checkNow > checkLast + (NANO_SEC)) {
					actual_cycle = ac;
					
					getCycle = interpreter.cycle;
					cycleInfo = "Emulation: " + getCycle + " | Android (strict): " + sys_cycle;
					interpreter.cycle = 0; // reset
					sys_cycle = ac = 0;
					checkLast = checkNow;
				}
				// normal cycle (?)
				if(timerNow >= timerLast + customMhz) {
					// IO
					ioHandler(cpu, cpu.PC);
					
					interpreter.cycle += interpreter.emulate8080(cpu);
					sys_cycle++;
					timerLast = timerNow;
				}	
				
			ac++;
		}
	}
	
	private long getNano() {
		return System.nanoTime() / 1_000;
	}
	
	public static boolean isCycleCorrect() {
		return getCycle > 1_999_990 & getCycle < 2_000_100;
	}
	// DEBUGGING
	public static void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void runTests(CpuComponents cpu) {
		System.out.println("Test: " + PlatformAdapter.TEST_NAME + "\nSTART: " + AppUtils.getTime());
		System.out.println("______________________________");

		addMsg("Test: " + PlatformAdapter.TEST_NAME);
		addMsg("START: " + AppUtils.getTime());
		addMsg("______________________________");
		addMsg("");

		while(!test_finished) {
			switch(cpu.memory[cpu.PC])  {
				case 0xdb: // IN
					debug_handleIN(cpu);
					break;
				case 0xd3: // OUT
					debug_handleOUT();
					break;
			}
			
			interpreter.cycle += interpreter.emulate8080(cpu);
			// print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
			print.check_overflow(cpu);
		}

		System.out.println();
		System.out.println("______________________________");
		System.out.println("END:   " + AppUtils.getTime());
		System.out.println("\n***\n");
		
		addMsg();
		addMsg("______________________________");
		addMsg("END:   " + AppUtils.getTime());
		addMsg();
		addMsg("***");
		addMsg();
	}

	private void debug_handleIN(CpuComponents cpu) 
	{
		if (PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] == null) {
			PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
		}

		int operation = cpu.C;
		if (operation == 2) {

			System.out.printf("%c", cpu.E);
			addMsg((char) cpu.E);
			if((char) cpu.E == 10) {
				PlatformAdapter.MSG_COUNT++;
				PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
			}
		} else if (operation == 9) {
			int addr = (cpu.D << 8) | cpu.E;
			do {
				//System.out.printf("%c", cpu.memory[addr++]);
				System.out.printf("%c", cpu.memory[addr]);
				addMsg((char) cpu.memory[addr]);
				if((char) cpu.memory[addr] == 10) {

					PlatformAdapter.MSG_COUNT++;
					PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
				}
				addr++;
			} while (cpu.memory[addr] != '$');
		}
		cpu.A = 0xff;
	}
	private void debug_handleOUT() {
		test_finished = true;
	}
	// Builder
	private void addMsg(char c) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] += c;
	}
	private void addMsg() {
		PlatformAdapter.MSG_COUNT++;
	}
	private void addMsg(String str) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT++] = str + "\n";
	}
}
