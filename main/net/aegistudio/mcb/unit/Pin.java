package net.aegistudio.mcb.unit;

import net.aegistudio.mcb.Cell;

/**
 * Allow user to propagate circuits to nearby blocks.
 * There're two states of pin:
 * <li>Monitor: listen for nearby redstone signals.
 * <li>Originator: listen for nearby originate signals.
 * 
 * @author aegistudio
 */

public interface Pin extends Unit {
	public void setPinVoltage(Cell cell, int voltage);
	
	public int getPinVoltage(Cell cell);
}
