package org.gheith.gameboy;

import java.io.Serializable;

public interface ISprite extends Serializable{
	
	public int getSpriteY();
	
	public int getSpriteX();
	
	public int getPixel(int posY, int posX);
	
	public boolean inRange(int posY);
	
	public boolean usePalletteZero();
	
	public int getSpriteAddress();
	
	public int getPriority();
}
