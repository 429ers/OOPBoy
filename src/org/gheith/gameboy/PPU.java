package org.gheith.gameboy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class PPU {
	private TileSet tileset1;
	private TileSet tileset2;
	private Map map;
	private ArrayList<Sprite> sprites;
	private Memory mem;
	private int currentX;
	private int currentY;
	private BufferedImage frame;
	private int scrollX;
	private int scrollY;
	
	public PPU(Memory mem) {
		this.mem = mem;
		frame = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		currentX = 0;
		currentY = 0;
	}
	
	public void loadTileSets() {
		tileset1 = new TileSet(mem, 0x8000, 256, true);
		tileset2 = new TileSet(mem, 0x8800, 256, false);
	}
	
	
	public void loadMap(boolean useTileSet1) {
		TileSet ts = useTileSet1 ? tileset1 : tileset2;
		map = new Map(mem, 0x9800, ts);
	}
	
	public void tick() {
		// Need to reset all values
		if (currentX == 0 && currentY == 0) {
			
		}
		// Need to reset 
		if (currentX == 0) {
			
		}
		if (currentX < 160 && currentY < 144) {
			int tileX = currentX / 32;
			int tileY = currentY / 32;
			Tile currentTile = map.getTile(tileX, tileY);
			int pixel = currentTile.getPixel(currentX % 32, currentY % 32);
			switch(pixel) {
			case 0:
				frame.setRGB(currentX, currentY, Color.BLACK.getRGB());
				break;
			case 1:
				frame.setRGB(currentX, currentY, Color.BLACK.getRGB());
				break;
			case 2:
				frame.setRGB(currentX, currentY, Color.WHITE.getRGB());
				break;
			case 3:
				frame.setRGB(currentX, currentY, Color.WHITE.getRGB());
				break;
			default:
				frame.setRGB(currentX, currentY, Color.BLACK.getRGB());
			}
		}
		// Entered V Blank
		else if (currentX == 0 && currentY == 145) {
			
		}
	}
}
