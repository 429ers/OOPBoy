package org.gheith.gameboy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class PPU {
	private TileSet tileset1;
	private TileSet tileset2;
	private Map map;
	private ArrayList<Sprite> sprites;
	private MMU mem;
	private int currentX;
	private int currentY;
	private BufferedImage frame;
	private GameBoyScreen gbs;
	private int scrollX;
	private int scrollY;
	private boolean drewFrame;
	
	public PPU(MMU mem, GameBoyScreen gbs) {
		this.mem = mem;
		frame = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		currentX = 0;
		currentY = 0;
		this.gbs = gbs;
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
		map = new Map(mem, 0x9800, ts);
	}
	
	public void tick() {
		//System.out.println("Current X " + currentX + "\n Current Y " + currentY);
		// Need to reset all values
		drewFrame = false;
		if (currentX == 0 && currentY == 0) {
			scrollY = mem.readByte(0xFF42);
			//System.out.println("Scroll Y " + scrollY);
		}
		// Need to reset 
		if (currentX == 0) {
			scrollX = mem.readByte(0xFF43);
		}
		if (currentX >= 92 && currentX < 80 + 172 && currentY < 144) {
			int tileX = (scrollX + currentX - 80) / 8;
			int tileY = (scrollY + currentY) / 8;
			Tile currentTile = map.getTile(tileX, tileY);
			int pixel = currentTile.getPixel((currentX - 80 + scrollX) % 8, (scrollY + currentY) % 8);
			switch(pixel) {
			case 0:
				frame.setRGB(currentX - 92, currentY, Color.WHITE.getRGB());
				break;
			case 1:
				frame.setRGB(currentX - 92, currentY, Color.LIGHT_GRAY.getRGB());
				break;
			case 2:
				frame.setRGB(currentX - 92, currentY, Color.DARK_GRAY.getRGB());
				break;
			case 3:
				frame.setRGB(currentX - 92, currentY, Color.BLACK.getRGB());
				break;
			default:
				frame.setRGB(currentX - 92, currentY, Color.BLACK.getRGB());
			}
		}
		// Entered V Blank
		if (currentX == 0 && currentY == 145) {
			gbs.drawFrame(frame);
			drewFrame = true;
			try {
				Thread.sleep(16);
				System.out.println("drawing frame");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		currentX++;
		if (currentX == 456) {
			currentX = 0;
			currentY++;
		}
		if (currentY == 154) {
			currentY = 0;
		}
		
		
		
	}
}
