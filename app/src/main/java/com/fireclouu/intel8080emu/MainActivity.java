package com.fireclouu.intel8080emu;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener, CheckBox.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
	private static final int REQUEST_PICK_DOC_MULTI = 1;
    private Button buttonLoadEmulator;
    private Button buttonChooseFile;
    private TextView tvChooseFile;
    private CheckBox cbTestRom;
    private Spinner spinnerTestRom;
    private String[] files;
    private String testRomFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_file_picker);

        buttonChooseFile = findViewById(R.id.buttonChooseFile);
        buttonLoadEmulator = findViewById(R.id.buttonLoadEmulator);
        tvChooseFile = findViewById(R.id.tvChooseFile);
        cbTestRom = findViewById(R.id.cbTestRom);
        spinnerTestRom = findViewById(R.id.spinnerTestRoms);

        buttonLoadEmulator.setOnClickListener(this);
        buttonChooseFile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View p1) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_PICK_DOC_MULTI);
            }


        });

        AssetManager assetManager = getAssets();
        files = null;

        try {
            files = assetManager.list("tests");
        } catch (IOException e) {
            Log.e(HostHook.TAG, e.getMessage());
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, files);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTestRom.setAdapter(arrayAdapter);
        spinnerTestRom.setOnItemSelectedListener(this);

        cbTestRom.setOnCheckedChangeListener(this);

        cbTestRom.setChecked(false);
        spinnerTestRom.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();

        if (id == R.id.buttonLoadEmulator) {
            Intent intent = new Intent(MainActivity.this, EmulatorActivity.class);
            intent.putExtra(HostHook.INTENT_FILE_IS_TEST_ROM, cbTestRom.isChecked());
            intent.putExtra(HostHook.INTENT_ROM_FILE_NAME, testRomFilename);
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
        testRomFilename = files[p3];
    }

    @Override
    public void onNothingSelected(AdapterView<?> p1) {
        // TODO: Implement this method
    }

    @Override
    public void onCheckedChanged(CompoundButton p1, boolean p2) {
        int visibility = p2 ? View.VISIBLE : View.GONE;
        spinnerTestRom.setVisibility(visibility);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Implement this method
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_DOC_MULTI && resultCode == RESULT_OK) {
            if (data == null) return;
            if (data.getClipData() == null) return;

            int itemCount = data.getClipData().getItemCount();

            String test = data.getClipData().getItemAt(0).getUri().toString();
            tvChooseFile.setText(test);

        }
    }


}
