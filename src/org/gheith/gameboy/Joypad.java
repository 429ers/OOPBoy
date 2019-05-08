package org.gheith.gameboy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

public class Joypad implements KeyListener, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1687374353560163196L;
	private MMU mmu;
	private InterruptHandler interruptHandler;
	private int a;
	private int b;
	private int select;
	private int start;
	private int up;
	private int down;
	private int left;
	private int right;

	public Joypad(MMU mmu, InterruptHandler interruptHandler) {
		this.mmu = mmu;
		this.interruptHandler = interruptHandler;
		mmu.setJoypad(this);
		a = 1;
		b = 1;
		select = 1;
		start = 1;
		up = 1;
		down = 1;
		left = 1;
		right = 1;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int code = e.getKeyCode();
		switch (code) {
		case KeyEvent.VK_LEFT:
			left = 0;
			break;
		case KeyEvent.VK_RIGHT:
			right = 0;
			break;
		case KeyEvent.VK_UP:
			up = 0;
			break;
		case KeyEvent.VK_DOWN:
			down = 0;
			break;
		case KeyEvent.VK_Z:
			a = 0;
			break;
		case KeyEvent.VK_X:
			b = 0;
			break;
		case KeyEvent.VK_ENTER:
			start = 0;
			break;
        case KeyEvent.VK_SHIFT:
		case KeyEvent.VK_BACK_SPACE:
			select = 0;
			break;
		}
		
		interruptHandler.issueInterruptIfEnabled(InterruptHandler.JOYPAD);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		int code = e.getKeyCode();
		switch (code) {
		case KeyEvent.VK_LEFT:
			left = 1;
			break;
		case KeyEvent.VK_RIGHT:
			right = 1;
			break;
		case KeyEvent.VK_UP:
			up = 1;
			break;
		case KeyEvent.VK_DOWN:
			down = 1;
			break;
		case KeyEvent.VK_Z:
			a = 1;
			break;
		case KeyEvent.VK_X:
			b = 1;
			break;
		case KeyEvent.VK_ENTER:
			start = 1;
			break;
		case KeyEvent.VK_BACK_SPACE:
			select = 1;
			break;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	
	public int readDirections() {
		//System.out.println("reading buttons");
		return (down << 3) + (up << 2) + (left << 1) + right;
	}
	
	public int readButtons() {
		return (start << 3) + (select << 2) + (b << 1) + a; 
	}

}
