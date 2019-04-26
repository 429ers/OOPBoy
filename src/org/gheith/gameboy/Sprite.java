package org.gheith.gameboy;

public class Sprite {
	private Tile tile1;
	private Tile tile2;
	int spriteX;
	int spriteY;
	boolean isLargeSprite;
	
	public Sprite(MMU mem, int spriteAddress, TileSet tileset, boolean isLargeSprite) {
		spriteY = mem.readByte(spriteAddress);
		spriteX = mem.readByte(spriteAddress + 1);
		this.isLargeSprite = isLargeSprite;
		if (isLargeSprite) {
			int tileNum = mem.readByte(spriteAddress + 2) & 0xFE;
			tile1 = tileset.getTile(tileNum);
			tile2 = tileset.getTile(tileNum + 1);
		}
		else {
			int tileNum = mem.readByte(spriteAddress + 2);
			tile1 = tileset.getTile(tileNum);
		}
	}
}
