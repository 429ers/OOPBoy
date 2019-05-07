package org.gheith.gameboy;

public class TileSetManager {
	private MMU mem;
	private TileSet[][] tileSets;
	
	public TileSetManager(MMU mem) {
		this.mem = mem;
	}
	
	public void updateTileSets() {
		tileSets[0][0] = new TileSet(mem, 0x8000, 256, true, true);
		tileSets[0][1] = new TileSet(mem, 0x8800, 256, false, true);
		tileSets[1][0] = new TileSet(mem, 0x8000, 256, true, false);
		tileSets[1][1] = new TileSet(mem, 0x8800, 256, false, false);
	}
	
	public TileSet getTileSet(int bank, int tileSetNum) {
		return tileSets[bank][tileSetNum];
	}
}
