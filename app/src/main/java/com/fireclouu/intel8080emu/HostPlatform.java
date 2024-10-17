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
import com.fireclouu.intel8080emu.emulator.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fireclouu.intel8080emu.emulator.*;
import android.media.*;
import android.os.*;

public class HostPlatform extends Platform {
	private final Display display;
	private final Activity activity;
    private final Context context;
    private SharedPreferences sp;
    private SoundPool soundPool;
    private SharedPreferences.Editor editor;
    private Vibrator vibrator;
    private LinearLayout llLogs;
    private ScrollView svLogs;
    private TextView tvLog;
    private Button buttonPause;
	private Handler handler;
 
    public HostPlatform(Activity activity, Context context, Display display, boolean isTestSuite) {
		super(isTestSuite);
		this.activity = activity;
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
		
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sp = context.getSharedPreferences(HostUtils.PREFS_NAME, 0);
        editor = sp.edit();
    }
	
	@Override
	public void initMediaHandler() {
		AudioAttributes.Builder audioAttribBuilder = new AudioAttributes.Builder();
		audioAttribBuilder.setUsage(AudioAttributes.USAGE_GAME);
		audioAttribBuilder.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN);

		if (Build.VERSION.SDK_INT >= 29) {
			audioAttribBuilder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
		}

		audioAttribBuilder.setFlags(AudioAttributes.FLAG_LOW_LATENCY);

		SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
		soundPoolBuilder.setAudioAttributes(audioAttribBuilder.build());
		soundPoolBuilder.setMaxStreams(3);

		soundPool = soundPoolBuilder.build();
	}
	
	@Override
	public int getMediaAudioIdAlienKilled() {
		return getSoundPoolLoadId(R.raw.alien_killed, 0);
	}

	@Override
	public int getMediaAudioIdAlienMove1() {
		return getSoundPoolLoadId(R.raw.enemy_move_1, 0);
	}

	@Override
	public int getMediaAudioIdAlienMove2() {
		return getSoundPoolLoadId(R.raw.enemy_move_2, 0);
	}

	@Override
	public int getMediaAudioIdAlienMove3() {
		return getSoundPoolLoadId(R.raw.enemy_move_3, 0);
	}

	@Override
	public int getMediaAudioIdAlienMove4() {
		return getSoundPoolLoadId(R.raw.enemy_move_4, 0);
	}

	@Override
	public int getMediaAudioIdFire() {
		return getSoundPoolLoadId(R.raw.fire, 0);
	}

	@Override
	public int getMediaAudioIdPlayerExploded() {
		return getSoundPoolLoadId(R.raw.explosion, 0);
	}

	@Override
	public int getMediaAudioIdShipHit() {
		return getSoundPoolLoadId(R.raw.ship_hit, 0);
	}

	@Override
	public int getMediaAudioIdShipIncoming() {
		return getSoundPoolLoadId(R.raw.ship_incoming, 0);
	}

    @Override
    public InputStream openFile(String fileName) {
        try {
            return context.getAssets().open(fileName);
        } catch (IOException e) {
            log(e, fileName + " could not be found!");
            return null;
        }
    }
	
	@Override
	public void draw(short[] memoryVram) {
		display.draw(memoryVram);
	}

    @Override
    public int playMedia(int index, int loop) {
        return soundPool.play(getMediaId(index), 1, 1, 0, loop, 1);
    }

	@Override
	public void stopSound(int id) {
		soundPool.stop(id);
	}

    public void releaseResource() {
        soundPool.release();
        soundPool = null;
    }

    public void setPrefs(String name, int value) {
        editor.putInt(name, value);
        editor.apply();
    }

    public int getPrefs(String name) {
        return sp.getInt(name, 0);
    }
	
	@Override
	public int fetchHighscoreOnPlatform() {
        return getPrefs(HostUtils.ITEM_HISCORE);
    }
	
	@Override
    public void saveHighscoreOnPlatform(int data) {
        int storedHiscore = getPrefs(HostUtils.ITEM_HISCORE);

        if (data > storedHiscore) {
            setPrefs(HostUtils.ITEM_HISCORE, data);
        }
    }

    @Override
    public void vibrate(long milli) {
        vibrator.vibrate(milli);
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
	public void log(Exception e, String message) {
		Log.e(HostUtils.TAG, message);
		if (e == null) return;
		Log.e(HostUtils.TAG, e.getMessage());
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

	private int getSoundPoolLoadId(int id, int priority) {
		return soundPool.load(context, id, priority);
	}
}
