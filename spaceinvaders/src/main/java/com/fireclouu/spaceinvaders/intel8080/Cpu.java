package com.fireclouu.spaceinvaders.intel8080;

public class Cpu {
    private final Mmu mmu;

    // SOURCES: superzazu
    private final static byte[] OPCODES_CYCLES = {    //  0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
            4, 10, 7, 5, 5, 5, 7, 4, 4, 10, 7, 5, 5, 5, 7, 4,  // 0
            4, 10, 7, 5, 5, 5, 7, 4, 4, 10, 7, 5, 5, 5, 7, 4,  // 1
            4, 10, 16, 5, 5, 5, 7, 4, 4, 10, 16, 5, 5, 5, 7, 4,  // 2
            4, 10, 13, 5, 10, 10, 10, 4, 4, 10, 13, 5, 5, 5, 7, 4,  // 3
            5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 7, 5,  // 4
            5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 7, 5,  // 5
            5, 5, 5, 5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 7, 5,  // 6
            7, 7, 7, 7, 7, 7, 7, 7, 5, 5, 5, 5, 5, 5, 7, 5,  // 7
            4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,  // 8
            4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,  // 9
            4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,  // A
            4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,  // B
            5, 10, 10, 10, 11, 11, 7, 11, 5, 10, 10, 10, 11, 17, 7, 11, // C
            5, 10, 10, 10, 11, 11, 7, 11, 5, 10, 10, 10, 11, 17, 7, 11, // D
            5, 10, 10, 18, 11, 11, 7, 11, 5, 5, 10, 4, 11, 17, 7, 11, // E
            5, 10, 10, 4, 11, 11, 7, 11, 5, 5, 10, 4, 11, 17, 7, 11  // F
    };
    private final short opcodeCycle = 0;
    private final Flags cc;
    ///  REGISTERS  ///
    private short b, c, d, e, h, l, a;
    ///  16-BIT REGISTER ADDRESSES  ///
    private int pc, sp;
    ///  INTERRUPT  ///
    private boolean hasInterrupt;

    public Cpu(Mmu mmu) {
        this.mmu = mmu;
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

        hasInterrupt = false;
        cc.init();
    }

    public byte getCurrentOpcodeCycle() {
        byte cycle;
        int opcode = mmu.readMemory(pc);
        cycle = OPCODES_CYCLES[opcode];
        boolean hasExtraCycle = false;
        switch (opcode) {
            case 0xc0:
            case 0xc4:
                hasExtraCycle = cc.z == 0;
                break;
            case 0xc8:
            case 0xcc:
                hasExtraCycle = cc.z == 1;
                break;
            case 0xd0:
            case 0xd4:
                hasExtraCycle = cc.cy == 0;
                break;
            case 0xd8:
            case 0xdc:
                hasExtraCycle = cc.cy == 1;
                break;
            case 0xe0:
            case 0xe4:
                hasExtraCycle = cc.p == 0;
                break;
            case 0xe8:
            case 0xec:
                hasExtraCycle = cc.p == 1;
                break;
            case 0xf0:
            case 0xf4:
                hasExtraCycle = cc.s == 0;
                break;
            case 0xf8:
            case 0xfc:
                hasExtraCycle = cc.s == 1;
                break;
        }

        if (hasExtraCycle) cycle += 6;

        return cycle;
    }

