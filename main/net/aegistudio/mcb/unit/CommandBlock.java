package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class CommandBlock implements Unit {
	public final CommandBlockEditor editor;
	public CommandBlock(CommandBlockEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void init(Cell cell) {
		cell.setData(new CommandBlockData(editor));
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(CommandBlockData.read(editor, inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(CommandBlockData.class).write(outputStream);
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		editor.edit(interaction, cell.getData(CommandBlockData.class), cell);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(Color.GRAY);
		for(int i = 0; i < 4; i ++)
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(Color.GREEN.darker());
		paintable.set(1, 2); paintable.set(2, 1);
		
		paintable.color(Color.RED.darker());
		paintable.set(1, 1); paintable.set(2, 2);
	}

	@Override
	public void tick(ItemFrame frame, Cell cell) {
		cell.getData(CommandBlockData.class).nonTick = false;
		
		boolean isPowered = false;
		for(Facing port : Facing.values()) {
			Cell adjacent = cell.adjacence(port.opposite());
			if(adjacent == null) continue;
			if(adjacent.getLevel(port) > 0) {
				isPowered = true;
				break;
			}
		}
		
		if(!cell.getData(CommandBlockData.class).lastInputState && isPowered)
			editor.execute(frame, cell.getData(CommandBlockData.class), cell);
		cell.getData(CommandBlockData.class).lastInputState = isPowered;
		
		Facing.all(port -> {
			Cell adjacent = cell.adjacence(port);
			if(adjacent == null || !(adjacent.getComponent() instanceof Comparator)) {
				cell.setLevel(port, 0);
				return;
			}
			cell.setLevel(port, cell.getData(CommandBlockData.class)
					.lastOutputState? 32 : 0);
		});
	}
}
