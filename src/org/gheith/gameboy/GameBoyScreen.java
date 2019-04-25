package org.gheith.gameboy;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;

public class GameBoyScreen extends Canvas{
	public Image img;
	
	public GameBoyScreen() {
		img = null;
	}
	public GameBoyScreen(Image img) {
		this.img = img;
	}
	
	public void drawFrame(Image img) {
		this.img = img;
		this.repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		if (img != null) {
			g.drawImage(img, 0, 0, img.getWidth(this), img.getHeight(this), this);
		}
	}
}