    public void step() {
        int currentPc = pc;

        // HL (M)
        int address = ((h << 8) | l);

        // increment PC every calls
        pc++;

        // cycles
        int opcode = mmu.readMemory(currentPc);

        switch (opcode) {
            case 0x01:
                b = mmu.readMemory(currentPc + 2);
                c = mmu.readMemory(currentPc + 1);
                pc += 2;
                break; // LXI B, D16
            case 0x02:
                instr_sta(b, c);
                break; // STAX B
            case 0x09:
                instr_dad(b, c);
                break; //DAD B
            case 0x0a:
                instr_lda(b, c);
                break; // LDAX B
            case 0x11:
                d = mmu.readMemory(currentPc + 2);
                e = mmu.readMemory(currentPc + 1);
                pc += 2;
                break; // LXI D, D16
            case 0x12:
                instr_sta(d, e);
                break; // STAX D
            case 0x19:
                instr_dad(d, e);
                break; //DAD D
            case 0x1a:
                instr_lda(d, e);
                break; // LDAX D
            case 0x21:
                h = mmu.readMemory(currentPc + 2);
                l = mmu.readMemory(currentPc + 1);
                pc += 2;
                break; // LXI H, D16
            case 0x22:
                instr_shld(currentPc);
                pc += 2;
                break; // SHLD adr
            case 0x27:
                // SOURCE: superzazu
                // get least significant nibble and add 6 if >9
                // same as most significant nibble
                byte cy = cc.cy;
                short correction = 0;
                short lsb = (short) (a & 0xf);
                short msb = (short) (a >> 4);
                if (cc.ac == 1 || lsb > 9) {
                    correction += 0x06;
                }
                if (cc.cy == 1 || msb > 9 || (msb == 9 && lsb > 9)) {
                    correction += 0x60;
                    cy = 1;
                }
                instr_add(correction, 0);
                cc.cy = cy;
                break; // DAA
            case 0x29:
                instr_dad(h, l);
                break; //DAD H
            case 0x2a:
                instr_lhld(currentPc);
                pc += 2;
                break; // LHLD adr
            case 0x2f:
                a = (short) ((~a & 0xff));
                break; // CMA
            case 0x31:
                sp = ((mmu.readMemory(currentPc + 2) << 8) | mmu.readMemory(currentPc + 1));
                pc += 2;
                break; // LXI SP, D16
            case 0x32:
                instr_sta(mmu.readMemory(currentPc + 2), mmu.readMemory(currentPc + 1));
                pc += 2;
                break; // STA adr
            case 0x39:
                instr_dad(sp);
                break; //DAD SP
            case 0x3a:
                instr_lda(mmu.readMemory(currentPc + 2), mmu.readMemory(currentPc + 1));
                pc += 2;
                break; // LDA adr
            case 0xeb:
                instr_xchg();
                break; // XCHG (HL to DE vice-versa)

            // IMMEDIATE
            case 0x06:
                b = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI B, D8
            case 0x0e:
                c = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI C, D8
            case 0x16:
                d = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI D, D8
            case 0x1e:
                e = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI E, D8
            case 0x26:
                h = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI H, D8
            case 0x2e:
                l = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI L, D8
            case 0x36:
                mmu.writeMemory(address, mmu.readMemory(currentPc + 1));
                pc++;
                break; // MVI M, D8
            case 0x3e:
                a = mmu.readMemory(currentPc + 1);
                pc++;
                break; // MVI A, D8

            // B
            case 0x40:
                break; // MOV B, B
            case 0x41:
                b = c;
                break; // MOV B, C
            case 0x42:
                b = d;
                break; // MOV B, D
            case 0x43:
                b = e;
                break; // MOV B, E
            case 0x44:
                b = h;
                break; // MOV B, H
            case 0x45:
                b = l;
                break; // MOV B, L
            case 0x46:
                b = mmu.readMemory(address);
                break; // MOV B, M
            case 0x47:
                b = a;
                break; // MOV B, A

            // C
            case 0x48:
                c = b;
                break; // MOV C, B
            case 0x49:
                break; // MOV C, C
            case 0x4a:
                c = d;
                break; // MOV C, D
            case 0x4b:
                c = e;
                break; // MOV C, E
            case 0x4c:
                c = h;
                break; // MOV C, H
            case 0x4d:
                c = l;
                break; // MOV C, L
            case 0x4e:
                c = mmu.readMemory(address);
                break; // MOV C, M
            case 0x4f:
                c = a;
                break; // MOV C, A

            // D
            case 0x50:
                d = b;
                break; // MOV D, B
            case 0x51:
                d = c;
                break; // MOV D, C
            case 0x52:
                break; // MOV D, D
            case 0x53:
                d = e;
                break; // MOV D, E
            case 0x54:
                d = h;
                break; // MOV D, H
            case 0x55:
                d = l;
                break; // MOV D, L
            case 0x56:
                d = mmu.readMemory(address);
                break; // MOV D, M
            case 0x57:
                d = a;
                break; // MOV D, A

            // E
            case 0x58:
                e = b;
                break; // MOV E, B
            case 0x59:
                e = c;
                break; // MOV E, C
            case 0x5a:
                e = d;
                break; // MOV E, D
            case 0x5b:
                break; // MOV E, E
            case 0x5c:
                e = h;
                break; // MOV E, H
            case 0x5d:
                e = l;
                break; // MOV E, L
            case 0x5e:
                e = mmu.readMemory(address);
                break; // MOV E, M
            case 0x5f:
                e = a;
                break; // MOV E, A

            // H
            case 0x60:
                h = b;
                break; // MOV H, B
            case 0x61:
                h = c;
                break; // MOV H, C
            case 0x62:
                h = d;
                break; // MOV H, D
            case 0x63:
                h = e;
                break; // MOV H, E
            case 0x64:
                break; // MOV H, H
            case 0x65:
                h = l;
                break; // MOV H, L
            case 0x66:
                h = mmu.readMemory(address);
                break; // MOV H, M
            case 0x67:
                h = a;
                break; // MOV H, A

            // L
            case 0x68:
                l = b;
                break; // MOV L, B
            case 0x69:
                l = c;
                break; // MOV L, C
            case 0x6a:
                l = d;
                break; // MOV L, D
            case 0x6b:
                l = e;
                break; // MOV L, E
            case 0x6c:
                l = h;
                break; // MOV L, H
            case 0x6d:
                break; // MOV L, L
            case 0x6e:
                l = mmu.readMemory(address);
                break; // MOV L, M
            case 0x6f:
                l = a;
                break; // MOV L, A

            // MEMORY
            case 0x70:
                mmu.writeMemory(address, b);
                break; // MOV M, B
            case 0x71:
                mmu.writeMemory(address, c);
                break; // MOV M, C
            case 0x72:
                mmu.writeMemory(address, d);
                break; // MOV M, D
            case 0x73:
                mmu.writeMemory(address, e);
                break; // MOV M, E
            case 0x74:
                mmu.writeMemory(address, h);
                break; // MOV M, H
            case 0x75:
                mmu.writeMemory(address, l);
                break; // MOV M, L
            case 0x77:
                mmu.writeMemory(address, a);
                break; // MOV M, A

            // A
            case 0x78:
                a = b;
                break; // MOV A, B
            case 0x79:
                a = c;
                break; // MOV A, C
            case 0x7a:
                a = d;
                break; // MOV A, D
            case 0x7b:
                a = e;
                break; // MOV A, E
            case 0x7c:
                a = h;
                break; // MOV A, H
            case 0x7d:
                a = l;
                break; // MOV A, L
            case 0x7e:
                a = mmu.readMemory(address);
                break; // MOV A, M
            case 0x7f:
                break; // MOV A, A

            // ADD
            case 0x80:
                instr_add(b, 0);
                break; // ADD B
            case 0x81:
                instr_add(c, 0);
                break; // ADD C
            case 0x82:
                instr_add(d, 0);
                break; // ADD D
            case 0x83:
                instr_add(e, 0);
                break; // ADD E
            case 0x84:
                instr_add(h, 0);
                break; // ADD H
            case 0x85:
                instr_add(l, 0);
                break; // ADD L
            case 0x86:
                instr_add(mmu.readMemory(address), 0);
                break; // ADD M
            case 0x87:
                instr_add(a, 0);
                break; // ADD A

            // ADC
            case 0x88:
                instr_add(b, cc.cy);
                break; // ADC B
            case 0x89:
                instr_add(c, cc.cy);
                break; // ADC C
            case 0x8a:
                instr_add(d, cc.cy);
                break; // ADC D
            case 0x8b:
                instr_add(e, cc.cy);
                break; // ADC E
            case 0x8c:
                instr_add(h, cc.cy);
                break; // ADC H
            case 0x8d:
                instr_add(l, cc.cy);
                break; // ADC L
            case 0x8e:
                instr_add(mmu.readMemory(address), cc.cy);
                break; // ADC M
            case 0x8f:
                instr_add(a, cc.cy);
                break; // ADC A

            // SUB
            case 0x90:
                instr_sub(b, 0);
                break; // SUB B
            case 0x91:
                instr_sub(c, 0);
                break; // SUB C
            case 0x92:
                instr_sub(d, 0);
                break; // SUB D
            case 0x93:
                instr_sub(e, 0);
                break; // SUB E
            case 0x94:
                instr_sub(h, 0);
                break; // SUB H
            case 0x95:
                instr_sub(l, 0);
                break; // SUB L
            case 0x96:
                instr_sub(mmu.readMemory(address), 0);
                break; // SUB M
            case 0x97:
                instr_sub(a, 0);
                break; // SUB A

            // SBB
            case 0x98:
                instr_sub(b, cc.cy);
                break; // SBB B
            case 0x99:
                instr_sub(c, cc.cy);
                break; // SBB C
            case 0x9a:
                instr_sub(d, cc.cy);
                break; // SBB D
            case 0x9b:
                instr_sub(e, cc.cy);
                break; // SBB E
            case 0x9c:
                instr_sub(h, cc.cy);
                break; // SBB H
            case 0x9d:
                instr_sub(l, cc.cy);
                break; // SBB L
            case 0x9e:
                instr_sub(mmu.readMemory(address), cc.cy);
                break; // SBB M
            case 0x9f:
                instr_sub(a, cc.cy);
                break; // SBB A

            // ANA
            case 0xa0:
                instr_ana(b);
                break; // ANA B
            case 0xa1:
                instr_ana(c);
                break; // ANA C
            case 0xa2:
                instr_ana(d);
                break; // ANA D
            case 0xa3:
                instr_ana(e);
                break; // ANA E
            case 0xa4:
                instr_ana(h);
                break; // ANA H
            case 0xa5:
                instr_ana(l);
                break; // ANA L
            case 0xa6:
                instr_ana(mmu.readMemory(address));
                break; // ANA M
            case 0xa7:
                instr_ana(a);
                break; // ANA A

            // XRA
            case 0xa8:
                instr_xra(b);
                break; // XRA B
            case 0xa9:
                instr_xra(c);
                break; // XRA C
            case 0xaa:
                instr_xra(d);
                break; // XRA D
            case 0xab:
                instr_xra(e);
                break; // XRA E
            case 0xac:
                instr_xra(h);
                break; // XRA H
            case 0xad:
                instr_xra(l);
                break; // XRA L
            case 0xae:
                instr_xra(mmu.readMemory(address));
                break; // XRA M
            case 0xaf:
                instr_xra(a);
                break; // XRA A

            // ORA
            case 0xb0:
                instr_ora(b);
                break; // ORA B
            case 0xb1:
                instr_ora(c);
                break; // ORA C
            case 0xb2:
                instr_ora(d);
                break; // ORA D
            case 0xb3:
                instr_ora(e);
                break; // ORA E
            case 0xb4:
                instr_ora(h);
                break; // ORA H
            case 0xb5:
                instr_ora(l);
                break; // ORA L
            case 0xb6:
                instr_ora(mmu.readMemory(address));
                break; // ORA M
            case 0xb7:
                instr_ora(a);
                break; // ORA A

            // CMP
            case 0xb8:
                instr_cmp(b);
                break; // CMP B
            case 0xb9:
                instr_cmp(c);
                break; // CMP C
            case 0xba:
                instr_cmp(d);
                break; // CMP D
            case 0xbb:
                instr_cmp(e);
                break; // CMP E
            case 0xbc:
                instr_cmp(h);
                break; // CMP H
            case 0xbd:
                instr_cmp(l);
                break; // CMP L
            case 0xbe:
                instr_cmp(mmu.readMemory(address));
                break; // CMP M
            case 0xbf:
                instr_cmp(a);
                break; // CMP A

            // INR
            case 0x04:
                b = instr_inr(b);
                break; // INR B
            case 0x0c:
                c = instr_inr(c);
                break; // INR C
            case 0x14:
                d = instr_inr(d);
                break; // INR D
            case 0x1c:
                e = instr_inr(e);
                break; // INR E
            case 0x24:
                h = instr_inr(h);
                break; // INR H
            case 0x2c:
                l = instr_inr(l);
                break; // INR L
            case 0x34:
                mmu.writeMemory(address, instr_inr(mmu.readMemory(address)));
                break; // INR M
            case 0x3c:
                a = instr_inr(a);
                break; // INR A

            // DCR
            case 0x05:
                b = instr_dcr(b);
                break; // DCR B
            case 0x0d:
                c = instr_dcr(c);
                break; // DCR C
            case 0x15:
                d = instr_dcr(d);
                break; // DCR D
            case 0x1d:
                e = instr_dcr(e);
                break; // DCR E
            case 0x25:
                h = instr_dcr(h);
                break; // DCR H
            case 0x2d:
                l = instr_dcr(l);
                break; // DCR L
            case 0x35:
                mmu.writeMemory(address, instr_dcr(mmu.readMemory(address)));
                break; // DCR M
            case 0x3d:
                a = instr_dcr(a);
                break; // DCR A

            // INX
            case 0x03:
                set_pair_bc(get_pair_bc() + 1);
                break; // INX B
            case 0x13:
                set_pair_de(get_pair_de() + 1);
                break; // INX D
            case 0x23:
                set_pair_hl(get_pair_hl() + 1);
                break; // INX H
            case 0x33:
                sp = (sp + 1) & 0xffff;
                break; // INX SP

            // DCX
            case 0x0b:
                set_pair_bc(get_pair_bc() - 1);
                break; // DCX B
            case 0x1b:
                set_pair_de(get_pair_de() - 1);
                break; // DCX D
            case 0x2b:
                set_pair_hl(get_pair_hl() - 1);
                break; // DCX H
            case 0x3b:
                sp = (sp - 1) & 0xffff;
                break; // DCX SP

            // ROTATES
            case 0x07:
                instr_rlc();
                break; // RLC
            case 0x0f:
                instr_rrc();
                break; // RRC
            case 0x17:
                instr_ral();
                break; // RAL
            case 0x1f:
                instr_rar();
                break; // RAR

            // CARRY FLAG
            case 0x37:
                cc.cy = 1;
                break; // STC
            case 0x3f:
                cc.cy = (cc.cy == 1) ? (byte) 0 : 1;
                break; // CMC

            // ALU (IMMEDIATE)
            case 0xc6:
                instr_add(mmu.readMemory(currentPc + 1), 0);
                pc++;
                break; // ADI D8
            case 0xce:
                instr_add(mmu.readMemory(currentPc + 1), cc.cy);
                pc++;
                break; // ACI D8
            case 0xd6:
                instr_sub(mmu.readMemory(currentPc + 1), 0);
                pc++;
                break; // SUI D8
            case 0xde:
                instr_sub(mmu.readMemory(currentPc + 1), cc.cy);
                pc++;
                break; // SBI D8
            case 0xe6:
                instr_ana(mmu.readMemory(currentPc + 1));
                pc++;
                break; // ANI D8
            case 0xee:
                instr_xra(mmu.readMemory(currentPc + 1));
                pc++;
                break; // XRI D8
            case 0xf6:
                instr_ora(mmu.readMemory(currentPc + 1));
                pc++;
                break; // ORI D8
            case 0xfe:
                instr_cmp(mmu.readMemory(currentPc + 1));
                pc++;
                break; // CPI D8

            // JUMPS
            case 0xc3:
                instr_jmp(currentPc);
                break; // JMP adr
            case 0xc9:
                instr_ret();
                break; // RET
            case 0xcd:
                instr_call(currentPc);
                break; // CALL adr
            case 0xe9:
                pc = address;
                break; // PCHL

            // RET (conditional)
            case 0xc0:
                conditional_ret(cc.z == 0);
                break; // RNZ
            case 0xc8:
                conditional_ret(cc.z == 1);
                break; // RZ
            case 0xd0:
                conditional_ret(cc.cy == 0);
                break; // RNC
            case 0xd8:
                conditional_ret(cc.cy == 1);
                break; // RC
            case 0xe0:
                conditional_ret(cc.p == 0);
                break; // RPO
            case 0xe8:
                conditional_ret(cc.p == 1);
                break; // RPE
            case 0xf0:
                conditional_ret(cc.s == 0);
                break; // RP
            case 0xf8:
                conditional_ret(cc.s == 1);
                break; // RM

            // JMP (conditional)
            case 0xc2:
                conditional_jmp(currentPc, cc.z == 0);
                break; // JNZ adr
            case 0xca:
                conditional_jmp(currentPc, cc.z == 1);
                break; // JZ adr
            case 0xd2:
                conditional_jmp(currentPc, cc.cy == 0);
                break; // JNC adr
            case 0xda:
                conditional_jmp(currentPc, cc.cy == 1);
                break; // JC adr
            case 0xe2:
                conditional_jmp(currentPc, cc.p == 0);
                break; // JPO adr
            case 0xea:
                conditional_jmp(currentPc, cc.p == 1);
                break; // JPE adr
            case 0xf2:
                conditional_jmp(currentPc, cc.s == 0);
                break; // JP adr
            case 0xfa:
                conditional_jmp(currentPc, cc.s == 1);
                break; // JM adr

            // CALL (conditional)
            case 0xc4:
                conditional_call(currentPc, cc.z == 0);
                break; // CNZ adr
            case 0xcc:
                conditional_call(currentPc, cc.z == 1);
                break; // CZ adr
            case 0xd4:
                conditional_call(currentPc, cc.cy == 0);
                break; // CNC adr
            case 0xdc:
                conditional_call(currentPc, cc.cy == 1);
                break; // CC adr
            case 0xe4:
                conditional_call(currentPc, cc.p == 0);
                break; // CPO adr
            case 0xec:
                conditional_call(currentPc, cc.p == 1);
                break; // CPE adr
            case 0xf4:
                conditional_call(currentPc, cc.s == 0);
                break; // CP adr
            case 0xfc:
                conditional_call(currentPc, cc.s == 1);
                break; // CM adr

            // POP
            case 0xc1:
                set_pair_bc(instr_pop());
                break; // POP B
            case 0xd1:
                set_pair_de(instr_pop());
                break; // POP D
            case 0xe1:
                set_pair_hl(instr_pop());
                break; // POP H
            case 0xf1:
                pop_psw();
                break; // POP PSW

            // PUSH
            case 0xc5:
                instr_push(get_pair_bc());
                break; // PUSH B
            case 0xd5:
                instr_push(get_pair_de());
                break; // PUSH D
            case 0xe5:
                instr_push(get_pair_hl());
                break; // PUSH H
            case 0xf5:
                push_psw();
                break; // PUSH PSW

            // XTHL, SPHL
            case 0xe3:
                instr_xthl();
                break; // XTHL
            case 0xf9:
                instr_sphl(address);
                break; // SPHL

            // RST
            case 0xc7:
                instr_call(0x00);
                break; // RST 0
            case 0xcf:
                instr_call(0x08);
                break; // RST 1
            case 0xd7:
                instr_call(0x10);
                break; // RST 2
            case 0xdf:
                instr_call(0x18);
                break; // RST 3
            case 0xe7:
                instr_call(0x20);
                break; // RST 4
            case 0xef:
                instr_call(0x28);
                break; // RST 5
            case 0xf7:
                instr_call(0x30);
                break; // RST 6
            case 0xff:
                instr_call(0x38);
                break; // RST 7

            // INTERRUPTS
            case 0xf3:
                hasInterrupt = false;
                break; // DI
            case 0xfb:
                hasInterrupt = true;
                break; // EI

            // I/O
            case 0xd3:
                pc++;
                break; // OUT D8
            case 0xdb:
                pc++;
                break; // IN D8 (stub) (Load I/O to Accumulator)

            // TERMINATE
            case 0x76:
                System.exit(0);
                break; // HLT

            case 0x00:
            case 0x08:
            case 0x10:
            case 0x18:
            case 0x20:
            case 0x28:
            case 0x38:
            case 0xcb:
            case 0xd9:
            case 0xdd:
            case 0xed:
            case 0xfd:
                break;
        }
    }

