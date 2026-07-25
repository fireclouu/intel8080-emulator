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
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity {
    // native
    private final String ASSET_TEST_LOCATION = "tests";
    private final String[] SUPPORTED_TEST_FILE_EXTENSIONS = {
        ".bin", ".com"
    };
    private int selectedTestFileIndex;
    private String[] TESTS = {
        "/assets/tests/8080EX1.COM",
        "/assets/tests/8080EXER.COM",
        "/assets/tests/8080EXER.MAC",
        "/assets/tests/8080EXM.COM",
        "/assets/tests/8080PRE.COM",
        "/assets/tests/cpudiag.bin",
        "/assets/tests/CPUTEST.COM",
        "/assets/tests/TST8080.COM",
    };

    // android-related go here
    private static final int REQUEST_PICK_DOC_MULTI = 1;
    private Button mBtnLoadEmulator;
    private Button mBtnChooseFile;
    private TextView mTvChooseFile;
    private CheckBox mCbTestRom;
    private Spinner mSpinnerTestRomSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_file_picker);

        mBtnChooseFile = findViewById(R.id.buttonChooseFile);
        mBtnLoadEmulator = findViewById(R.id.buttonLoadEmulator);
        mTvChooseFile = findViewById(R.id.tvChooseFile);
        mCbTestRom = findViewById(R.id.cbTestRom);
        mSpinnerTestRomSelector = findViewById(R.id.spinnerTestRoms);

        // default behavior while checkbox 
        // for test rom tests not selected
        mSpinnerTestRomSelector.setVisibility(View.GONE);

        setupBtnLoadEmulator();
        setupBtnChooseFile();
        setupCbForTestRom();
        makeArrayAdapterForFiles();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_DOC_MULTI && resultCode == RESULT_OK) {
            if (data == null) return;
            if (data.getClipData() == null) return;

            int itemCount = data.getClipData().getItemCount();

            String test = data.getClipData().getItemAt(0).getUri().toString();
            mTvChooseFile.setText(test);
        }
    }

    private void setupBtnLoadEmulator() {
        mBtnLoadEmulator.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                String testRomFileName = TESTS[selectedTestFileIndex];

                Intent intent = new Intent(MainActivity.this, EmulatorActivity.class);
                intent.putExtra(HostUtils.INTENT_FILE_IS_TEST_ROM, mCbTestRom.isChecked());
                intent.putExtra(HostUtils.INTENT_ROM_FILE_NAME, testRomFileName);
                startActivity(intent);
            }
        });
    }

    private void setupBtnChooseFile() {
        mBtnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_PICK_DOC_MULTI);
            }
        });
    }

    private void setupCbForTestRom() {
        mCbTestRom.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton p1, boolean p2) {
                int visibility = p2 ? View.VISIBLE : View.GONE;
                mSpinnerTestRomSelector.setVisibility(visibility);
            }
        });
        mCbTestRom.setChecked(false);
    }

    private void makeArrayAdapterForFiles() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TESTS);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerTestRomSelector.setAdapter(arrayAdapter);
        mSpinnerTestRomSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                selectedTestFileIndex = p3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {
                // TODO: Implement this method
            }
        });
    }
}
