package net.aegistudio.mcb.board;

import org.bukkit.Location;
import net.aegistudio.mcb.Facing;

public interface PropagatePolicy {
	public void in(Location location, Facing face, CircuitBoardCanvas canvas);
	
	public void out(Location location, Facing face, CircuitBoardCanvas canvas);
}
