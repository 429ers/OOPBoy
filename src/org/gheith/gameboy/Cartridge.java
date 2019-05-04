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
            return new Mbc1(rom, fileName);
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
    	if (location > rom.length) {
    		throw new IllegalArgumentException("Memory does not exist");
    	}
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
	private static final int RAM_BANK_SIZE = 0x2000;
	
	private boolean ramEnabled;
	private boolean isRomBankingMode;
	private boolean hasBattery;
	private int ramBank;
	private int upperBits;
	private String fileName;
    
    private byte[][] banks;
    private byte[] ram;
    int currentBank = 1;
    
    public Mbc1(byte[] rom, String fileName){
    	this.fileName = fileName + ".sav";
        int numBanks = rom.length / BANK_SIZE;
        banks = new byte[numBanks][BANK_SIZE];
        ramEnabled = false;
        isRomBankingMode = false;
        for(int i = 0; i < rom.length; i++){
            banks[i / BANK_SIZE][i % BANK_SIZE] = rom[i];
        }
        ram = new byte[0xFFFF];
        hasBattery = rom[0x0147] == 0x03;
        if (hasBattery) {
	        File ramData = new File(this.fileName);
	        if (ramData.exists()) {
	        	try {
	        		System.out.println("found data");
					FileInputStream ramInput = new FileInputStream(ramData);
					ramInput.read(ram);
					ramInput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        else {
	        	try {
					ramData.createNewFile();
					FileOutputStream out = new FileOutputStream(ramData);
					for (int i = 0; i < ram.length; i++) {
		        		out.write(0);
		        	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        }
        }
    }

    public int readByte(int location) {
        if(location > 0xBFFF) throw new InvalidParameterException("Out of cartridge memory");
        
        if (location >= 0xA000 && location <= 0xBFFF) {
        	int ramLocation = (ramBank * RAM_BANK_SIZE) + location % 0xA000;
        	return ram[ramLocation] & 0xFF;
        }
        
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
            
            if (isRomBankingMode) {
            	newBank += upperBits << 5;
            }
            this.currentBank = newBank;
        }
        
        // RAM enable
        if (location >= 0x0000 && location <= 0x1FFF) {
        	ramEnabled = (toWrite & 0x0A) == 0x0A;
        }
        
        if (location >= 0xA000 && location <= 0xBFFF) {
        	int ramLocation = (ramBank * RAM_BANK_SIZE) + location % 0xA000;
        	ram[ramLocation] = (byte) toWrite;
        	// Write to save file
        	if (hasBattery) {
	        	try {
					RandomAccessFile cartridgeRam = new RandomAccessFile(this.fileName, "rw");
					cartridgeRam.seek(ramLocation);
					cartridgeRam.write(toWrite);
					cartridgeRam.close();
					System.out.println("wrote to save file");
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        // Either ram bank number or upper 2 bits of rom bank number
        if (location >= 0x4000 && location <= 0x5FFF) {
        	if (!isRomBankingMode) {
        		ramBank = toWrite;
        	}
        	else {
        		upperBits = toWrite;
        	}
        }
        
        // RAM or ROM bits
        if (location >= 0x6000 && location <= 0x7FFF) {
        	isRomBankingMode = toWrite == 0;
        }
    }
}