package org.gheith.gameboy;

import java.io.Serializable;

public class TileSetManager implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8958150457625059639L;
	private TileSet[][] tileSets;
	private boolean isGBCMode;
	
	public TileSetManager(boolean isGBCMode) {
		this.isGBCMode = isGBCMode;
		tileSets = new TileSet[2][2];
		tileSets[0][0] = new TileSet(true);
		tileSets[0][1] = new TileSet(false);
		if (isGBCMode) {
			tileSets[1][0] = new TileSet(true);
			tileSets[1][1] = new TileSet(false);
		}
	}
	

	
	public void updateTileSets(int memAddress, int data, int bank) {
		if (memAddress >= 0x8000 && memAddress <= 0x8FFF) {
			int tileAddress = memAddress % 0x8000;
			int tileNum = tileAddress / 16;
			int byteNum = tileAddress % 16;
			tileSets[0][0].updateTile(tileNum, byteNum, data);
		}
		if (memAddress >= 0x8800 && memAddress <= 0x97FF) {
			int tileAddress = memAddress % 0x8800;
			int tileNum = tileAddress / 16;
			tileNum -= 128;
			int byteNum = tileAddress % 16;
			tileSets[0][1].updateTile(tileNum, byteNum, data);
		}
	}
	
	public TileSet getTileSet(int bank, int tileSetNum) {
		return tileSets[bank][tileSetNum];
	}
}
