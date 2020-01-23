package com.fireclouu.intel8080emu.Emulator.BaseClass;

public interface MediaAdapter
{
	// INVADERS
	public static int MEDIA_EFFECT_FIRE;
	public static int MEDIA_EFFECT_PLAYER_EXPLODED;
	public static int MEDIA_EFFECT_SHIP_INCOMING;
	public static int MEDIA_EFFECT_ALIEN_MOVE_1;
	public static int MEDIA_EFFECT_ALIEN_MOVE_2;
	public static int MEDIA_EFFECT_ALIEN_MOVE_3;
	public static int MEDIA_EFFECT_ALIEN_MOVE_4;
	public static int MEDIA_EFFECT_ALIEN_KILLED;
	public static int MEDIA_EFFECT_ALIEN_FAST;
	
	public void setEffectFire(int id) 
	public void setEffectPlayerExploded(int id) 
	public void setEffectShipIncoming(int id) 
	public void setEffectAlienMove(int id1, int id2, int id3, int id4) 
	public void setEffectAlienKilled(int id) 
	public void setEffectAlienFast(int id) 
	
	public void play(int id);
}
