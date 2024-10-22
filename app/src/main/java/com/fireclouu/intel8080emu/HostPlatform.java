package com.fireclouu.intel8080emu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fireclouu.intel8080emu.emulator.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class HostPlatform extends Platform {
    private final Display display;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private SoundPool soundPool;
    private final TextView tvLog;
    private final Button buttonPause;
    private Handler handler;
    private final Vibrator vibrator;

    public HostPlatform(Activity activity, Context context, Display display, boolean isTestSuite) {
        super(isTestSuite);
        this.context = context;
        this.display = display;
        tvLog = activity.findViewById(R.id.tvLog);
        buttonPause = activity.findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                togglePause();
            }
        });

        tvLog.setText("");

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sharedPreferences = context.getSharedPreferences(HostUtils.PREFS_NAME, 0);
    }

    @Override
    public void initMediaHandler() {
        AudioAttributes.Builder audioAttribBuilder = new AudioAttributes.Builder();
        audioAttribBuilder.setUsage(AudioAttributes.USAGE_GAME);
        audioAttribBuilder.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN);

        if (Build.VERSION.SDK_INT >= 29) {
            audioAttribBuilder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            audioAttribBuilder.setFlags(AudioAttributes.FLAG_LOW_LATENCY);
        }

        SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();
        soundPoolBuilder.setAudioAttributes(audioAttribBuilder.build());
        soundPoolBuilder.setMaxStreams(3);

        soundPool = soundPoolBuilder.build();
    }

    @Override
    public void releaseResources() {
        soundPool.release();
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
        return getSoundPoolLoadId(R.raw.ship_incoming, 1);
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
    public void draw(short[] memoryVideoRam) {
        display.draw(memoryVideoRam);
    }

    @Override
    public int playMedia(int index, int loop) {
        return soundPool.play(getMediaId(index), 1, 1, 0, loop, 1);
    }

    @Override
    public void stopSound(int id) {
        soundPool.stop(id);
    }

    public void putIntOnSharedPreferences(String name, int value) {
        sharedPreferences.edit().putInt(name, value).apply();
    }

    public int getIntOnSharedPreferences(String name) {
        return sharedPreferences.getInt(name, 0);
    }

    @Override
    public int fetchHighScoreOnPlatform() {
        return getIntOnSharedPreferences(HostUtils.ITEM_HIGH_SCORE);
    }

    @Override
    public void saveHighScoreOnPlatform(int data) {
        int storedHighScore = getIntOnSharedPreferences(HostUtils.ITEM_HIGH_SCORE);

        if (data > storedHighScore) {
            putIntOnSharedPreferences(HostUtils.ITEM_HIGH_SCORE, data);
        }
    }

    @Override
    public void vibrate(long milli) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(milli, 20));
        } else {
            vibrator.vibrate(milli);
        }
    }

    @Override
    public void writeLog(final String message) {
        // TODO: implement
    }

    @Override
    public void log(Exception e, String message) {
        Log.e(HostUtils.TAG, message);
        if (e == null) return;
        Log.e(HostUtils.TAG, Objects.requireNonNull(e.getMessage()));
    }

    @Override
    public void togglePause() {
        super.togglePause();
    }

    @Override
    public String getTestAssetPath() {
        return "tests/";
    }

    @Override
    public void sendNotification(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getSoundPoolLoadId(int id, int priority) {
        return soundPool.load(context, id, priority);
    }
}
