package org.gheith.gameboy;

import java.io.Serializable;

public class MMU implements Serializable {
    private byte[] mem = new byte[0xFFFF+1];
    private Cartridge rom;
    private int[] bootRom = new int[] { 0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF, 0x0E, 0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0, 0x47, 0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B, 0xFE, 0x34, 0x20, 0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9, 0x3E, 0x19, 0xEA, 0x10, 0x99, 0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20, 0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E, 0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04, 0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20, 0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2, 0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E, 0xC1, 0xFE, 0x64, 0x20, 0x06, 0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xE2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20, 0xD2, 0x05, 0x20, 0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11, 0x17, 0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B, 0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E, 0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC, 0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E, 0x3C, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x3C, 0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A, 0x13, 0xBE, 0x00, 0x00, 0x23, 0x7D, 0xFE, 0x34, 0x20, 0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB, 0x86, 0x00, 0x00, 0x3E, 0x01, 0xE0, 0x50 };
    private int[] vramBank0 = new int[0x1800];
    private int[] vramBank1 = new int[0x1800];
    
    public static final int IF_REGISTER = 0xFF0F;
    public static final int IE_REGISTER = 0xFFFF;
    public static final int DIV_REGISTER = 0xFF04;
    public static final int TIMA_REGISTER = 0xFF05;
    public static final int TMA_REGISTER = 0xFF06;
    public static final int TAC_REGISTER = 0xFF07;
    public static final int DMA_REGISTER = 0xFF46;
    public static final int LY_COMPARE_REGISTER = 0xFF45;
    public static final int STEREO_SOUND_REGISTER = 0xFF25;
    
    boolean DEBUG = false;
    private boolean bootRomEnabled = true;
    private CPU cpu;
    private PPU ppu;
    private Joypad joypad;
    private int currentVRAMBank = 0;
    private ColorPaletteManager backgroundManager;
    private ColorPaletteManager spriteManager;
    private TileSetManager tileSetManager;
    SoundChip soundChip = new SoundChip();
    
    public void setTileSetManager(TileSetManager manager) {
    	this.tileSetManager = manager;
    }
    
    public void setColorPaletteManagers(ColorPaletteManager background, ColorPaletteManager sprite) {
    	this.backgroundManager = background;
    	this.spriteManager = sprite;
    }
    
    public void setCPU(CPU cpu){
        this.cpu = cpu;
    }
    
    public CPU getCPU() {
    	return this.cpu;
    }

    public Joypad getJoypad() {
        return joypad;
    }

    public void setPPU(PPU ppu) {
        this.ppu = ppu;
    }
    
    public PPU getPPU() {
    	return this.ppu;
    }
    
    public MMU() {
        //no need to copy boot rom since that is intercepted by readByte
    }
    
    // Load rom from disk
    public MMU(String fileName) {
        this.rom = Cartridge.fromFile(fileName);
    }
    
    public void cleanUp() {
    	if (rom != null) {
    		rom.cleanUp();
    	}
    }
    
    public void setJoypad(Joypad joypad) {
    	this.joypad = joypad;
    }
    
    public int readByteFromVRAM(int location, boolean isBankZero) {
    	
    	int index = location % 0x8000;
    	if (isBankZero) {
    		return vramBank0[index];
    	}
    	else {
    		return vramBank1[index];
    	}
    }
    
    public void writeByteToVRAM(int location, int data, boolean isBankZero) {
    	if (location >= 0x8000 && location <= 0x97FF) {
    		tileSetManager.updateTileSets(location, data, 0);
    	}
    	int index = location % 0x8000;
    	if (isBankZero) {
    		// Write to bank one
    		vramBank0[index] = data;
    	}
    	else {
    		vramBank1[index] = data;
    	}
    }
    
    public int readByte(int location) {
        if(bootRomEnabled && location < bootRom.length){
            return bootRom[location];
        }
        
        if(location < 0x8000){
            return rom.readByte(location);
        }
        
        if (location >= 0xA000 && location <= 0xBFFF) {
        	return rom.readByte(location);
        }
        
        if (location >= 0x8000 && location <= 0x97FF) {
        	return readByteFromVRAM(location, currentVRAMBank == 0);
        }
        
        if(location == DIV_REGISTER){
            return cpu.timer.getDIV();
        }
        
        if(location == TIMA_REGISTER){
            return cpu.timer.getTIMA();
        }
        
        if(location == 0xFF00){ //joypad input
        	
            if (BitOps.extract(mem[0xFF00] & 0xff, 5, 5) == 0) {
            	return joypad.readButtons();
            }
            
            if(BitOps.extract(mem[0xFF00] & 0xff, 4, 4) == 0) {
            	return joypad.readDirections();
            }
            else {
            	return 0xFF;
            }
        }
        
        return mem[location] & 0xff;
    }
    
    public void memdump(int startLocation, int numBytes){
        int endLocation = startLocation + numBytes;
        int trueStartLocation = (startLocation / 8) * 8; //floor so it's divisible by 8
        endLocation = (endLocation + 7) / 8 * 8; //ceil to 8
        
        for(int i = trueStartLocation; i < endLocation; i++){
            if(i % 8 == 0){
                System.out.printf("%04x: ", i);
            }
            
            if(i - startLocation >= 0 && i - startLocation < numBytes){
                System.out.print("\033[34m");
            }else{
                System.out.print("\033[0m");
            }
            
            System.out.printf("%02x ", readByte(i));
            
            if(i % 8 == 7){
                System.out.println();
            }
            
            System.out.print("\033[0m");
        }

        System.out.print("\033[0m");
    }
    
