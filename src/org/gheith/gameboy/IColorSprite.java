package org.gheith.gameboy;

import java.io.Serializable;

public interface IColorSprite extends Serializable{
	
	public int getSpriteY();
	
	public int getSpriteX();
	
	public int getPixel(int posY, int posX);
	
	public boolean inRange(int posY);
	
	public int getPaletteNumber();
	
	public int getSpriteAddress();
	
	public int getPriority();
}
