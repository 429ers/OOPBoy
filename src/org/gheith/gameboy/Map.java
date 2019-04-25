package org.gheith.gameboy;

public class Map {
	private Tile[][] map;
	
	public Map(Memory memory, int startAddress, TileSet tileSet) {
		map = new Tile[32][32];
		
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int tileNumberAddress = startAddress + (i * 32) + j;
				int tileNumber = memory.readByte(tileNumberAddress);
				map[i][j] = tileSet.getTile(tileNumber);
			}
		}
	}
	
	public Tile getTile(int x, int y) {
		return map[x][y];
	}
	
}
