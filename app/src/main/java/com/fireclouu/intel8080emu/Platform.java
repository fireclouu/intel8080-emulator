package com.fireclouu.intel8080emu;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;

import com.fireclouu.intel8080emu.Emulator.BaseClass.DisplayAdapter;
import com.fireclouu.intel8080emu.Emulator.BaseClass.MachineResources;
import com.fireclouu.intel8080emu.Emulator.BaseClass.PlatformAdapter;
import com.fireclouu.intel8080emu.Emulator.BaseClass.ResourceAdapter;
import com.fireclouu.intel8080emu.Emulator.BaseClass.StringUtils;

import java.io.IOException;
import java.io.InputStream;

public class Platform extends PlatformAdapter implements ResourceAdapter
{
	private final Context context;
	private SharedPreferences sp;
	private SoundPool soundPool, spShipFX;
	private SharedPreferences.Editor editor;
	private Vibrator vibrator;
	
	public Platform(Context context, DisplayAdapter display) {
		super(display);
		this.context = context;
		platformInit();
	}
	
	private void platformInit() {
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		spShipFX = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		sp = context.getSharedPreferences("si_prefs", 0);
		editor = sp.edit();
	}
	
	@Override
	public InputStream openFile(String romName) {
		try
		{
			return context.getAssets().open(romName);
		} catch (IOException e) {
			String exception = e.getMessage() == null ? "openFile: Message is null" : e.getMessage();
			Log.e(StringUtils.TAG, exception);
			return null;
		}
	}
	
	/////   API   /////
	@Override
	public void playSound(int id, int loop)
	{
		soundPool.play(id, 1, 1, 0, loop, 1);
	}

	@Override
	public void reloadResource()
	{
		setEffectShipIncoming(R.raw.ship_incoming);
	}

	@Override
	public void setEffectFire(int id) {
		MachineResources.MEDIA_EFFECT_FIRE = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectPlayerExploded(int id) {
		MachineResources.MEDIA_EFFECT_PLAYER_EXPLODED = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectShipIncoming(int id) {
		MachineResources.MEDIA_EFFECT_SHIP_INCOMING = spShipFX.load(context, id, 1);
	}

	@Override
	public void setEffectAlienMove(int id1, int id2, int id3, int id4) {
		MachineResources.MEDIA_EFFECT_ALIEN_MOVE_1 = soundPool.load(context, id1, 1);
		MachineResources.MEDIA_EFFECT_ALIEN_MOVE_2 = soundPool.load(context, id2, 1);
		MachineResources.MEDIA_EFFECT_ALIEN_MOVE_3 = soundPool.load(context, id3, 1);
		MachineResources.MEDIA_EFFECT_ALIEN_MOVE_4 = soundPool.load(context, id4, 1);
	}

	@Override
	public void setEffectAlienKilled(int id) {
		MachineResources.MEDIA_EFFECT_ALIEN_KILLED = soundPool.load(context, id, 1);
	}

	@Override
	public void setEffectShipHit(int id) {
		MachineResources.MEDIA_EFFECT_SHIP_HIT = soundPool.load(context, id, 1);
	}

	// Hacks for SoundPool bug

	@Override
	public void playShipFX() {
		spShipFX.play(MachineResources.MEDIA_EFFECT_SHIP_INCOMING, 1, 1, 0, -1, 1);
	}

	@Override
	public void releaseShipFX() {
		spShipFX.stop(MachineResources.MEDIA_EFFECT_SHIP_INCOMING);
		spShipFX.unload(MachineResources.MEDIA_EFFECT_SHIP_INCOMING);
		spShipFX.release();

		initShipFX();
	}

	@Override
	public void initShipFX() {
		spShipFX = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		reloadResource();
	}


	public void releaseResource() {
		soundPool.release();
		soundPool = null;
	}
	
	/////   SHARED PREFS   /////
	@Override
	public void putPrefs(String name, int value) {
		editor.putInt(name, value);
		editor.apply();
	}

	@Override
	public int getPrefs(String name) {
		return sp.getInt(name, 0);
	}
	
	/////   ACCESSORIES   /////
	@Override
	public void vibrate(long milli) {
		vibrator.vibrate(milli);
	}
	
}
