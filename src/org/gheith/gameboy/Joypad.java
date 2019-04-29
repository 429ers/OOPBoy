package org.gheith.gameboy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Joypad implements KeyListener {
	
	private MMU mmu;

	public Joypad(MMU mmu) {
		this.mmu = mmu;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("key hit");
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN) {
			int reg = mmu.readByte(0xFF00) & 0x2F;
			mmu.writeByte(0x00FF, reg | 0x00);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
