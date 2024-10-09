package com.fireclouu.intel8080emu.emulator;

import com.fireclouu.intel8080emu.emulator.base.DisplayAdapter;
import com.fireclouu.intel8080emu.emulator.base.IOAdapter;
import com.fireclouu.intel8080emu.emulator.base.PlatformAdapter;
import com.fireclouu.intel8080emu.emulator.base.ResourceAdapter;
import com.fireclouu.intel8080emu.HostHook;

public class Emulator implements IOAdapter {
	PlatformAdapter platform;
    // Timer and interrupts
    private final long MAX_CYCLE_PER_SECOND = 2_000_000;
    private final double NANO_SEC = 1_000_000.0;
    private final double VBLANK_START = (1.0 / 60.0) * (NANO_SEC);
    private final double MIDFRAME = VBLANK_START / 2.0;
    private final double WHEN_TO_RUN_CYCLE = 1.0 / 2_000_000.0;
    private final double fixedMhz = (WHEN_TO_RUN_CYCLE * (NANO_SEC * 10.0));
    private final byte INTERRUPT_MID = 1;
    private final byte INTERRUPT_FULL = 2;
    public short[] port = new short[8];
    public short[] last_port_value = new short[8];
    // states
    private boolean isLooping = true;
    private boolean isPaused = false;
    private Cpu cpu;
	private Guest guest;
    private HostHook hostHook;
    private Mmu mmu;
    private double timePrev;
    private double timeNextInterrupt;
    private byte interruptType = INTERRUPT_MID;
    // I/O
    private short shift_lsb;
    private short shift_msb;
    private byte shift_offset = 0;
    private short readPort;
    private long cycleHostTotal = 0;
    private long cyclePerSecond = 0;
    private long cycleGuestTotal = 0;

    public Emulator(Guest guest) {
		this.guest = guest;
        this.cpu = guest.getCpu();
        this.mmu = guest.getMmu();
        this.hostHook = HostHook.getInstance();
		platform = hostHook.getPlatform();

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
                        api.playSound(Guest.MEDIA_AUDIO.FIRE.getId(), 0);
                    }

                    if ((value & 0x4) > 0 && (last_port_value[3] & 0x4) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.PLAYER_EXPLODED.getId(), 0);
                        api.vibrate(300);

                    }

                    if ((value & 0x8) > 0 && (last_port_value[3] & 0x8) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.ALIEN_KILLED.getId(), 0);
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
                        api.playSound(Guest.MEDIA_AUDIO.ALIEN_MOVE_1.getId(), 0);
                    }

                    if ((value & 0x2) > 0 && (last_port_value[5] & 0x2) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.ALIEN_MOVE_2.getId(), 0);
                    }

                    if ((value & 0x4) > 0 && (last_port_value[5] & 0x4) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.ALIEN_MOVE_3.getId(), 0);
                    }

                    if ((value & 0x8) > 0 && (last_port_value[5] & 0x8) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.ALIEN_MOVE_4.getId(), 0);
                    }

                    if ((value & 0x10) > 0 && (last_port_value[5] & 0x10) == 0) {
                        api.playSound(Guest.MEDIA_AUDIO.SHIP_HIT.getId(), 0);
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

    public void tick(DisplayAdapter display, ResourceAdapter api) {
        // interrupt for space invaders
        // Check midframe interrupt and rst 1 if true
        // check final frame interrupt and rst 2 if true
        if ((cpu.getInterrupt() == 1) && (getSystemNanoTime() >= timeNextInterrupt)) {
            cpu.sendInterrupt(interruptType);
			if (interruptType == INTERRUPT_FULL) display.draw(guest.getMemory());
            interruptType = (interruptType == INTERRUPT_MID) ? INTERRUPT_FULL : INTERRUPT_MID;
            timeNextInterrupt = getSystemNanoTime() + MIDFRAME;
        }

        // 2mhz cycle catch-up
        while (getSystemNanoTime() > timePrev + NANO_SEC && cyclePerSecond < MAX_CYCLE_PER_SECOND) {
            
			if (platform.isLogging()) {
				writeLog(getLogCurrentPc());
			}
			
			ioHandler(cpu, api, cpu.getPC());
            int cycle = cpu.step();
            cyclePerSecond += cycle;
            cycleGuestTotal += cycle;
        }

        // normal cycle
        if (getSystemNanoTime() < timePrev + NANO_SEC) {
            if (platform.isLogging()) {
				writeLog(getLogCurrentPc());
			}
			
			ioHandler(cpu, api, cpu.getPC());
            int cycle = cpu.step();
            cyclePerSecond += cycle;
            cycleGuestTotal += cycle;
        }

        // cycle reset
        if (getSystemNanoTime() > timePrev + NANO_SEC) {
            cyclePerSecond = 0;
			timePrev = getSystemNanoTime();
        }

        cycleHostTotal++;
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

    private long getSystemNanoTime() {
        return System.nanoTime() / 1_000;
    }

    private void writeLog(String message) {
        // platform.writeLog(message);
    }

    private String getLogCurrentPc() {
        return Disassembler.disassemble(mmu, cpu.getPC(), mmu.readMemory(cpu.getPC()));
    }
}
