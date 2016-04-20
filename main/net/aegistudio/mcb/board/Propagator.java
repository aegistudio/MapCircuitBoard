package net.aegistudio.mcb.board;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.unit.Pin;

public class Propagator {
	public final int[] voltage = new int[32];
	
	public void propagateVoltage(Grid grid, Facing side) {
		side.side((r, c, i) -> {
			Cell cell = grid.getCell(r, c);
			if(cell == null) return;
			if(cell.getComponent() instanceof Pin)
				((Pin)cell.getComponent()).setPinVoltage(cell, voltage[i]);
		}, 1, 30, 31);
	}
	
	public void retrieveVoltage(Grid grid, Facing side) {
		side.side((r, c, i) -> {
			Cell cell = grid.getCell(r, c);
			if(cell == null) return;
			if(cell.getComponent() instanceof Pin)
				voltage[i] = ((Pin)cell.getComponent()).getPinVoltage(cell);
		}, 1, 30, 31);
	}
	
	public void setVoltage(int voltage) {
		for(int i = 0; i < 32; i ++) this.voltage[i] = voltage;
	}
	
	public int getVoltage() {
		int max = -1;
		for(int i = 0; i < 32; i ++) 
			if(this.voltage[i] > max) max = this.voltage[i];
		return max;
	}
}
