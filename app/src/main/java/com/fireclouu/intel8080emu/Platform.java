package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import java.io.*;
import android.view.*;

public class Platform extends PlatformAdapter implements ResourceAdapter
{
	private final Context context;
	private SharedPreferences sp;
	private SoundPool soundPool, spShipFX;
	private SharedPreferences.Editor editor;
	private Vibrator vibrator;
	private DisplayAdapter display;
	private LinearLayout llLogs;
	private TextView tvLog;
	private Handler handler;
	private String outputMessage = "";
	private Runnable runnable;
	
	public Platform(Activity activity, Context context, DisplayAdapter display) {
		super(display);
		this.display = display;
		this.context = context;
		tvLog = activity.findViewById(R.id.tvLog);
		llLogs = activity.findViewById(R.id.llLogs);
		
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run()
			{
				tvLog.setText(outputMessage);
				handler.post(this);
			}
		};
		handler.post(runnable);
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

	@Override
	public boolean isDrawing()
	{
		return display.isDrawing();
	}
	
	
	@Override
	public void writeLog(String message)
	{
		if (!isLogging()) return;
		outputMessage = message;
	}

	@Override
	public void setLogState(boolean value)
	{
		int visibility = value ? View.VISIBLE : View.GONE;
		llLogs.setVisibility(visibility);
		super.setLogState(value);
	}
	
	
}
