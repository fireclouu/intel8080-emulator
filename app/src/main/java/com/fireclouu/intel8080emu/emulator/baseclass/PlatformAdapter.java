package com.fireclouu.intel8080emu.emulator.baseclass;

import android.os.Handler;
import android.os.Looper;

import com.fireclouu.intel8080emu.emulator.Emulator;
import com.fireclouu.intel8080emu.emulator.Guest;
import com.fireclouu.intel8080emu.emulator.KeyInterrupts;
import com.fireclouu.intel8080emu.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class PlatformAdapter implements ResourceAdapter {
    protected Handler handler;
    protected ExecutorService executor;
    private Emulator emulator;
    private DisplayAdapter display;
    private Guest guest;
    private KeyInterrupts keyInterrupts;
    private boolean isLogging;
    private String testRomFileName;
    private boolean isTestSuite;

    public PlatformAdapter(DisplayAdapter display, boolean isTestSuite) {
        this.display = display;
        this.isTestSuite = isTestSuite;
    }

    public abstract InputStream openFile(String romName);

    public abstract void writeLog(String message);

    public abstract boolean isDrawing();

    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }

    // Main
    public void start() {
        init();
        if (isTestSuite) {
            loadFile("tests/" + testRomFileName, 0x100);
            guest.getMmu().writeTestSuitePatch();
            guest.getCpu().setPC(0x100);
            writeLog("File name: " + testRomFileName + "\n");
            for (int i = 0; i <= 25; i++) {
                writeLog("-");
            }
            writeLog("\n");
        } else {
            loadSplitFiles();
        }
    }

    public void init() {
        guest = new Guest();
        emulator = new Emulator(guest);
        keyInterrupts = new KeyInterrupts(emulator);
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // media
        setEffectShipHit(R.raw.ship_hit);
        setEffectAlienKilled(R.raw.alien_killed);
        setEffectAlienMove(R.raw.enemy_move_1, R.raw.enemy_move_2, R.raw.enemy_move_3, R.raw.enemy_move_4);
        setEffectFire(R.raw.fire);
        setEffectPlayerExploded(R.raw.explosion);
        setEffectShipIncoming(R.raw.ship_incoming);
    }

    public short[] loadFile(String filename, int addr, boolean sizeActual) {
        short[] holder = null;
        short[] tmp = new short[0x10_000];
        InputStream file = openFile(filename);
        short read;
        int counter = 0;
        try {
            while ((read = (short) file.read()) != -1) {
                tmp[addr++] = read;
                counter++;
            }
            file.close();
            file = null;
        } catch (IOException e) {
            // OUT_MSG = filename + " cannot be read!";
            return null;
        }

        holder = new short[(sizeActual ? counter : StringUtils.Component.PROGRAM_LENGTH)];

        counter = 0;
        for (short tmp2 : tmp) {
            holder[counter++] = tmp2;
        }

        return holder;
    }

    public void loadFile(String filename, int addr) {
        InputStream file = openFile(filename);
        short read;

        try {
            while ((read = (short) file.read()) != -1) {
                guest.getMmu().writeMemory(addr++, read);
            }
            file.close();
            file = null;
        } catch (IOException e) {
            // OUT_MSG = filename + " cannot be read!";
        }
    }

    private byte loadSplitFiles() {
        int counter = 0;
        for (String files : StringUtils.File.FILES) {
            if (isAvailable(files)) {
                loadFile(files, StringUtils.File.ROM_ADDRESS[counter++]);
            } else {
                // System.out.println(OUT_MSG);
                return 1;
            }
        }
        return 0;
    }

    private boolean isAvailable(String filename) {
        if (StringUtils.File.FILES.length == 0) {
            // OUT_MSG = "No files specified.";
            return false;
        }
        if (StringUtils.File.ROM_ADDRESS.length == 0) {
            // OUT_MSG = "File is empty.";
            return false;
        }
        if (StringUtils.File.ROM_ADDRESS.length != StringUtils.File.FILES.length) {
            // OUT_MSG = "File online, but roms and memory address unaligned.";
            return false;
        }
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

    public int getHighscore() {
        return getPrefs(StringUtils.ITEM_HISCORE);
    }

    public void setHighscore(int data) {
        int storedHiscore = getPrefs(StringUtils.ITEM_HISCORE);

        if (data > storedHiscore) {
            putPrefs(StringUtils.ITEM_HISCORE, data);
        }
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
        emulator.tick(display, this);
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

    public String getTestRomFileName() {
        return this.testRomFileName;
    }

    public void setTestRomFileName(String testRomFileName) {
        this.testRomFileName = testRomFileName;
    }
}
