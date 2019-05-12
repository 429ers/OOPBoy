package org.the429ers.gameboy;

public class ColorSmallSprite implements IColorSprite {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6581426955314021739L;
	private Tile tile1;
	private int spriteX;
	private int spriteY;
	private int flags;
	private int paletteNumber;
	private int priority;
	private int spriteAddress;
	private int vramBank;
	private int tileNumber;
	private boolean xFlip;
	private boolean yFlip;
	private TileSetManager tileSetManager;
	
	public ColorSmallSprite(int spriteAddress, TileSetManager tileSetManager) {
		this.spriteAddress = spriteAddress;
		this.tileSetManager = tileSetManager;
	}
	
	public ColorSmallSprite(MMU mem, int spriteAddress, TileSetManager tileSetManager) {
		this.spriteAddress = spriteAddress;
		spriteY = mem.readByte(spriteAddress) & 0xFF;
		spriteX = mem.readByte(spriteAddress + 1) & 0xFF;
		flags = mem.readByte(spriteAddress + 3) & 0xFF;
		this.priority = (int) BitOps.extract(flags, 7, 7);
		int bank = (int) BitOps.extract(flags, 3, 3);
		TileSet tileset = tileSetManager.getTileSet(bank, 0);
		int tileNum = mem.readByte(spriteAddress + 2) & 0xFF;
		tile1 = tileset.getTile(tileNum);
		if (BitOps.extract(flags, 5, 5) == 1) {
			tile1 = tile1.flipTileOverXAxis();
			//System.out.println("flip x");
		}
		if (BitOps.extract(flags, 6, 6) == 1) {
			tile1 = tile1.flipTileOverYAxis();
			//System.out.println("flip y");
		}
		paletteNumber = (int) BitOps.extract(flags, 2, 0) & 0xFF;
		setAttributes(flags);
		setTileNumber(tileNum);
		
	}
	
	@Override
	public void setYPos(int yPos) {
		this.spriteY = yPos;
	}
	
	@Override
	public void setXPos(int xPos) {
		this.spriteX = xPos;
	}
	
	@Override
	public void setTileNumber(int tileNumber) {
		this.tileNumber = tileNumber;
	}
	
	@Override
	public void setAttributes(int attributes) {
		this.priority = (int) BitOps.extract(attributes, 7, 7);
		this.yFlip = (int) BitOps.extract(attributes, 6, 6) == 1;
		this.xFlip = (int) BitOps.extract(attributes, 5, 5) == 1;
		this.vramBank = (int) BitOps.extract(attributes, 3, 3);
		this.paletteNumber = (int) BitOps.extract(attributes, 2, 0);
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
		/*
		Tile t = tileSetManager.getTileSet(vramBank, 0).getTile(tileNumber);
		if (xFlip && yFlip) {
			return t.getPixelXandYFlip(posY, posX);
		}
		else if (xFlip) {
			return t.getPixelXFlip(posY, posX);
		}
		else if (yFlip) {
			return t.getPixelYFlip(posY, posX);
		}
		else {
			return t.getPixel(posY, posX);
		}
		*/
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
