package net.aegistudio.mcb.unit;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class CommandBlock implements Unit {
	public final MapCircuitBoard plugin;
	public CommandBlock(MapCircuitBoard plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void init(Cell cell) {
		cell.setData(new CommandBlockData("", false));
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(CommandBlockData.read(inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(CommandBlockData.class).write(outputStream);
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		if(cell.getData(CommandBlockData.class).nonTick) {
			// Show ui.
		}
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		
	}

	@Override
	public void tick(Cell cell) {
		cell.getData(CommandBlockData.class).nonTick = false;
	}
}
