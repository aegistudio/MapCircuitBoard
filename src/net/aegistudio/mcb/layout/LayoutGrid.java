package net.aegistudio.mcb.layout;

import java.io.InputStream;
import java.util.ArrayDeque;

import javax.swing.JFrame;
import javax.swing.UIManager;

import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.CellPaintable;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.stdaln.AwtGridComponent;
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

	@Override
	public void load(InputStream inputStream) throws Exception {
		                                      
	}

	@Override
	public void save(InputStream outputStream) throws Exception {
	
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
				next.setDistance(unit, port, (short)1);
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
	
	public static void main(String[] arguments) {
		try {UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");} catch(Exception e) {}
		LayoutGrid layout = new LayoutGrid();
		JFrame frame = new JFrame("Layout");
		frame.setLayout(null);
		frame.setResizable(false);
		
		AwtGridComponent component = new AwtGridComponent(layout);
		component.setLocation(0, 0);
		frame.add(component);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setSize(component.getWidth(), component.getHeight());
		frame.setVisible(true);
	}
}
