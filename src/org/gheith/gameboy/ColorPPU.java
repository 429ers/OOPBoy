package org.gheith.gameboy;

public class ColorPPU implements IPPU {
	private MMU mem;
	private LCDControl lcdControl;
	
	public ColorPPU(MMU mem) {
		this.mem = mem;
		lcdControl = new LCDControl(mem);
	}
	
	
	
	
}
