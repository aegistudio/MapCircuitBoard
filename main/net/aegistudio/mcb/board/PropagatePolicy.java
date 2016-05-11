package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.TickableBoard;

public interface PropagatePolicy {
	public boolean in(Location location, Facing face, TickableBoard canvas, ItemFrame frame);
	
	public boolean out(Location location, Facing face, TickableBoard canvas, ItemFrame frame);
}
