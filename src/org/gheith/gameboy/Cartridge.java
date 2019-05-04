package org.gheith.gameboy;

import java.io.*;
import java.security.InvalidParameterException;

public interface Cartridge extends Serializable {
    static Cartridge fromFile(String fileName) {
        File file = new File(fileName);
        byte[] rom = new byte[(int)file.length()];
        
        try {
            new FileInputStream(file).read(rom);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int cartridgeType = rom[0x0147] & 0xff;
        
        if(cartridgeType == 0x00){
            return new Rom(rom);
        }else if(cartridgeType < 0x04){
            return new Mbc1(rom);
        }
        
        throw new InvalidParameterException("Cartridge type not supported yet");
    }
    
    int readByte(int location);
    void writeByte(int location, int toWrite);
}

class Rom implements Cartridge {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7294699536390467641L;
	byte[] rom;

    public Rom(byte[] rom) {
        this.rom = rom;
    }
    
    public int readByte(int location){
        return rom[location] & 0xff;
    }
    
    public void writeByte(int location, int toWrite){
        //do nothing
    }
}

class Mbc1 implements Cartridge {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3769278111043856834L;

	public static final int BANK_SIZE = 0x4000;
    
    private byte[][] banks;
    int currentBank = 1;
    
    public Mbc1(byte[] rom){
        int numBanks = rom.length / BANK_SIZE;
        
        banks = new byte[numBanks][BANK_SIZE];
        
        for(int i = 0; i < rom.length; i++){
            banks[i / BANK_SIZE][i % BANK_SIZE] = rom[i];
        }
    }

    public int readByte(int location) {
        if(location > 0x7fff) throw new InvalidParameterException("Out of cartridge memory");
        
        if(location < BANK_SIZE){
            return banks[0][location] & 0xff;
        }
        
        return banks[currentBank][location - BANK_SIZE] & 0xff;
    }

    public void writeByte(int location, int toWrite) {
        if(location >= 0x2000 && location <= 0x3fff) {
            //bank switching
            int newBank = toWrite & 0x1f;
            if(newBank == 0) newBank = 1;
            
            this.currentBank = newBank;
        }
    }
}