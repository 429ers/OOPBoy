package org.gheith.gameboy;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ColorPPU implements IPPU {
	private MMU mem;
	private LCDControl lcdControl;
	private ColorPaletteManager colorPaletteManager;
	private TileSetManager tileSetManager;
	private int currentX;
	private int currentY;
	private int scrollX;
	private int scrollY;
	private int cycleCount;
	private boolean drewFrame;
	private GameBoyScreen gbs;
	private int LYCompare;
	private int windowX;
	private int windowY;
	private ColorTileMap currentTileMap;
	private Map<Integer, IColorSprite> sprites;
	private transient BufferedImage frame;
	
	public ColorPPU(MMU mem) {
		this.mem = mem;
		lcdControl = new LCDControl(mem);
		colorPaletteManager = new ColorPaletteManager();
		tileSetManager = new TileSetManager(mem);
		gbs = new GameBoyScreen();
		frame = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		sprites = new HashMap<>();
	}

	public void tick() {
	
		
		scrollX = mem.readByte(0xFF43);
		if (cycleCount == OAM_SEARCH_START) {
			scrollY = mem.readByte(0xFF42);
			if (currentY < ACTUAL_LINES) {
				int status = mem.readByte(0xFF41) & 0x3F;
				mem.writeByte(0xFF41, status | 0x80);
			}
			mem.writeByte(0xFF44, currentY);
			if (currentY == 0) {
				this.tileSetManager.updateTileSets();
				//this.loadMap(lcdControl., useBackgroundMap1);
			}
			if (lcdControl.isSpritesEnabled()) {
				loadSprites();
			}
			//loadWindow(useTileSet1, useWindowTileMap1);
			windowX = mem.readByte(0xff4b) - 7;
			windowY = mem.readByte(0xff4a);
			currentX = 0;
			scrollX = mem.readByte(0xFF43);
		}
		if (cycleCount == PIXEL_TRANSFER_START) {
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0xC0);
		}
		
		
		
		// Actually transfer pixels
		if (cycleCount >= PIXEL_TRANSFER_START && cycleCount < PIXEL_TRANSFER_START + 160 && currentY < ACTUAL_LINES) {
			
		}
		// H-Blank Interrupt
		if (cycleCount == H_BLANK_START && currentY < ACTUAL_LINES) {
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0xC0);
		}
		
		
		// Increment currentY
		if (cycleCount == H_BLANK_END) {
			currentY++;
			if (currentY == 154) {
				currentY = 0;
			}
		}
		
		drewFrame = false;
		// Send V-Blank interrupt
		if (currentY == 145 && cycleCount == 0) {
			drawFrame();
		}
		
		//send LCDC interrupt
        if (currentY == LYCompare){
            int interruptRegister = mem.readByte(0xFF0F) & 0xFE;
            mem.writeByte(0xFF0F, interruptRegister | 0x02);
        }
		
		cycleCount++;
		cycleCount %= LINE_LENGTH;
	}
	
	
	private void drawFrame() {
		int status = mem.readByte(0xFF41) & 0x3F;
		mem.writeByte(0xFF41, status | 0x40);
		gbs.drawFrame(frame);
		drewFrame = true;
		int interruptRegister = mem.readByte(0xFF0F) & 0xFE;
		mem.writeByte(0xFF0F, interruptRegister | 0x01);
		//mem.writeByte(0xFF85, 0xFF);
		//mem.writeByte(0xFF44, 0x90);
	}
	
	public void loadSprites() {
		sprites.clear();
		int spriteCount = 0;
		int spritesFound = 0;
		int memAddress = 0xFE00;
		while (spriteCount < 40 && spritesFound < 10) {
			IColorSprite s = null;
			if (!lcdControl.isUseSmallSprites()) {
				s = new ColorLargeSprite(mem, memAddress, tileSetManager);
			}
			else {
				s = new ColorSmallSprite(mem, memAddress, tileSetManager);
			}
			if (s.inRange(currentY + 16)) {
				spritesFound++;
				for (int i = 0; i < 8; i++) {
					if (!sprites.containsKey(s.getSpriteX() + i)) {
						if (s.getPixel(currentY - (s.getSpriteY() - 16), i) != 0) {
							sprites.put(s.getSpriteX() + i, s);
						}
					}
					else {
						IColorSprite conflictSprite = sprites.get(s.getSpriteX() + i);
						boolean isTransparent = s.getPixel(currentY - (s.getSpriteY() - 16), i) == 0;
						if (s.getSpriteX() < conflictSprite.getSpriteX() && !isTransparent) {
							sprites.put(s.getSpriteX() + i, s);
						}
					}
				}
			}
			spriteCount++;
			memAddress += 4;
		}
	}
	
	
	
}
