package com.fireclouu.intel8080emu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fireclouu.spaceinvaders.intel8080.Inputs;

import java.util.Arrays;

public class EmulatorActivity extends Activity implements Button.OnClickListener {
    private final static String ANDROID_SERIALIZE_KEY_PLATFORM = "Platform";

    DisplaySurface DisplaySurface;
    AndroidPlatform androidPlatform;

    private LinearLayout llLogs;
    private LinearLayout llLogs2;

    private RelativeLayout rlEmulator;

    private boolean isTestSuite;

    private String romFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_emulation);
        isTestSuite = getIntent().getBooleanExtra(HostUtils.INTENT_FILE_IS_TEST_ROM, false);
        romFileName = getIntent().getStringExtra(HostUtils.INTENT_ROM_FILE_NAME);

        llLogs2 = findViewById(R.id.llLogs2);
        llLogs = findViewById(R.id.llLogs);
        rlEmulator = findViewById(R.id.rlEmulator);

        llLogs.setVisibility(isTestSuite ? View.VISIBLE : View.GONE);
        rlEmulator.setVisibility(!isTestSuite ? View.VISIBLE : View.GONE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if (isTestSuite) llLogs.setLayoutParams(params);

        requestFullscreen();
        DisplaySurface = findViewById(R.id.mainDisplay);

        GameButton mButtonCoin = findViewById(R.id.btn_p1_coin);
        GameButton mButtonStart = findViewById(R.id.btn_p1_start);
        GameButton mButtonLeft = findViewById(R.id.btn_p1_left);
        GameButton mButtonFire = findViewById(R.id.btn_p1_fire);
        GameButton mButtonRight = findViewById(R.id.btn_p1_right);

        Button mButtonChangePlayer = findViewById(R.id.btn_change_player);
        Button mButtonLogs = findViewById(R.id.btn_logs);

        mButtonCoin.setKeyCode(Inputs.KEY_COIN);
        mButtonStart.setKeyCode(Inputs.KEY_P1_START);
        mButtonLeft.setKeyCode(Inputs.KEY_LEFT);
        mButtonFire.setKeyCode(Inputs.KEY_FIRE);
        mButtonRight.setKeyCode(Inputs.KEY_RIGHT);

//        Toast.makeText(getApplicationContext(), "Loading... " + nativeInit(this, display.getSurface()), Toast.LENGTH_SHORT).show();

        if (savedInstanceState != null) {
            androidPlatform = (AndroidPlatform) savedInstanceState.getSerializable(ANDROID_SERIALIZE_KEY_PLATFORM);
            if (androidPlatform != null) {
                androidPlatform.setContext(this);
                androidPlatform.setDisplay(DisplaySurface);
            }
        }

        if (androidPlatform == null)
            androidPlatform = new AndroidPlatform(this, this, DisplaySurface, isTestSuite);

        // TODO: implement user defined file fetch, this is useless for now
        androidPlatform.setFilePath(romFileName);

        for (GameButton button : Arrays.asList(mButtonCoin, mButtonStart, mButtonLeft, mButtonFire, mButtonRight)) {
            button.setPlatform(androidPlatform);
        }

        for (Button button : Arrays.asList(mButtonChangePlayer, mButtonLogs)) {
            button.setOnClickListener(this);
        }

        androidPlatform.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ANDROID_SERIALIZE_KEY_PLATFORM, androidPlatform);
    }

    @Override
    protected void onResume() {
        androidPlatform.emulationResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        androidPlatform.emulationPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        androidPlatform.emulationTerminate();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        byte playerPort = androidPlatform.getPlayerPort();
        int buttonId = view.getId();
        if (buttonId == R.id.btn_change_player) {
            playerPort = playerPort == Inputs.INPUT_PORT_1 ? Inputs.INPUT_PORT_2 : Inputs.INPUT_PORT_1;
            androidPlatform.setPlayerPort(playerPort);
            ((Button) view).setText("P" + playerPort);
        }

        if (buttonId == R.id.btn_logs) {
            boolean isDebugging = !androidPlatform.isDebugging();
            androidPlatform.setDebugging(isDebugging);
            androidPlatform.nativeSetDebugging(isDebugging);
//            boolean isVisible = llLogs.getVisibility() == View.VISIBLE;
//            int toggledVisibility = isVisible ? View.GONE : View.VISIBLE;
//            llLogs.setVisibility(toggledVisibility);
//            isVisible = llLogs.getVisibility() == View.VISIBLE;

        }
    }

    private void requestFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
