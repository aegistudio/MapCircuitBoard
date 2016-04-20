package net.aegistudio.mcb.board;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockRedstoneEvent;

import net.aegistudio.mcb.MapCircuitBoard;

public class TorchPowerListener extends PowerUpdateListener {
	public TorchPowerListener(MapCircuitBoard board) {
		super(board);
	}

	@EventHandler
	public void onRedstoneTorch(BlockRedstoneEvent event) {
		Material material = event.getBlock().getType();
		if(material != Material.REDSTONE_TORCH_OFF)
			return;
		if(event.getNewCurrent() == 0) return;
		
		updateState(event);
	}
}
