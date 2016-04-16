package net.aegistudio.mcb.layout;

import net.aegistudio.mcb.AbstractCell;
import net.aegistudio.mcb.unit.Unit;

public class LayoutUnitCell extends AbstractCell<LayoutGrid, Unit>{

	protected LayoutUnitCell(LayoutGrid grid, Unit component, int row, int column) {
		super(grid, row, column);
		super.setComponent(component);
	}

	@Override
	public void tick() {
		
	}
}
