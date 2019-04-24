package org.gheith.gameboy;

/***
 * 
 * Stores tile data in an 8x8 array, where the int represents a color.
 *
 */
public class Tile {
	int[][] tileData;
	
	/***
	 * 
	 * @param upperBytes upper 8 bytes of tile
	 * @param lowerBytes lower 8 bytes of tile
	 * Decodes tile data
	 */
	public Tile(long upperBytes, long lowerBytes) {
		tileData = new int[8][8];
		int left = 63;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 8; j++) {
				tileData[i][j] = (int) BitOps.extract(upperBytes, left, left - 1);
				tileData[i + 4][j] = (int) BitOps.extract(lowerBytes, left, left- 1);
				left -= 2;
			}
		}
	}
}
