package com.fireclouu.intel8080emu.emulator.base;

public interface ResourceAdapter {

    void playSound(int id, int loop);

    // special stream for looping sfx
    abstract void playShipFX();

    void releaseShipFX();

    /////   SAVED PREFS   //////
    void putPrefs(String name, int value);

    int getPrefs(String name);

    /////   ACCESSORIES   //////
    void vibrate(long milli);
}
