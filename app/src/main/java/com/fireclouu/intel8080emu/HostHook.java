package com.fireclouu.intel8080emu;

import com.fireclouu.intel8080emu.emulator.baseclass.PlatformAdapter;

public class HostHook {
    private static HostHook hostHook;
    private PlatformAdapter platform;

    private HostHook() {
    }

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
                return platform.getHighscore();
        }

        return null;
    }

    public PlatformAdapter getPlatform() {
        return this.platform;
    }

    public void setPlatform(PlatformAdapter platform) {
        this.platform = platform;
    }

    public void setData(HostHook.ACTION_TYPE type, Object data) {
        if (platform == null) return;
        switch (type) {
            case SET_HISCORE:
                platform.setHighscore((int) data);
                break;
        }
    }

    public enum ACTION_TYPE {
        GET_HISCORE, SET_HISCORE,
    }
}
