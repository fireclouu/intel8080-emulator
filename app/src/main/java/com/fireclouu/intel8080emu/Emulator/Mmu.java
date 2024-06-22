package com.fireclouu.intel8080emu.Emulator;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class Mmu
{
	private static final int SP_MEM_ADDR_HI_SCORE_MSB = 0x20f5;
	private static final int SP_MEM_ADDR_HI_SCORE_LSB = 0x20f4;
	public static PlatformAdapter platform;
	public static ResourceAdapter resource;
	
	private static boolean isInitialHiscoreInjected = false;
	private static boolean readHiscoreMsb = false;
	private static boolean readHiscoreLsb = false;
	
	public static void writeMemory(CpuComponents cpu, int address, short value) {
		// this example exhibits how important MMU is to emulation
		if ((address == SP_MEM_ADDR_HI_SCORE_MSB || address == SP_MEM_ADDR_HI_SCORE_LSB)) {
			readHiscoreMsb = address == SP_MEM_ADDR_HI_SCORE_MSB ? true : readHiscoreMsb;
			readHiscoreLsb = address == SP_MEM_ADDR_HI_SCORE_LSB ? true : readHiscoreLsb;
			if (!isInitialHiscoreInjected) {
				int storedHiscore = resource.getPrefs(StringUtils.ITEM_HISCORE);
				short hiscoreNibble = 0;
				if (address == SP_MEM_ADDR_HI_SCORE_MSB) {
					hiscoreNibble = (short) (storedHiscore >> 8);
				}
				if (address == SP_MEM_ADDR_HI_SCORE_LSB) {
					hiscoreNibble = (short) (storedHiscore & 0xff);
				}
				value = hiscoreNibble;
				isInitialHiscoreInjected = readHiscoreMsb && readHiscoreLsb;
			}
			if (readHiscoreLsb && readHiscoreMsb) {
				short hiScoreDataMsb = address == SP_MEM_ADDR_HI_SCORE_MSB ? value : Mmu.readMemory(cpu, SP_MEM_ADDR_HI_SCORE_MSB);
				short hiScoreDataLsb = address == SP_MEM_ADDR_HI_SCORE_LSB ? value : Mmu.readMemory(cpu, SP_MEM_ADDR_HI_SCORE_LSB);
				setHighscore(hiScoreDataMsb << 8 | hiScoreDataLsb);
				readHiscoreMsb = false;
				readHiscoreLsb = false;
			}
		}
		cpu.memory[address & 0xffff] = value;
	}
	
	public static short readMemory(CpuComponents cpu, int address) {
		return cpu.memory[address & 0xffff];
	}
	
	public static short[] getMemory(CpuComponents cpu) {
		return cpu.memory;
	}
	
	public static void setMemory(CpuComponents cpu, short[] memory) {
		cpu.memory = memory;
	}
	private static void setHighscore(int data) {
		platform.setHighscore(data);
	}
}
