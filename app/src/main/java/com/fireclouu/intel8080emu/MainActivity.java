package com.fireclouu.intel8080emu;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;
import android.widget.AdapterView.*;
import android.content.res.*;
import java.io.*;
import android.util.*;
import android.widget.CompoundButton.*;

public class MainActivity extends Activity implements View.OnClickListener, CheckBox.OnCheckedChangeListener, AdapterView.OnItemSelectedListener
{
	private Button buttonLoadEmulator;
	private CheckBox cbTestRom;
	private Spinner spinnerTestRom;
	
	private String[] files;
	private String testRomFilename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_file_picker);
		
		buttonLoadEmulator = findViewById(R.id.buttonLoadEmulator);
		cbTestRom = findViewById(R.id.cbTestRom);
		spinnerTestRom = findViewById(R.id.spinnerTestRoms);
		
		buttonLoadEmulator.setOnClickListener(this);
		
		
		AssetManager assetManager = getAssets();
		files = null;
		
		try {
			files = assetManager.list("tests");
		} catch (IOException e) {
			Log.e(StringUtils.TAG, e.getMessage());
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
			intent.putExtra(StringUtils.INTENT_FILE_IS_TEST_ROM, cbTestRom.isChecked());
			intent.putExtra(StringUtils.INTENT_TEST_ROM_FILE_NAME, testRomFilename);
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
}
