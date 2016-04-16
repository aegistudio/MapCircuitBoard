package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public enum Facing implements Data {
	NORTH(+1, 0),
	EAST(0, +1),
	SOUTH(-1, 0),
	WEST(0, -1);
	
	public final int offsetRow, offsetColumn;
	private Facing(int offRow, int offColumn) {
		this.offsetRow = offRow;
		this.offsetColumn = offColumn;
	}
	
	public static Facing load(InputStream input) throws Exception {
		return get(input.read());
	}
	
	public void save(OutputStream output) throws Exception {
		output.write(this.ordinal());
	}
	
	public static Facing get(int ordinal) {
		return values()[(ordinal + values().length) % values().length];
	}
	
	public Facing nextQuad() {
		return get(this.ordinal() + 1);
	}
	
	public Facing previousQuad() {
		return get(this.ordinal() - 1);
	}
	
	public interface FacingApplicable { void apply(int row, int column, Facing vector); }
	
	public <T> void allQuads(int row, int column, FacingApplicable function) {
		Facing facing = this;
		do {
			function.apply(row + facing.offsetRow, column + facing.offsetColumn, facing);
			facing = facing.nextQuad();
		}
		while(facing != this);
	}
	
	public Facing opposite() {
		return get(this.ordinal() - (values().length / 2));
	}
	
	public <T> T call(int row, int column, BiFunction<Integer, Integer, T> function) {
		return function.apply(row + this.offsetRow, column + this.offsetColumn);
	}
	
	public void consume(int row, int column, BiConsumer<Integer, Integer> consumer) {
		consumer.accept(row + this.offsetRow, column + this.offsetColumn);
	}
	
	public static void all(Consumer<Facing> consumer) {
		for(Facing f : values()) consumer.accept(f);
	}
	
	public Facing duplicate() {
		return this;
	}
	
	public void side(BiConsumer<Integer, Integer> consumer) {
		for(int i = 0; i < 4; i ++)
			if(this.offsetColumn == 0) {
				consumer.accept(i, this.offsetRow > 0? 3 : 0);
			}
			else if(this.offsetRow == 0)
				consumer.accept(this.offsetColumn > 0? 3 : 0, i);
	}
}