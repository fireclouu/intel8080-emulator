package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface ApiAdapter
{
	// INVADERS
	public static int MEDIA_EFFECT_FIRE;
	public static int MEDIA_EFFECT_PLAYER_EXPLODED;
	public static int MEDIA_EFFECT_SHIP_INCOMING; // separate
	public static int MEDIA_EFFECT_ALIEN_MOVE_1;
	public static int MEDIA_EFFECT_ALIEN_MOVE_2;
	public static int MEDIA_EFFECT_ALIEN_MOVE_3;
	public static int MEDIA_EFFECT_ALIEN_MOVE_4;
	public static int MEDIA_EFFECT_ALIEN_KILLED;
	public static int MEDIA_EFFECT_SHIP_HIT;
	
	public void setEffectFire(int id) 
	public void setEffectPlayerExploded(int id) 
	public void setEffectShipIncoming(int id) 
	public void setEffectAlienMove(int id1, int id2, int id3, int id4) 
	public void setEffectAlienKilled(int id) 
	public void setEffectShipHit(int id) 
	
	public void playSound(int id, int loop);
	public void stop(int id);
	
	public void reloadResource();
	
	// special stream for looping sfx
	public void playShipFX();
	public void releaseShipFX();
	public void initShipFX()
}
