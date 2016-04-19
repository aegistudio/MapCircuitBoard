package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import net.aegistudio.mcb.Facing;

public class AirPropagatePolicy implements PropagatePolicy{
	@Override
	public void in(Location location, Facing side, CircuitBoardCanvas canvas) {
		Block block = location.getBlock();
		if(block.getType() != Material.AIR) return;
		
		new Propagator().propagateVoltage(canvas.grid, side);
	}

	@Override
	public void out(Location location, Facing face, CircuitBoardCanvas canvas) {
		// Do nothing.
	}
}
