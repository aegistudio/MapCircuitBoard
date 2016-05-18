package net.aegistudio.mcb.layout;

import net.aegistudio.mcb.AbstractCell;
import net.aegistudio.mcb.Data;
import net.aegistudio.mcb.unit.Unit;

public class LayoutUnitCell extends AbstractCell<LayoutGrid, Unit>{

	protected LayoutUnitCell(LayoutGrid grid, Unit component, int row, int column) {
		super(grid, row, column);
		super.component = component;
	}
	
	public <T extends Data> void setData(T data) {
		Data old = super.getData(Data.class);
		super.setData(data);
		if(data != old) super.getGrid()
			.update(getRow(), getColumn(), this);
	}
}
