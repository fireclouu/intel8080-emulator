package com.fireclouu.intel8080emu;

import com.fireclouu.intel8080emu.emulator.Platform;
import android.os.Environment;

public class HostUtils {
	public static final String ITEM_HISCORE = "HISCORE";
    public static final String PREFS_NAME = "data";
    public static final String INTENT_FILE_IS_TEST_ROM = "intentFileIsTestRom";
    public static final String INTENT_ROM_FILE_NAME = "intentTestRomFileName";
	public static final String TAG = "FIRECLOUU_SI";
	public static final String STORAGE_LOCATION = 
		Environment.getExternalStorageDirectory().getPath() + "Download/";
    
	private HostUtils() {}
    
    public enum ACTION_TYPE {
        GET_HISCORE, SET_HISCORE,
    }
}
