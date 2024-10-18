package com.fireclouu.intel8080emu.emulator;

import com.fireclouu.intel8080emu.emulator.Emulator;
import com.fireclouu.intel8080emu.emulator.Guest;
import com.fireclouu.intel8080emu.emulator.Inputs;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

public abstract class Platform {
    private ExecutorService executor;
	private Runnable runnable;
	private Guest guest;
	
	private final int[] mediaIds = new int[9];
    private Emulator emulator;
    private Inputs keyInterrupts;
    private boolean isLogging;
    private String fileName;
    private boolean isFileTestSuite = false;
	private int idMediaPlayed;
	
	public abstract void draw(short[] memoryVram);
	public abstract void stopSound(int id);
	public abstract void vibrate(long milli);
	public abstract void writeLog(String message);
	public abstract void log(Exception e, String message);
	public abstract void saveHighscoreOnPlatform(int data);
	public abstract int playMedia(int id, int loop);
	public abstract int fetchHighscoreOnPlatform();
	public abstract InputStream openFile(String romName);
	public abstract String getTestAssetPath();
	public abstract void sendNotification(String message);
	
	public abstract int getMediaAudioIdAlienKilled();
	public abstract int getMediaAudioIdAlienMove1();
	public abstract int getMediaAudioIdAlienMove2();
	public abstract int getMediaAudioIdAlienMove3();
	public abstract int getMediaAudioIdAlienMove4();
	public abstract int getMediaAudioIdFire();
	public abstract int getMediaAudioIdPlayerExploded();
	public abstract int getMediaAudioIdShipHit();
	public abstract int getMediaAudioIdShipIncoming();
	public abstract void initMediaHandler();
	// public abstract float[] convertVramToFloatPoints(Orientation drawOrientation, short[] memory);
	
	public Platform(boolean isTestSuite) {
		this.guest = new Guest(this);
		this.executor = Executors.newSingleThreadExecutor();
		this.runnable = null;
		this.isFileTestSuite = isTestSuite;
		this.emulator = new Emulator(guest);
        this.keyInterrupts = new Inputs(emulator);
    }
	
	private void init() {
		initRunnable();
		initMediaHandler();
		// audio assets
		setMediaId(Guest.Media.Audio.ALIEN_KILLED, getMediaAudioIdAlienKilled());
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_1, getMediaAudioIdAlienMove1());
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_2, getMediaAudioIdAlienMove2());
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_3, getMediaAudioIdAlienMove3());
		setMediaId(Guest.Media.Audio.ALIEN_MOVE_4, getMediaAudioIdAlienMove4());
		setMediaId(Guest.Media.Audio.FIRE, getMediaAudioIdFire());
		setMediaId(Guest.Media.Audio.PLAYER_EXPLODED, getMediaAudioIdPlayerExploded());
		setMediaId(Guest.Media.Audio.SHIP_HIT, getMediaAudioIdShipHit());
		setMediaId(Guest.Media.Audio.SHIP_INCOMING, getMediaAudioIdShipIncoming());
	}
	
    public void start() {
		init();
		
		if (fileIsTestSuite()) {
			if (!isFileLoadedToRom(getTestAssetPath() + fileName, 0x0100)) return;
			guest.getMmu().writeTestSuitePatch();
			guest.getCpu().setPC(0x0100);
		}
        
		if (!isExpectedFilesLoaded()) return;
		executor.execute(runnable);
    }
	
	private void initRunnable() {
		if (fileIsTestSuite()) {
			runnable = new Runnable() {
				
				@Override
				public void run() {
					while (emulator.isLooping()) {
						if (isPaused()) continue;
						emulator.tickCpuOnly();
					}
				}
				
			};
		} else {
			runnable = new Runnable() {

				@Override
				public void run() {
					while (emulator.isLooping()) {
						if (isPaused()) continue;
						emulator.tick();
					}
				}
					
			};
		}
	}
	
    private boolean isFileLoadedToRom(String fileName, int startAddress) {
		boolean result = true;
        InputStream file = openFile(fileName);
        short read;

        try {
            while ((read = (short) file.read()) != -1) {
				guest.writeMemoryRom(startAddress++, read);
            }
            file.close();
        } catch (IOException e) {
			log(e, fileName + " cannot be read!");
			result = false;
        } finally {
			file = null;
		}
		
		return result;
    }

    private boolean isExpectedFilesLoaded() {
        for (Map.Entry<String, Integer> item : Guest.mapFileData.entrySet()) {
            if (!isFileLoadedToRom(item.getKey(), item.getValue())) return false;
        }
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

    

    public boolean fileIsTestSuite() {
        return this.isFileTestSuite;
    }

    public void setRomFileName(String romFileName) {
        this.fileName = romFileName;
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
