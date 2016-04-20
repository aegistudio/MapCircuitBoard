package net.aegistudio.mcb.board;

import org.bukkit.Location;
import net.aegistudio.mcb.Facing;

public interface PropagatePolicy {
	public boolean in(Location location, Facing face, CircuitBoardCanvas canvas);
	
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas);
}
