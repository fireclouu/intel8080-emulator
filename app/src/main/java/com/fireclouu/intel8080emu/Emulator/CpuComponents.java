package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.StringUtils;

public class CpuComponents
{
	///  PSW FLAG POSITIONS  ///
	public final int 
	PSW_FLAG_POS_CY = 0b00000001, // on bit pos 0 (Carry)
	PSW_FLAG_POS_PA = 0b00000100, // on bit pos 2 (Parity)
	PSW_FLAG_POS_AC = 0b00010000, // on bit pos 4 (Aux. carry)
	PSW_FLAG_POS_ZE = 0b01000000, // on bit pos 6 (Zero)
	PSW_FLAG_POS_SN = 0b10000000; // on bit pos 7 (Sign)

	///  REGISTERS  ///
	public short B, C, D, E, H, L, A;

	///  16-BIT REGISTER ADDRESSES  ///
	public int PC, SP;

	///  MEMORY  ///
	public short memory[];

	///  CONDITIONALS  ///
	public ConditionCodes cc;

	///  INTERRUPT  ///
	public byte int_enable;

	// RESET / INIT
	public CpuComponents() {
		// reset components
		cc = new ConditionCodes();
		init();
	}

	public void init() {
		// init register (byte) / address (word)
		B = 0;
		C = 0;
		D = 0;
		E = 0;
		H = 0;
		L = 0;
		A = 0;

		PC = 0;
		SP = 0;

		int_enable = 0;

		// load file on memory
		memory = new short[StringUtils.Component.PROGRAM_LENGTH];

		// init flags (bit)
		cc.init();
	}
}

class ConditionCodes
{
	///  FLAGS  ///
	public byte Z, S, P, CY, AC;

	///  IN / OUT  ///
	public byte pad;

	public ConditionCodes() {
		// reset flags
		init();
	}

	public void init() {
		// init flags (bit)
		Z = 0;
		S = 0;
		P = 0;
		CY = 0;
		AC = 0;
		pad = 0;
	}
}
