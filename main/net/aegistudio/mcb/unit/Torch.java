package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Torch implements Unit {
	public static final Torch INSTANCE = new Torch();
	private Torch() {}
	
	@Override
	public void init(Cell cell) {
		cell.setData(Facing.NORTH);
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(Facing.load(inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(Facing.class).save(outputStream);
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		cell.setData(cell.getData(Facing.class).nextQuad());
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		Facing inputSide = cell.getData(Facing.class);
		
		paintable.color(cell.getLevel(inputSide.opposite()) != 0? 
				Color.YELLOW : Color.BLACK);
		
		for(int i = 0; i < 4; i ++) 
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(Color.GREEN);
		inputSide.side((i, j)->paintable.set(i, j));
	}
	
	@Override
	public void tick(Cell cell) {
		Cell adjCell = cell.adjacence(cell.getData(Facing.class));
		Facing inputSide = cell.getData(Facing.class);
		int level = 0;
		if(adjCell != null) 
			level = adjCell.getLevel(inputSide.opposite());
	
		for(Facing face : Facing.values()) {
			if(face == inputSide)
				cell.setLevel(inputSide, 0);
			else cell.setLevel(face, level != 0? 0 : 32);
		}
	}
}