    /// INTERRUPT
    public void sendInterrupt(int vectorAddress) {
        mmu.writeMemory(sp - 1, (short) ((pc & 0xff00) >> 8));
        mmu.writeMemory(sp - 2, (short) (pc & 0xff));
        sp = (sp - 2) & 0xffff;
        pc = vectorAddress;
        hasInterrupt = false;
    }

    // CONDITIONAL
    private void conditional_call(int opcode, boolean cond) {
        if (cond) {
            instr_call(opcode);
        } else {
            pc += 2;
        }
    }

    private void conditional_jmp(int opcode, boolean cond) {
        if (cond) {
            instr_jmp(opcode);
        } else {
            pc += 2;
        }
    }

    private void conditional_ret(boolean cond) {
        if (cond) {
            instr_ret();
        }
    }

    // REGISTER PAIRS
    private int get_pair_bc() {
        return (b << 8) | c;
    }

    private void set_pair_bc(int val) {
        b = (short) ((val >> 8) & 0xff);
        c = (short) (val & 0xff);
    }

    private int get_pair_de() {
        return (d << 8) | e;
    }

    private void set_pair_de(int val) {
        d = (short) ((val >> 8) & 0xff);
        e = (short) (val & 0xff);
    }

    private int get_pair_hl() {
        return (h << 8) | l;
    }

