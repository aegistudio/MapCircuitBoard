package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Repeater implements Unit {
	public static final Repeater[] INSTANCES = new Repeater[Facing.values().length];
	static {
		Facing.all(face -> new Repeater(face));
	}
	private final Facing inputSide;
	private Repeater(Facing facing) {
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
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), Repeater.INSTANCES[newQuad.ordinal()]);
	}

	public static final Color ACTIVATED = new Color(1.0f, 0.0f, 0.0f);
	public static final Color DEACTIVATED = new Color(0.2f, 0.0f, 0.0f);
	
	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(Color.GRAY);
		for(int i = 0; i < 4; i ++)
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		

		Cell adjCell = cell.adjacence(inputSide);
		int level = 0;
		if(adjCell != null)
			level = adjCell.getLevel(inputSide.opposite());
		
		paintable.color(new Color(0.2f + 0.8f * level / 32, 0, 0));
		
		inputSide.side((i, j) -> paintable.set(i, j), 0, 3, 3);
		inputSide.side((i, j) -> paintable.set(i + 1, j + 1), 0, 1, 1);
		
		paintable.color(cell.getLevel(inputSide.opposite()) != 0? 
				ACTIVATED : DEACTIVATED);
		inputSide.opposite().side((i, j) -> paintable.set(i, j), 1, 2, 3);
	}
	
	@Override
	public void tick(Cell cell) {
		Cell adjCell = cell.adjacence(inputSide);
		int level = 0;
		if(adjCell != null) 
			level = adjCell.getLevel(inputSide.opposite());
	
		for(Facing face : Facing.values()) {
			if(face == inputSide.opposite())
				cell.setLevel(face, level != 0? 32 : 0);
			else cell.setLevel(face, 0);
		}
	}
}
