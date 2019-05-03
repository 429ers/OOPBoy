package org.gheith.gameboy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.*;

class MainMenuBar extends MenuBar {
    GameBoy gameBoy;
    
    public MainMenuBar(GameBoy gb) {
        this.gameBoy = gb;
        
        Menu menu = new Menu("File");
        
        MenuItem reset = new MenuItem("Reset");
        reset.addActionListener((ActionEvent e) -> {
            GameBoy oldGameBoy = gameBoy;
            gameBoy = new GameBoy(gameBoy.romFileName);
            gameBoy.start();
            
            oldGameBoy.dispose();
        });
        
        MenuItem openRom = new MenuItem("Open ROM");
        openRom.addActionListener((ActionEvent e) -> {
            //https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
            gameBoy.pause();
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(null);
            
            if(returnVal == JFileChooser.APPROVE_OPTION){
                String fileName = fc.getSelectedFile().getAbsolutePath();
                gameBoy.dispose();
                gameBoy = new GameBoy(fileName);
            }
            
            gameBoy.start();
        });
        
        MenuItem pause = new MenuItem("Pause");
        pause.addActionListener((ActionEvent e) -> {
            if(!gameBoy.paused) {
                gameBoy.pause();
                pause.setLabel("Resume");
            }else{
                gameBoy.start();
                pause.setLabel("Pause");
            }
        });
        
        menu.add(openRom);
        menu.add(reset);
        menu.add(pause);
        this.add(menu);
    }
}

public class GameBoy extends JFrame{
    
    static final String DEFAULT_ROM = "roms/Tetris.gb";
    
    HashSet<Integer> breakPoints = new HashSet<>();
    LinkedList<Integer> history = new LinkedList<>();
    MMU mmu;
    CPU cpu;
    PPU ppu;
    GameBoyScreen gbs;
    String romFileName;
    boolean paused;
    
    Scanner fin = new Scanner(System.in);
    int numInstructonsUntilBreak = -1;
    long framesDrawn = 0;
    boolean breaked = false;
    
    public GameBoy(String fileName) {
        super();
        this.romFileName = fileName;
        BufferedImage img = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
        gbs = new GameBoyScreen(img);
        gbs.setDoubleBuffered(true);
        gbs.setPreferredSize(new Dimension(500, 500));
        gbs.setFocusable(true);
        this.add(gbs);
        this.setMenuBar(new MainMenuBar(this));
        this.pack();
        this.setTitle("GheithBoy");
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mmu = new MMU(fileName);
        cpu = new CPU(mmu);
        ppu = new PPU(mmu, gbs);
        gbs.addKeyListener(new Joypad(mmu));
        
        ppu.loadTileSets();
        ppu.loadMap(true, true);
    }
    
    public void tick() {
        gbs.setFocusable(true);
        //ignore breakpoints while nm is used
        if(numInstructonsUntilBreak < 0 && breakPoints.contains(cpu.regs.PC.read())){
            breaked = true;
        }

        if(breaked){
            System.out.print("Suspended at " + Integer.toString(cpu.regs.PC.read(), 16) + ": ");
            String cmd = fin.next();
            if(cmd.equals("b")){
                breakPoints.add(fin.nextInt(16));
                return;
            }else if(cmd.equals("d")){
                breakPoints.remove(fin.nextInt(16));
                return;
            }else if(cmd.equals("c")) {
                breaked = false;
            }else if(cmd.equals("xc")){
                cpu.coreDump();
                return;
            }else if(cmd.equals("xm")){
                mmu.memdump(fin.nextInt(16), fin.nextInt());
                return;
            }else if(cmd.equals("sm")) {
                mmu.writeByte(fin.nextInt(16), fin.nextInt(16));
                return;
            }else if(cmd.equals("nm")) {
                breaked = false;
                numInstructonsUntilBreak = fin.nextInt();
            }else if(cmd.equals("xh")) {
                for(int i : history){
                    System.out.printf("%x ", i);
                }
                System.out.println();
            }else if(!cmd.equals("n")){
                System.out.println("Command not recognized");
                return;
            }
        }

        if(history.size() >= 20) {
            history.removeFirst();
        }
        history.addLast(cpu.regs.PC.read());
        cpu.executeOneInstruction(breaked);
        int cycles = cpu.getClockCycleDelta();
        for (int i = 0; i < cycles; i++) {
            ppu.tick();
            if(ppu.drewFrame()){
                framesDrawn++;
                if(framesDrawn % 2 == 0) {
                    mmu.soundChip.tick();
                }
            }
            cpu.timer.tick();
            //System.out.println("ticking ppu");
        }

        if(numInstructonsUntilBreak >= 0){
            if(numInstructonsUntilBreak == 0){
                breaked = true;
            }
            numInstructonsUntilBreak --;
            System.out.println(numInstructonsUntilBreak);
        }
    }
    
    public void pause() {
        paused = true;
    }
    
    public void start() {
        new Thread(() -> {
            paused = false;

            while (!paused) {
                this.tick();
            }

            System.out.println("stopped ticking");
        }).start();
    }
    
    public void dispose() {
        this.pause();
        super.dispose();
    }

	public static void main(String[] args) throws InterruptedException {

        GameBoy gb = new GameBoy(DEFAULT_ROM);

        Scanner fin = new Scanner(System.in);
        if(args.length > 0 && args[0].equals("-d")) {
            System.out.print("First breakpoint (hex): ");
            gb.breakPoints.add(fin.nextInt(16));
        }
        
        gb.start();
	}

}
