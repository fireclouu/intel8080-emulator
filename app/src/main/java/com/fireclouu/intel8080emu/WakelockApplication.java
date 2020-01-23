package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.os.*;

public class WakelockApplication
{
	Context context;

	// Permission in AndroidManifest

	PowerManager pm;
	PowerManager.WakeLock wl;

	public WakelockApplication(Activity activity) {
		this.context = activity;

		pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
		wl = pm.newWakeLock(pm.SCREEN_DIM_WAKE_LOCK, "INTEL_8080_EMU");
	}

	public void startWakelock() {
		wl.acquire();
	}

	public void releaseWakelock() {
		wl.release();
	}
}
