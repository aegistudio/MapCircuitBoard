package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Torch implements Unit {
	public static final Torch[] INSTANCES = new Torch[Facing.values().length];
	static {
		Facing.all(face -> new Torch(face));
	}
	private final Facing inputSide;
	private Torch(Facing facing) {
		this.inputSide = facing;
		INSTANCES[facing.ordinal()] = this;
	}
	
	@Override
	public void init(Cell cell) {
		
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {

	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		Facing newQuad = interaction.rightHanded? inputSide.nextQuad() : inputSide.previousQuad();
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), Torch.INSTANCES[newQuad.ordinal()]);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(cell.getLevel(inputSide.opposite()) != 0? 
				Color.YELLOW : Color.BLACK);
		
		for(int i = 0; i < 4; i ++) 
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(Color.GREEN);
		inputSide.side((i, j) -> paintable.set(i, j));
	}
	
	@Override
	public void tick(Cell cell) {
		Cell adjCell = cell.adjacence(inputSide);
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
