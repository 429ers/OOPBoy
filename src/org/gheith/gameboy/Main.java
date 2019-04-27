package org.gheith.gameboy;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        
        Scanner fin = new Scanner(System.in);
        
		JFrame frame = new JFrame();
		BufferedImage img = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		GameBoyScreen gbs = new GameBoyScreen(img);
		gbs.setSize(200, 200);
		frame.add(gbs);
		frame.pack();
		frame.setVisible(true);
         
		MMU mmu = new MMU("Tetris.gb");
        CPU cpu = new CPU(mmu);
        PPU ppu = new PPU(mmu, gbs);
        ppu.loadTileSets();
        ppu.loadMap(true);
        HashSet<Integer> breakPoints = new HashSet<>();
        
        boolean breaked = false;
        
        System.out.print("First breakpoint (hex): ");
        breakPoints.add(fin.nextInt(16));
        
        while (true) {
            if(breakPoints.contains(cpu.regs.PC.read())){
                breaked = true;
            }

            if(breaked){
                System.out.print("Suspended at " + Integer.toString(cpu.regs.PC.read(), 16) + ": ");
                String cmd = fin.next();
                if(cmd.equals("b")){
                    breakPoints.add(fin.nextInt(16));
                    continue;
                }else if(cmd.equals("d")){
                    breakPoints.remove(fin.nextInt(16));
                    continue;
                }else if(cmd.equals("c")) {
                    breaked = false;
                }else if(cmd.equals("xc")){
                    cpu.coreDump();
                    continue;
                }else if(cmd.equals("xm")){
                    mmu.memdump(fin.nextInt(16), fin.nextInt());
                    continue;
                }else if(!cmd.equals("n")){
                    System.out.println("Command not recognized");
                    continue;
                }
            }
            
        	cpu.executeOneInstruction(true);
        	int cycles = cpu.getClockCycleDelta();
        	for (int i = 0; i < cycles; i++) {
                ppu.tick();
                //System.out.println("ticking ppu");
            }
        }
        
	}

}
