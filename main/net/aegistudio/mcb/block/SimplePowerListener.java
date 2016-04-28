package net.aegistudio.mcb.block;

import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockRedstoneEvent;

import net.aegistudio.mcb.MapCircuitBoard;

public class SimplePowerListener extends PowerUpdateListener {
	private final Material guardMaterial;
	private final Predicate<Integer> guardCurrent;
	public SimplePowerListener(Material guardMaterial, 
			Predicate<Integer> guardCurrent, MapCircuitBoard board) {
		
		super(board);
		this.guardMaterial = guardMaterial;
		this.guardCurrent = guardCurrent;
	}

	@EventHandler
	public void onRedstoneTorch(BlockRedstoneEvent event) {
		Material material = event.getBlock().getType();
		if(material != guardMaterial) return;
		if(guardCurrent.test(event.getNewCurrent())) 
			updateState(event);
	}
}
