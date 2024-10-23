package com.fireclouu.intel8080emu.emulator;

public class Inputs {
    public static final byte KEY_COIN = 0b00000001;
    public static final byte KEY_P2_START = 0b00000010;
    public static final byte KEY_P1_START = 0b00000100;
    public static final byte KEY_FIRE = 0b00010000;
    public static final byte KEY_LEFT = 0b00100000;
    public static final byte KEY_RIGHT = 0b01000000;

    public static final byte INPUT_PORT_1 = 0;
    public static final byte INPUT_PORT_2 = 1;

    private final Emulator emulator;
    private byte playerPort = INPUT_PORT_1;

    public Inputs(Emulator emulator) {
        this.emulator = emulator;
    }

    public void sendInput(int port, byte key, boolean isDown) {
        if (port == INPUT_PORT_2 && key == KEY_P1_START) {
            port = 1;
            key = KEY_P2_START;
        }

        if (isDown) {
			emulator.setPortXor(port, key);
        } else {
			emulator.setPortAnd(port, (byte) ~key);
        }
    }

    public byte getPlayerPort() {
        return this.playerPort;
    }

    public void setPlayerPort(byte playerPort) {
        this.playerPort = playerPort;
    }
}
