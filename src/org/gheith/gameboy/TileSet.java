package org.gheith.gameboy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TileSet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4048349332307406439L;
	private Map<Integer, Tile> tiles;
	private boolean isSetOne;
	private boolean isBankOne;
	
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne, boolean isBankOne) {
		this.isSetOne = isSetOne;
		tiles = new HashMap<Integer, Tile>();
		int tileNum = isSetOne ? 0 : -128;
		for (int i = 0; i < numTiles; i++) {
			int[] tileBytes = new int[16];
			int tileAddress = startAddress + i * 16;
			for (int j = 0; j < 16; j++) {
				tileBytes[j] = memory.readByteFromVRAM(tileAddress + j, isBankOne);
			}
			tiles.put(tileNum, new Tile(tileBytes));
			tileNum++;
		}
		this.isBankOne = isBankOne;
	}
	
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne) {
		this.isSetOne = isSetOne;
		tiles = new HashMap<Integer, Tile>();
		int tileNum = isSetOne ? 0 : -128;
		for (int i = 0; i < numTiles; i++) {
			int[] tileBytes = new int[16];
			int tileAddress = startAddress + i * 16;
			for (int j = 0; j < 16; j++) {
				tileBytes[j] = memory.readByte(tileAddress + j);
			}
			tiles.put(tileNum, new Tile(tileBytes));
			tileNum++;
		}
		this.isBankOne = true;
	}
	
	
	public Tile getTile(int tileNumber) {
		return tiles.get(tileNumber);
	}
	
	public boolean isTileSetOne() {
		return isSetOne;
	}
	
}
