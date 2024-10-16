package com.fireclouu.intel8080emu.emulator;

import android.os.Handler;
import android.os.Looper;

import com.fireclouu.intel8080emu.emulator.Emulator;
import com.fireclouu.intel8080emu.emulator.Guest;
import com.fireclouu.intel8080emu.emulator.KeyInterrupts;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

public abstract class Platform {
    protected Handler handler;
    protected ExecutorService executor;
	protected Guest guest;
	
	private final int[] mediaIds = new int[9];
    private Emulator emulator;
    private KeyInterrupts keyInterrupts;
    private boolean isLogging;
    private String romFileName;
    private boolean isTestSuite;
	private int idMediaPlayed;
	
	public abstract void draw(short[] memoryVram);
	public abstract void stopSound(int id);
	public abstract void vibrate(long milli);
	public abstract void writeLog(String message);
	public abstract void saveHighscoreOnPlatform(int data);
	public abstract int playSound(int id, int loop);
	public abstract int fetchHighscoreOnPlatform();
	public abstract InputStream openFile(String romName);
	// public abstract float[] convertVramToFloatPoints(Orientation drawOrientation, short[] memory);
	
	public Platform(boolean isTestSuite) {
        this.isTestSuite = isTestSuite;
		guest = new Guest(this);
    }

    // Main
    public void start() {
		emulator = new Emulator(guest);
        keyInterrupts = new KeyInterrupts(emulator);
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
		
        if (isTestSuite) {
            loadFile("tests/" + romFileName, 0x0100);
            guest.getMmu().writeTestSuitePatch();
            guest.getCpu().setPC(0x0100);
            writeLog("File name: " + romFileName + "\n");
            for (int i = 0; i <= 25; i++) {
                writeLog("-");
            }
            writeLog("\n");
        } else {
            isFilesLoaded();
        }
    }

    public void loadFile(String filename, int addr) {
        InputStream file = openFile(filename);
        short read;

        try {
            while ((read = (short) file.read()) != -1) {
				guest.writeMemoryRom(addr++, read);
            }
            file.close();
        } catch (IOException e) {
            // OUT_MSG = filename + " cannot be read!";
        }
    }

    private boolean isFilesLoaded() {
        for (Map.Entry<String, Integer> item : Guest.mapFileData.entrySet()) {
            if (isAvailable(item.getKey())) {
                loadFile(item.getKey(), item.getValue());
            } else {
                // System.out.println(OUT_MSG);
                return false;
            }
        }
        return true;
    }

    private boolean isAvailable(String filename) {
        try {
            if (openFile(filename) == null) {
                // OUT_MSG = "File \"" + filename + "\" could not be found.";
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
        // OUT_MSG = "File online , loaded successfully!";
        return true;
    }

    public void sendInput(int port, byte key, boolean isDown) {
        keyInterrupts.sendInput(port, key, isDown);
    }

    public byte getPlayerPort() {
        return keyInterrupts.getPlayerPort();
    }

    public void setPlayerPort(byte playerPort) {
        keyInterrupts.setPlayerPort(playerPort);
    }

    public void setPause(boolean value) {
        emulator.setPause(value);
    }

    public boolean isPaused() {
        return emulator.isPaused();
    }

    public void stop() {
        emulator.stop();
        executor.shutdown();
    }

    public boolean isLogging() {
        return isLogging;
    }

    public void toggleLog(boolean value) {
        this.isLogging = value;
    }

    public void togglePause() {
        boolean pause = !emulator.isPaused();
        emulator.setPause(pause);
    }

    public void tickEmulator() {
        emulator.tick();
    }

    public void tickCpuOnly() {
        emulator.tickCpuOnly();
    }

    public boolean isLooping() {
        return emulator.isLooping();
    }

    public boolean isTestSuite() {
        return this.isTestSuite;
    }

    public void enableTestSuite() {
        this.isTestSuite = true;
    }

    public void disableTestSuite() {
        this.isTestSuite = false;
    }

    public void setRomFileName(String romFileName) {
        this.romFileName = romFileName;
    }
	
    public void setMediaAudioIdFire(int id) {
		setMediaId(Guest.Media.Audio.FIRE, id);
    }

    public void setMediaAudioIdPlayerExploded(int id) {
		setMediaId(Guest.Media.Audio.PLAYER_EXPLODED, id);
    }

    public void setMediaAudioIdShipIncoming(int id) {
		setMediaId(Guest.Media.Audio.SHIP_INCOMING, id);
    }
	
    public void setMediaAudioIdAlienMove(int id1, int id2, int id3, int id4) {
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_1, id1);
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_2, id2);
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_3, id3);
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_4, id4);
    }

    public void setMediaAudioIdAlienKilled(int id) {
		setMediaId(Guest.Media.Audio.ALIEN_KILLED, id);
    }

    public void setMediaAudioIdShipHit(int id) {
		setMediaId(Guest.Media.Audio.SHIP_HIT, id);
    }
	
	public void setIdMediaPlayed(int idMediaPlayed) {
		this.idMediaPlayed = idMediaPlayed;
	}
	
	private void setMediaId(int index, int id) {
		mediaIds[index] = id;
	}
	
	public int getMediaId(int index) {
		return mediaIds[index];
	}

	public int getIdMediaPlayed() {
		return this.idMediaPlayed;
	}
}
