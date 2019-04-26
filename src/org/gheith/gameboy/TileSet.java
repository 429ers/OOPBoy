package org.gheith.gameboy;

import java.util.HashMap;
import java.util.Map;

public class TileSet {
	private Map<Integer, Tile> tiles;
	
	public TileSet(MMU memory, int startAddress, int numTiles, boolean isSetOne) {
		tiles = new HashMap<Integer, Tile>();
		int tileNum = isSetOne ? 0 : -128;
		for (int i = 0; i < numTiles; i++) {
			int tileAddress = startAddress + i * 16;
			long upperData = memory.readQuadWord(tileAddress);
			long lowerData = memory.readQuadWord(tileAddress + 8);
			tiles.put(tileNum, new Tile(upperData, lowerData));
			tileNum++;
		}
	}
	
	public Tile getTile(int tileNumber) {
		return tiles.get(tileNumber);
	}
	
}
