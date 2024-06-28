package com.fireclouu.intel8080emu.Emulator;

public class Interpreter {
	// SOURCES: superzazu
	private final static short[] OPCODES_CYCLES = {	//  0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
		4,  10, 7,  5,  5,  5,  7,  4,  4,  10, 7,  5,  5,  5,  7,  4,  // 0
		4,  10, 7,  5,  5,  5,  7,  4,  4,  10, 7,  5,  5,  5,  7,  4,  // 1
		4,  10, 16, 5,  5,  5,  7,  4,  4,  10, 16, 5,  5,  5,  7,  4,  // 2
		4,  10, 13, 5,  10, 10, 10, 4,  4,  10, 13, 5,  5,  5,  7,  4,  // 3
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 4
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 5
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 6
		7,  7,  7,  7,  7,  7,  7,  7,  5,  5,  5,  5,  5,  5,  7,  5,  // 7
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // 8
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // 9
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // A
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // B
		5,  10, 10, 10, 11, 11, 7,  11, 5,  10, 10, 10, 11, 17, 7,  11, // C
		5,  10, 10, 10, 11, 11, 7,  11, 5,  10, 10, 10, 11, 17, 7,  11, // D
		5,  10, 10, 18, 11, 11, 7,  11, 5,  5,  10, 4,  11, 17, 7,  11, // E
		5,  10, 10, 4,  11, 11, 7,  11, 5,  5,  10, 4,  11, 17, 7,  11  // F
	};
	
	private final Cpu cpu;
	private final Mmu mmu;
	
	public short opcodeCycle = 0;
	
	public Interpreter(Cpu cpu, Mmu mmu) {
		this.cpu = cpu;
		this.mmu = mmu;
	}
	
