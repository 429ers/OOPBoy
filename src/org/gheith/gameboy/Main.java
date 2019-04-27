package org.gheith.gameboy;

import java.awt.Canvas;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        
		JFrame frame = new JFrame();
		BufferedImage img = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		GameBoyScreen gbs = new GameBoyScreen(img);
		gbs.setSize(200, 200);
		frame.add(gbs);
		frame.pack();
		frame.setVisible(true);
         
		MMU mmu = new MMU();
        CPU cpu = new CPU(mmu);
        PPU ppu = new PPU(mmu, gbs);
        ppu.loadTileSets();
        ppu.loadMap(true);
        
        while (true) {
        	cpu.executeOneInstruction(false);
        	int cycles = cpu.getClockCycleDelta();
        	for (int i = 0; i < cycles; i++) {
        		ppu.tick();
        		//System.out.println("ticking ppu");
        	}
        	
        }
        
	}

}
