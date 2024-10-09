package com.fireclouu.intel8080emu.emulator;
import com.fireclouu.intel8080emu.HostHook;

public class Mmu {
    private static final int SP_MEM_ADDR_HI_SCORE_MSB = 0x20f5;
    private static final int SP_MEM_ADDR_HI_SCORE_LSB = 0x20f4;
	
	private Guest guest;
    private boolean isInitialHiscoreInjected;
    private boolean readHiscoreMsb;
    private boolean readHiscoreLsb;

    private HostHook hostHook;
    private boolean isTestSuite;

    public Mmu(Guest guest) {
        init(guest);
    }
	
	public void init(Guest guest) {
        isInitialHiscoreInjected = false;
        readHiscoreMsb = false;
        readHiscoreLsb = false;
		
		this.guest = guest;
        this.hostHook = HostHook.getInstance();
        isTestSuite = hostHook.getPlatform().isTestSuite();
    }

    private short interceptValue(int address, short value) {
        if ((address == SP_MEM_ADDR_HI_SCORE_MSB || address == SP_MEM_ADDR_HI_SCORE_LSB)) {
            readHiscoreMsb = address == SP_MEM_ADDR_HI_SCORE_MSB || readHiscoreMsb;
            readHiscoreLsb = address == SP_MEM_ADDR_HI_SCORE_LSB || readHiscoreLsb;
            if (!isInitialHiscoreInjected) {
                Object data = hostHook.getData(HostHook.ACTION_TYPE.GET_HISCORE);
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
                hostHook.setData(HostHook.ACTION_TYPE.SET_HISCORE, hiScore);
                readHiscoreMsb = false;
                readHiscoreLsb = false;
            }
        }

        // TODO: prevent writing to VRAM if host is still drawing if threaded

        return value;
    }

    // tests suite patches
    public void writeTestSuitePatch() {
        // run only if rom has loaded on memory
        if (guest.getMemory() == null || guest.getMemory().length <= 0) return;
        writeMemoryRomSafe(0x0000, (short) 0xD3);
        writeMemoryRomSafe(0x0001, (short) 0x00);
        writeMemoryRomSafe(0x0005, (short) 0xDB);
        writeMemoryRomSafe(0x0006, (short) 0x00);
        writeMemoryRomSafe(0x0007, (short) 0xC9);
    }

    public void writeMemoryRomSafe(int address, short value) {
		if (address < 0x1FFF) return;
		writeMemory(address, value);
        
    }
	
	public void writeMemory(int address, short value) {
		if (!isTestSuite) {
			value = interceptValue(address, value);
		}
		
        guest.writeMemory(address, value);
	}

    public short readMemory(int address) {
        return guest.getMemory()[address & 0xffff];
    }
}