    private void set_pair_hl(int val) {
        h = (short) ((val >> 8) & 0xff);
        l = (short) (val & 0xff);
    }

    // ALUOP
    // ADD, ADC, ADI, ACI
    private void instr_add(int var, int cy) {
        short res = (short) ((a + (var & 0xff) + cy) & 0xff);
        cc.cy = checkCarry(8, a, (var & 0xff), cy);
        cc.ac = checkCarry(4, a, (var & 0xff), cy);
        flagsZSP(res);
        a = res;
    }

    // ANA, ANI
    private void instr_ana(int var) {
        short res = (short) ((a & var) & 0xff);
        cc.cy = 0;
        cc.ac = ((a | var) & 0x8) != 0 ? (byte) 1 : 0;
        flagsZSP(res);
        a = res;
    }

    // CMP, CMI
    private void instr_cmp(int var) {
        // (two's) complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
        int res = (a - var) & 0xffff;
        cc.cy = (res >> 8) != 0 ? (byte) 1 : 0;
        cc.ac = (~(a ^ res ^ var) & 0x10) != 0 ? (byte) 1 : 0;
        flagsZSP(res & 0xff);
    }

    // ORA, ORI
    private void instr_ora(int var) {
        a |= (short) (var & 0xff);
        cc.cy = cc.ac = 0;
        flagsZSP(a);
    }

