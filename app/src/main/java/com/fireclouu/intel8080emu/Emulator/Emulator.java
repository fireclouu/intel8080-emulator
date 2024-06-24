package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class Emulator implements IOAdapter
{
	// states
	private boolean isLooping = true;
	private boolean isPaused = false;
	private boolean stateTest = false;
	
	private Interpreter interpreter;
	private Disassembler print;

	// Timer and interrupts
	private final double NANO_SEC = 1_000_000.0; // template
	
	private final double VBLANK_START = (1.0 / 60.0) * (NANO_SEC);
	private final double MIDFRAME = VBLANK_START / 2.0;
	private final double WHEN_TO_RUN_CYCLE = 1.0 / 2_000_000.0;
	private final byte INTERRUPT_MID  = 1;
	private final byte INTERRUPT_FULL = 2;
	
	private double timerNow = 0;
	private double timerLast = 0;
	private final double fixedMhz = (WHEN_TO_RUN_CYCLE * (NANO_SEC * 10.0)) ;
	private double updateHz;
	private double nextInterrupt;
	
	private byte fireInterrupt = INTERRUPT_MID;
	
	// I/O
	private short shift_lsb;
	private short shift_msb;
	private byte  shift_offset = 0;
	private short readPort;
	public static volatile short[] port = new short[8];
	public static volatile short[] last_port_value = new short[8];
	
	// temp
	public static long storeCycle = 0;
	 
	// can be removed
	double checkNow = 0;
	double checkLast = 0;
	public static int ac = 0, actual_cycle = 0;
	
	public static String cycleInfo = ""; // avoid null
	
	public Emulator() {
		init();
	}
	
	private void init() {
		interpreter = new Interpreter();
		print = new Disassembler();
	}
	
	@Override
	public short handleIN(CpuComponents cpu, short port) {
		short a = 0;
		switch(port) {
			case 0: // ?
				return 0;
			case 1: // INPUTS
				return Emulator.port[1];
			case 2: // INPUTS
				return 0;
			case 3: // SHIFT REGISTER DATA (ROTATE)
				int v = (shift_msb << 8) | shift_lsb;    
				a = (short) ((v >> (8-shift_offset)) & 0xff);  
				break;
		}
		return a;
	}

	@Override
	public void handleOUT(CpuComponents cpu, ResourceAdapter api, short port, short value) {
		switch(port) {
			case 2: // SHIFT AMOUNT 
				shift_offset = (byte) (value & 0x7);    
				break;  
			case 3: // SOUND 1
				if (value != last_port_value[3]) {
					
					if ((value & 0x1) > 0 && (last_port_value[3] & 0x1) == 0)    
					{
						api.playShipFX();
					} else if ((value & 0x1) == 0 && (last_port_value[3] & 0x1) > 0) {
						api.releaseShipFX();
					}
					
					if((value & 0x2) > 0 && (last_port_value[3] & 0x2) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_FIRE, 0);
					}
					if((value & 0x4) > 0 && (last_port_value[3] & 0x4) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_PLAYER_EXPLODED, 0);
						api.vibrate(300);
						
					}
					if((value & 0x8) > 0 && (last_port_value[3] & 0x8) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_KILLED, 0);
					}
					
					last_port_value[3] = value;
				}
				
				break;
				
			case 4:    
				shift_lsb = shift_msb;
				shift_msb = value;
				break;
				
			case 5:
				// alien moving sound
				// bit 0 (0)
				if (value != last_port_value[5]) {
					if ((value & 0x1) > 0 && (last_port_value[5] & 0x1) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_1, 0);
						
						api.vibrate(20);
					}
					if ((value & 0x2) > 0 && (last_port_value[5] & 0x2) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_2, 0);
						api.vibrate(20);
					}
					if ((value & 0x4) > 0 && (last_port_value[5] & 0x4) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_3, 0);
						api.vibrate(20);
					}
					if ((value & 0x8) > 0 && (last_port_value[5] & 0x8) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_4, 0);
						api.vibrate(20);
					}
					if ((value & 0x10) > 0 && (last_port_value[5] & 0x10) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_SHIP_HIT, 0);
					}
					
					last_port_value[5] = value;
				}
		}
	}
	
	public void ioHandler(CpuComponents cpu, ResourceAdapter media, int opcode) {
		readPort = 0;
		short dataPort = Mmu.readMemory(cpu, opcode + 1);
		int data = Mmu.readMemory(cpu, opcode);
		switch(data)  {
			case 0xdb: // IN
				readPort = dataPort;
				cpu.A = handleIN(cpu, readPort);
				break;
			case 0xd3: // OUT
				readPort = dataPort;
				handleOUT(cpu, media, readPort, cpu.A);
				break;
		}
	}
	
	public void start(CpuComponents cpu, DisplayAdapter display, ResourceAdapter api) {
		if (StringUtils.Component.DEBUG) {
			runTest(cpu);
			return;
		}
		
		while(isLooping) {
			if (isPaused) continue;
			timerNow = getNano();
			checkNow = timerNow;
			
			// 60hz
			if ((updateHz + VBLANK_START < timerNow)) {
				display.draw(Mmu.getMemory(cpu));
				updateHz = timerNow;
			}
			
			// interrupt for space invaders
			// Check midframe interrupt and rst 1 if true
			// check final frame interrupt and rst 2 if true
			if ((cpu.int_enable == 1) && (timerNow > nextInterrupt)) {
				interpreter.sendInterrupt(cpu, fireInterrupt);
				fireInterrupt = (fireInterrupt == INTERRUPT_MID) ? INTERRUPT_FULL : INTERRUPT_MID;
				nextInterrupt = timerNow + (MIDFRAME);
			}
			
				// 2MHz
				// cycle catch-up
				while((checkNow > checkLast + (NANO_SEC)) && Interpreter.machineTotalCycle <= StringUtils.Component.MAX_CYCLE_SPEED_PER_SECOND) {
					// IO
					ioHandler(cpu, api, cpu.PC);
					Interpreter.machineTotalCycle += interpreter.interpret(cpu);
				}
				// cycle reset
				if (checkNow > checkLast + (NANO_SEC)) {
					short guestHighScoreMsb = Mmu.readMemory(cpu, 0x20f5);
					short guestHighScoreLsb = Mmu.readMemory(cpu, 0x20f4);
					
					actual_cycle = ac;
					ac = 0;
					
					storeCycle = Interpreter.machineTotalCycle;
					cycleInfo = "HiScore: " + String.format("%02x", guestHighScoreMsb) + String.format("%02x", guestHighScoreLsb) + " | Emulation speed: " + storeCycle;
					Interpreter.machineTotalCycle = 0; // reset
					checkLast = checkNow;
				}
				// normal cycle (?)
				if(timerNow >= timerLast + fixedMhz) {
					// IO
					ioHandler(cpu, api, cpu.PC);
					Interpreter.machineTotalCycle += interpreter.interpret(cpu);
					timerLast = timerNow;
				}	
				
			ac++;
		}
	}
	
	public void stop() {
		isLooping = false;
	}
	
	public void pause() {
		isPaused = true;
	}
	
	public void resume() {
		isPaused = false;
	}
	
	private long getNano() {
		return System.nanoTime() / 1_000;
	}
	
	public static boolean isCycleCorrect() {
		return storeCycle > 1_999_990 & storeCycle < 2_000_100;
	}
	
	// DEBUGGING
	private void runTest(CpuComponents cpu) {
		int counter = 0;
		for (String name : StringUtils.File.FILES) {
			
			stateTest = false;
			
			cpu.init();
			Mmu.setMemory(cpu, PlatformAdapter.tf[counter]);
			
			// load file and injects
			// SOURCE: superzazu — intel 8080 c99
			// inject "out 1,a" at 0x0000 (signal to stop the test)
			Mmu.writeMemory(cpu, 0x0000, (short) 0xD3);
			Mmu.writeMemory(cpu, 0x0001, (short) 0x00);
			Mmu.writeMemory(cpu, 0x0005, (short) 0xDB);
			Mmu.writeMemory(cpu, 0x0006, (short) 0x00);
			Mmu.writeMemory(cpu, 0x0007, (short) 0xC9);
			// inject "in a,0" at 0x0005 (signal to output some characters)
			// jump pc to 0x100
			cpu.PC = 0x0100;
			
			System.out.println("Test: " + name + "\nSTART: " + StringUtils.getTime());
			System.out.println("______________________________");

			addMsg("Test: " + name);
			addMsg("START: " + StringUtils.getTime());
			addMsg("______________________________");
			addMsg("");

			while(!stateTest) {
				switch(PlatformAdapter.tf[counter][cpu.PC])
				{
					case 0xdb: // IN
						debug_handleIN(cpu);
						break;
					case 0xd3: // OUT
						debug_handleOUT();
						break;
				}

				Interpreter.machineTotalCycle += interpreter.interpret(cpu);
				// print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
				print.check_overflow(cpu);
			}

			System.out.println();
			System.out.println("______________________________");
			System.out.println("END:   " + StringUtils.getTime());
			System.out.println("\n***\n");

			addMsg();
			addMsg("______________________________");
			addMsg("END:   " + StringUtils.getTime());
			addMsg() ;
			addMsg("***");
			addMsg();
			
			counter++;
		}
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
			int data;
			do {
				data = Mmu.readMemory(cpu, addr);
				System.out.printf("%c", data);
				addMsg((char) data);
				if((char) data == 10) {

					PlatformAdapter.MSG_COUNT++;
					PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
				}
				addr++;
			} while (data != '$');
		}
		cpu.A = 0xff;
	}
	private void debug_handleOUT() {
		stateTest = true;
	}
	// String Builder
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
