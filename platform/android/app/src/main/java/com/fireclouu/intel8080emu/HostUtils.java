package com.fireclouu.intel8080emu;

import android.os.Environment;

public class HostUtils {
	public static final String ITEM_HIGH_SCORE = "HIGH_SCORE";
    public static final String PREFS_NAME = "data";
    public static final String INTENT_FILE_IS_TEST_ROM = "intentFileIsTestRom";
    public static final String INTENT_ROM_FILE_NAME = "intentTestRomFileName";
	public static final String TAG = "FIRECLOUU_SI";
	public static final String STORAGE_LOCATION = 
		Environment.getExternalStorageDirectory().getPath() + "Download/";
    
	private HostUtils() {}
    
    public enum ACTION_TYPE {
        GET_HIGH_SCORE, SET_HIGH_SCORE,
    }
}
