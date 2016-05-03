package net.aegistudio.mcb.unit;

import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Component;

public interface Unit extends Component {
	
	/**
	 * Invoked when the map ticks (async with world tick! notice!).
	 * 
	 * @param cell
	 */
	public void tick(ItemFrame frame, Cell cell);
}
