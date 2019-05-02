package org.gheith.gameboy;

import java.io.*;
import java.security.InvalidParameterException;

public interface Cartridge {
    static Cartridge fromFile(String fileName) {
        File file = new File(fileName);
        byte[] romBytes = new byte[(int)file.length()];
        int romSize = 0;
        try {
            romSize = new FileInputStream(file).read(romBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        int[] rom = new int[romBytes.length];
        
        for(int i = 0; i < rom.length; i++){
            rom[i] = romBytes[i] & 0xff;
        }

        int cartridgeType = rom[0x0147];
        
        if(cartridgeType == 0x00){
            return new Rom(rom);
        }else if(cartridgeType == 0x01){
            return new Mbc1(rom);
        }
        
        throw new InvalidParameterException("Cartridge type not supported yet");
    }
    
    int readByte(int location);
    void writeByte(int location, int toWrite);
}

class Rom implements Cartridge {
    int[] rom;

    public Rom(int[] rom) {
        this.rom = rom;
    }
    
    public int readByte(int location){
        return rom[location];
    }
    
    public void writeByte(int location, int toWrite){
        //do nothing
    }
}

class Mbc1 implements Cartridge {
    public static final int BANK_SIZE = 0x4000;
    
    private int[][] banks;
    int currentBank = 1;
    
    public Mbc1(int[] rom){
        int numBanks = rom.length / BANK_SIZE;
        
        banks = new int[numBanks][BANK_SIZE];
        
        for(int i = 0; i < rom.length; i++){
            banks[i / BANK_SIZE][i % BANK_SIZE] = rom[i];
        }
    }

    public int readByte(int location) {
        if(location > 0x7fff) throw new InvalidParameterException("Out of cartridge memory");
        
        if(location < BANK_SIZE){
            return banks[0][location];
        }
        
        return banks[currentBank][location - BANK_SIZE];
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