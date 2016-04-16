package net.aegistudio.mcb.board;

import net.aegistudio.mcb.AbstractGrid;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.layout.LayoutGrid;
import net.aegistudio.mcb.layout.LayoutUnitCell;
import net.aegistudio.mcb.layout.LayoutWireCell;
import net.aegistudio.mcb.unit.Unit;
import net.aegistudio.mcb.wire.Wire;

public class ActualGrid extends AbstractGrid {
	public final LayoutGrid layout;

	public ActualGrid(LayoutGrid layout) {
		this.layout = layout;
		this.layout.all((r, c, cell, component) -> setCell(r, c, cell));
	}
	
	private AbstractGrid.CellObserver observer = (r, c, previous, current) -> setCell(r, c, current);
	public void add() {
		this.layout.observers.add(observer);
	}
	
	public void remove() {
		this.layout.observers.remove(observer);
	}
	
	void setCell(int r, int c, Cell cell) {
		if(cell == null) return;
		if(cell instanceof LayoutWireCell) 
			this.cells[r][c] = new ActualWireCell(this, (LayoutWireCell) cell);
		else this.cells[r][c] = new ActualUnitCell(this, (LayoutUnitCell) cell);
	}
	
	@Override
	public void setCell(int row, int column, Component component) {			}
	
	public void tick() {
		this.all((r, c, cell, wire) -> {
			cell.tick();
		}, Wire.class);
		
		this.all((r, c, cell, unit) -> {
			cell.tick();
		}, Unit.class);
	}

	@Override
	protected Cell decode(int code, int row, int column) {
		return super.getCell(row, column);
	}

	@Override
	protected int encode(Cell cell) {
		return cell instanceof ActualUnitCell? 1 : 0;
	}
}
