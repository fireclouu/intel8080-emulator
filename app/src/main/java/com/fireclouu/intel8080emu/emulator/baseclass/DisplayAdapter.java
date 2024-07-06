package com.fireclouu.intel8080emu.emulator.baseclass;

public interface DisplayAdapter {
    int DISPLAY_WIDTH = (32 * 8); // 256 (0x2400 to 0x2407 = bit 0 to bit 7)
    int DISPLAY_HEIGHT = (224);   // 224

    byte ORIENTATION_DEFAULT = 0;
    byte DRAW_ORIENTATION_PORTRAIT = 1;
    byte DRAW_ORIENTATION_LANDSCAPE = 1;

    float[] convertVramToFloatPoints(int drawOrientation, short[] memory);

    void draw(short[] memory);

    boolean isDrawing();
}
