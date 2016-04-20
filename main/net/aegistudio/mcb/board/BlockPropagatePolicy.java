package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;

import net.aegistudio.mcb.Facing;

public class BlockPropagatePolicy implements PropagatePolicy {
	@Override
	public boolean in(Location location, Facing side, CircuitBoardCanvas canvas) {
		Block block = location.getBlock();
		if(block.getType() == Material.AIR) return false;
		
		int power = block.getBlockPower();
		Propagator propagate = new Propagator();
		propagate.setVoltage(2 * power);
		propagate.propagateVoltage(canvas.grid, side);
		return true;
	}

	@Override
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas) {
		Block block = location.getBlock();
		if(block.getType() == Material.AIR) return false;
		
		Propagator propagate = new Propagator();
		propagate.setVoltage(-1);
		propagate.retrieveVoltage(canvas.grid, face);
		if(propagate.getVoltage() < 0) return true;
		
		int newVoltage = propagate.getVoltage() / 2;
		
		if(block.getBlockPower() != newVoltage)
			canvas.plugin.getServer().getPluginManager().callEvent(
					new BlockRedstoneEvent(block, block.getBlockPower(), newVoltage > 0? 15 : 0));
		return true;
	}
}
