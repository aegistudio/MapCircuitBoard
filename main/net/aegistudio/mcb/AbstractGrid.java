package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import net.aegistudio.mpp.algo.Paintable;

public abstract class AbstractGrid implements Grid {
	protected final Cell[][] cells = new Cell[32][32];
	@Override
	public void paint(Paintable paintable) {
		CellPaintable cellPaintable = new CellPaintable(paintable);
		all((r, c, cell, component) -> {
			cellPaintable.setLocation(r, c);
			component.paint(cell, cellPaintable);
		});
	}
	
	@Override
	public Cell getCell(int row, int column) {
		return bound(row, column)? cells[row][column] : null;
	}

	protected boolean bound(int row, int column) {
		if(row >= 32 || row < 0) return false;
		if(column >= 32 || column < 0) return false;
		return true;
	}

	public static final Integer END_GRID = 255;
	@Override
	public void load(InputStream inputStream, ComponentFactory table) throws Exception {
		while(true) {
			int row = inputStream.read();
			if(row == END_GRID) break;
			int column = inputStream.read();
			Cell cell = decode(inputStream.read(), row, column);
			try {
				cell.load(inputStream, table);
				this.cells[row][column] = cell;
			}
			catch(Exception e) {
				// When it fails, print stack trace and set to air.
				e.printStackTrace();
				this.cells[row][column] = null;
			}
		}
	}

	protected abstract Cell decode(int code, int row, int column);
	protected abstract int encode(Cell cell);
	
	@Override
	public void save(OutputStream outputStream, ComponentFactory table) throws Exception {
		for(int r = 0; r < 32; r ++) 
			for(int c = 0; c < 32; c ++) {
				Cell current = this.getCell(r, c);
				if(current != null) {
					outputStream.write(current.getRow());
					outputStream.write(current.getColumn());
					outputStream.write(encode(current));
					current.save(outputStream, table);
				}
			}
		outputStream.write(END_GRID);
	}
	

	public void all(Visitor v) {
		for(int r = 0; r < 32; r ++)
			for(int c = 0; c < 32; c ++) {
				Cell cell = this.getCell(r, c);
				v.visit(r, c, cell, cell == null? Air.INSTANCE : cell.getComponent());
			}
	}
	
	public <T extends Component> void all(Visitor v, Class<T> componentClazz) {
		all((row, column, cell, component) -> {
			if(componentClazz.isAssignableFrom(component.getClass()))
				v.visit(row, column, cell, component);
		});
	}
	
	public interface Visitor {
		public void visit(int row, int column, Cell cell, Component component);
	}
	
	/**
	 * Any class could register or unregister observation to this grid by 
	 * add or removing themselves from this set.
	 */
	public final HashSet<CellObserver> observers = new HashSet<CellObserver>();
	
	public static interface CellObserver {
		public void update(int row, int column, Cell oldCell, Cell newCell);
	}
	
	public void update(int row, int column, Cell previous) {
		// Push mode for notifying cell update.
				observers.forEach(observer -> observer.update(
						row, column, previous, this.cells[row][column]));
	}
}
