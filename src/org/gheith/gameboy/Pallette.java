package org.gheith.gameboy;

import java.awt.Color;
import java.util.Map;
import java.util.HashMap;

public class Pallette {
	private Map<Integer, Color> colorMap;
	
	public Pallette(int data) {
		colorMap = new HashMap<>();
		for (int i = 0; i < 4 ; i++) {
			int color = (int) BitOps.extract(data, (2 * i) + 1, 2 * i);
			Color currentColor = null;
			switch (color) {
			case 0:
				currentColor = Color.WHITE;
				break;
			case 1:
				currentColor = Color.LIGHT_GRAY;
				break;
			case 2:
				currentColor = Color.DARK_GRAY;
				break;
			case 3:
				currentColor = Color.BLACK;
				break;
			}
			colorMap.put(i, currentColor);
		}
	}
	
	public Color getColor(int colorNum) {
		return colorMap.get(colorNum);
	}
}
