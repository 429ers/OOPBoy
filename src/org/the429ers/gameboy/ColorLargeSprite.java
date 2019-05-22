package org.the429ers.gameboy;

public class ColorLargeSprite implements IColorSprite {

    private Tile tile1;
    private Tile tile2;
    private int spriteX;
    private int spriteY;
    private int flags;
    private int paletteNumber;
    private int priority;
    private int spriteAddress;
    private int tileNum0;
    private int tileNum1;
    private int vramBank;
    private boolean xFlip;
    private boolean yFlip;
    
    public ColorLargeSprite(MMU mem, int spriteAddress, TileSetManager tileSetManager) {
        this.spriteAddress = spriteAddress;
        spriteY = mem.readByte(spriteAddress);
        spriteX = mem.readByte(spriteAddress + 1);
        flags = mem.readByte(spriteAddress + 3);
        this.priority = (int) BitOps.extract(flags, 7, 7);
        int tileNum = mem.readByte(spriteAddress + 2) & 0xFE;
        int bank = (int) BitOps.extract(flags, 3, 3);
        this.paletteNumber = (int) BitOps.extract(flags, 2, 0) & 0xFF;
        TileSet tileset = tileSetManager.getTileSet(bank, 0);
        tile1 = tileset.getTile(tileNum);
        tile2 = tileset.getTile(tileNum + 1);
        if (BitOps.extract(flags, 5, 5) == 1) {
            tile1 = tile1.flipTileOverXAxis();
            tile2 = tile2.flipTileOverXAxis();
            //System.out.println("flip x");
        }
        if (BitOps.extract(flags, 6, 6) == 1) {
            tile1 = tile1.flipTileOverYAxis();
            tile2 = tile2.flipTileOverYAxis();
            Tile temp = tile1;
            tile1 = tile2;
            tile2 = temp;
            //System.out.println("flip y");
        }
    }

    @Override
    public int getSpriteY() {
        return spriteY;
    }

    @Override
    public int getSpriteX() {
        return spriteX;
    }
    
    @Override
    public int getPixel(int posY, int posX) {
        if (posY >= 8) {
            return tile2.getPixel(posY - 8, posX);
        }
        else {
            return tile1.getPixel(posY, posX);
        }
    }


    @Override
    public boolean inRange(int posY) {
        return posY >= spriteY && posY < spriteY + 16;
    }

    @Override
    public int getPaletteNumber() {
        return paletteNumber;
    }

    @Override
    public int getSpriteAddress() {
        return spriteAddress;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setYPos(int yPos) {
        this.spriteY = yPos;
    }
    
    @Override
    public void setXPos(int xPos) {
        this.spriteX = xPos;
    }
    
    @Override
    public void setTileNumber(int tileNumber) {
        this.tileNum0 = tileNumber & 0xFE;
        this.tileNum1 = tileNum0 + 1;
    }

    @Override
    public void setAttributes(int attributes) {
        // TODO Auto-generated method stub
        this.priority = (int) BitOps.extract(attributes, 7, 7);
        this.yFlip = (int) BitOps.extract(attributes, 6, 6) == 1;
        this.xFlip = (int) BitOps.extract(attributes, 5, 5) == 1;
        this.vramBank = (int) BitOps.extract(attributes, 3, 3);
        this.paletteNumber = (int) BitOps.extract(attributes, 2, 0);
    }
}
