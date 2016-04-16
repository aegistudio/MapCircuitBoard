package net.aegistudio.mcb.wire;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class FullDirectionalWire implements Wire {
	public static final FullDirectionalWire INSTANCE = new FullDirectionalWire();
	private FullDirectionalWire() {	}
	
	@Override
	public void init(Cell cell) {
		cell.setData(null);
	}

	@Override
	public void load(Cell cell, InputStream inputStream) {
		
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) {
		
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(new Color(0.2f + 0.8f * cell
				.getLevel(Facing.NORTH) / 32.0f, 0.0f, 0.0f));
		
		paintable.set(1, 1);		paintable.set(1, 2);
		paintable.set(2, 1);		paintable.set(2, 2);
		
		if(cell.adjacence(Facing.NORTH) != null) {
			paintable.set(1, 3);
			paintable.set(2, 3);
		}
		
		if(cell.adjacence(Facing.SOUTH) != null) {
			paintable.set(1, 0);
			paintable.set(2, 0);
		}
		
		if(cell.adjacence(Facing.EAST) != null) {
			paintable.set(3, 1);
			paintable.set(3, 2);
		}
		
		if(cell.adjacence(Facing.WEST) != null) {
			paintable.set(0, 1);
			paintable.set(0, 2);
		}
	}

	@Override
	public short distance(Facing outdirection) {
		return 1;
	}

}
