package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class Emulator implements IOAdapter {
	// states
	private boolean isLooping = true;
	private boolean isPaused = false;
	private boolean stateTest = false;

	private PlatformAdapter platform;
	private Cpu cpu;
	private Mmu mmu;

	// Timer and interrupts
	private final long GUEST_MAX_CYCLE_PER_SECOND = 2_000_000;
	private final double NANO_SEC = 1_000_000.0;
	
	private final double VBLANK_START = (1.0 / 60.0) * (NANO_SEC);
	private final double MIDFRAME = VBLANK_START / 2.0;
	private final double WHEN_TO_RUN_CYCLE = 1.0 / 2_000_000.0;
	private final double fixedMhz = (WHEN_TO_RUN_CYCLE * (NANO_SEC * 10.0));
	private final byte INTERRUPT_MID  = 1;
	private final byte INTERRUPT_FULL = 2;

	private double timeNow;
	private double timePrev;
	private double updateHz;
	private double timeNextInterrupt;
	private byte interruptType = INTERRUPT_MID;

	// I/O
	private short shift_lsb;
	private short shift_msb;
	private byte  shift_offset = 0;
	private short readPort;
	private boolean isCatchingUp;
	private boolean isTimePrevNeedsUpdate;
	private String log;
	
	public short[] port = new short[8];
	public short[] last_port_value = new short[8];

	public long cycleHostTotal = 0;
	public long cyclePerSecond = 0;

	public Emulator(PlatformAdapter platform, Cpu cpu, Mmu mmu) {
		init(platform, cpu, mmu);
	}

	private void init(PlatformAdapter platform, Cpu cpu, Mmu mmu) {
		this.platform = platform;
		this.cpu = cpu;
		this.mmu = mmu;
		
		isCatchingUp = false;
		isTimePrevNeedsUpdate = false;
		cyclePerSecond = 0;
		cycleHostTotal = 0;
	}

	@Override
	public short handleIn(Cpu cpu, short port) {
		short a = 0;
		switch (port) {
			case 0: // ?
				return 0;
			case 1: // input
				return this.port[KeyInterrupts.INPUT_PORT_1];
			case 2: // input
				return this.port[KeyInterrupts.INPUT_PORT_2];
			case 3: // rotate shift register
				int v = (shift_msb << 8) | shift_lsb;    
				a = (short) ((v >> (8 - shift_offset)) & 0xff);  
				break;
		}
		return a;
	}

	@Override
	public void handleOut(Cpu cpu, ResourceAdapter api, short port, short value) {
		switch (port) {
			case 2: // shift amount
				shift_offset = (byte) (value & 0x7);    
				break;  
			case 3: // sound 1
				if (value != last_port_value[3]) {
					if ((value & 0x1) > 0 && (last_port_value[3] & 0x1) == 0) {
						api.playShipFX();
					} else if ((value & 0x1) == 0 && (last_port_value[3] & 0x1) > 0) {
						api.releaseShipFX();
					}

					if ((value & 0x2) > 0 && (last_port_value[3] & 0x2) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_FIRE, 0);
					}
					
					if ((value & 0x4) > 0 && (last_port_value[3] & 0x4) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_PLAYER_EXPLODED, 0);
						api.vibrate(300);

					}
					
					if ((value & 0x8) > 0 && (last_port_value[3] & 0x8) == 0) {
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
						// api.vibrate(20);
					}
					
					if ((value & 0x2) > 0 && (last_port_value[5] & 0x2) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_2, 0);
						// api.vibrate(20);
					}
					
					if ((value & 0x4) > 0 && (last_port_value[5] & 0x4) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_3, 0);
						// api.vibrate(20);
					}
					
					if ((value & 0x8) > 0 && (last_port_value[5] & 0x8) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_ALIEN_MOVE_4, 0);
						// api.vibrate(20);
					}
					
					if ((value & 0x10) > 0 && (last_port_value[5] & 0x10) == 0) {
						api.playSound(MachineResources.MEDIA_EFFECT_SHIP_HIT, 0);
					}
					
					last_port_value[5] = value;
				}
		}
	}

	public void ioHandler(Cpu cpu, ResourceAdapter media, int opcode) {
		readPort = 0;
		short dataPort = mmu.readMemory(opcode + 1);
		int data = mmu.readMemory(opcode);
		switch (data) {
			case 0xdb: // in
				readPort = dataPort;
				cpu.setRegA(handleIn(cpu, readPort));
				break;
			case 0xd3: // out
				readPort = dataPort;
				handleOut(cpu, media, readPort, cpu.getRegA());
				break;
		}
	}

	public void step(DisplayAdapter display, ResourceAdapter api) {
			isCatchingUp = false;
			isTimePrevNeedsUpdate = false;
			timeNow = getNano();
			
			// 60hz
			if ((updateHz + VBLANK_START <= timeNow)) {
				display.draw(mmu.getMemory());
				updateHz = timeNow;
			}

			// interrupt for space invaders
			// Check midframe interrupt and rst 1 if true
			// check final frame interrupt and rst 2 if true
			if ((cpu.getInterrupt() == 1) && (timeNow >= timeNextInterrupt)) {
				cpu.sendInterrupt(interruptType);
				interruptType = (interruptType == INTERRUPT_MID) ? INTERRUPT_FULL : INTERRUPT_MID;
				timeNextInterrupt = timeNow + (MIDFRAME);
			}

			// 2mhz cycle catch-up
			while((timeNow > timePrev + (NANO_SEC)) && cyclePerSecond < GUEST_MAX_CYCLE_PER_SECOND) {
				ioHandler(cpu, api, cpu.getPC());
				if (platform.isLogging()) {
					log = Disassembler.disassemble(mmu, cpu.getPC(), (int) mmu.readMemory(cpu.getPC()));
					callHost(log);
				}
				cyclePerSecond += cpu.step();
				isTimePrevNeedsUpdate = true;
				isCatchingUp = true;
			}
	
			// normal cycle
			if((timeNow > timePrev + fixedMhz) && !isCatchingUp) {
				ioHandler(cpu, api, cpu.getPC());
				if (platform.isLogging()) {
					log = Disassembler.disassemble(mmu, cpu.getPC(), (int) mmu.readMemory(cpu.getPC()));
					callHost(log);
				}
				cyclePerSecond += cpu.step();
				isTimePrevNeedsUpdate = true;
			}	
			
			// cycle reset
			if (timeNow >= timePrev + (NANO_SEC)) {
				cyclePerSecond = 0;
				isTimePrevNeedsUpdate = true;
			}
			
			if (isTimePrevNeedsUpdate) timePrev = timeNow;
			cycleHostTotal++;
	}
	
	// DEBUGGING
	private void runTest(Cpu cpu) {
		int counter = 0;
		for (String name : StringUtils.File.FILES) {
			stateTest = false;

			cpu.init();
			// Mmu.setMemory(cpu, PlatformAdapter.tf[counter]);

			// load file and injects
			// SOURCE: superzazu — intel 8080 c99
			// inject "out 1,a" at 0x0000 (signal to stop the test)
			mmu.writeMemory(0x0000, (short) 0xD3);
			mmu.writeMemory(0x0001, (short) 0x00);
			mmu.writeMemory(0x0005, (short) 0xDB);
			mmu.writeMemory(0x0006, (short) 0x00);
			mmu.writeMemory(0x0007, (short) 0xC9);
			// inject "in a,0" at 0x0005 (signal to output some characters)
			// jump pc to 0x100
			cpu.setPC(0x0100);

			System.out.println("Test: " + name + "\nSTART: " + StringUtils.getTime());
			System.out.println("______________________________");

			addMsg("Test: " + name);
			addMsg("START: " + StringUtils.getTime());
			addMsg("______________________________");
			addMsg("");

			while (!stateTest) {
				// implement
				/*switch(PlatformAdapter.tf[counter][cpu.PC])
				 {
				 case 0xdb: // IN
				 debug_handleIN(cpu);
				 break;
				 case 0xd3: // OUT
				 debug_handleOUT();
				 break;
				 }*/

				cyclePerSecond += cpu.step();
				// print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
				// Disassembler.check_overflow(cpu, cyclePerSecond);
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

	private void debug_handleIN(Cpu cpu)  {
		if (PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] == null) {
			PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
		}

		int operation = cpu.getRegC();
		if (operation == 2) {
			System.out.printf("%c", cpu.getRegE());
			addMsg((char) cpu.getRegE());
			if ((char) cpu.getRegE() == 10) {
				PlatformAdapter.MSG_COUNT++;
				PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
			}
		} else if (operation == 9) {
			int addr = (cpu.getRegD() << 8) | cpu.getRegE();
			int data;
			do {
				data = mmu.readMemory(addr);
				System.out.printf("%c", data);
				addMsg((char) data);
				if ((char) data == 10) {

					PlatformAdapter.MSG_COUNT++;
					PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] = "";
				}
				addr++;
			} while (data != '$');
		}
		cpu.setRegA((short) 0xff);
	}
	private void debug_handleOUT() {
		stateTest = true;
	}
	
	private void addMsg(char c) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT] += c;
	}
	
	private void addMsg() {
		PlatformAdapter.MSG_COUNT++;
	}
	
	private void addMsg(String str) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.MSG_COUNT++] = str + "\n";
	}
	
	public void stop() {
		isLooping = false;
	}

	public void setPause(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	private long getNano() {
		return System.nanoTime() / 1_000;
	}
	
	private void callHost(String message) {
		platform.writeLog(message);
	}
}
