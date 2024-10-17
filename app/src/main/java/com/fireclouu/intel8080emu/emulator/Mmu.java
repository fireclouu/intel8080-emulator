package com.fireclouu.intel8080emu.emulator;

public class Mmu {
    private static final int SP_MEM_ADDR_HI_SCORE_MSB = 0x20f5;
    private static final int SP_MEM_ADDR_HI_SCORE_LSB = 0x20f4;
	
	private final Guest guest;
	private final Platform platform;
    private boolean isInitialHiscoreInjected;
    private boolean readHiscoreMsb;
    private boolean readHiscoreLsb;

    private boolean isTestSuite;

    public Mmu(Guest guest, Platform platform) {
		this.guest = guest;
		this.platform = platform;
        init(guest);
    }
	
	public void init(Guest guest) {
        isInitialHiscoreInjected = false;
        readHiscoreMsb = false;
        readHiscoreLsb = false;
        isTestSuite = platform.isTestSuite();
    }

    private short interceptValue(int address, short value) {
        if ((address == SP_MEM_ADDR_HI_SCORE_MSB || address == SP_MEM_ADDR_HI_SCORE_LSB)) {
            readHiscoreMsb = address == SP_MEM_ADDR_HI_SCORE_MSB || readHiscoreMsb;
            readHiscoreLsb = address == SP_MEM_ADDR_HI_SCORE_LSB || readHiscoreLsb;
            if (!isInitialHiscoreInjected) {
                Object data = platform.fetchHighscoreOnPlatform();
                int storedHiscore = data != null ? (int) data : 0;
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
                int hiScore = hiScoreDataMsb << 8 | hiScoreDataLsb;
                platform.saveHighscoreOnPlatform(hiScore);
                readHiscoreMsb = false;
                readHiscoreLsb = false;
            }
        }
		
        return value;
    }

    // tests suite patches
    public void writeTestSuitePatch() {
        writeMemory(0x0000, (short) 0xD3);
        writeMemory(0x0001, (short) 0x00);
        writeMemory(0x0005, (short) 0xDB);
        writeMemory(0x0006, (short) 0x00);
        writeMemory(0x0007, (short) 0xC9);
    }

	public void writeMemory(int address, short value) {
		// TODO: separate
		if (!isTestSuite) {
			value = interceptValue(address, value);
		}
		
		int map = address & 0xF000;
		switch (map) {
			case 0x0000:
			case 0x1000:
				break;
			case 0x2000:
				map = address & 0x0F00;
				switch (map) {
					case 0x0000:
					case 0x0100:
					case 0x0200:
					case 0x0300:
						map = address & 0x0FFF;
						guest.writeMemoryRam(map, value);
						break;
					default:
						map = address - 0x2400;
						guest.writeMemoryVram(map, value);
				}
				break;
			case 0x3000:
				map = address - 0x2400;
				guest.writeMemoryVram(map, value);
				break;
			case 0x4000:
				if ((address & 0x0F00) >= 0x0400) break;
				writeMemory(address - Guest.MEMORY_MAP_RAM_MIN, value);
				break;
		}
	}
	
    public short readMemory(int address) {
		int map = address & 0xF000;
		short data = 0;
		switch (map) {
			case 0x0000:
			case 0x1000:
				data = guest.getDataOnRom(address);
				break;
			case 0x2000:
				map = address & 0x0F00;
				switch (map) {
					case 0x0000:
					case 0x0100:
					case 0x0200:
					case 0x0300:
						map = address & 0x0FFF;
						data = guest.getDataOnRam(map);
						break;
					default:
						map = address - 0x2400;
						data = guest.getDataOnVram(map);
						break;
				}
				break;
			case 0x3000:
				map = address - 0x2400;
				data = guest.getDataOnVram(map);
				break;
			case 0x4000:
				if ((address & 0x0F00) >= 0x0400) break;
				data = readMemory(address - Guest.MEMORY_MAP_RAM_MIN);
				break;
		}
		
		return data;
    }
}
