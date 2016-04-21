package net.aegistudio.mcb.wire;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class BiInsulatedWire implements Wire {
	public static final BiInsulatedWire INSTANCES[] = new BiInsulatedWire[8]; static {
		new BiInsulatedWire(Facing.WEST, Facing.EAST, 0);
		new BiInsulatedWire(Facing.NORTH, Facing.EAST, 1);
		new BiInsulatedWire(Facing.NORTH, Facing.SOUTH, 2);
		new BiInsulatedWire(Facing.EAST, Facing.SOUTH, 3);
		new BiInsulatedWire(Facing.EAST, Facing.WEST, 4);
		new BiInsulatedWire(Facing.SOUTH, Facing.WEST, 5);
		new BiInsulatedWire(Facing.SOUTH, Facing.NORTH, 6);
		new BiInsulatedWire(Facing.WEST, Facing.NORTH, 7);
	}
	
	public final Facing sidea, sideb;
	public final int index;
	private BiInsulatedWire(Facing sidea, Facing sideb, int index) {
		this.sidea = sidea;
		this.sideb = sideb;
		this.index = index;
		INSTANCES[index] = this;
	}
	
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
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), 
				INSTANCES[(index + 1) % 8]);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(Color.GRAY);
		for(int i = 0; i < 4; i ++)
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(new Color(0.2f + 0.8f * cell
				.getLevel(sidea) / 32.0f, 0.0f, 0.0f));
		for(int i = 1; i <= 2; i ++)
			for(int j = 1; j <= 2; j ++)
				paintable.set(i, j);
		sidea.side((r, c, i) -> paintable.set(c, r), 1, 2, 3);
		sideb.side((r, c, i) -> paintable.set(c, r), 1, 2, 3);
	}

	@Override
	public short distance(Facing outdirection) {
		if(outdirection == sidea) return 1;
		if(outdirection == sideb) return 1;
		return Short.MAX_VALUE;
	}
}
