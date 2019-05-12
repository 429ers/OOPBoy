package org.the429ers.gameboy;

public class SpriteManager {
	private IColorSprite[] sprites;
	private TileSetManager tileSetManager;
	private LCDControl lcd;
	private MMU mmu;
	
	public SpriteManager(MMU mmu, TileSetManager tileSetManager, LCDControl lcd) {
		sprites = new IColorSprite[40];
		this.tileSetManager = tileSetManager;
		this.lcd = lcd;
		this.mmu = mmu;
	}
	
	public void initializeSprites() {
		for (int i = 0; i < 40; i++) {
			IColorSprite s = lcd.isUseSmallSprites() ? new ColorSmallSprite(mmu, 0xFE00 + (4 * i), tileSetManager) : new ColorLargeSprite(mmu, 0xFE00 + (4 * i), tileSetManager);
			sprites[i] = s;
		}
	}
	
	public IColorSprite getSprite(int index) {
		return sprites[index];
	}
	
	public void writeData(int location, int data) {
		int spriteNum = (location % 0xFE00) / 4;
		int byteNum = (location % 0xFE00) % 4;
		IColorSprite sprite = sprites[spriteNum];
		switch (byteNum) {
		case 0:
			sprite.setYPos(data);
			break;
		case 1:
			sprite.setXPos(data);
			break;
		case 2:
			sprite.setTileNumber(data);
			break;
		case 3:
			sprite.setAttributes(data);
			break;
		}
	}
}
