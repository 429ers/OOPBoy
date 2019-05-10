package org.the429ers.gameboy;

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
    	System.out.printf("Game is using cartridge type %x\n", cartridgeType);
        if(cartridgeType == 0x00){
            return new Rom(rom);
        }else if(cartridgeType < 0x04){
            return new Mbc1(rom, fileName);
        }
        else if (cartridgeType >= 0x0F && cartridgeType <= 0x13) {
        	return new Mbc3(rom, fileName);
        }
        else {
        	System.out.printf("Cartridge type %x is not supported\n", cartridgeType);
        }
        
        throw new InvalidParameterException("Cartridge type not supported yet");
    }
    
    int readByte(int location);
    void writeByte(int location, int toWrite);
    void cleanUp();
}



