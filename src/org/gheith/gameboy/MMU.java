package org.gheith.gameboy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MMU {
    private int[] mem = new int[0xFFFF+1];
    private byte[] romData = new byte[0xFFFF+1];
    private int[] bootRom = new int[] { 0x31, 0xFE, 0xFF, 0xAF, 0x21, 0xFF, 0x9F, 0x32, 0xCB, 0x7C, 0x20, 0xFB, 0x21, 0x26, 0xFF, 0x0E, 0x11, 0x3E, 0x80, 0x32, 0xE2, 0x0C, 0x3E, 0xF3, 0xE2, 0x32, 0x3E, 0x77, 0x77, 0x3E, 0xFC, 0xE0, 0x47, 0x11, 0x04, 0x01, 0x21, 0x10, 0x80, 0x1A, 0xCD, 0x95, 0x00, 0xCD, 0x96, 0x00, 0x13, 0x7B, 0xFE, 0x34, 0x20, 0xF3, 0x11, 0xD8, 0x00, 0x06, 0x08, 0x1A, 0x13, 0x22, 0x23, 0x05, 0x20, 0xF9, 0x3E, 0x19, 0xEA, 0x10, 0x99, 0x21, 0x2F, 0x99, 0x0E, 0x0C, 0x3D, 0x28, 0x08, 0x32, 0x0D, 0x20, 0xF9, 0x2E, 0x0F, 0x18, 0xF3, 0x67, 0x3E, 0x64, 0x57, 0xE0, 0x42, 0x3E, 0x91, 0xE0, 0x40, 0x04, 0x1E, 0x02, 0x0E, 0x0C, 0xF0, 0x44, 0xFE, 0x90, 0x20, 0xFA, 0x0D, 0x20, 0xF7, 0x1D, 0x20, 0xF2, 0x0E, 0x13, 0x24, 0x7C, 0x1E, 0x83, 0xFE, 0x62, 0x28, 0x06, 0x1E, 0xC1, 0xFE, 0x64, 0x20, 0x06, 0x7B, 0xE2, 0x0C, 0x3E, 0x87, 0xE2, 0xF0, 0x42, 0x90, 0xE0, 0x42, 0x15, 0x20, 0xD2, 0x05, 0x20, 0x4F, 0x16, 0x20, 0x18, 0xCB, 0x4F, 0x06, 0x04, 0xC5, 0xCB, 0x11, 0x17, 0xC1, 0xCB, 0x11, 0x17, 0x05, 0x20, 0xF5, 0x22, 0x23, 0x22, 0x23, 0xC9, 0xCE, 0xED, 0x66, 0x66, 0xCC, 0x0D, 0x00, 0x0B, 0x03, 0x73, 0x00, 0x83, 0x00, 0x0C, 0x00, 0x0D, 0x00, 0x08, 0x11, 0x1F, 0x88, 0x89, 0x00, 0x0E, 0xDC, 0xCC, 0x6E, 0xE6, 0xDD, 0xDD, 0xD9, 0x99, 0xBB, 0xBB, 0x67, 0x63, 0x6E, 0x0E, 0xEC, 0xCC, 0xDD, 0xDC, 0x99, 0x9F, 0xBB, 0xB9, 0x33, 0x3E, 0x3C, 0x42, 0xB9, 0xA5, 0xB9, 0xA5, 0x42, 0x3C, 0x21, 0x04, 0x01, 0x11, 0xA8, 0x00, 0x1A, 0x13, 0xBE, 0x00, 0x00, 0x23, 0x7D, 0xFE, 0x34, 0x20, 0xF5, 0x06, 0x19, 0x78, 0x86, 0x23, 0x05, 0x20, 0xFB, 0x86, 0x00, 0x00, 0x3E, 0x01, 0xE0, 0x50 };
    
    boolean bootRomEnabled = true;
    CPU cpu;
    
    public void setCPU(CPU cpu){
        this.cpu = cpu;
    }

    public MMU() {
        //no need to copy boot rom since that is intercepted by readByte
    }
    
    // Load rom from disk
    public MMU(String rom) {
        int romSize = 0;
    	try {
			romSize = new FileInputStream(rom).read(romData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }
    	
    	for (int i = 0; i <= romSize; i++) {
    		mem[i] = romData[i] & 0xFF;
    	}
    	
    }
    
    public int readByte(int location) {
        if(bootRomEnabled && location < bootRom.length){
            return bootRom[location];
        }
        
        if(location == 0xFF00){ //joypad input
            return 0xff; //0xff means nothing is pressed
        }
        
        return mem[location];
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
            System.out.println("Boot rom disabled");
        }
        
        if(location == 0xFF0F) { // IF register
            if((toWrite & 1) == 1){ //VBLANK requested
                cpu.interruptHandler.issueInterruptIfEnabled(InterruptHandler.VBLANK);
            }
        }
        
        mem[location] = toWrite & 0xFF;
        if (location == 0xFF42) {
        	System.out.printf("Wrote to Scroll Y: %x\n", mem[location]);
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

    public ReadWritable SPr8Location(Register sp, Register pc, RegisterFile.FlagSet flags) {
        byte r8 = (byte)readByte(pc.read()+1); //r8 is a signed byte value
        int spVal = sp.read();
        int address = spVal + r8;
        
        //https://stackoverflow.com/questions/5159603/gbz80-how-does-ld-hl-spe-affect-h-and-c-flags
        if(r8 >= 0){
            flags.setFlag(RegisterFile.HFLAG, (spVal & 0xF) + (r8 & 0xF) > 0xF);
            flags.setFlag(RegisterFile.CFLAG, (spVal & 0xFF) + r8 > 0xFF);
        }else{
            flags.setFlag(RegisterFile.HFLAG, (address & 0xF) <= (spVal & 0xF));
            flags.setFlag(RegisterFile.CFLAG, (address & 0xFF) <= (spVal & 0xFF));
        }

        return new Location(address);
    }
}
