package com.fireclouu.intel8080emu.emulator.baseclass;

public interface ResourceAdapter {
    void setEffectFire(int id);

    void setEffectPlayerExploded(int id);

    void setEffectShipIncoming(int id);

    void setEffectAlienMove(int id1, int id2, int id3, int id4);

    void setEffectAlienKilled(int id);

    void setEffectShipHit(int id);

    void playSound(int id, int loop);

    void reloadResource();

    // special stream for looping sfx
    void playShipFX();

    void releaseShipFX();

    void initShipFX();

    /////   SAVED PREFS   //////
    void putPrefs(String name, int value);

    int getPrefs(String name);

    /////   ACCESSORIES   //////
    void vibrate(long milli);
}