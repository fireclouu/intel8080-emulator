package com.fireclouu.intel8080emu.Emulator;
import com.fireclouu.intel8080emu.Emulator.BaseClass.*;

public class Mmu
{
	private static final int SP_MEM_ADDR_HI_SCORE_MSB = 0x20f5;
	private static final int SP_MEM_ADDR_HI_SCORE_LSB = 0x20f4;
	private PlatformAdapter platform;
	private ResourceAdapter resource;
	
	private static boolean isInitialHiscoreInjected = false;
	private static boolean readHiscoreMsb = false;
	private static boolean readHiscoreLsb = false;
	
	private short[] memory;
	
	public Mmu(PlatformAdapter platform, ResourceAdapter resource) {
		this.platform = platform;
		this.resource = resource;
		init();
	}
	
	public void init() {
		memory = new short[StringUtils.Component.PROGRAM_LENGTH];
	}
	
	public void writeMemory(int address, short value) {
		// this example exhibits how important MMU is to emulation
		// set highscore whenever emulator writes to its address
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
				short hiScoreDataMsb = address == SP_MEM_ADDR_HI_SCORE_MSB ? value : readMemory(SP_MEM_ADDR_HI_SCORE_MSB);
				short hiScoreDataLsb = address == SP_MEM_ADDR_HI_SCORE_LSB ? value : readMemory(SP_MEM_ADDR_HI_SCORE_LSB);
				setHighscore(hiScoreDataMsb << 8 | hiScoreDataLsb);
				readHiscoreMsb = false;
				readHiscoreLsb = false;
			}
		}
		
		// prevent writing to VRAM if host is still drawing
		if ((address >= Machine.VRAM_START && address <= Machine.VRAM_END) && platform.isDrawing()) return;
		
		memory[address & 0xffff] = value;
	}
	
	public short readMemory(int address) {
		return memory[address & 0xffff];
	}
	
	public short[] getMemory() {
		return memory;
	}
	
	public void setMemory(short[] memory) {
		this.memory = memory;
	}
	private void setHighscore(int data) {
		platform.setHighscore(data);
	}
}
