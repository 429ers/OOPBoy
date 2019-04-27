package org.gheith.gameboy;

import java.util.HashMap;
import java.util.Map;

public class TileSet {
	private Map<Integer, Tile> tiles;
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne) {
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
	}
	
	public Tile getTile(int tileNumber) {
		return tiles.get(tileNumber);
	}
	
}
