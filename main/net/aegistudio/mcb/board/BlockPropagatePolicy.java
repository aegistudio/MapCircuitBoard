package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import net.aegistudio.mcb.Facing;

public class BlockPropagatePolicy implements PropagatePolicy {
	@Override
	public void in(Location location, Facing side, CircuitBoardCanvas canvas) {
		Block block = location.getBlock();
		if(block.getType() == Material.AIR) return;
		
		int power = block.getBlockPower();
		Propagator propagate = new Propagator();
		propagate.setVoltage(2 * power);
		propagate.propagateVoltage(canvas.grid, side);
	}

	@Override
	public void out(Location location, Facing face, CircuitBoardCanvas canvas) {
		// Not yet implemented
	}
}
