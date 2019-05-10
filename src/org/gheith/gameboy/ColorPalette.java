package org.gheith.gameboy;

import java.awt.Color;

public class ColorPalette {
	private ColorData[] colors;
	
	public ColorPalette() {
		colors = new ColorData[4];
		for (int i = 0; i < 4; i++) {
			colors[i] = new ColorData();
		}
	}
	
	public Color getColor(int colorNum) {
		return colors[colorNum].getColor();
	}
	
	public void setRed(int colorNum, int data) {
		ColorData color = colors[colorNum];
		int red = (int) (BitOps.extract(data, 4, 0));
		color.red = red;
	}
	
	public void setLowGreen(int colorNum, int data) {
		ColorData color = colors[colorNum];
		int green = color.green;
		green = green & 0b11000;
		green += BitOps.extract(data, 7, 5);
		color.green = green;
	}
	
	public void setHighGreen(int colorNum, int data) {
		ColorData color = colors[colorNum];
		int green = color.green;
		green = green & 0b00111;
		green += (BitOps.extract(data, 1, 0) << 3);
		color.green = green;
	}
	
	public void setBlue(int colorNum, int data) {
		ColorData color = colors[colorNum];
		color.blue = (int) (BitOps.extract(data, 6, 2));
	}
	
	private static class ColorData {
		private int red = 0;
		private int green = 0;
		private int blue = 0;
		
		private Color getColor() {
			return new Color(transform(red), transform(blue), transform(green));
		}
	}
	
	private static int transform(int color) {
		if (color == 31) return 255;
		return (int) (8.2258064516129 * (double) color);
	}
}
