
package org.gheith.gameboy;

public class ColorSmallSprite implements IColorSprite {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6581426955314021739L;
	private Tile tile1;
	int spriteX;
	int spriteY;
	int flags;
	int paletteNumber;
	int priority;
	int spriteAddress;
	
	public ColorSmallSprite(MMU mem, int spriteAddress, TileSetManager tileSetManager) {
		this.spriteAddress = spriteAddress;
		spriteY = mem.readByte(spriteAddress);
		spriteX = mem.readByte(spriteAddress + 1);
		flags = mem.readByte(spriteAddress + 3);
		this.priority = (int) BitOps.extract(flags, 7, 7);
		int bank = (int) BitOps.extract(flags, 3, 3);
		TileSet tileset = tileSetManager.getTileSet(bank, 0);
		int tileNum = mem.readByte(spriteAddress + 2);
		tile1 = tileset.getTile(tileNum);
		if (BitOps.extract(flags, 5, 5) == 1) {
			tile1 = tile1.flipTileOverXAxis();
			//System.out.println("flip x");
		}
		if (BitOps.extract(flags, 6, 6) == 1) {
			tile1 = tile1.flipTileOverYAxis();
			//System.out.println("flip y");
		}
		paletteNumber = (int) BitOps.extract(flags, 2, 0);
		
	}
	
	@Override
	public int getSpriteY() {
		return spriteY;
	}
	
	@Override
	public int getSpriteX() {
		return spriteX;
	}
	
	
	@Override
	public int getPixel(int posY, int posX) {
		return tile1.getPixel(posY, posX);
	}
	
	@Override
	public boolean inRange(int posY) {
		return posY >= spriteY && posY < spriteY + 8;
	}
	

	@Override
	public int getSpriteAddress() {
		return spriteAddress;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int getPaletteNumber() {
		return paletteNumber;
	}

}
