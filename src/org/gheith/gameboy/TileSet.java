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
	private boolean isSetZero;
	private boolean isBankZero;
	
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne, boolean isBankZero) {
		this.isSetZero = isSetOne;
		tiles = new HashMap<Integer, Tile>();
		int tileNum = isSetOne ? 0 : -128;
		for (int i = 0; i < numTiles; i++) {
			int[] tileBytes = new int[16];
			int tileAddress = startAddress + i * 16;
			for (int j = 0; j < 16; j++) {
				int bank = isBankZero ? 0 : 1;
				tileBytes[j] = memory.readByteFromVRAM(tileAddress + j, bank);
			}
			tiles.put(tileNum, new Tile(tileBytes));
			tileNum++;
		}
		this.isBankZero = isBankZero;
	}
	
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne) {
		this.isSetZero = isSetOne;
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
		this.isBankZero = true;
	}
	
	public TileSet(boolean isSetZero) {
		this.isSetZero = isSetZero;
		tiles = new HashMap<>();
		int tileNum = isSetZero ? 0 : -128;
		for (int i = 0; i < 256; i++) {
			tiles.put(tileNum, new Tile());
			tileNum++;
		}
		this.isBankZero = true;
	}
	
	public void updateTile(int tileNum, int byteNum, int data) {
		Tile t = tiles.get(tileNum);
		t.updateTile(byteNum, data);
	}
	
	public Tile getTile(int tileNumber) {
		return tiles.get(tileNumber);
	}
	
	public boolean isTileSetOne() {
		return isSetZero;
	}
	
}
