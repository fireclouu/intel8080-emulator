package com.fireclouu.intel8080emu.emulator.base;

import com.fireclouu.intel8080emu.emulator.Cpu;

public interface IOAdapter {
    int PORT_KEY_LEFT = 0;
    int PORT_KEY_UP = 1;
    int PORT_KEY_RIGHT = 2;
    int PORT_KEY_DOWN = 3;
    int PORT_KEY_FIRE = 4;
    int PORT_COIN = 5;

    short handleIn(Cpu cpu, short port);

    void handleOut(Cpu cpu, ResourceAdapter media, short port, short value);
}
