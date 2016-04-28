package net.aegistudio.mcb.block;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockRedstoneEvent;

import net.aegistudio.mcb.MapCircuitBoard;

public class LampPowerListener extends PowerUpdateListener {
	public LampPowerListener(MapCircuitBoard board) {
		super(board);
	}

	@EventHandler
	public void onRedstoneLamp(BlockRedstoneEvent event) {
		Material material = event.getBlock().getType();
		if(material != Material.REDSTONE_LAMP_ON)
			return;
		if(event.getNewCurrent() != 0) return;
		
		updateState(event);
	}
}
