package com.fireclouu.intel8080emu.emulator;

public class Disassembler
{
	// SOURCE: superzazu
	public final static String[] DISASSEMBLE_TABLE = {
		"nop", "lxi b,#", "stax b", "inx b", "inr b", "dcr b", "mvi b,#", "rlc",
		"nop", "dad b", "ldax b", "dcx b", "inr c", "dcr c", "mvi c,#", "rrc",
		"nop", "lxi d,#", "stax d", "inx d", "inr d", "dcr d", "mvi d,#", "ral",
		"nop", "dad d", "ldax d", "dcx d", "inr e", "dcr e", "mvi e,#", "rar",
		"nop", "lxi h,#", "shld", "inx h", "inr h", "dcr h", "mvi h,#", "daa",
		"nop", "dad h", "lhld", "dcx h", "inr l", "dcr l", "mvi l,#", "cma",
		"nop", "lxi sp,#","sta $", "inx sp", "inr M", "dcr M", "mvi M,#", "stc",
		"nop", "dad sp", "lda $", "dcx sp", "inr a", "dcr a", "mvi a,#", "cmc",
		"mov b,b", "mov b,c", "mov b,d", "mov b,e", "mov b,h", "mov b,l",
		"mov b,M", "mov b,a", "mov c,b", "mov c,c", "mov c,d", "mov c,e",
		"mov c,h", "mov c,l", "mov c,M", "mov c,a", "mov d,b", "mov d,c",
		"mov d,d", "mov d,e", "mov d,h", "mov d,l", "mov d,M", "mov d,a",
		"mov e,b", "mov e,c", "mov e,d", "mov e,e", "mov e,h", "mov e,l",
		"mov e,M", "mov e,a", "mov h,b", "mov h,c", "mov h,d", "mov h,e",
		"mov h,h", "mov h,l", "mov h,M", "mov h,a", "mov l,b", "mov l,c",
		"mov l,d", "mov l,e", "mov l,h", "mov l,l", "mov l,M", "mov l,a",
		"mov M,b", "mov M,c", "mov M,d", "mov M,e", "mov M,h", "mov M,l", "hlt",
		"mov M,a", "mov a,b", "mov a,c", "mov a,d", "mov a,e", "mov a,h",
		"mov a,l", "mov a,M", "mov a,a", "add b", "add c", "add d", "add e",
		"add h", "add l", "add M", "add a", "adc b", "adc c", "adc d", "adc e",
		"adc h", "adc l", "adc M", "adc a", "sub b", "sub c", "sub d", "sub e",
		"sub h", "sub l", "sub M", "sub a", "sbb b", "sbb c", "sbb d", "sbb e",
		"sbb h", "sbb l", "sbb M", "sbb a", "ana b", "ana c", "ana d", "ana e",
		"ana h", "ana l", "ana M", "ana a", "xra b", "xra c", "xra d", "xra e",
		"xra h", "xra l", "xra M", "xra a", "ora b", "ora c", "ora d", "ora e",
		"ora h", "ora l", "ora M", "ora a", "cmp b", "cmp c", "cmp d", "cmp e",
		"cmp h", "cmp l", "cmp M", "cmp a", "rnz", "pop b", "jnz $", "jmp $",
		"cnz $", "push b", "adi #", "rst 0", "rz", "ret", "jz $", "nop", "cz $",
		"call $", "aci #", "rst 1", "rnc", "pop d", "jnc $", "out p", "cnc $",
		"push d", "sui #", "rst 2", "rc", "nop", "jc $", "in p", "cc $", "nop",
		"sbi #", "rst 3", "rpo", "pop h", "jpo $", "xthl", "cpo $", "push h",
		"ani #", "rst 4", "rpe", "pchl", "jpe $", "xchg", "cpe $", "nop", "xri #",
		"rst 5", "rp", "pop psw", "jp $", "di", "cp $", "push psw","ori #",
		"rst 6", "rm", "sphl", "jm $", "ei", "cm $", "nop", "cpi #", "rst 7"
	};

	public static String disassemble(Mmu mmu, int pc, int data) {
		String returnValue = "";
		String inst = DISASSEMBLE_TABLE[data];

		switch(data) {
				// byte
			case 0x06: case 0x0e:
			case 0x16: case 0x1e:
			case 0x26: case 0x2e:
			case 0x36: case 0x3e:
			case 0xc6: case 0xce:
			case 0xd3: case 0xd6: case 0xdb: case 0xde:
			case 0xe6: case 0xee:
			case 0xf6: case 0xfe:
				inst += toHex02(mmu.readMemory(pc + 1));
				break;

				// word
			case 0x01:
			case 0x11:
			case 0x21: case 0x22: case 0x2a:
			case 0x31: case 0x32: case 0x3a:
			case 0xc2: case 0xc3: case 0xc4: case 0xca: case 0xcc: case 0xcd:
			case 0xd2: case 0xd4: case 0xda: case 0xdc:
			case 0xe2: case 0xe4: case 0xea: case 0xec:
			case 0xf2: case 0xf4: case 0xfa: case 0xfc:
				inst += toHex04((mmu.readMemory(pc + 2) << 8)
					| mmu.readMemory(pc + 1));
				break;
		}

		returnValue = "PC: " + toHex04(pc) + "  " + "OP: " + toHex02(data) + "  " + inst + "  ";
		return returnValue;
	}

	private static String toHex04(int value) {
		char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[4];
		for (int j = 0; j < 4; j++) {
			int v = (value >> (12 - j * 4)) & 0x0F;
			hexChars[j] = hexArray[v];
		}
		return new String(hexChars);
	}

	private static String toHex02(int value) {
		char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[2];
		for (int j = 0; j < 2; j++) {
			int v = (value >> (4 - j * 4)) & 0x0F;
			hexChars[j] = hexArray[v];
		}
		return new String(hexChars);
	}
}
