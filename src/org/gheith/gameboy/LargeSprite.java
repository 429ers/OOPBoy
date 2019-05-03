package org.gheith.gameboy;

public class LargeSprite implements ISprite {
	
	private Tile tile1;
	private Tile tile2;
	private int spriteX;
	private int spriteY;
	private int flags;
	private boolean usePalletteZero;
	private int priority;
	private int spriteAddress;
	
	public LargeSprite(MMU mem, int spriteAddress, TileSet tileset) {
		this.spriteAddress = spriteAddress;
		spriteY = mem.readByte(spriteAddress);
		spriteX = mem.readByte(spriteAddress + 1);
		flags = mem.readByte(spriteAddress + 3);
		this.priority = (int) BitOps.extract(flags, 7, 7);
		int tileNum = mem.readByte(spriteAddress + 2) & 0xFE;
		tile1 = tileset.getTile(tileNum);
		tile2 = tileset.getTile(tileNum + 1);
		if (BitOps.extract(flags, 5, 5) == 1) {
			tile1 = tile1.flipTileOverXAxis();
			tile2 = tile2.flipTileOverXAxis();
			//System.out.println("flip x");
		}
		if (BitOps.extract(flags, 6, 6) == 1) {
			tile1 = tile1.flipTileOverYAxis();
			tile2 = tile2.flipTileOverYAxis();
			Tile temp = tile1;
			tile1 = tile2;
			tile2 = temp;
			//System.out.println("flip y");
		}
		if (BitOps.extract(flags, 4, 4) == 0) {
			usePalletteZero = true;
		}
		else {
			usePalletteZero = false;
		}
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
		if (posY >= 8) {
			return tile2.getPixel(posY - 8, posX);
		}
		else {
			return tile1.getPixel(posY, posX);
		}
	}


	@Override
	public boolean inRange(int posY) {
		return posY >= spriteY && posY < spriteY + 16;
	}

	@Override
	public boolean usePalletteZero() {
		return usePalletteZero;
	}

	@Override
	public int getSpriteAddress() {
		return spriteAddress;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}



}
