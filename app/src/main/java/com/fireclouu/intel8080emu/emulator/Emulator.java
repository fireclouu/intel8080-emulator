package com.fireclouu.intel8080emu.emulator;

import com.fireclouu.intel8080emu.emulator.Platform;

public class Emulator {
	private Platform platform;
    // Timer and interrupts
	private final long FRAME_INTERVAL_VBLANK = 8_330_000L;
	private final long FRAME_INTERVAL_END = 16_670_000L;
	private final long ONE_SECOND_IN_NANO = 1_000_000_000L;
    private final long MAX_CYCLE_PER_SECOND = 2_000_000;
    
	private boolean isVblankServiced = false;
    private short[] port = new short[8];
    private short[] lastPortValue = new short[8];
    // states
    private boolean isLooping = true;
    private boolean isPaused = false;
    private Cpu cpu;
	private Guest guest;
    private Mmu mmu;
    
    // I/O
    private short shiftLsb;
    private short shiftMsb;
    private byte shiftOffset = 0;
    private short readPort;
   
    private long cyclePerSecond = 0;
    private long cycleGuestTotal = 0;
	
	private final long programTimeEpoch;
	private long currentTime;
	private long cpuLastTime;
	private long cpuElapsedTime;
	private long frameLastTime;
	private long frameElapsedTime;

    public Emulator(Guest guest) {
		this.guest = guest;
		this.platform = guest.getPlatform();
        this.cpu = guest.getCpu();
        this.mmu = guest.getMmu();
		this.programTimeEpoch = System.nanoTime();
		
        cyclePerSecond = 0;
    }

    public short handleIn(Cpu cpu, short port) {
        short a = 0;
        switch (port) {
            case 0: // ?
                return 0;
            case 1: // input
                return this.port[Inputs.INPUT_PORT_1];
            case 2: // input
                return this.port[Inputs.INPUT_PORT_2];
            case 3: // rotate shift register
                int v = (shiftMsb << 8) | shiftLsb;
                a = (short) ((v >> (8 - shiftOffset)) & 0xff);
                break;
        }
        return a;
    }

    public void handleOut(Cpu cpu, short port, short value) {
        switch (port) {
            case 2: // shift amount
                shiftOffset = (byte) (value & 0x7);
                break;
            case 3: // sound
                if (value != lastPortValue[3]) {
                    if ((value & 0x1) > 0 && (lastPortValue[3] & 0x1) == 0) {
                        int id = platform.playMedia(Guest.Media.Audio.SHIP_INCOMING, -1);
						platform.setIdMediaPlayed(id);
                    } else if ((value & 0x1) == 0 && (lastPortValue[3] & 0x1) > 0) {
						platform.stopSound(platform.getIdMediaPlayed());
                    }

                    if ((value & 0x2) > 0 && (lastPortValue[3] & 0x2) == 0) {
                        platform.playMedia(Guest.Media.Audio.FIRE, 0);
                    }

                    if ((value & 0x4) > 0 && (lastPortValue[3] & 0x4) == 0) {
                        platform.playMedia(Guest.Media.Audio.PLAYER_EXPLODED, 0);
                        platform.vibrate(300);
                    }

                    if ((value & 0x8) > 0 && (lastPortValue[3] & 0x8) == 0) {
                        platform.playMedia(Guest.Media.Audio.ALIEN_KILLED, 0);
						platform.vibrate(20);
                    }

                    lastPortValue[3] = value;
                }
                break;
            case 4:
                shiftLsb = shiftMsb;
                shiftMsb = value;
                break;
            case 5:
                // alien moving sound
                if (value != lastPortValue[5]) {
                    if ((value & 0x1) > 0 && (lastPortValue[5] & 0x1) == 0) {
                        platform.playMedia(Guest.Media.Audio.ALIEN_MOVE_1, 0);
                    }

                    if ((value & 0x2) > 0 && (lastPortValue[5] & 0x2) == 0) {
                        platform.playMedia(Guest.Media.Audio.ALIEN_MOVE_2, 0);
                    }

                    if ((value & 0x4) > 0 && (lastPortValue[5] & 0x4) == 0) {
                        platform.playMedia(Guest.Media.Audio.ALIEN_MOVE_3, 0);
                    }

                    if ((value & 0x8) > 0 && (lastPortValue[5] & 0x8) == 0) {
                        platform.playMedia(Guest.Media.Audio.ALIEN_MOVE_4, 0);
                    }

                    if ((value & 0x10) > 0 && (lastPortValue[5] & 0x10) == 0) {
                        platform.playMedia(Guest.Media.Audio.SHIP_HIT, 0);
						platform.vibrate(800);
                    }

                    lastPortValue[5] = value;
                }
        }
    }

