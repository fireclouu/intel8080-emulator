package com.fireclouu.intel8080emu.emulator.baseclass;

import android.os.Environment;

import java.util.Date;

public final class StringUtils {
    public static final String TAG = "FRCL_SPACE_INVADERS";
    public static final String ITEM_HISCORE = "HISCORE";
    public static final String PREFS_NAME = "data";
    public static final String INTENT_FILE_IS_TEST_ROM = "intentFileIsTestRom";
    public static final String INTENT_TEST_ROM_FILE_NAME = "intentTestRomFileName";

    private StringUtils() {
    }

    public static String getTime() {
        Date date = new Date();
        return String.format("%02d", date.getHours()) + ":" + String.format("%02d", date.getMinutes()) + ":" + String.format("%02d", date.getSeconds());
    }

    public static final class File {
        public static final String[] FILES = {"invaders.h", "invaders.g", "invaders.f", "invaders.e",};
        public static final int[] ROM_ADDRESS = {0x0000, 0x0800, 0x1000, 0x1800,
                //0x0100
        };

        private File() {
        }
    }

    public static final class Component {
        public static final boolean PRINT_LESS = true;
        public static final int PROGRAM_LENGTH = 0x10_000;
        public static final String STORAGE_LOCATION = Environment.getExternalStorageDirectory().getPath() + "Download/";
        private Component() {
        }
    }
}
