package net.aegistudio.mcb.layout;

import java.io.InputStream;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.AbstractGrid.CellObserver;
import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.unit.Unit;
import net.aegistudio.mcb.wire.Wire;

public class SpectatedLayoutGrid extends LayoutGrid implements CellObserver {
	public TreeSet<Short> units = new TreeSet<Short>();
	public TreeSet<Short> specialWires = new TreeSet<Short>();
	
	public SpectatedLayoutGrid() {
		super();
		super.observers.add(this);
	}
	
	public void load(InputStream inputStream, ComponentFactory table) throws Exception {
		super.load(inputStream, table);
		
		units.clear(); specialWires.clear();
		all((r, c, cell, component) -> update(r, c, null, cell));
	}
	
	public static void decode(short encoded, BiConsumer<Integer, Integer> target) {
		target.accept(row(encoded), column(encoded));
	}
	
	public static int column(short encoded) {
		return (short) ((encoded >> 0) & 0x001f);
	}
	
	public static int row(short encoded) {
		return (short) ((encoded >> 5) & 0x001f);
	}

	public static short encode(int row, int column) {
		return (short)((row << 5) + (column << 0));
	}
	
	public static interface FullConsumer { public void consume(int r, int c, short value);	}	
	public static void encodeFullWithCheck(int row, int column, FullConsumer resultConsumer) {
		if(row < 0 || row >= 32) return;
		if(column < 0 || column >= 32) return;
		resultConsumer.consume(row, column, encode(row, column));
	}
	
	public static void encodeWithCheck(int row, int column, Consumer<Short> resultConsumer) {
		encodeFullWithCheck(row, column, (r, c, v) -> resultConsumer.accept(v));
	}
	
	public <C extends Component> void all(Visitor visitor, Class<C> componentClazz) {
		TreeSet<Short> targetSet = null;
		if(componentClazz == Unit.class) 
			targetSet = units;
		else if(componentClazz == Wire.class) 
			targetSet = specialWires;
		
		if(targetSet != null) {
			targetSet.forEach(encoded -> decode(encoded, (row, column) -> {
				Cell cell = super.getCell(row, column);
				visitor.visit(row, column, cell, cell == null? 
						Air.INSTANCE : cell.getComponent());
			}));
		}
		else super.all(visitor, componentClazz);
	}

	public FullConsumer wireUpdate = (r, c, value) -> {
		Cell wireCell = super.getCell(r, c);
		if(wireCell == null) return;
		if(!(wireCell.getComponent() instanceof Wire)) return;
		specialWires.remove(value);
		Facing.all(f -> {
			Cell adjacence = f.call(r, c, (nr, nc) -> super.getCell(nr, nc));
			if(adjacence == null) return;
			if(adjacence.getComponent() instanceof Unit)
				specialWires.add(value);
		});
	};
	
	@Override
	public void update(int row, int column, Cell oldCell, Cell newCell) {
		short specialMask = encode(row, column);
		if(oldCell == null && newCell == null) return;
		if(oldCell != null && newCell != null) {
			if(oldCell.getComponent() instanceof Unit 
					&& newCell.getComponent() instanceof Unit) return;
			if(oldCell.getComponent() instanceof Wire
					&& newCell.getComponent() instanceof Wire) return;
		}
		
		if(oldCell != null) 
			if(oldCell.getComponent() instanceof Wire) 
				this.specialWires.remove(specialMask);
			else {
				this.units.remove(specialMask);
				encodeFullWithCheck(row + 1, column + 1, wireUpdate);
				encodeFullWithCheck(row - 1, column + 1, wireUpdate);
				encodeFullWithCheck(row + 1, column - 1, wireUpdate);
				encodeFullWithCheck(row - 1, column - 1, wireUpdate);
			}
		
		if(newCell != null)
			if(newCell.getComponent() instanceof Unit) {
				this.units.add(specialMask);
				encodeFullWithCheck(row + 1, column + 1, wireUpdate);
				encodeFullWithCheck(row - 1, column + 1, wireUpdate);
				encodeFullWithCheck(row + 1, column - 1, wireUpdate);
				encodeFullWithCheck(row - 1, column - 1, wireUpdate);
			}
			else encodeFullWithCheck(row, column, wireUpdate);
	}
}