	public short interpret() {
		// opcode
		int opcode = cpu.pc;

		// HL (M)
		int addr = ((cpu.h << 8) | cpu.l);

		// increment PC every calls
		cpu.pc++;

		// cycles
		int opMemory = mmu.readMemory(opcode);
		opcodeCycle = OPCODES_CYCLES[opMemory];

		switch (opMemory) {	
			case 0x01:
				cpu.b = mmu.readMemory(opcode + 2);
				cpu.c = mmu.readMemory(opcode + 1);
				cpu.pc += 2;
				break; // LXI B, D16
			case 0x02:
				instr_sta(cpu.b, cpu.c);
				break; // STAX B	
			case 0x09:
				instr_dad(cpu, cpu.b, cpu.c);
				break; //DAD B
			case 0x0a:
				instr_lda(cpu.b, cpu.c);
				break; // LDAX B	
			case 0x11:
				cpu.d = mmu.readMemory(opcode + 2);
				cpu.e = mmu.readMemory(opcode + 1);
				cpu.pc += 2;
				break; // LXI D, D16
			case 0x12:
				instr_sta(cpu.d, cpu.e);
				break; // STAX D	
			case 0x19:
				instr_dad(cpu, cpu.d, cpu.e);
				break; //DAD D
			case 0x1a:
				instr_lda(cpu.d, cpu.e);
				break; // LDAX D	
			case 0x21:
				cpu.h = mmu.readMemory(opcode + 2);
				cpu.l = mmu.readMemory(opcode + 1);
				cpu.pc += 2;
				break; // LXI H, D16
			case 0x22:
				instr_shld(opcode);
				cpu.pc += 2;
				break; // SHLD adr
			case 0x27:
				// SOURCE: superzazu
				// get least significant nibble and add 6 if >9
				// same as most significant nibble
				byte cy = cpu.cc.cy;
				short correction = 0;
				short lsb = (short) (cpu.a & 0xf);
				short msb = (short) (cpu.a >> 4);
				if (cpu.cc.ac == 1 || lsb > 9) {
					correction += 0x06;
				}
				if (cpu.cc.cy == 1 || msb > 9 || (msb >= 9 && lsb > 9)) {
					correction += 0x60;
					cy = 1;
				}
				instr_add(correction, 0);
				cpu.cc.cy = cy;
				break; // DAA	
			case 0x29:
				instr_dad(cpu, cpu.h, cpu.l);
				break; //DAD H
			case 0x2a:
				instr_lhld(opcode);
				cpu.pc += 2;
				break; // LHLD adr
			case 0x2f:
				cpu.a = (short) ((~cpu.a & 0xff));
				break; // CMA
			case 0x31:
				cpu.sp = ((mmu.readMemory(opcode + 2) << 8) | mmu.readMemory(opcode + 1));
				cpu.pc += 2;
				break; // LXI SP, D16
			case 0x32:
				instr_sta(mmu.readMemory(opcode + 2), mmu.readMemory(opcode + 1));
				cpu.pc += 2;
				break; // STA adr	
			case 0x39:
				instr_dad(cpu, cpu.sp);
				break; //DAD SP
			case 0x3a:
				instr_lda(mmu.readMemory(opcode + 2), mmu.readMemory(opcode + 1));
				cpu.pc += 2;
				break; // LDA adr
			case 0xeb:
				instr_xchg();
				break; // XCHG (HL to DE vice-versa)

			///  MOV  ///

			// IMMEDIATE
			case 0x06: cpu.b = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI B, D8
			case 0x0e: cpu.c = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI C, D8
			case 0x16: cpu.d = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI D, D8
			case 0x1e: cpu.e = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI E, D8
			case 0x26: cpu.h = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI H, D8
			case 0x2e: cpu.l = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI L, D8
			case 0x36: 
				mmu.writeMemory(addr, mmu.readMemory(opcode + 1));
				cpu.pc++; 
				break; // MVI M, D8
			case 0x3e: cpu.a = mmu.readMemory(opcode + 1); cpu.pc++; break; // MVI A, D8

			// B
			case 0x40: break; // MOV B, B
			case 0x41: cpu.b = cpu.c; break; // MOV B, C
			case 0x42: cpu.b = cpu.d; break; // MOV B, D
			case 0x43: cpu.b = cpu.e; break; // MOV B, E
			case 0x44: cpu.b = cpu.h; break; // MOV B, H
			case 0x45: cpu.b = cpu.l; break; // MOV B, L
			case 0x46: cpu.b = mmu.readMemory(addr); break; // MOV B, M
			case 0x47: cpu.b = cpu.a; break; // MOV B, A

			// C
			case 0x48: cpu.c = cpu.b; break; // MOV C, B
			case 0x49: break; // MOV C, C
			case 0x4a: cpu.c = cpu.d; break; // MOV C, D
			case 0x4b: cpu.c = cpu.e; break; // MOV C, E
			case 0x4c: cpu.c = cpu.h; break; // MOV C, H
			case 0x4d: cpu.c = cpu.l; break; // MOV C, L
			case 0x4e: cpu.c = mmu.readMemory(addr); break; // MOV C, M
			case 0x4f: cpu.c = cpu.a; break; // MOV C, A

			// D
			case 0x50: cpu.d = cpu.b; break; // MOV D, B
			case 0x51: cpu.d = cpu.c; break; // MOV D, C
			case 0x52: break; // MOV D, D
			case 0x53: cpu.d = cpu.e; break; // MOV D, E
			case 0x54: cpu.d = cpu.h; break; // MOV D, H
			case 0x55: cpu.d = cpu.l; break; // MOV D, L
			case 0x56: cpu.d = mmu.readMemory(addr); break; // MOV D, M
			case 0x57: cpu.d = cpu.a; break; // MOV D, A

			// E
			case 0x58: cpu.e = cpu.b; break; // MOV E, B
			case 0x59: cpu.e = cpu.c; break; // MOV E, C
			case 0x5a: cpu.e = cpu.d; break; // MOV E, D
			case 0x5b: break; // MOV E, E
			case 0x5c: cpu.e = cpu.h; break; // MOV E, H
			case 0x5d: cpu.e = cpu.l; break; // MOV E, L
			case 0x5e: cpu.e = mmu.readMemory(addr);; break; // MOV E, M
			case 0x5f: cpu.e = cpu.a; break; // MOV E, A

			// H
			case 0x60: cpu.h = cpu.b; break; // MOV H, B
			case 0x61: cpu.h = cpu.c; break; // MOV H, C
			case 0x62: cpu.h = cpu.d; break; // MOV H, D
			case 0x63: cpu.h = cpu.e; break; // MOV H, E
			case 0x64: break; // MOV H, H
			case 0x65: cpu.h = cpu.l; break; // MOV H, L
			case 0x66: cpu.h = mmu.readMemory(addr);; break; // MOV H, M
			case 0x67: cpu.h = cpu.a; break; // MOV H, A

			// L
			case 0x68: cpu.l = cpu.b; break; // MOV L, B
			case 0x69: cpu.l = cpu.c; break; // MOV L, C
			case 0x6a: cpu.l = cpu.d; break; // MOV L, D
			case 0x6b: cpu.l = cpu.e; break; // MOV L, E
			case 0x6c: cpu.l = cpu.h; break; // MOV L, H
			case 0x6d: break; // MOV L, L
			case 0x6e: cpu.l = mmu.readMemory(addr); break; // MOV L, M
			case 0x6f: cpu.l = cpu.a; break; // MOV L, A

			// MEMORY
			case 0x70: mmu.writeMemory(addr, cpu.b); break; // MOV M, B
			case 0x71: mmu.writeMemory(addr, cpu.c); break; // MOV M, C
			case 0x72: mmu.writeMemory(addr, cpu.d); break; // MOV M, D
			case 0x73: mmu.writeMemory(addr, cpu.e); break; // MOV M, E
			case 0x74: mmu.writeMemory(addr, cpu.h); break; // MOV M, H
			case 0x75: mmu.writeMemory(addr, cpu.l); break; // MOV M, L
			case 0x77: mmu.writeMemory(addr, cpu.a); break; // MOV M, A

			// A
			case 0x78: cpu.a = cpu.b; break; // MOV A, B
			case 0x79: cpu.a = cpu.c; break; // MOV A, C
			case 0x7a: cpu.a = cpu.d; break; // MOV A, D
			case 0x7b: cpu.a = cpu.e; break; // MOV A, E
			case 0x7c: cpu.a = cpu.h; break; // MOV A, H
			case 0x7d: cpu.a = cpu.l; break; // MOV A, L
			case 0x7e: cpu.a = mmu.readMemory(addr); break; // MOV A, M
			case 0x7f: break; // MOV A, A

			///  ALU  ///

			// ADD
			case 0x80: instr_add(cpu.b, 0); break; // ADD B
			case 0x81: instr_add(cpu.c, 0); break; // ADD C
			case 0x82: instr_add(cpu.d, 0); break; // ADD D
			case 0x83: instr_add(cpu.e, 0); break; // ADD E
			case 0x84: instr_add(cpu.h, 0); break; // ADD H
			case 0x85: instr_add(cpu.l, 0); break; // ADD L
			case 0x86: instr_add(mmu.readMemory(addr), 0); break; // ADD M
			case 0x87: instr_add(cpu.a, 0); break; // ADD A

			// ADC
			case 0x88: instr_add(cpu.b, cpu.cc.cy); break; // ADC B
			case 0x89: instr_add(cpu.c, cpu.cc.cy); break; // ADC C
			case 0x8a: instr_add(cpu.d, cpu.cc.cy); break; // ADC D
			case 0x8b: instr_add(cpu.e, cpu.cc.cy); break; // ADC E
			case 0x8c: instr_add(cpu.h, cpu.cc.cy); break; // ADC H
			case 0x8d: instr_add(cpu.l, cpu.cc.cy); break; // ADC L
			case 0x8e: instr_add(mmu.readMemory(addr), cpu.cc.cy); break; // ADC M
			case 0x8f: instr_add(cpu.a, cpu.cc.cy); break; // ADC A

			// SUB
			case 0x90: instr_sub(cpu.b, 0); break; // SUB B
			case 0x91: instr_sub(cpu.c, 0); break; // SUB C
			case 0x92: instr_sub(cpu.d, 0); break; // SUB D
			case 0x93: instr_sub(cpu.e, 0); break; // SUB E
			case 0x94: instr_sub(cpu.h, 0); break; // SUB H
			case 0x95: instr_sub(cpu.l, 0); break; // SUB L
			case 0x96: instr_sub(mmu.readMemory(addr), 0); break; // SUB M
			case 0x97: instr_sub(cpu.a, 0); break; // SUB A

			// SBB
			case 0x98: instr_sub(cpu.b, cpu.cc.cy); break; // SBB B
			case 0x99: instr_sub(cpu.c, cpu.cc.cy); break; // SBB C
			case 0x9a: instr_sub(cpu.d, cpu.cc.cy); break; // SBB D
			case 0x9b: instr_sub(cpu.e, cpu.cc.cy); break; // SBB E
			case 0x9c: instr_sub(cpu.h, cpu.cc.cy); break; // SBB H
			case 0x9d: instr_sub(cpu.l, cpu.cc.cy); break; // SBB L
			case 0x9e: instr_sub(mmu.readMemory(addr), cpu.cc.cy); break; // SBB M
			case 0x9f: instr_sub(cpu.a, cpu.cc.cy); break; // SBB A

			// ANA
			case 0xa0: instr_ana(cpu.b); break; // ANA B
			case 0xa1: instr_ana(cpu.c); break; // ANA C
			case 0xa2: instr_ana(cpu.d); break; // ANA D
			case 0xa3: instr_ana(cpu.e); break; // ANA E
			case 0xa4: instr_ana(cpu.h); break; // ANA H
			case 0xa5: instr_ana(cpu.l); break; // ANA L
			case 0xa6: instr_ana(mmu.readMemory(addr)); break; // ANA M
			case 0xa7: instr_ana(cpu.a); break; // ANA A

			// XRA
			case 0xa8: instr_xra(cpu.b); break; // XRA B
			case 0xa9: instr_xra(cpu.c); break; // XRA C
			case 0xaa: instr_xra(cpu.d); break; // XRA D
			case 0xab: instr_xra(cpu.e); break; // XRA E
			case 0xac: instr_xra(cpu.h); break; // XRA H
			case 0xad: instr_xra(cpu.l); break; // XRA L
			case 0xae: instr_xra(mmu.readMemory(addr)); break; // XRA M
			case 0xaf: instr_xra(cpu.a); break; // XRA A

			// ORA
			case 0xb0: instr_ora(cpu.b); break; // ORA B
			case 0xb1: instr_ora(cpu.c); break; // ORA C
			case 0xb2: instr_ora(cpu.d); break; // ORA D
			case 0xb3: instr_ora(cpu.e); break; // ORA E
			case 0xb4: instr_ora(cpu.h); break; // ORA H
			case 0xb5: instr_ora(cpu.l); break; // ORA L
			case 0xb6: instr_ora(mmu.readMemory(addr)); break; // ORA M
			case 0xb7: instr_ora(cpu.a); break; // ORA A

			// CMP
			case 0xb8: instr_cmp(cpu.b); break; // CMP B
			case 0xb9: instr_cmp(cpu.c); break; // CMP C
			case 0xba: instr_cmp(cpu.d); break; // CMP D
			case 0xbb: instr_cmp(cpu.e); break; // CMP E
			case 0xbc: instr_cmp(cpu.h); break; // CMP H
			case 0xbd: instr_cmp(cpu.l); break; // CMP L
			case 0xbe: instr_cmp(mmu.readMemory(addr)); break; // CMP M
			case 0xbf: instr_cmp(cpu.a); break; // CMP A

			// INR
			case 0x04: cpu.b = instr_inr(cpu.b); break; // INR B
			case 0x0c: cpu.c = instr_inr(cpu.c); break; // INR C
			case 0x14: cpu.d = instr_inr(cpu.d); break; // INR D
			case 0x1c: cpu.e = instr_inr(cpu.e); break; // INR E
			case 0x24: cpu.h = instr_inr(cpu.h); break; // INR H
			case 0x2c: cpu.l = instr_inr(cpu.l); break; // INR L
			case 0x34: 
				mmu.writeMemory(addr, instr_inr(mmu.readMemory(addr))); 
				break; // INR M
			case 0x3c: cpu.a = instr_inr(cpu.a); break; // INR A

			// DCR
			case 0x05: cpu.b = instr_dcr(cpu.b); break; // DCR B
			case 0x0d: cpu.c = instr_dcr(cpu.c); break; // DCR C
			case 0x15: cpu.d = instr_dcr(cpu.d); break; // DCR D
			case 0x1d: cpu.e = instr_dcr(cpu.e); break; // DCR E
			case 0x25: cpu.h = instr_dcr(cpu.h); break; // DCR H
			case 0x2d: cpu.l = instr_dcr(cpu.l); break; // DCR L
			case 0x35: 
				mmu.writeMemory(addr, instr_dcr(mmu.readMemory(addr))); 
				break; // DCR M
			case 0x3d: cpu.a = instr_dcr(cpu.a); break; // DCR A

			// INX
			case 0x03: set_pair_bc(get_pair_bc() + 1); break; // INX B
			case 0x13: set_pair_de(get_pair_de() + 1); break; // INX D
			case 0x23: set_pair_hl(get_pair_hl() + 1); break; // INX H
			case 0x33: cpu.sp = (cpu.sp + 1) & 0xffff; break; // INX SP

			// DCX
			case 0x0b: set_pair_bc(get_pair_bc() - 1); break; // DCX B	
			case 0x1b: set_pair_de(get_pair_de() - 1); break; // DCX D
			case 0x2b: set_pair_hl(get_pair_hl() - 1); break; // DCX H
			case 0x3b: cpu.sp = (cpu.sp - 1) & 0xffff; break; // DCX SP

			// ROTATES
			case 0x07: instr_rlc(); break; // RLC
			case 0x0f: instr_rrc(); break; // RRC
			case 0x17: instr_ral(); break; // RAL
			case 0x1f: instr_rar(); break; // RAR

			// CARRY FLAG
			case 0x37: cpu.cc.cy = 1; break; // STC
			case 0x3f: cpu.cc.cy = (cpu.cc.cy == 1) ? (byte) 0 : 1; break; // CMC	

			// ALU (IMMEDIATE)
			case 0xc6: instr_add(mmu.readMemory(opcode + 1), 0); cpu.pc++; break; // ADI D8		
			case 0xce: instr_add(mmu.readMemory(opcode + 1), cpu.cc.cy); cpu.pc++; break; // ACI D8	
			case 0xd6: instr_sub(mmu.readMemory(opcode + 1), 0); cpu.pc++; break; // SUI D8	
			case 0xde: instr_sub(mmu.readMemory(opcode + 1), cpu.cc.cy); cpu.pc++; break; // SBI D8
			case 0xe6: instr_ana(mmu.readMemory(opcode + 1)); cpu.pc++; break; // ANI D8
			case 0xee: instr_xra(mmu.readMemory(opcode + 1)); cpu.pc++; break; // XRI D8
			case 0xf6: instr_ora(mmu.readMemory(opcode + 1)); cpu.pc++; break; // ORI D8
			case 0xfe: instr_cmp(mmu.readMemory(opcode + 1)); cpu.pc++; break; // CPI D8

			///  BRANCH  ////

			// JUMPS
			case 0xc3: instr_jmp(opcode); break; // JMP adr
			case 0xc9: instr_ret(); break; // RET
			case 0xcd: instr_call(opcode); break; // CALL adr
			case 0xe9: cpu.pc = addr; break; // PCHL

			// RET (conditional)
			case 0xc0: conditional_ret(cpu.cc.z == 0); break; // RNZ
			case 0xc8: conditional_ret(cpu.cc.z == 1); break; // RZ
			case 0xd0: conditional_ret(cpu.cc.cy == 0); break; // RNC
			case 0xd8: conditional_ret(cpu.cc.cy == 1); break; // RC
			case 0xe0: conditional_ret(cpu.cc.p == 0); break; // RPO
			case 0xe8: conditional_ret(cpu.cc.p == 1); break; // RPE	
			case 0xf0: conditional_ret(cpu.cc.s == 0); break; // RP
			case 0xf8: conditional_ret(cpu.cc.s == 1); break; // RM

			// JMP (conditional)
			case 0xc2: conditional_jmp(opcode, cpu.cc.z == 0); break; // JNZ adr
			case 0xca: conditional_jmp(opcode, cpu.cc.z == 1); break; // JZ adr
			case 0xd2: conditional_jmp(opcode, cpu.cc.cy == 0); break; // JNC adr
			case 0xda: conditional_jmp(opcode, cpu.cc.cy == 1); break; // JC adr
			case 0xe2: conditional_jmp(opcode, cpu.cc.p == 0); break; // JPO adr
			case 0xea: conditional_jmp(opcode, cpu.cc.p == 1); break; // JPE adr
			case 0xf2: conditional_jmp(opcode, cpu.cc.s == 0); break; // JP adr
			case 0xfa: conditional_jmp(opcode, cpu.cc.s == 1); break; // JM adr

			// CALL (conditional)
			case 0xc4: conditional_call(opcode, cpu.cc.z == 0); break; // CNZ adr
			case 0xcc: conditional_call(opcode, cpu.cc.z == 1); break; // CZ adr
			case 0xd4: conditional_call(opcode, cpu.cc.cy == 0); break; // CNC adr
			case 0xdc: conditional_call(opcode, cpu.cc.cy == 1); break; // CC adr
			case 0xe4: conditional_call(opcode, cpu.cc.p == 0); break; // CPO adr
			case 0xec: conditional_call(opcode, cpu.cc.p == 1); break; // CPE adr
			case 0xf4: conditional_call(opcode, cpu.cc.s == 0); break; // CP adr
			case 0xfc: conditional_call(opcode, cpu.cc.s == 1); break; // CM adr

			///  STACK  ///

			// POP
			case 0xc1: set_pair_bc(instr_pop()); break; // POP B
			case 0xd1: set_pair_de(instr_pop()); break; // POP D
			case 0xe1: set_pair_hl(instr_pop()); break; // POP H
			case 0xf1: pop_psw(); break; // POP PSW

			// PUSH
			case 0xc5: instr_push(get_pair_bc()); break; // PUSH B
			case 0xd5: instr_push(get_pair_de()); break; // PUSH D
			case 0xe5: instr_push(get_pair_hl()); break; // PUSH H
			case 0xf5: push_psw(); break; // PUSH PSW

			// XTHL, SPHL
			case 0xe3: instr_xthl(); break; // XTHL
			case 0xf9: instr_sphl(addr); break; // SPHL

			///  SIGNAL  ///

			// RST
			case 0xc7: instr_call(0x00); break; // RST 0
			case 0xcf: instr_call(0x08); break; // RST 1
			case 0xd7: instr_call(0x10); break; // RST 2
			case 0xdf: instr_call(0x18); break; // RST 3
			case 0xe7: instr_call(0x20); break; // RST 4
			case 0xef: instr_call(0x28); break; // RST 5
			case 0xf7: instr_call(0x30); break; // RST 6
			case 0xff: instr_call(0x38); break; // RST 7

			// INTERRUPTS
			case 0xf3: cpu.enableInterrupt = 0; break; // DI
			case 0xfb: cpu.enableInterrupt = 1; break; // EI

			// I/O
			case 0xd3: cpu.pc++; break; // OUT D8
			case 0xdb: cpu.pc++; break; // IN D8 (stub) (Load I/O to Accumulator)

			// TERMINATE
			case 0x76: System.exit(0); break; // HLT

			///  NO OPERATIONS  ///
			case 0x00: case 0x08: case 0x10: case 0x18: case 0x20: case 0x28:
			case 0x38: case 0xcb: case 0xd9: case 0xdd: case 0xed:
			case 0xfd: break;
		}
		return opcodeCycle;
	}

