package org.gheith.gameboy;

import java.awt.image.BufferedImage;

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
	private transient BufferedImage frame;
	
	public ColorPPU(MMU mem) {
		this.mem = mem;
		lcdControl = new LCDControl(mem);
		colorPaletteManager = new ColorPaletteManager();
		tileSetManager = new TileSetManager(mem);
		gbs = new GameBoyScreen();
		frame = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
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
			//spritesEnabled = BitOps.extract(lcdc, 1, 1) == 1;
			if (lcdControl.isSpritesEnabled()) {
				//loadSprites();
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
	
	
	
}
