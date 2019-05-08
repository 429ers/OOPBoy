package org.gheith.gameboy;

public class TileTest {
	
	public static void main(String args[]) {
		int[] tileData = new int[] {0x7c, 0x7c, 0x00, 0xC6, 0xc6, 0x00, 0x00, 0xfe, 0xc6, 0xc6, 0x00, 0xc6, 0xc6, 0x00, 0x00, 0x00, 0x00};
	
		Tile tile = new Tile();
		
		for (int i = 0; i < 2; i++) {
			tile.updateTile(i, tileData[i]);
		}
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(tile.getPixel(i, j));
			}
			System.out.println("");
		}
	}
}