	/// INTERRUPT
	public void sendInterrupt(int interrupt_num) {
		// PUSH PC
		mmu.writeMemory(cpu.sp - 1, (short) ((cpu.pc & 0xff00) >> 8));
		mmu.writeMemory(cpu.sp - 2, (short) (cpu.pc & 0xff));
		cpu.sp = (cpu.sp - 2) & 0xffff;
		cpu.pc = 8 * interrupt_num;
		cpu.enableInterrupt = 0;
	}

	/// SUBROUTINES

	// CONDITIONAL
	private void conditional_call(int opcode, boolean cond) {
		if (cond) {
			instr_call(opcode);
			opcodeCycle += 6;
		} else {
			cpu.pc += 2;
		}
	}
	
	private void conditional_jmp(int opcode, boolean cond) {
		if (cond) {
			instr_jmp(opcode);
		} else {
			cpu.pc += 2;
		}
	}
	
	private void conditional_ret(boolean cond) {
		if (cond) { instr_ret(); opcodeCycle += 6; }
	}

	// REGISTER PAIRS
	private int get_pair_bc() {
		return (cpu.b << 8) | cpu.c;
	}
	
	private int get_pair_de() {
		return (cpu.d << 8) | cpu.e;
	}
	
	private int get_pair_hl() {
		return (cpu.h << 8) | cpu.l;
	}
	
