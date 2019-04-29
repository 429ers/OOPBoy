package org.gheith.gameboy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class PPU {
	private TileSet tileset1;
	private TileSet tileset2;
	private TileMap map;
	private MMU mem;
	private int currentX;
	private int currentY;
	private BufferedImage frame;
	private GameBoyScreen gbs;
	private int scrollX;
	private int scrollY;
	private int cycleCount;
	private boolean drewFrame;
	private Map<Integer, Sprite> sprites;
	
	/*
	public static final int OAM_SEARCH_LENGTH = 20;
	public static final int OAM_SEARCH_START = 0;
	public static final int OAM_SEARCH_END = 19;
	public static final int PIXEL_TRANSFER_LENGTH = 43;
	public static final int PIXEL_TRANSFER_START = 20;
	public static final int PIXEL_TRANSFER_END = 62;
	public static final int H_BLANK_LENGTH = 51;
	public static final int H_BLANK_START = 63;
	public static final int H_BLANK_END = 113;
	public static final int V_BLANK = 10;
	public static final int ACTUAL_LINES = 144;
	public static final int V_BLANK_LINES = 10;
	public static final int LINE_LENGTH = 114;
	*/
	
	
	public static final int OAM_SEARCH_LENGTH = 80;
	public static final int OAM_SEARCH_START = 0;
	public static final int OAM_SEARCH_END = 79;
	public static final int PIXEL_TRANSFER_LENGTH = 172;
	public static final int PIXEL_TRANSFER_START = 80;
	public static final int PIXEL_TRANSFER_END = 251;
	public static final int H_BLANK_LENGTH = 204;
	public static final int H_BLANK_START = 252;
	public static final int H_BLANK_END = 455;
	public static final int V_BLANK = 10;
	public static final int ACTUAL_LINES = 144;
	public static final int V_BLANK_LINES = 10;
	public static final int LINE_LENGTH = 456;
	
	int framesDrawn = 0;
	
	
	public PPU(MMU mem, GameBoyScreen gbs) {
		this.mem = mem;
		frame = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		currentX = 0;
		currentY = 0;
		this.gbs = gbs;
		sprites = new HashMap<Integer, Sprite>();
	}
	
	public boolean drewFrame() {
		return drewFrame;
	}
	
	public void loadTileSets() {
		tileset1 = new TileSet(mem, 0x8000, 256, true);
		tileset2 = new TileSet(mem, 0x8800, 256, false);
	}
	
	
	public void loadMap(boolean useTileSet1) {
		TileSet ts = useTileSet1 ? tileset1 : tileset2;
		map = new TileMap(mem, 0x9800, ts);
	}
	
	public void tick() {
		// Lie to the CPU and pretend we're transfering pixels to the LCD
		if (cycleCount >= PIXEL_TRANSFER_START && cycleCount <= PIXEL_TRANSFER_END) {
					
		}
		/*
		else if (cycleCount == 0) {
			//System.out.println("executing this thing");
			
		}
		*/
		if (cycleCount == OAM_SEARCH_START) {
			scrollY = mem.readByte(0xFF42);
			loadSprites();
			if (currentY < ACTUAL_LINES) {
				int status = mem.readByte(0xFF41) & 0x3F;
				mem.writeByte(0xFF41, status | 0x80);
			}
			mem.writeByte(0xFF44, currentY);
			if (currentY == 0) {
				this.loadTileSets();
				this.loadMap(true);
			}
			currentX = 0;
			scrollX = mem.readByte(0xFF43);
		}
		if (cycleCount == PIXEL_TRANSFER_START) {
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0xC0);
		}
		
		
		
		// Actually transfer pixels
		if (cycleCount >= PIXEL_TRANSFER_START && cycleCount < PIXEL_TRANSFER_START + 160 && currentY < ACTUAL_LINES) {
			int yPos = currentY + scrollY;
			int xPos = scrollX + currentX;
			Tile currentTile;
			if (sprites.containsKey(currentX + 8)) {
				currentTile = sprites.get(currentX + 8).getTile();
			}
			else {
				currentTile = map.getTile(yPos / 8, xPos / 8);
			}
			int pixel = currentTile.getPixel(yPos % 8, xPos % 8);
			switch(pixel) {
			case 0:
				frame.setRGB(currentX, currentY, Color.WHITE.getRGB());
				break;
			case 1:
				frame.setRGB(currentX, currentY, Color.LIGHT_GRAY.getRGB());
				break;
			case 2:
				frame.setRGB(currentX, currentY, Color.DARK_GRAY.getRGB());
				break;
			case 3:
				frame.setRGB(currentX, currentY, Color.BLACK.getRGB());
				break;
			default:
				frame.setRGB(currentX, currentY, Color.BLACK.getRGB());
			}
			currentX++;
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
		
		// Send V-Blank interrupt
		if (currentY == 145 && cycleCount == 0) {
			
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0x40);
			gbs.drawFrame(frame);
			drewFrame = true;
			int interruptRegister = mem.readByte(0xFF0F) & 0xFE;
			mem.writeByte(0xFF0F, interruptRegister | 0x01); 
			//mem.writeByte(0xFF85, 0xFF);
			//mem.writeByte(0xFF44, 0x90);
		}
		
		cycleCount++;
		cycleCount %= LINE_LENGTH;
	}
	
	
	
	public void tickOld() {
		//System.out.println("Current X " + currentX + "\n Current Y " + currentY);
		// Need to reset all values
		drewFrame = false;
		mem.writeByte(0xFF44, currentY);
		if (currentX == 0 && currentY == 0) {
			scrollY = mem.readByte(0xFF42);
			System.out.println("Scroll Y " + scrollY);
			mem.writeByte(0xF085, 0x00);
			loadTileSets();
			loadMap(true);
		}
		// Need to reset 
		if (currentX == 0) {
			scrollX = mem.readByte(0xFF43);
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0x80);
		}
		if (currentX == 80) {
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0xC0);
			
		}
		if (currentX >= 80 && currentX < 80 + 160 && currentY < 144) {
			
			int tileX = (scrollX + currentX - 80) / 8;
			int tileY = (scrollY + currentY) / 8;
			//Tile currentTile = map.getTile(tileX, tileY);
			//int pixel = currentTile.getPixel((currentX - 80 + scrollX) % 8, (scrollY + currentY) % 8);
			Tile currentTile = map.getTile(tileY, tileX);
			int pixel = currentTile.getPixel((scrollY + currentY) % 8, (currentX - 80 + scrollX) % 8);
			switch(pixel) {
			case 0:
				frame.setRGB(currentX - 80, currentY, Color.WHITE.getRGB());
				break;
			case 1:
				frame.setRGB(currentX - 80, currentY, Color.LIGHT_GRAY.getRGB());
				break;
			case 2:
				frame.setRGB(currentX - 80, currentY, Color.DARK_GRAY.getRGB());
				break;
			case 3:
				frame.setRGB(currentX - 80, currentY, Color.BLACK.getRGB());
				break;
			default:
				frame.setRGB(currentX - 80, currentY, Color.BLACK.getRGB());
			}
		}
		if (currentX == 80 + 172) {
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status);
		}
		// Entered V Blank
		if (currentX == 0 && currentY == 146) {
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int status = mem.readByte(0xFF41) & 0x3F;
			mem.writeByte(0xFF41, status | 0x40);
			gbs.drawFrame(frame);
			drewFrame = true;
			int interruptRegister = mem.readByte(0xFF0F) & 0xFE;
			mem.writeByte(0xFF0F, interruptRegister | 0x01); 
			//mem.writeByte(0xFF85, 0xFF);
			//mem.writeByte(0xFF44, 0x90);
			System.out.println("drawing frame");

			
		}
		
		currentX++;
		if (currentX == 456) {
			currentX = 0;
			currentY++;
			//mem.writeByte(0xFF44, currentY);
		}
		if (currentY == 154) {
			currentY = 0;
		}	
	}
	
	public void loadSprites() {
		sprites.clear();
		int spriteCount = 0;
		int spritesFound = 0;
		int memAddress = 0xFE00;
		while (spriteCount < 40 && spritesFound < 10) {
			Sprite s = new Sprite(mem, memAddress, tileset1, false);
			if (s.getSpriteY() == currentY + 16) {
				spritesFound++;
				for (int i = 0; i < 8; i++) {
					if (!sprites.containsKey(s.getSpriteX() + i)) {
						sprites.put(s.getSpriteX() + i, s);
					}
				}
			}
			spriteCount++;
			memAddress += 4;
		}
	}
	
}
