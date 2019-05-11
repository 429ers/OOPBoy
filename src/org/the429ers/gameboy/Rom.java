package org.the429ers.gameboy;

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

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public boolean isGBC() {
		// TODO Auto-generated method stub
		return false;
	}
}