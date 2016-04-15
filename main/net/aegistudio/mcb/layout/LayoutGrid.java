package net.aegistudio.mcb.layout;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;

import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.CellPaintable;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.unit.Unit;
import net.aegistudio.mcb.wire.Wire;
import net.aegistudio.mpp.algo.Paintable;

public class LayoutGrid implements Grid {
	private final Cell[][] layout = new Cell[32][32];
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
		return bound(row, column)? layout[row][column] : null;
	}

	private boolean bound(int row, int column) {
		if(row >= 32 || row < 0) return false;
		if(column >= 32 || column < 0) return false;
		return true;
	}
	
	@Override
	public void setCell(int row, int column, Component component) {
		if(!bound(row, column)) return;
		if(component == Air.INSTANCE) component = null;
		
		// Analyze its previous grid component, decide whether to change it.
		Cell previous = this.layout[row][column];
		if(previous != null) {
			if(previous.getComponent() == component) return;
			if(previous instanceof LayoutUnitCell) 
				Facing.all(face -> unplantUnit((LayoutUnitCell)previous, face));
			else if(previous instanceof LayoutWireCell)
				unplantWire((LayoutWireCell) previous);
		}
		else if(component == null) return;
		this.layout[row][column] = null;
		
		// Now plant the new component into it.
		if(component instanceof Wire) {
			this.layout[row][column] = new LayoutWireCell(this, (Wire)component, row, column);
			Facing.all((facing) ->
					this.plantWire((LayoutWireCell) this.layout[row][column], facing));
		}
		else if(component instanceof Unit) {
			this.layout[row][column] = new LayoutUnitCell(this, (Unit)component, row, column);
			Facing.all((facing) -> 
				this.plantUnit((LayoutUnitCell) this.layout[row][column], facing));
		}
		if(component != null)
			component.init(this.layout[row][column]);
	}

	public static final Integer END_GRID = 255;
	@Override
	public void load(InputStream inputStream) throws Exception {
		while(true) {
			int row = inputStream.read();
			if(row == END_GRID) break;
			int column = inputStream.read();
			Cell cell = inputStream.read() != 0? 
					new LayoutWireCell(this, null, row, column): 
					new LayoutUnitCell(this, null, row, column);
			cell.load(inputStream);
			this.layout[row][column] = cell;
		}
		
		all((r, c, cell, comp) -> {
			if(cell != null && cell instanceof LayoutUnitCell)
				Facing.all(face -> plantUnit((LayoutUnitCell) cell, face));
		});
	}

	@Override
	public void save(OutputStream outputStream) throws Exception {
		for(int r = 0; r < 32; r ++) 
			for(int c = 0; c < 32; c ++) {
				Cell current = this.getCell(r, c);
				if(current != null) {
					outputStream.write(current.getRow());
					outputStream.write(current.getColumn());
					outputStream.write(current instanceof LayoutWireCell? 1 : 0);
					current.save(outputStream);
				}
			}
		outputStream.write(END_GRID);
	}
	
	void plantWire(LayoutWireCell wire, Facing direction) {
		Cell cell = direction.call(wire.getRow(), wire.getColumn(), (r, c) -> this.getCell(r, c));
		if(cell == null) return;
		if(cell instanceof LayoutUnitCell) {
			LayoutUnitCell unit = (LayoutUnitCell) cell;
			this.plantUnit(unit, direction.opposite());
		}
		if(!(cell instanceof LayoutWireCell)) return;
		LayoutWireCell adjacence = (LayoutWireCell)cell;
		adjacence.allReachablePort((LayoutUnitCell unit, Facing port)-> {
			if(updateDistance(adjacence, wire, unit, port, direction.opposite())) {
				Facing.all((propagate) -> this.bfTraverse(wire, propagate, (current, next, face) -> {
					return updateDistance(current, next, 
							unit, port, face);
				}));
			}
		});
	}
	
	void unplantWire(LayoutWireCell wire) {
		wire.allReachablePort((LayoutUnitCell cell, Facing port) -> {
			unplantUnit(cell, port);
			layout[wire.getRow()][wire.getColumn()] = null;
			plantUnit(cell, port);
			layout[wire.getRow()][wire.getColumn()] = wire;
		});
	}
	
	void unplantUnit(LayoutUnitCell unit, Facing port) {
		this.bfTraverse(unit, port, (current, next, face) -> {
			return next.removeDistance(unit, port);
		});
	}
	
	private boolean updateDistance(LayoutWireCell source, LayoutWireCell destination, 
			LayoutUnitCell unit, Facing port, Facing face) {
		
		short distance = (short) Math.min(source.getComponent().distance(face)
				+ destination.getComponent().distance(face.opposite())
				+ source.getDistance(unit, port), Short.MAX_VALUE);
		if(distance == Short.MAX_VALUE) return false;
		if(distance < destination.getDistance(unit, port)) {
			destination.setDistance(unit, port, distance);
			return true;
		}
		else return false;
	}
	
	void plantUnit(LayoutUnitCell unit, Facing port) {
		this.bfTraverse(unit, port, (current, next, face) -> {
			if(current == null) {
				next.setDistance(unit, port, (short) Math.min(1 + next.getComponent()
						.distance(face.opposite()), Short.MAX_VALUE));
				return true;
			}
			else return updateDistance(current, next, 
					unit, port, face);
		});
	}
	
	public void all(Visitor v) {
		for(int r = 0; r < 32; r ++)
			for(int c = 0; c < 32; c ++) {
				Cell cell = this.getCell(r, c);
				v.visit(r, c, cell, cell == null? Air.INSTANCE : cell.getComponent());
			}
	}
	
	public interface Visitor {
		public void visit(int row, int column, Cell cell, Component component);
	}
	
	public interface Traverser { 
		/**
		 * @param source from which cell
		 * @param next to which cell
		 * @param vector the next cell
		 * @return accept next for next traversing
		 */
		public boolean accept(LayoutWireCell source, LayoutWireCell next, Facing vector);
	}
	
	void bfTraverse(Cell source, Facing port, Traverser consumer) {
		// Retrieve seed node.
		Cell seed = port.call(source.getRow(), source.getColumn(), (r, c) -> this.getCell(r, c));
		if(!(seed instanceof LayoutWireCell)) return;
		
		// Breadth first traversal
		LayoutWireCell begin = source instanceof LayoutWireCell? (LayoutWireCell)source : null;
		if(consumer.accept(begin, (LayoutWireCell)seed, port)) {
			ArrayDeque<LayoutWireCell> queue = new ArrayDeque<LayoutWireCell>();
			queue.addLast((LayoutWireCell)seed);
			while(!queue.isEmpty()) {
				LayoutWireCell current = queue.removeFirst();
				port.allQuads(current.getRow(), current.getColumn(), (xt, yt, f) -> {
					Cell adjacence = this.getCell(xt, yt);
					if(adjacence != null && adjacence != current)
						if(adjacence instanceof LayoutWireCell) {
							LayoutWireCell next = (LayoutWireCell) adjacence;
							if(consumer.accept(current, next, f))
								queue.addLast(next);
						}
				});
			}
		}
	}
}
