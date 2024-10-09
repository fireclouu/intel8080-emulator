package com.fireclouu.intel8080emu.emulator;
import java.util.LinkedHashMap;

public class Guest {
	public enum MEDIA_AUDIO {
		FIRE (0),
		PLAYER_EXPLODED (1),
		SHIP_INCOMING (2),
		ALIEN_MOVE_1 (3),
		ALIEN_MOVE_2 (3),
		ALIEN_MOVE_3 (3),
		ALIEN_MOVE_4 (3),
		ALIEN_KILLED (0),
		SHIP_HIT (1);
		
		private int id;
		MEDIA_AUDIO(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}
	}
	public static LinkedHashMap<String, Integer> mapFileData;
	
	public static final int VRAM_START = 0x2400;
    public static final int VRAM_END = 0x3FFF;
	public static final int MAX_PROGRAM_SIZE = 0x10_000;
    private final Cpu cpu;
    private final Mmu mmu;
	private short[] memory;

    public Guest() {
		mapFileData = new LinkedHashMap<>();
		mapFileData.put("invaders.h", 0x0000);
		mapFileData.put("invaders.g", 0x0800);
		mapFileData.put("invaders.f", 0x1000);
		mapFileData.put("invaders.e", 0x1800);
		
        this.mmu = new Mmu(this);
        this.cpu = new Cpu(mmu);
		memory = new short[MAX_PROGRAM_SIZE];
    }

    public Cpu getCpu() {
        return this.cpu;
    }

    public Mmu getMmu() {
        return this.mmu;
    }
	
	public short[] getMemory() {
        return memory;
    }
}
