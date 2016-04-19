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

	public static final Color ACTIVATED = new Color(1.0f, 0.0f, 0.0f);
	public static final Color DEACTIVATED = new Color(0.2f, 0.0f, 0.0f);
	
	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(cell.getLevel(inputSide.opposite()) != 0? 
				ACTIVATED : DEACTIVATED);
		
		for(int i = 1; i <= 2; i ++) 
			for(int j = 1; j <= 2; j ++)
				paintable.set(i, j);
		
		paintable.color(Color.GRAY.darker());
		inputSide.side((i, j) -> paintable.set(i, j), 1, 2, 3);
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
