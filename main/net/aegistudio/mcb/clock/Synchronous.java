package net.aegistudio.mcb.clock;

import net.aegistudio.mcb.MapCircuitBoard;

public class Synchronous implements Clocking {
	int task = -1;
	@Override
	public void start(MapCircuitBoard plugin) {
		task = plugin.getServer().getScheduler()
			.scheduleSyncRepeatingTask(plugin, () -> plugin.clock(), 1, 1);
	}

	@Override
	public void stop(MapCircuitBoard plugin) {
		if(task >= 0) plugin.getServer().getScheduler().cancelTask(task);
	}
}