	private void set_pair_bc(int val) {
		cpu.b = (short) ((val >> 8) & 0xff);
		cpu.c = (short) (val & 0xff);
	}
	
	private void set_pair_de(int val) {
		cpu.d = (short) ((val >> 8) & 0xff);
		cpu.e = (short) (val & 0xff);
	}
	
	private void set_pair_hl(int val) {
		cpu.h = (short) ((val >> 8) & 0xff);
		cpu.l = (short) (val & 0xff);
	}

	// ALUOP
	// ADD, ADC, ADI, ACI
	private void instr_add(int var, int cy) {
		short res = (short) ((cpu.a + (var & 0xff) + cy) & 0xff);
		cpu.cc.cy = checkIfCarry(8, cpu.a, (var & 0xff), cy);
		cpu.cc.ac = checkIfCarry(4, cpu.a, (var & 0xff), cy);
		flagsZSP(res);
		cpu.a = res;
	}
	
	// ANA, ANI
	private void instr_ana(int var) {
		short res =  (short) ((cpu.a & var) & 0xff);
		cpu.cc.cy = 0;
		cpu.cc.ac = ((cpu.a | var) & 0x8) != 0 ? (byte) 1 : 0;
		flagsZSP(res);
		cpu.a = res;
	}
	
	// CMP, CMI
	private void instr_cmp(int var) {
		// (two's) complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
		int res = (cpu.a - var) & 0xffff;
		cpu.cc.cy = (res >> 8) != 0 ? (byte) 1 : 0;
		cpu.cc.ac = (~(cpu.a ^ res ^ var) & 0x10) != 0 ? (byte) 1 : 0;
		flagsZSP(res & 0xff);
	}
	
