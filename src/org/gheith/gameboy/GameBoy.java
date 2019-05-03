package org.gheith.gameboy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        
        MenuItem quickSave = new MenuItem("Quicksave");
        MenuItem quickLoad = new MenuItem("Quickload");
        quickSave.addActionListener(gameBoy);
        quickLoad.addActionListener(gameBoy);
        
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
        menu.add(quickSave);
        menu.add(quickLoad);
        
        this.add(menu);
    }
}

public class GameBoy extends JFrame implements ActionListener{

    HashSet<Integer> breakPoints = new HashSet<>();
    LinkedList<Integer> history = new LinkedList<>();
    MMU mmu;
    CPU cpu;
    PPU ppu;
    GameBoyScreen gbs;
    private boolean quickSave;
    private boolean quickLoad;
    
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
        quickSave = false;
        quickLoad = false;
    }
    
    public void saveState() {
    	try {
			FileOutputStream saveFile = new FileOutputStream("savedata.gbsave");
			ObjectOutputStream saveState = new ObjectOutputStream(saveFile);
			saveState.writeObject(gbs);
			saveState.writeObject(mmu);
			saveState.writeObject(cpu);
			saveState.writeObject(ppu);
			saveState.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void loadState() {
    	GameBoyScreen oldgbs = this.gbs;
    	try {
			FileInputStream saveFile = new FileInputStream("savedata.gbsave");
			ObjectInputStream saveState = new ObjectInputStream(saveFile);
			this.gbs = (GameBoyScreen) saveState.readObject();
			this.mmu = (MMU) saveState.readObject();
			this.cpu = (CPU) saveState.readObject();
			this.ppu = (PPU) saveState.readObject();
			saveState.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	this.remove(oldgbs);
    	this.add(gbs);
    	gbs.addKeyListener(new Joypad(mmu));
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
        if (quickSave) {
        	System.out.println("quicksaving...");
        	saveState();
        	quickSave = false;
        	System.out.println("done");
        	//throw new IllegalStateException("asdf");
        }
        if (quickLoad) {
        	System.out.println("quickloading...");
        	loadState();
        	quickLoad = false;
        	System.out.println("done");
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Quicksave")) {
			quickSave = true;
		}
		else if (e.getActionCommand().equals("Quickload")) {
			quickLoad = true;
		}
	}

}
