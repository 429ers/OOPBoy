package org.gheith.gameboy;

public class Sprite {
	private Tile tile1;
	private Tile tile2;
	int spriteX;
	int spriteY;
	int flags;
	boolean isLargeSprite;
	
	public Sprite(MMU mem, int spriteAddress, TileSet tileset, boolean isLargeSprite) {
		spriteY = mem.readByte(spriteAddress);
		spriteX = mem.readByte(spriteAddress + 1);
		flags = mem.readByte(spriteAddress + 3);
		this.isLargeSprite = isLargeSprite;
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
			}
			if (BitOps.extract(flags, 6, 6) == 1) {
				tile1 = tile1.flipTileOverYAxis();
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
}
