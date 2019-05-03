package org.gheith.gameboy;

public interface ISprite {
	
	public int getSpriteY();
	
	public int getSpriteX();
	
	public Tile getTile();
	
	public int getPixel(int posY, int posX);
	
	public boolean inRange(int posY);
	
	public boolean usePalletteZero();
	
	public int getSpriteAddress();
}
