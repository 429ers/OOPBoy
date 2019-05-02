package org.gheith.gameboy;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Image;

public class GameBoyScreen extends JPanel {
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
			g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
		}
	}
}
