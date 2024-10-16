package com.fireclouu.intel8080emu;

import com.fireclouu.intel8080emu.emulator.Platform;
import android.os.Environment;

public class HostHook {
	private static HostHook hostHook;
	private Platform platform;
	
	public static final String ITEM_HISCORE = "HISCORE";
    public static final String PREFS_NAME = "data";
    public static final String INTENT_FILE_IS_TEST_ROM = "intentFileIsTestRom";
    public static final String INTENT_ROM_FILE_NAME = "intentTestRomFileName";
	public static final String TAG = "FIRECLOUU_SI";
	public static final String STORAGE_LOCATION = 
		Environment.getExternalStorageDirectory().getPath() + "Download/";
    
	private HostHook() {}

    public static HostHook getInstance() {
        if (hostHook == null) {
            hostHook = new HostHook();
        }

        return hostHook;
    }

    public Object getData(HostHook.ACTION_TYPE type) {
        if (platform == null) return null;

        switch (type) {
            case GET_HISCORE:
                return platform.fetchHighscoreOnPlatform();
        }

        return null;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    public void setPlatform(HostPlatform platform) {
        this.platform = platform;
    }

    public void setData(HostHook.ACTION_TYPE type, Object data) {
        if (platform == null) return;
        switch (type) {
            case SET_HISCORE:
                platform.saveHighscoreOnPlatform((int) data);
                break;
        }
    }

    public enum ACTION_TYPE {
        GET_HISCORE, SET_HISCORE,
    }
}
