package net.aegistudio.mcb;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import net.aegistudio.mpp.algo.Paintable;

public class CellPaintable implements Paintable {
	private int row, column;
	public void setLocation(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	private final Paintable paintable;
	public CellPaintable(Paintable paintable) {
		this.paintable = paintable;
	}
	
	@Override
	public void bcolor(byte c) {
		paintable.bcolor(c);
	}
	
	@Override
	public void color(Color c) {
		paintable.color(c);
	}
	
	@Override
	public byte bget(int x, int y) {
		return call(x, y, (x0, y0) -> paintable.bget(x0, y0));
	}

	@Override
	public Color get(int x, int y) {
		return call(x, y, (x0, y0) -> paintable.get(x0, y0));
	}
	
	@Override
	public void set(int x, int y) {
		consume(x, y, (x0, y0) -> paintable.set(x0, y0));
	}
	
	@Override	public int width() {	return 4;	}
	@Override	public int height() {	return 4;	}
	
	private <T> T call(int x, int y, BiFunction<Integer, Integer, T> function) {
		return function.apply(column * 4 + x, row * 4 + y);
	}
	
	private void consume(int x, int y, BiConsumer<Integer, Integer> function) {
		function.accept(column * 4 + x, row * 4 + y);
	}
}
