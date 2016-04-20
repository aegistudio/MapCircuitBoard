package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Facing;

public class AirPropagatePolicy implements PropagatePolicy{
	@Override
	public boolean in(Location location, Facing side, CircuitBoardCanvas canvas, ItemFrame frame) {
		Block block = location.getBlock();
		if(block.getType() != Material.AIR) return false;
		
		new Propagator().propagateVoltage(canvas.grid, side);
		return true;
	}

	@Override
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas, ItemFrame frame) {
		// Do nothing.
		Block block = location.getBlock();
		if(block.getType() != Material.AIR) return false;
		
		return true;
	}
}
