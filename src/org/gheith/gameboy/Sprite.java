package org.gheith.gameboy;

public class Sprite {
	private Tile tile1;
	private Tile tile2;
	int spriteX;
	int spriteY;
	int flags;
	boolean isLargeSprite;
	boolean usePalletteZero;
	int priority;
	int spriteAddress;
	
	public Sprite(MMU mem, int spriteAddress, TileSet tileset, boolean isLargeSprite) {
		this.spriteAddress = spriteAddress;
		spriteY = mem.readByte(spriteAddress);
		spriteX = mem.readByte(spriteAddress + 1);
		flags = mem.readByte(spriteAddress + 3);
		this.isLargeSprite = isLargeSprite;
		this.priority = (int) BitOps.extract(flags, 7, 7);
		if (isLargeSprite) {
			int tileNum = mem.readByte(spriteAddress + 2) & 0xFE;
			tile1 = tileset.getTile(tileNum);
			tile2 = tileset.getTile(tileNum + 1);
		}
		else {
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
			if (BitOps.extract(flags, 4, 4) == 0) {
				usePalletteZero = true;
			}
			else {
				usePalletteZero = false;
			}
		}
	}
	
	public int getSpriteY() {
		return spriteY;
	}
	
	public int getSpriteX() {
		return spriteX;
	}
	
	public Tile getTile() {
		return tile1;
	}
	
	public boolean inRange(int posY) {
		return posY >= spriteY && posY < spriteY + 8;
	}
	
	public boolean usePalletteZero() {
		return usePalletteZero;
	}
	
	public int getSpriteAddress() {
		return spriteAddress;
	}
}
