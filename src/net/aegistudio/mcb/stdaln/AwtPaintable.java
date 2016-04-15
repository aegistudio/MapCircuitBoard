package net.aegistudio.mcb.stdaln;

import java.awt.Color;
import java.awt.Graphics;

import net.aegistudio.mpp.algo.Paintable;

public class AwtPaintable implements Paintable {
	private Graphics g;	public void setGraphics(Graphics g) {
		this.g = g;
	}
	
	@Override
	public void bcolor(byte c) {
		
	}

	@Override
	public byte bget(int x, int y) {
		return 0;
	}

	@Override
	public void color(Color c) {
		g.setColor(c);
	}

	@Override
	public Color get(int x, int y) {
		return null;
	}

	@Override
	public int height() {
		return size * 4 * 32;
	}

	@Override
	public int width() {
		return size * 4 * 32;
	}
	
	int size = 5;
	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	public void set(int x, int y) {
		g.fillRect(size * x, size * y, size, size);
	}
}
