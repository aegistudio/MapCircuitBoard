package net.aegistudio.mcb.board;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.AbstractCell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.layout.LayoutWireCell;
import net.aegistudio.mcb.wire.Wire;

public class ActualWireCell extends AbstractCell<ActualGrid, Wire> {
	protected LayoutWireCell wireCell;
	public ActualWireCell(ActualGrid grid, LayoutWireCell wire) {
		super(grid, wire.getRow(), wire.getColumn());
		this.wireCell = wire;
		super.component = this.wireCell.getComponent();
	}
	
	// Using lazy evalutaion, otherwise unable to emulate massive circuit!
	public int getLevel(Facing f) {
		if(super.getLevel(f) >= Short.MAX_VALUE) {
			super.setLevel(f, 0);
			wireCell.allReachablePort((cell, face) -> {
				ActualUnitCell unit = (ActualUnitCell) getGrid().getCell(cell.getRow(), cell.getColumn());
				int currentLevel = Math.max(0, unit.getLevel(face) - 
						wireCell.getDistance(cell, face) - component.distance(f));
				if(super.getLevel(f) < currentLevel) super.setLevel(f, currentLevel);
			});
		}
		return super.getLevel(f);
	}
	
	public void load(InputStream inputStream) throws Exception {	}
	
	public void save(OutputStream outputStream) throws Exception {	}
	
	public void tick() {
		Facing.all((face)->super.setLevel(face, Short.MAX_VALUE));
	}
}
