package org.the429ers.gameboy;

import java.io.Serializable;
import java.util.Base64;

public class MMU implements Serializable {
    private byte[] mem = new byte[0xFFFF+1];
    private Cartridge rom;
    private int[] bootRom = new int[] { 0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF, 0x0E, 0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0, 0x47, 0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B, 0xFE, 0x34, 0x20, 0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9, 0x3E, 0x19, 0xEA, 0x10, 0x99, 0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20, 0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E, 0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04, 0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20, 0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2, 0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E, 0xC1, 0xFE, 0x64, 0x20, 0x06, 0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xE2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20, 0xD2, 0x05, 0x20, 0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11, 0x17, 0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B, 0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E, 0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC, 0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E, 0x3C, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x3C, 0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A, 0x13, 0xBE, 0x00, 0x00, 0x23, 0x7D, 0xFE, 0x34, 0x20, 0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB, 0x86, 0x00, 0x00, 0x3E, 0x01, 0xE0, 0x50 };
    private byte[] cgbBootRom = Base64.getDecoder().decode("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");
    private byte[][] wram = new byte[8][0x1000]; 
    private int wramBank = 1;
    private int[] vramBank0 = new int[0x2000];
    private int[] vramBank1 = new int[0x2000];
    private boolean isCGB;
    
    public static final int IF_REGISTER = 0xFF0F;
    public static final int IE_REGISTER = 0xFFFF;
    public static final int DIV_REGISTER = 0xFF04;
    public static final int TIMA_REGISTER = 0xFF05;
    public static final int TMA_REGISTER = 0xFF06;
    public static final int TAC_REGISTER = 0xFF07;
    public static final int DMA_REGISTER = 0xFF46;
    public static final int LY_COMPARE_REGISTER = 0xFF45;
    public static final int STEREO_SOUND_REGISTER = 0xFF25;
    public static final int VRAM_BANK_SELECT_REGISTER = 0xFF4F;
    public static final int CGB_DMA_SOURCE_HIGH = 0xFF51;
    public static final int CGB_DMA_SOURCE_LOW = 0xFF52;
    public static final int CGB_DMA_DESTINATION_HIGH = 0xFF53;
    public static final int CGB_DMA_DESTINATION_LOW = 0xFF54;
    public static final int CGB_DMA_START = 0xFF55;
    
    private int dmaSourceHigh;
    private int dmaSourceLow;
    private int dmaDestHigh;
    private int dmaDestLow;
    private int dmaSource;
    private int dmaDest;
    private int bytesToTransfer;
    private boolean hBlankDMA;

    
    boolean DEBUG = false;
    private boolean bootRomEnabled = true;
    private CPU cpu;
    private IPPU ppu;
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

    public void setPPU(IPPU ppu) {
        this.ppu = ppu;
    }
    
    public IPPU getPPU() {
    	return this.ppu;
    }
    
    public MMU() {
        //no need to copy boot rom since that is intercepted by readByte
    }
    
    // Load rom from disk
    public MMU(String fileName) {
        this.rom = Cartridge.fromFile(fileName);
        this.isCGB = rom.isGBC();
    }
    
    public void cleanUp() {
    	if (rom != null) {
    		rom.cleanUp();
    	}
    }
    
    public void setJoypad(Joypad joypad) {
    	this.joypad = joypad;
    }
    
    public int readByteFromVRAM(int location, int bank) {
    	if (location < 0x8000 || location > 0x9FFF) {
    		System.out.printf("Invalid VRAM address: %x\n", location);
    	}
    	
    	int index = location % 0x8000;
    	if (bank == 0) {
    		return vramBank0[index];
    	}
    	else {
    		return vramBank1[index];
    	}
    }
    
    public void writeByteToVRAM(int location, int data, int bank) {
    	if (bank != 0 && bank != 1) {
    		throw new IllegalArgumentException("invalid vram bank");
    	}
    	if (location >= 0x8000 && location <= 0x97FF) {
    		tileSetManager.updateTileSets(location, data, 0);
    	}
    	int index = location % 0x8000;
    	if (bank == 0) {
    		// Write to bank one
    		vramBank0[index] = data;
    	}
    	else {
    		vramBank1[index] = data;
    	}
    }
    
    private boolean withinCgbBootRom(int location){
        return  (location >= 0x000 && location <= 0x0ff) ||
                (location >= 0x200 && location <= 0x8ff);
    }
    
