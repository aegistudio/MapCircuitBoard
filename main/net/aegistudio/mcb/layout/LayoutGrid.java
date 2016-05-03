package net.aegistudio.mcb.layout;

import java.io.InputStream;
import java.util.ArrayDeque;

import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.AbstractGrid;
import net.aegistudio.mcb.unit.Unit;
import net.aegistudio.mcb.wire.Wire;

public class LayoutGrid extends AbstractGrid {
	@Override
	public void setCell(int row, int column, Component component) {
		if(!bound(row, column)) return;
		if(component == Air.INSTANCE) component = null;
		
		// Analyze its previous grid component, decide whether to change it.
		Cell previous = this.cells[row][column];
		if(previous != null) {
			if(previous.getComponent() == component) return;
			if(previous instanceof LayoutUnitCell) 
				Facing.all(face -> unplantUnit((LayoutUnitCell)previous, face));
			else if(previous instanceof LayoutWireCell)
				unplantWire((LayoutWireCell) previous);
		}
		else if(component == null) return;
		this.cells[row][column] = null;
		
		// Now plant the new component into it.
		if(component instanceof Wire) {
			this.cells[row][column] = new LayoutWireCell(this, (Wire)component, row, column);
			Facing.all((facing) ->
					this.plantWire((LayoutWireCell) this.cells[row][column], facing));
		}
		else if(component instanceof Unit) {
			this.cells[row][column] = new LayoutUnitCell(this, (Unit)component, row, column);
			Facing.all((facing) -> 
				this.plantUnit((LayoutUnitCell) this.cells[row][column], facing));
		}
		if(component != null)
			component.init(this.cells[row][column]);
		
		this.update(row, column, previous);
	}
	
	protected Cell decode(int value, int row, int column) {
		return value != 0? new LayoutWireCell(this, null, row, column): 
			new LayoutUnitCell(this, null, row, column);
	}
	
	protected int encode(Cell cell) {
		return cell instanceof LayoutWireCell? 1 : 0;
	}
	
	@Override
	public void load(InputStream inputStream, ComponentFactory table) throws Exception {
		super.load(inputStream, table);
		all((r, c, cell, comp) -> {
			if(cell != null && cell instanceof LayoutUnitCell)
				Facing.all(face -> plantUnit((LayoutUnitCell) cell, face));
		});
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
			cells[wire.getRow()][wire.getColumn()] = null;
			plantUnit(cell, port);
			cells[wire.getRow()][wire.getColumn()] = wire;
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

	@Override
	public void tick(ItemFrame frame) {		}
}