    public int readWord(int location) {
        return (readByte(location+1) << 8) + readByte(location);
    }
    
    public int readDoubleWord(int location) {
    	return (readWord(location + 2) << 16) + readWord(location);
    }
    
    public long readQuadWord(int location) {
    	return (((long)readDoubleWord(location + 4)) << 32) + readDoubleWord(location);
    }
    
    public void writeByte(int location, int toWrite){
        if(location == 0xff50 && toWrite == 0x01){
            bootRomEnabled = false;
        }
        
        if(location < 0x7fff){
            rom.writeByte(location, toWrite);
            return;
        }
        
        if (location >= 0xA000 && location <= 0xBFFF) {
        	rom.writeByte(location, toWrite);
        	return;
        }
        
        if (location >= 0x8000 && location <= 0x97FF) {
        	writeByteToVRAM(location, toWrite, currentVRAMBank == 1);
        	return;
        }
        
        if (location == 0xFF4F) {
        	currentVRAMBank = toWrite;
        }
        
        if (location == 0xFF68) {
        	backgroundManager.setIndex(toWrite);
        }
        
        if (location == 0xFF69) {
        	backgroundManager.writeColor(toWrite);
        }
        
        if (location == 0xFF6A) {
        	spriteManager.setIndex(toWrite);
        }
        
        if (location == 0xFF6B) {
        	spriteManager.writeColor(toWrite);
        }
        
        if(location == IF_REGISTER) { // IF register
            boolean interrupted = cpu.interruptHandler.handleIF(toWrite);
            if (interrupted) return;
        }
        
        if(location == IE_REGISTER) { //IE register
            cpu.interruptHandler.handleIE(toWrite);
        }
        
        if(location == DMA_REGISTER) { //DMA transfer register
            int sourceBegin = toWrite << 8;
            int destBegin = 0xfe00;
            for(int i = 0; i < 256; i++){
                mem[destBegin + i] = mem[sourceBegin+i];
            }
            
            //System.out.printf("DMA transfer requested from %x complete from %x\n", cpu.regs.PC.read(), toWrite);
        }
        
        if(location == TMA_REGISTER){ //timer modulo register
            cpu.timer.setModulo(toWrite);
        }
        
        if(location == TIMA_REGISTER){
            cpu.timer.setTIMA(toWrite);
        }
        
        if(location == DIV_REGISTER){
            cpu.timer.resetDIV();
        }
        
        if(location == TAC_REGISTER){
            cpu.timer.handleTAC(toWrite);
        }
        
        if(location == LY_COMPARE_REGISTER){
            ppu.setLYCompare(toWrite);
        }
        
        if(location >= 0xff10 && location <= 0xff14){
            soundChip.square1.handleByte(location - 0xff10, toWrite);
        }else if(location >= 0xff15 && location <= 0xff19){
            soundChip.square2.handleByte(location - 0xff15, toWrite);
        }else if(location >= 0xff1a && location <= 0xff1e){
            soundChip.waveChannel.handleByte(location - 0xff1a, toWrite);
        }else if(location >= 0xff1f && location <= 0xff23){
            soundChip.noiseChannel.handleByte(location - 0xff1f, toWrite);
        }
        
        if(location == STEREO_SOUND_REGISTER) {
            soundChip.handleStereo(toWrite);
        }
        
        if(location >= 0xff30 && location <= 0xff3f){
            soundChip.waveChannel.handleWaveByte(location- 0xff30, toWrite);
        }
        
        mem[location] = (byte)(toWrite & 0xFF);
        
        if(DEBUG && location == 0xff44){
            System.out.printf("Wrote to LY: %x\n", mem[location] & 0xff);
        }
        if(DEBUG && location == 0xFF42) {
        	System.out.printf("Wrote to Scroll Y: %x\n", mem[location] & 0xff);
        }
    }
    
    public void writeWord(int location, int toWrite) {
        toWrite &= 0xffff;
        writeByte(location, toWrite & 0xff);
        writeByte(location + 1, toWrite >> 8);
    }

    //basically an abstraction of the various addressing modes
    class Location implements ReadWritable{
        private int address;

        public Location(int address){
            this.address = address;
        }

        @Override
        public int read() {
            return MMU.this.readByte(address);
        }

        @Override
        public void write(int val) {
            MMU.this.writeByte(address, val);
        }
        
        public void writeLong(int val) {
            MMU.this.writeByte(address, val & 0xff);
            MMU.this.writeByte(address + 1, (val >> 8) & 0xff);
        }
    }

    public Location shortRegisterLocation(Register r) {
        return new Location(0xff00 + r.read());
    }
    
    public Location registerLocation(Readable r) {
        return new Location(r.read());
    }

    public ReadWritable a8Location(Register pc){
        int address = 0xff00;
        address += readByte(pc.read()+1);
        
        return new Location(address);
    }

    public ReadWritable a16Location(Register pc) {
        int address = readWord(pc.read()+1);
        
        return new Location(address);
    }
}