    public void ioHandler() {
        readPort = 0;
        short dataPort = mmu.readMemory(cpu.getPC() + 1);
        int data = mmu.readMemory(cpu.getPC());
        switch (data) {
            case 0xdb: // in
                readPort = dataPort;
                cpu.setRegA(handleIn(cpu, readPort));
                break;
            case 0xd3: // out
                readPort = dataPort;
                handleOut(cpu, readPort, cpu.getRegA());
                break;
        }
    }

    public void tick() {
		currentTime = getRelativeTimeEpoch();
		frameElapsedTime = currentTime - frameLastTime;
		cpuElapsedTime = currentTime - cpuLastTime;
		
		// frame timings
		if (cpu.hasInterrupt()) {
			if (frameElapsedTime >= FRAME_INTERVAL_VBLANK && !isVblankServiced) {
				cpu.sendInterrupt(0x08);
				isVblankServiced = true;
			} else if (frameElapsedTime >= FRAME_INTERVAL_END) {
				cpu.sendInterrupt(0x10);
				platform.draw(guest.getMemoryVram());
				frameLastTime = currentTime;
				isVblankServiced = false;
			}
		}

        // cycle catch-up
		while (cpuElapsedTime >= ONE_SECOND_IN_NANO && cyclePerSecond < MAX_CYCLE_PER_SECOND) {
			if (platform.isLogging()) {
				writeLog(getLogCurrentPc());
			}

			ioHandler();
			int cycle = cpu.step();
			cyclePerSecond += cycle;
			cycleGuestTotal += cycle;
		}

		// normal cycle
		if (cpuElapsedTime < ONE_SECOND_IN_NANO) {
			if (platform.isLogging()) {
				writeLog(getLogCurrentPc());
			}

			ioHandler();
			int cycle = cpu.step();
			cyclePerSecond += cycle;
			cycleGuestTotal += cycle;
		}

		// cycle reset
		if (cpuElapsedTime > ONE_SECOND_IN_NANO) {
			cyclePerSecond = 0;
			cpuLastTime = currentTime;
		}
    }

    public void tickCpuOnly() {
        int op = mmu.readMemory(cpu.getPC());
        switch (op) {
            case 0xdb:
                testSuiteIn();
                break;
            case 0xd3:
                testSuiteOut();
                break;
        }

        cycleGuestTotal += cpu.step();
    }

    private void testSuiteIn() {
        int operation = cpu.getRegC();

        if (operation == 2) {
            char data = (char) cpu.getRegE();
            String dataString = Character.valueOf(data).toString();
            writeLog(dataString);

            if (cpu.getRegE() == 10) {
                writeLog("\n");
            }
        } else if (operation == 9) {
            int addr = (cpu.getRegD() << 8) | cpu.getRegE();
            char data;

            do {
                data = (char) mmu.readMemory(addr);
                if (data == '$') break;
                String dataString = Character.valueOf(data).toString();
                writeLog(dataString);

                if (cpu.getRegE() == 10) {
                    writeLog("\n");
                }
                addr++;
            } while (data != '$');
        }

        cpu.setRegA((short) 0xff);
    }

    private void testSuiteOut() {
        stop();
    }

    public void stop() {
        isLooping = false;
    }

    public boolean isLooping() {
        return this.isLooping;
    }

    public void setPause(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    private void writeLog(String message) {
        // platform.writeLog(message);
    }

    private String getLogCurrentPc() {
        return Disassembler.disassemble(mmu, cpu.getPC(), mmu.readMemory(cpu.getPC()));
    }
	
	public void setPortValue(int index, byte key) {
		port[index] = key;
	}
	
	public short getPortValue(int index) {
		return port[index];
	}
	
	public void setPortXor(int index, byte key) {
		port[index] |= key;
	}
	
	public void setPortAnd(int index, byte key) {
		port[index] &= key;
	}
	
	private long getRelativeTimeEpoch() {
		return System.nanoTime() - programTimeEpoch;
	}
}
