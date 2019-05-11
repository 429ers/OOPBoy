package org.the429ers.gameboy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

public class Mbc3 implements Cartridge{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7093578351089391407L;
	
	public static final int BANK_SIZE = 0x4000;
	private static final int RAM_BANK_SIZE = 0x4000;
	
	private boolean ramEnabled;
	private boolean hasBattery;
	private boolean hasRam;
	private boolean isLatched;
	private boolean isGBC;
	private int ramBank;
	private String fileName;
    
    private byte[][] banks;
    private byte[] ram;
    int currentBank = 1;
    
    public Mbc3(byte[] rom, String fileName) {
    	int gbcByte = rom[0x143] & 0xFF;
    	this.isGBC = gbcByte == 0x80;
    	this.fileName = fileName + ".sav";
        int numBanks = rom.length / BANK_SIZE;
        banks = new byte[numBanks][BANK_SIZE];
        ramEnabled = false;
        for(int i = 0; i < rom.length; i++){
            banks[i / BANK_SIZE][i % BANK_SIZE] = rom[i];
        }
        ram = new byte[0xFFFF];
        hasBattery = (rom[0x0147] == 0x0F) || (rom[0x0147] == 0x10) || (rom[0x147] == 0x13);
        hasRam = (rom[0x0147] == 0x10) || (rom[0x0147] == 0x12) || (rom[0x147] == 0x13);
        if (hasBattery) {
	        File ramData = new File(this.fileName);
	        if (ramData.exists()) {
	        	try {
	        		//System.out.println("found data");
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

	@Override
	public int readByte(int location) {
		if(location > 0xBFFF) throw new InvalidParameterException("Out of cartridge memory");
        
		else if (location >= 0xA000 && location <= 0xBFFF) {
			if (ramEnabled) {
	        	if (ramBank >= 8) {
	        		//System.out.println("read rtc");
	        		return 5;
	        	}
	        	//System.out.println("read ram");
	        	int ramLocation = (ramBank * RAM_BANK_SIZE) + location % 0xA000;
	        	return ram[ramLocation] & 0xFF;
			}
			else {
				return 0xFF;
			}
        }
        
		else if(location < BANK_SIZE){
            return banks[0][location] & 0xff;
        }
        
		else {
			return banks[currentBank][location - BANK_SIZE] & 0xff;
		}
	}

	@Override
	public void writeByte(int location, int toWrite) {
		// RAM/RTC enable
		if (location >= 0x0000 && location <= 0x1FFF) {
        	if ((toWrite & 0x0A) == 0x0A) {
            	ramEnabled = true;
        		//System.out.println("enabled ram");
        	}
        	else if (toWrite == 0) {
        		ramEnabled = false;
        		//System.out.println("disabled ram");
        	}
		}
		
		else if (location >= 0xA000 && location <= 0xBFFF && ramEnabled) {
			if (ramBank < 0x8) {
				//System.out.println("wrote to ram");
				int ramLocation = (ramBank * RAM_BANK_SIZE) + (location % 0xA000);
				ram[ramLocation] = (byte) toWrite;
			}
			else {
				//System.out.println("wrote rtc");
			}
		}
		
		// Rom bank selection
		else if (location >= 0x2000 && location <= 0x3FFF) {
			currentBank = toWrite & 0x7f;
			if (currentBank == 0) {
				currentBank = 1;
			}
		}
		
		// RAM bank / RTC selection
		else if (location >= 0x4000 && location <= 0x5FFF) {
			ramBank = toWrite;
			//System.out.printf("selected ram bank %x\n", ramBank);
		}
		
		// Latch clock
		else if (location >= 0x6000 && location <= 0x7FFF)  {
			// Store clock data in RTC regs
			if (!isLatched && toWrite == 1) {
				//System.out.println("latched rtc");
				isLatched = true;
			}
			else if (isLatched && toWrite == 0) {
				isLatched = false;
			}
		}
		
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		// Write to save file
    	if (hasBattery) {
        	try {
        		FileOutputStream cartridgeRam = new FileOutputStream(this.fileName);
				cartridgeRam.write(ram);
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
