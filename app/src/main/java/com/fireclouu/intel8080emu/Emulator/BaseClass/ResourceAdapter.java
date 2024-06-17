package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface ResourceAdapter
{
	public void setEffectFire(int id);
	public void setEffectPlayerExploded(int id);
	public void setEffectShipIncoming(int id);
	public void setEffectAlienMove(int id1, int id2, int id3, int id4);
	public void setEffectAlienKilled(int id);
	public void setEffectShipHit(int id);
	
	public void playSound(int id, int loop);
	public void stop(int id);
	
	public void reloadResource();
	
	// special stream for looping sfx
	public void playShipFX();
	public void releaseShipFX();
	public void initShipFX();
	
	/////   SAVED PREFS   //////
	public void putPrefs(String name, int value);
	public int getPrefs(String name);
	
	/////   ACCESSORIES   //////
	public void vibrate(long milli);
}