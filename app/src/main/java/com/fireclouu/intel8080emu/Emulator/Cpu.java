package com.fireclouu.intel8080emu.Emulator;

import com.fireclouu.intel8080emu.Emulator.BaseClass.StringUtils;

public class Cpu {
	///  PSW FLAG POSITIONS  ///
	public final int 
	PSW_FLAG_POS_CY = 0b00000001, // on bit pos 0 (Carry)
	PSW_FLAG_POS_PA = 0b00000100, // on bit pos 2 (Parity)
	PSW_FLAG_POS_AC = 0b00010000, // on bit pos 4 (Aux. carry)
	PSW_FLAG_POS_ZE = 0b01000000, // on bit pos 6 (Zero)
	PSW_FLAG_POS_SN = 0b10000000; // on bit pos 7 (Sign)

	///  REGISTERS  ///
	public short b, c, d, e, h, l, a;

	///  16-BIT REGISTER ADDRESSES  ///
	public int pc, sp;
	
	Flags cc;

	///  INTERRUPT  ///
	public byte enableInterrupt;

	public Cpu() {
		cc = new Flags();
		init();
	}

	public void init() {
		b = 0;
		c = 0;
		d = 0;
		e = 0;
		h = 0;
		l = 0;
		a = 0;

		pc = 0;
		sp = 0;

		enableInterrupt = 0;
		cc.init();
	}
}

class Flags {
	public byte z, s, p, cy, ac;

	public Flags() {
		init();
	}

	public void init() {
		z = 0;
		s = 0;
		p = 0;
		cy = 0;
		ac = 0;
	}
}
