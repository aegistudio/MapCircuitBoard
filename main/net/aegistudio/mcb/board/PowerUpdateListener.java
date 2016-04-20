package net.aegistudio.mcb.board;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.metadata.MetadataValue;

import net.aegistudio.mcb.MapCircuitBoard;

public class PowerUpdateListener implements Listener {
	public final MapCircuitBoard board;
	public PowerUpdateListener(MapCircuitBoard board) {
		this.board = board;
	}
	
	protected void updateState(BlockRedstoneEvent event) {
		MetadataValue value = null;
		for(MetadataValue current : event.getBlock()
				.getMetadata(BlockPropagatePolicy.REDSTONE_STATE))
			
			if(current.getOwningPlugin().equals(board)) {
				value = current;
				break;
			}
		
		if(value != null) {
			event.setNewCurrent(value.asInt());
			event.getBlock().removeMetadata(
					BlockPropagatePolicy.REDSTONE_STATE, board);
		}
	}
}
