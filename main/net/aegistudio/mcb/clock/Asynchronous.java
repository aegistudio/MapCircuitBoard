package net.aegistudio.mcb.clock;

import net.aegistudio.mcb.MapCircuitBoard;

public class Asynchronous implements Clocking {
	int task = -1;
	
	@SuppressWarnings("deprecation")
	@Override
	public void start(MapCircuitBoard plugin) {
		task = plugin.getServer().getScheduler()
			.scheduleAsyncRepeatingTask(plugin, () -> plugin.clock(), 1, 0);
	}

	@Override
	public void stop(MapCircuitBoard plugin) {
		if(task >= 0) plugin.getServer().getScheduler().cancelTask(task);
	}
}
