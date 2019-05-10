package org.gheith.gameboy;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ColorPaletteManager implements Serializable {
	private ColorPalette[] palettes;
	private int currentIndex;
	private boolean autoIncrement;
	
	public ColorPaletteManager() {
		palettes = new ColorPalette[8];
	}
	
	public ColorPalette getPalette(int paletteNum) {
		return palettes[paletteNum];
	}
	
	public void setIndex(int index) {
		currentIndex = (int) BitOps.extract(index, 5, 0);
		autoIncrement = BitOps.extract(index, 7, 7) == 1;
	}
	
	public void writeColor(int data) {
		int index = currentIndex / 8;
		int colorNum = (currentIndex % 8) / 2;
		if (index % 2 == 0) {
			palettes[index].setRed(colorNum, data);
			palettes[index].setLowGreen(colorNum, data);
		}
		else {
			palettes[index].setHighGreen(colorNum, data);
			palettes[index].setBlue(colorNum, data);
		}
		if (autoIncrement) {
			currentIndex++;
			currentIndex %= 0x40;
		}
	}
}
