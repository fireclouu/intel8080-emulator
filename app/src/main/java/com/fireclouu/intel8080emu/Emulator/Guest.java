package com.fireclouu.intel8080emu.Emulator;

public class Guest {
	private final Cpu cpu;
	private final Mmu mmu;
	
	public Guest() {
		this.mmu = new Mmu();
		this.cpu = new Cpu(mmu);
	}
	
	public Cpu getCpu() {
		return this.cpu;
	}
	
	public Mmu getMmu() {
		return this.mmu;
	}
}