    public int readByte(int location) {
        if(bootRomEnabled){
            if(isCGB && withinCgbBootRom(location)){
                return cgbBootRom[location] & 0xff;
            }else if(location < bootRom.length) {
                return bootRom[location];
            }
        }
        
        if (location == 0xFF68) {
        	System.out.println("requested read of index bg");
        }
        
        if (location == 0xFF69) {
        	System.out.println("requested read of data bg");
        }
        
        if (location == 0xFF6A) {
        	System.out.println("requested read of index sprite");
        }
        
        if (location == 0xFF6B) {
        	System.out.println("requested read of data sprite");
        }
        
        
        if (location >= 0xC000 && location <= 0xCFFF) {
        	int wramLocation = location % 0xC000;
        	return wram[0][wramLocation] & 0xFF;
        }
        
        if (location >= 0xD000 && location <= 0xDFFF) {
        	int wramLocation = location % 0xD000;
        	if (isCGB) {
        		return wram[wramBank][wramLocation] & 0xFF;
        	}
        	else {
        		return wram[1][wramLocation] & 0xFF;
        	}
        }
        
        if(location < 0x8000){
            return rom.readByte(location);
        }
        
        if (location >= 0xA000 && location <= 0xBFFF) {
        	return rom.readByte(location);
        }
        
        if (location >= 0x8000 && location <= 0x9FFF) {
        	return readByteFromVRAM(location, currentVRAMBank);
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
        if(location == 0xff50){
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
        
        if (location >= 0x8000 && location <= 0x9FFF) {
        	writeByteToVRAM(location, toWrite, currentVRAMBank);
        	return;
        }
        
        if (location == VRAM_BANK_SELECT_REGISTER) {
        	currentVRAMBank = toWrite & 0x01;
        }
        
        if (location == CGB_DMA_SOURCE_HIGH) {
        	dmaSourceHigh = toWrite & 0x1F;
        }
        
        if (location == CGB_DMA_SOURCE_LOW) {
        	dmaSourceLow = toWrite & 0xF0;
        }
        
        if (location == CGB_DMA_DESTINATION_HIGH) {
        	dmaDestHigh = toWrite & 0x1F;
        }
        
        if (location == CGB_DMA_DESTINATION_LOW) {
        	dmaDestHigh = toWrite & 0xF0;
        }
        
        if (location == CGB_DMA_START) {
        	dmaSource = (dmaSourceHigh << 8) + dmaSourceLow;
        	dmaDest = (dmaDestHigh << 8) + dmaDestLow;
        	hBlankDMA = BitOps.extract(toWrite, 7, 7) == 1;
        	bytesToTransfer = (int) BitOps.extract(toWrite, 6, 0);
        	bytesToTransfer++;
        	bytesToTransfer *= 0x10;
        	// Do general purpose DMA
        	if (!hBlankDMA) {
        		for (int i = 0; i < bytesToTransfer; i++) {
        			this.writeByte(dmaDest, this.readByte(dmaSource + i));
        			dmaDest++;
        		}
        		bytesToTransfer = 0;
        	}
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
        
        
        if (location == 0xFF70) {
        	wramBank = toWrite;
        	if (wramBank == 0) {
        		wramBank = 1;
        	}
        	//System.out.println("want to switch wram bank!");
        }
        
        if (location >= 0xC000 && location <= 0xCFFF) {
        	int wramLocation = location % 0xC000;
        	wram[0][wramLocation] = (byte) (toWrite & 0xFF);
        	return;
        }
        
        if (location >= 0xD000 && location <= 0xDFFF) {
        	int wramLocation = location % 0xD000;
        	if (isCGB) {
        		wram[wramBank][wramLocation] = (byte) (toWrite & 0xFF);
        	}
        	else {
        		wram[1][wramLocation] = (byte) (toWrite & 0xFF);
        	}
        	return;
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
                //mem[destBegin + i] = mem[sourceBegin+i];
            	this.writeByte(destBegin + i, this.readByte(sourceBegin + i));
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
    
    public void hBlankDMA() {
    	if (hBlankDMA) {
	    	for (int i = 0; i < 16; i++) {
	    		this.writeByte(dmaDest, this.readByte(dmaSource + i));
				dmaDest++;
	    	}
	    	dmaSource += 16;
	    	bytesToTransfer -= 16;
	    	if (bytesToTransfer == 0) {
	    		hBlankDMA = false;
	    	}
    	}
    }
    
    public boolean isCGB() {
    	return isCGB;
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
