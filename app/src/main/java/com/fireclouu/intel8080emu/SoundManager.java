package com.fireclouu.intel8080emu;
import android.content.*;
import android.media.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class SoundManager implements MediaAdapter
{
	Context context;
	SoundPool soundPool;
	
	public SoundManager(Context context) {
		this.context = context;
		
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
	}
	
	@Override
	public void play(final int id) {
		soundPool.play(id, 1, 1, 0, 0, 1);
	}
	
	@Override
	public void setEffectFire(int id) {
		this.MEDIA_EFFECT_FIRE = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectPlayerExploded(int id) {
		this.MEDIA_EFFECT_PLAYER_EXPLODED = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectShipIncoming(int id) {
		this.MEDIA_EFFECT_SHIP_INCOMING = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectAlienMove(int id1, int id2, int id3, int id4) {
		this.MEDIA_EFFECT_ALIEN_MOVE_1 = soundPool.load(context, id1, 1);
		this.MEDIA_EFFECT_ALIEN_MOVE_2 = soundPool.load(context, id2, 1);
		this.MEDIA_EFFECT_ALIEN_MOVE_3 = soundPool.load(context, id3, 1);
		this.MEDIA_EFFECT_ALIEN_MOVE_4 = soundPool.load(context, id4, 1);
	}

	@Override
	public void setEffectAlienKilled(int id) {
		this.MEDIA_EFFECT_ALIEN_KILLED = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectAlienFast(int id) {
		this.MEDIA_EFFECT_ALIEN_FAST = soundPool.load(context, id, 1);
	}
	
	
	public void releaseResource() {
		soundPool.release();
		soundPool = null;
	}
}