    // SUB, SBB, SUI, SBI
    private void instr_sub(int var, int cy) {
        int inv_var = ~var;
        instr_add(inv_var, (~cy & 0x1));
        cc.cy = (byte) (~cc.cy & 0x1);

    }

    // XRA, XRI
    private void instr_xra(int var) {
        a ^= (short) (var & 0xff);
        cc.cy = cc.ac = 0;
        flagsZSP(a);
    }

    private short instr_inr(int var) {
        cc.ac = checkCarry(4, var, 1, 0);
        short res = (short) ((var + 1) & 0xff);
        flagsZSP(res);
        return res;
    }

    private short instr_dcr(int var) {
        cc.ac = checkCarry(4, var, -1, 0);
        short res = (short) ((var - 1) & 0xff);
        flagsZSP(res);
        return res;
    }

    // JUMPS
    private void instr_call(int opcode) {
        int nextAddress = opcode + 3;
        mmu.writeMemory(sp - 1, (short) ((nextAddress >> 8) & 0xff));
        mmu.writeMemory(sp - 2, (short) (nextAddress & 0xff));
        sp = (sp - 2) & 0xffff;
        instr_jmp(opcode);
    }

    private void instr_dad(int... var) {
        int hl = (h << 8) | l;
        int pair;
        if (var.length == 2) {
            pair = (var[0] << 8) | var[1];
        } else {
            pair = var[0];
        }
        int res = hl + pair; // may result greater than 16 bit, raise CY if occurred
        cc.cy = ((res & 0xf_0000) > 0) ? (byte) 1 : 0; // cut all values from lower 16 bit and check if higher 16 bit has value
        h = (short) ((res & 0xff00) >> 8); // store higher 8-bit to H
        l = (short) (res & 0xff); // store lower  8-bit to L
    }