	// ORA, ORI
	private void instr_ora(int var) {
		cpu.a |= (short) (var & 0xff);
		cpu.cc.cy = cpu.cc.ac = 0;
		flagsZSP(cpu.a);
	}
	
	// SUB, SBB, SUI, SBI
	private void instr_sub(int var, int cy) {
		int inv_var = ~var;
		instr_add(inv_var, (~cy & 0x1)); 
		cpu.cc.cy = (byte) (~cpu.cc.cy & 0x1);

	}

	// XRA, XRI
	private void instr_xra(int var) {
		cpu.a ^= (short) (var & 0xff);
		cpu.cc.cy = cpu.cc.ac = 0;
		flagsZSP(cpu.a);
	}

	private short instr_inr(int var) {
		cpu.cc.ac = checkIfCarry(4, var, 1, 0);
		short res = (short) ((var + 1) & 0xff);
		//cpu.cc.AC = (res & 0xf) == 0 ? (byte) 1 : 0;
		flagsZSP(res);
		return res;
	}
	
	private short instr_dcr(int var) {
		cpu.cc.ac = checkIfCarry(4, var, -1, 0);
		short res = (short) ((var - 1) & 0xff);
		//cpu.cc.AC = (res & 0xf) == 0 ? (byte) 0 : 1;
		flagsZSP(res);
		return res;
	}

