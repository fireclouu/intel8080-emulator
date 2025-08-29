package com.fireclouu.spaceinvaders.intel8080;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Platform {
    private boolean isInstantiated = false;
    private ExecutorService executor;
    private Runnable runnable;
    private final Guest guest;

    private final int[] mediaIds = new int[9];
    private final Emulator emulator;
    private final Inputs inputs;
    private String fileName;
    private final boolean isFileTestSuite;
    private int idMediaPlayed;

    private boolean isDebugging = false;

    public abstract void draw(short[] memoryVideoRam);

    public abstract void stopSound(int id);

    public abstract void vibrate(long milli);

    public abstract void writeLog(String message);

    public abstract void log(Exception e, String message);

    public abstract void saveHighScoreOnPlatform(int data);

    public abstract int playMedia(int id, int loop, int priority);

    public abstract int fetchHighScoreOnPlatform();

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

    public abstract void releaseResources();

    public abstract void showDebug();

    public Platform(boolean isTestSuite) {
        this.isFileTestSuite = isTestSuite;
        this.guest = new Guest(this);
        this.emulator = new Emulator(guest);
        this.inputs = new Inputs(emulator);
    }

    public boolean isInstantiated() {
        return this.isInstantiated;
    }

    private void init() {
        executor = Executors.newSingleThreadExecutor();
        runnable = null;

        initRunnable();
        initMediaHandler();

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
        if (isInstantiated) return;

        init();

        // load test suite, else load space invaders
        if (fileIsTestSuite()) {
            if (!isFileLoadedToRom(getTestAssetPath() + fileName, 0x0100)) return;
            guest.getMmu().writeTestSuitePatch();
            guest.getCpu().setPC(0x0100);
        } else {
            if (!isSpaceInvadersLoaded()) return;
        }

        emulator.setPause(false);
        emulator.setRunningState(true);
        executor.execute(runnable);

        isInstantiated = true;
    }

    private void initRunnable() {
        runnable = fileIsTestSuite() ? () -> {
            while (emulator.isRunning()) {
                if (emulator.isPaused()) continue;
                if (isDebugging) showDebug();
                emulator.tickCpuOnly();
            }
        } : () -> {
            while (emulator.isRunning()) {
                if (emulator.isPaused()) continue;
                if (isDebugging) showDebug();
                emulator.tick();
            }
        };
    }

    private boolean isFileLoadedToRom(String fileName, int startAddress) {
        boolean result = true;
        InputStream file = openFile(fileName);
        short read;

        try {
            while ((read = (short) file.read()) != -1) {
                if (fileIsTestSuite()) {
                    guest.writeMemory(startAddress++, read);
                }
                else {
                    guest.writeMemoryRom(startAddress++, read);
                }
            }
            file.close();
        } catch (IOException e) {
            log(e, fileName + " cannot be read!");
            result = false;
        }
        return result;
    }

    private boolean isSpaceInvadersLoaded() {
        for (Map.Entry<String, Integer> item : Guest.mapFileData.entrySet()) {
            if (!isFileLoadedToRom(item.getKey(), item.getValue())) return false;
        }
        return true;
    }

    public void sendInput(int port, byte key, boolean isDown) {
        inputs.sendInput(port, key, isDown);
    }

    public byte getPlayerPort() {
        return inputs.getPlayerPort();
    }

    public void setPlayerPort(byte playerPort) {
        inputs.setPlayerPort(playerPort);
    }

    public void emulationPause() {
        emulator.setPause(true);
    }

    public void emulationResume() {
        emulator.setPause(false);
    }

    public void emulationTerminate() {
        emulator.setRunningState(false);
        executor.shutdown();
        releaseResources();
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

    public boolean isDebugging() {
        return isDebugging;
    }

    public void setDebugging(boolean debugging) {
        isDebugging = debugging;
    }

    public Cpu getCpu() {
        return emulator.getCpu();
    }

    public String getFileName() {
        return fileName;
    }
}
