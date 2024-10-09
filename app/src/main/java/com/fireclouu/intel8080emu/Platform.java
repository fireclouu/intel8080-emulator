package com.fireclouu.intel8080emu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fireclouu.intel8080emu.emulator.base.DisplayAdapter;
import com.fireclouu.intel8080emu.emulator.base.PlatformAdapter;
import com.fireclouu.intel8080emu.emulator.base.ResourceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fireclouu.intel8080emu.emulator.*;
import android.media.*;

public class Platform extends PlatformAdapter implements ResourceAdapter {
    private final Context context;
    private SharedPreferences sp;
    private SoundPool soundPool;
    private SharedPreferences.Editor editor;
    private Vibrator vibrator;
    private final DisplayAdapter display;
    private final LinearLayout llLogs;
    private final ScrollView svLogs;
    private final TextView tvLog;
    private final Button buttonPause;
    private final ExecutorService exec2;
	
	private int idSoundPoolPlayMediaShipIncoming;
	
    public Platform(Activity activity, Context context, DisplayAdapter display, boolean isTestSuite) {
        super(display, isTestSuite);
		this.context = context;
        this.display = display;
        
        tvLog = activity.findViewById(R.id.tvLog);
        llLogs = activity.findViewById(R.id.llLogs);
        buttonPause = activity.findViewById(R.id.buttonPause);
        svLogs = activity.findViewById(R.id.svLogs);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                togglePause();
            }
        });

        tvLog.setText("");
		AudioAttributes.Builder audioAttribBuilder = new AudioAttributes.Builder();
		audioAttribBuilder.setUsage(AudioAttributes.USAGE_GAME);
		audioAttribBuilder.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN);
		audioAttribBuilder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
		audioAttribBuilder.setFlags(AudioAttributes.FLAG_LOW_LATENCY);
		
		SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
		soundPoolBuilder.setAudioAttributes(audioAttribBuilder.build());
		soundPoolBuilder.setMaxStreams(2);
		
		soundPool = soundPoolBuilder.build();
		
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sp = context.getSharedPreferences(HostHook.PREFS_NAME, 0);
        editor = sp.edit();

        // media
		setMediaAudioIdFire(getSoundPoolLoadId(R.raw.fire, 0));
		setMediaAudioIdPlayerExploded(getSoundPoolLoadId(R.raw.explosion, 0));
		setMediaAudioIdShipIncoming(getSoundPoolLoadId(R.raw.ship_incoming, 1));
		setMediaAudioIdAlienMove(
			getSoundPoolLoadId(R.raw.enemy_move_1, 0),
			getSoundPoolLoadId(R.raw.enemy_move_2, 0),
			getSoundPoolLoadId(R.raw.enemy_move_3, 0),
			getSoundPoolLoadId(R.raw.enemy_move_4, 0)
		);
		setMediaAudioIdAlienKilled(getSoundPoolLoadId(R.raw.alien_killed, 0));
        setMediaAudioIdShipHit(getSoundPoolLoadId(R.raw.ship_hit, 0));
        exec2 = Executors.newSingleThreadExecutor();
        HostHook.getInstance().setPlatform(this);
    }

    @Override
    public InputStream openFile(String romName) {
        try {
            return context.getAssets().open(romName);
        } catch (IOException e) {
            String exception = e.getMessage() == null ? "openFile: Message is null" : e.getMessage();
            Log.e(HostHook.TAG, exception);
            return null;
        }
    }

    /////   API   /////
    @Override
    public void playSound(int id, int loop) {
        soundPool.play(id, 1, 1, 0, loop, 1);
    }

    @Override
    public void playShipFX() {
        idSoundPoolPlayMediaShipIncoming = soundPool.play(Guest.MEDIA_AUDIO.SHIP_INCOMING.getId(), 1, 1, 1, -1, 1);
    }

    @Override
    public void releaseShipFX() {
        soundPool.stop(idSoundPoolPlayMediaShipIncoming);
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
    public boolean isDrawing() {
        return display.isDrawing();
    }

    @Override
    public void writeLog(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvLog.append(message);
                svLogs.post(new Runnable() {
                    @Override
                    public void run() {
                        svLogs.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    public void toggleLog(boolean value) {
        super.toggleLog(value);
        tvLog.setText("");
    }

    @Override
    public void togglePause() {
        super.togglePause();
    }

    @Override
    public void start() {
        super.start();
		
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // FIXME: terminate when pause to
                // optimize battery usage
				
                while (isLooping()) {
                    if (!isPaused()) {
                        if (!isTestSuite()) {
                            tickEmulator();
                        } else {
                            tickCpuOnly();
                        }
                    }
                }

                writeLog("\n");
                for (int i = 0; i < 25; i++) {
                    writeLog("-");
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Emulation terminated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
	
	private int getSoundPoolLoadId(int id, int priority) {
		return soundPool.load(context, id, priority);
	}
}
