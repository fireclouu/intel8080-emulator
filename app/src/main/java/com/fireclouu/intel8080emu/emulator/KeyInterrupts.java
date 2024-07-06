package com.fireclouu.intel8080emu.emulator;

public class KeyInterrupts {
    public static final byte KEY_COIN = 0b00000001;
    public static final byte KEY_P2_START = 0b00000010;
    public static final byte KEY_P1_START = 0b00000100;
    public static final byte KEY_FIRE = 0b00010000;
    public static final byte KEY_LEFT = 0b00100000;
    public static final byte KEY_RIGHT = 0b01000000;

    public static final byte INPUT_PORT_1 = 0x1;
    public static final byte INPUT_PORT_2 = 0x2;

    private Emulator emulator = null;
    private byte playerPort = INPUT_PORT_1;

    public KeyInterrupts(Emulator emulator) {
        this.emulator = emulator;
    }

    public void sendInput(int port, byte key, boolean isDown) {
        // player 2 button fix
        if (port == INPUT_PORT_2 && key == KEY_P1_START) {
            port = 1;
            key = KEY_P2_START;
        }

        if (isDown) {
            emulator.port[port] |= key;
        } else {
            emulator.port[port] &= ~key;
        }
    }

    public byte getPlayerPort() {
        return this.playerPort;
    }

    public void setPlayerPort(byte playerPort) {
        this.playerPort = playerPort;
    }
}