	// JUMPS
	private void instr_call(int opcode) {
		int nextAddr = opcode + 3;
		mmu.writeMemory(cpu.sp - 1, (short) ((nextAddr >> 8) & 0xff));
		mmu.writeMemory(cpu.sp - 2, (short) (nextAddr & 0xff));
		cpu.sp = (cpu.sp - 2) & 0xffff;
		instr_jmp(opcode);
	}
	
	private void instr_dad(Cpu cpu, int... var) {
		int hl = (cpu.h << 8) | cpu.l; // addr = 16bit
		int pair;
		if (var.length == 2) {
			pair = (var[0] << 8) | var[1];
		} else {
			pair = var[0];
		}
		int res = hl + pair; // may result greater than 16 bit, raise CY if occured
		cpu.cc.cy = ((res & 0xf_0000) > 0) ? (byte) 1 : 0; // cut all values from lower 16 bit and check if higher 16 bit has value
		cpu.h = (short) ((res & 0xff00) >> 8); // store higher 8-bit to H
		cpu.l = (short) (res & 0xff); // store lower  8-bit to L
	}
	
	private void instr_jmp(int opcode) {
		cpu.pc = (mmu.readMemory(opcode + 2) << 8) | mmu.readMemory(opcode + 1);
	}
	
	private void instr_lda(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		cpu.a = mmu.readMemory(addr);
	}
	
