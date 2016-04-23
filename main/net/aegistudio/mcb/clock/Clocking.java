package net.aegistudio.mcb.clock;

import net.aegistudio.mcb.MapCircuitBoard;

public interface Clocking {
	public void start(MapCircuitBoard plugin);
	
	public void stop(MapCircuitBoard plugin);
}
