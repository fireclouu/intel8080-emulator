package com.fireclouu.intel8080emu.emulator;
import java.util.LinkedHashMap;

public class Guest {
	public static class Media {
		public static class Audio {
			public static final int FIRE = 0;
			public static final int PLAYER_EXPLODED = 1;
			public static final int SHIP_INCOMING = 2;
			public static final int ALIEN_MOVE_1 = 3;
			public static final int ALIEN_MOVE_2 = 4;
			public static final int ALIEN_MOVE_3 = 5;
			public static final int ALIEN_MOVE_4 = 6;
			public static final int ALIEN_KILLED = 7;
			public static final int SHIP_HIT = 8;
		}
	}
	
	public static class Display {
		public static class Orientation {
			public static final int DEFAULT = 0;
			public static final int PORTRAIT = 1;
			public static final int LANDSCAPE = 2;
		}
		public static final int WIDTH = (32 * 8); // 256 (32 = byte per line; 8 = bit per byte)
		public static final int HEIGHT = (224);   // 224
	}
	
	public static LinkedHashMap<String, Integer> mapFileData;
	
	public static final int MEMORY_MAP_ROM_MIN = 0x0000;
	public static final int MEMORY_MAP_ROM_MAX = 0x1FFF;
	public static final int MEMORY_MAP_RAM_MIN = 0x2000;
	public static final int MEMORY_MAP_RAM_MAX = 0x23FF;
	public static final int MEMORY_MAP_VRAM_MIN = 0x2400;
	public static final int MEMORY_MAP_VRAM_MAX = 0x3FFF;
	public static final int RAM_MIRROR = 0x4000;
	
	private final int SIZE_ROM = (MEMORY_MAP_ROM_MAX - MEMORY_MAP_ROM_MIN) + 1;
	private final int SIZE_RAM = (MEMORY_MAP_RAM_MAX - MEMORY_MAP_RAM_MIN) + 1;
	private final int SIZE_VRAM = (MEMORY_MAP_VRAM_MAX - MEMORY_MAP_VRAM_MIN) + 1;
	
	private final Platform platform;
    private final Cpu cpu;
    private final Mmu mmu;
	private final short[] memoryRom = new short[SIZE_ROM];
	private final short[] memoryRam = new short[SIZE_RAM];
	private final short[] memoryVram = new short[SIZE_VRAM];

    public Guest(Platform platform) {
		this.platform = platform;
        this.mmu = new Mmu(this, platform);
        this.cpu = new Cpu(mmu);
		
		mapFileData = new LinkedHashMap<>();
		mapFileData.put("invaders.h", 0x0000);
		mapFileData.put("invaders.g", 0x0800);
		mapFileData.put("invaders.f", 0x1000);
		mapFileData.put("invaders.e", 0x1800);
    }

    public Cpu getCpu() {
        return this.cpu;
    }

    public Mmu getMmu() {
        return this.mmu;
    }
	
	public short getDataOnRom(int address) {
		return memoryRom[address];
	}
	
	public short getDataOnRam(int address) {
		return memoryRam[address];
	}
	
	public short getDataOnVram(int address) {
		return memoryVram[address];
	}
	
	public short[] getMemoryVram() {
		return memoryVram;
	}
	
	public Platform getPlatform() {
		return platform;
	}
	
	public void writeMemoryRom(int address, short value) {
		memoryRom[address] = value;
	}
	
	public void writeMemoryRam(int address, short value) {
		memoryRam[address] = value;
	}
	
	public void writeMemoryVram(int address, short value) {
		memoryVram[address] = value;
	}
}
