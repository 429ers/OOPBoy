package org.the429ers.gameboy;

public class ColorTileMap {
private TileWithAttributes[][] map;
	
	public ColorTileMap(MMU mem, int startAddress, TileSetManager tileSetManager, int tileSetNum) {
		map = new TileWithAttributes[32][32];
		
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int tileNumberAddress = startAddress + (i * 32) + j;
				int attributes = mem.readByteFromVRAM(tileNumberAddress, false);
				int tileVRAMBankNumber = (int) BitOps.extract(attributes, 3, 3);
				TileSet tileSet = tileSetManager.getTileSet(tileVRAMBankNumber, tileSetNum);
				int tileNumber;
				if (tileSet.isTileSetOne()) {
					tileNumber = mem.readByte(tileNumberAddress);
				}
				else {
					tileNumber = (byte) mem.readByte(tileNumberAddress);
				}
				if (tileSet.getTile(tileNumber) == null) {
					System.out.println("failed to find tile num "+ tileNumber);
				}
				Tile tile = tileSet.getTile(tileNumber);
				int paletteNumber = (int) BitOps.extract(attributes, 2, 0);
				boolean hasPriority = BitOps.extract(attributes, 7, 7) == 1;
				map[i][j] = new TileWithAttributes(tile, paletteNumber, hasPriority);
			}
		}
		
		
		
	}
	
	public Tile getTile(int y, int x) {
		return map[y % 32][x % 32].tile;
	}
	
	public int getPaletteNumber(int y, int x) {
		return map[y % 32][x % 32].paletteNumber;
	}
	
	public boolean hasPriority(int y, int x) {
		return map[y % 32][x % 32].hasPriority;
	}
	
	public static class TileWithAttributes {
		private Tile tile;
		private int paletteNumber;
		private boolean hasPriority;
		
		public TileWithAttributes(Tile tile, int paletteNumber, boolean hasPriority) {
			this.tile = tile;
			this.paletteNumber = paletteNumber;
			this.hasPriority = hasPriority;
		}
		
		
	}
}
