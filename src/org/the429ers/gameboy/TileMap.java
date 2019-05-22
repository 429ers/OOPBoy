package org.the429ers.gameboy;

import java.io.Serializable;

public class TileMap implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1536789195081436980L;
    private int[][] map;
    private TileSetManager tileSetManager;
    int tileSetNum;
    
    public TileMap(MMU memory, int startAddress, int tileSetNum, TileSetManager manager) {
        map = new int[32][32];
        this.tileSetManager = manager;
        this.tileSetNum = tileSetNum;
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                int tileNumberAddress = startAddress + (i * 32) + j;
                int tileNumber;
                if (tileSetNum == 0) {
                    tileNumber = memory.readByte(tileNumberAddress);
                }
                else {
                    tileNumber = (byte) memory.readByte(tileNumberAddress);
                }
                map[i][j] = tileNumber;
            }
        }
        
        
        
    }
    
    
    

    
    public Tile getTile(int x, int y) {
        TileSet t = tileSetManager.getTileSet(0, tileSetNum);
        int tileNum = map[x % 32][y % 32];
        Tile tile = t.getTile(map[x % 32][y % 32]);
        if (tile == null) {
            System.out.println("failed to find tile num: " + tileNum);
        }
        return t.getTile(map[x % 32][y % 32]);
    }
    
}
