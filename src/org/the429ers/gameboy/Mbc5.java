package org.the429ers.gameboy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;

public class Mbc5 implements Cartridge {
	/**
	 * 
	 */
	private static final long serialVersionUID = -206354286731307487L;
	public static final int BANK_SIZE = 0x4000;
	
	private boolean ramEnabled;
	private boolean hasBattery;
	private int ramBank;
	private String fileName;
    private boolean isGBC;
    private byte[][] banks;
    private byte[][] ram;
    int currentBank = 1;
    
    public Mbc5(byte[] rom, String fileName){
    	isGBC = rom[0x143] == 0x80 || rom[0x143] == 0xC0;
    	this.fileName = fileName + ".sav";
        int numBanks = rom.length / BANK_SIZE;
        banks = new byte[numBanks][BANK_SIZE];
        ramEnabled = false;
        for(int i = 0; i < rom.length; i++){
            banks[i / BANK_SIZE][i % BANK_SIZE] = rom[i];
        }
        ram = new byte [16][2 << 20];
        hasBattery = rom[0x0147] == 0x03;
        if (hasBattery) {
	        File ramData = new File(this.fileName);
	        if (ramData.exists()) {
	        	try {
	        		System.out.println("found data");
					FileInputStream ramInput = new FileInputStream(ramData);
					ObjectInputStream objectIn = new ObjectInputStream(ramInput);
					ram = (byte[][]) objectIn.readObject();
					ramInput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
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
					out.close();
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
        	int ramLocation = location % 0xA000;
        	return ram[ramBank][ramLocation] & 0xFF;
        }
        
        if(location < BANK_SIZE){
            return banks[0][location] & 0xff;
        }
        
        return banks[currentBank][location - BANK_SIZE] & 0xff;
    }

    public void writeByte(int location, int toWrite) {
        //Low 8 bits of rom bank
    	if(location >= 0x2000 && location <= 0x2fff) {
            //bank switching
            int newBank = toWrite & 0xff;
            this.currentBank &= 0x100;
            this.currentBank += newBank;
        }
        
    	// High bit of rom bank
        if(location >= 0x3000 && location <= 0x3fff) {
            //bank switching
            int newBank = toWrite & 0x1;
            this.currentBank += newBank << 8;
        }
        
        // RAM enable
        if (location >= 0x0000 && location <= 0x1FFF) {
        	ramEnabled = (toWrite & 0x0A) == 0x0A;
        }
        
        if (location >= 0xA000 && location <= 0xBFFF && ramEnabled) {
        	int ramLocation = location % 0xA000;
        	ram[ramBank][ramLocation] = (byte) toWrite;
        	
        }
        
        // Ram bank number
        if (location >= 0x4000 && location <= 0x5FFF) {
        	ramBank = toWrite & 0xf;
        }
        
    }

	@Override
	public void cleanUp() {
		// Write to save file
    	if (hasBattery) {
        	try {
        		FileOutputStream cartridgeRam = new FileOutputStream(this.fileName);
        		ObjectOutputStream objectOut = new ObjectOutputStream(cartridgeRam);
				objectOut.writeObject(ram);
				cartridgeRam.close();
				//System.out.println("wrote to save file");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}

	@Override
	public boolean isGBC() {
		// TODO Auto-generated method stub
		return isGBC;
	}
}