	private void instr_lhld(int opcode) {
		int addr = (mmu.readMemory(opcode + 2) << 8) | mmu.readMemory(opcode + 1);
		cpu.h = mmu.readMemory(addr + 1);
		cpu.l = mmu.readMemory(addr);
	}
	
	private void pop_psw() {
		int PSW = mmu.readMemory(cpu.sp);
		cpu.cc.cy = ((PSW & cpu.PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
		cpu.cc.p  = ((PSW & cpu.PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
		cpu.cc.ac = ((PSW & cpu.PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
		cpu.cc.z  = ((PSW & cpu.PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
		cpu.cc.s  = ((PSW & cpu.PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;
		cpu.a = mmu.readMemory(cpu.sp + 1);
		cpu.sp = (cpu.sp + 2) & 0xffff;
	}
	
	private int instr_pop() {
		int res = (mmu.readMemory(cpu.sp + 1) << 8) | mmu.readMemory(cpu.sp);
		cpu.sp = (cpu.sp + 2) & 0xffff;
		return res;
	}
	
	private void instr_push(int pair) {
		mmu.writeMemory(cpu.sp - 1, (short) (pair >> 8));
		mmu.writeMemory(cpu.sp - 2, (short) (pair & 0xff));
		cpu.sp = (cpu.sp - 2) & 0xffff;
	}
	
	private void push_psw() {
		// A and PSW (formed binary value via flags , plus its filler value)
		mmu.writeMemory(cpu.sp - 1, cpu.a);
		// prepare variable higher than 0xff, but with 0's in bit 0-7
		// this way, it serves as flags' default state waiting to be flipped, like a template
		// also helps to retain flags proper positioning
		// skip pos 5 and 3, default 0 value
		int psw =
			(cpu.cc.s     <<  7)  |   // place sign flag status on pos 7
			(cpu.cc.z     <<  6)  |   // place zero flag status on pos 6
			(cpu.cc.ac    <<  4)  |   // place aux. carry flag status on pos 4
			(cpu.cc.p     <<  2)  |   // place parity flag status on pos 2
			(1            <<  1)  |
			(cpu.cc.cy)  ;   // place carry flag status on pos 0
		mmu.writeMemory(cpu.sp - 2, (short) (psw & 0xff));
		cpu.sp = (cpu.sp - 2) & 0xffff;
	}
	
	private void instr_ral() {
		final byte cy = cpu.cc.cy;
		cpu.cc.cy = (byte) ((cpu.a >> 7) & 0xf);
		cpu.a = (short) (((cpu.a << 1) | cy) & 0xff);
	}
	
	private void instr_rar() {
		final byte cy = cpu.cc.cy;
		cpu.cc.cy = (byte) (cpu.a & 1);
		cpu.a = (short) (((cpu.a >> 1) | (cy << 7)) & 0xff);
	}
	
	private void instr_ret() {
		int addr = mmu.readMemory(cpu.sp + 1) << 8 | mmu.readMemory(cpu.sp);
		cpu.sp = (cpu.sp + 2) & 0xffff;
		cpu.pc = addr;
	}
	
	private void instr_rlc() {
		cpu.cc.cy = (byte) (cpu.a >> 7); // get bit 7 as carry
		cpu.a = (short) (((cpu.a << 1) | cpu.cc.cy) & 0xff); // rotate to left, wrapping its content
	}
	
	private void instr_rrc() {
		cpu.cc.cy = (byte) (cpu.a & 1); // get bit 0 as carry
		cpu.a = (short) ((cpu.a >> 1) | (cpu.cc.cy << 7) & 0xff); // rotate to right, wrapping its contents by placing bit 0 to bit 7
	}
	
	private void instr_shld(int opcode) {
		int addr = mmu.readMemory(opcode + 2) << 8 | mmu.readMemory(opcode + 1);
		mmu.writeMemory(addr + 1, cpu.h);
		mmu.writeMemory(addr, cpu.l);
	}
	
	private void instr_sphl(int address) {
		cpu.sp = address;
	}
	
	private void instr_sta(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		mmu.writeMemory(addr, cpu.a);
	}
	
	private void instr_xchg() {
		// SWAP H and D
		cpu.h = (short) (cpu.h + cpu.d);
		cpu.d = (short) (cpu.h - cpu.d);
		cpu.h = (short) (cpu.h - cpu.d);
		// SWAP L and E
		cpu.l = (short) (cpu.l + cpu.e);
		cpu.e = (short) (cpu.l - cpu.e);
		cpu.l = (short) (cpu.l - cpu.e);
	}
	
	// FIXME
	private void instr_xthl() {
		// SWAP H and Top + 1  SP (under of top stack)
		cpu.h = (short) (cpu.h + mmu.readMemory(cpu.sp + 1));
		mmu.writeMemory(cpu.sp + 1, (short) (cpu.h - mmu.readMemory(cpu.sp + 1)));
		cpu.h = (short) (cpu.h - mmu.readMemory(cpu.sp + 1));
		// SWAP L and Top SP (top stack)
		cpu.l = (short) (cpu.l + mmu.readMemory(cpu.sp));
		mmu.writeMemory(cpu.sp, (short) (cpu.l - mmu.readMemory(cpu.sp)));
		cpu.l = (short) (cpu.l - mmu.readMemory(cpu.sp));
	}

	/// FLAGS
	private void flagsZSP(int result) {
		cpu.cc.z = ((result & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.s = (byte) ((result >> 7) & 0x1);
		cpu.cc.p = flagParity(result & 0xff);
	}
	private byte flagParity(int result) {
		int res = 0;
		for (int i = 0; i < 8; i++) {
			if (((result >> i) & 0x1) == 1) res++;
		}
		return (res % 2 == 0) ? (byte) 1 : 0;
	}

	// SOURCE — superzazu
	// returns if there was a carry between bit "bit_no" and "bit_no - 1" when
	// executing "a + b + cy"
	private byte checkIfCarry(int bit_no, int a, int b, int cy) {
		int res = a + b + cy;
		int carry = res ^ a ^ b;
		return ((carry & (1 << bit_no)) != 0) ? (byte) 1 : 0;
	}
}