    private void instr_jmp(int opcode) {
        pc = (mmu.readMemory(opcode + 2) << 8) | mmu.readMemory(opcode + 1);
    }

    private void instr_lda(int hi_nib, int lo_nib) {
        int address = (hi_nib << 8) | lo_nib;
        a = mmu.readMemory(address);
    }

    private void instr_lhld(int opcode) {
        int address = (mmu.readMemory(opcode + 2) << 8) | mmu.readMemory(opcode + 1);
        h = mmu.readMemory(address + 1);
        l = mmu.readMemory(address);
    }

    private void pop_psw() {
        int PSW = mmu.readMemory(sp);
        int PSW_FLAG_POS_CY = 0b00000001;
        int PSW_FLAG_POS_PA = 0b00000100;
        int PSW_FLAG_POS_AC = 0b00010000;
        int PSW_FLAG_POS_ZE = 0b01000000;
        int PSW_FLAG_POS_SN = 0b10000000;

        cc.cy = ((PSW & PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
        cc.p = ((PSW & PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
        cc.ac = ((PSW & PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
        cc.z = ((PSW & PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
        cc.s = ((PSW & PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;
        a = mmu.readMemory(sp + 1);
        sp = (sp + 2) & 0xffff;
    }

    private int instr_pop() {
        int res = (mmu.readMemory(sp + 1) << 8) | mmu.readMemory(sp);
        sp = (sp + 2) & 0xffff;
        return res;
    }

    private void instr_push(int pair) {
        mmu.writeMemory(sp - 1, (short) (pair >> 8));
        mmu.writeMemory(sp - 2, (short) (pair & 0xff));
        sp = (sp - 2) & 0xffff;
    }

    private void push_psw() {
        mmu.writeMemory(sp - 1, a);
        // prepare variable higher than 0xff, but with 0's in bit 0-7
        // this way, it serves as flags' default state waiting to be flipped, like a template
        // also helps to retain flags proper positioning
        // skip pos 5 and 3, default 0 value
        int psw = (cc.s << 7) |   // place sign flag status on pos 7
                (cc.z << 6) |   // place zero flag status on pos 6
                (cc.ac << 4) |   // place aux. carry flag status on pos 4
                (cc.p << 2) |   // place parity flag status on pos 2
                (1 << 1) | (cc.cy);   // place carry flag status on pos 0
        mmu.writeMemory(sp - 2, (short) (psw & 0xff));
        sp = (sp - 2) & 0xffff;
    }

    private void instr_ral() {
        final byte cy = cc.cy;
        cc.cy = (byte) ((a >> 7) & 0xf);
        a = (short) (((a << 1) | cy) & 0xff);
    }

    private void instr_rar() {
        final byte cy = cc.cy;
        cc.cy = (byte) (a & 1);
        a = (short) (((a >> 1) | (cy << 7)) & 0xff);
    }

    private void instr_ret() {
        int address = mmu.readMemory(sp + 1) << 8 | mmu.readMemory(sp);
        sp = (sp + 2) & 0xffff;
        pc = address;
    }

    private void instr_rlc() {
        cc.cy = (byte) (a >> 7); // get bit 7 as carry
        a = (short) (((a << 1) | cc.cy) & 0xff); // rotate to left, wrapping its content
    }

    private void instr_rrc() {
        cc.cy = (byte) (a & 1); // get bit 0 as carry
        a = (short) ((a >> 1) | (cc.cy << 7) & 0xff); // rotate to right, wrapping its contents by placing bit 0 to bit 7
    }

    private void instr_shld(int opcode) {
        int address = mmu.readMemory(opcode + 2) << 8 | mmu.readMemory(opcode + 1);
        mmu.writeMemory(address + 1, h);
        mmu.writeMemory(address, l);
    }

    private void instr_sphl(int address) {
        sp = address;
    }

    private void instr_sta(int hi_nib, int lo_nib) {
        int address = (hi_nib << 8) | lo_nib;
        mmu.writeMemory(address, a);
    }

    private void instr_xchg() {
        // SWAP H and D
        h = (short) (h + d);
        d = (short) (h - d);
        h = (short) (h - d);
        // SWAP L and E
        l = (short) (l + e);
        e = (short) (l - e);
        l = (short) (l - e);
    }

    private void instr_xthl() {
        h = (short) (h + mmu.readMemory(sp + 1));
        mmu.writeMemory(sp + 1, (short) (h - mmu.readMemory(sp + 1)));
        h = (short) (h - mmu.readMemory(sp + 1));
        l = (short) (l + mmu.readMemory(sp));
        mmu.writeMemory(sp, (short) (l - mmu.readMemory(sp)));
        l = (short) (l - mmu.readMemory(sp));
    }

    /// FLAGS
    private void flagsZSP(int result) {
        cc.z = ((result & 0xff) == 0) ? (byte) 1 : 0;
        cc.s = (byte) ((result >> 7) & 0x1);
        cc.p = flagParity(result & 0xff);
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
    private byte checkCarry(int bit_no, int a, int b, int cy) {
        int res = a + b + cy;
        int carry = res ^ a ^ b;
        return ((carry & (1 << bit_no)) != 0) ? (byte) 1 : 0;
    }

    public short getRegB() {
        return this.b;
    }
    public short getRegC() {
        return this.c;
    }
    public short getRegD() {
        return this.d;
    }
    public short getRegE() {
        return this.e;
    }
    public short getRegH() {
        return this.h;
    }
    public short getRegL() {
        return this.l;
    }
    public short getRegA() {
        return this.a;
    }
    public void setRegA(short a) {
        this.a = a;
    }
    public int getPC() {
        return this.pc;
    }
    public void setPC(int pc) {
        this.pc = pc;
    }
    public int getSP() {
        return this.sp;
    }

    public boolean hasInterrupt() {
        return this.hasInterrupt;
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
