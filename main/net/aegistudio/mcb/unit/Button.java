package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Binary;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Button implements Unit {
	public static final Button INSTANCE = new Button();
	private Button() {}
	
	@Override
	public void init(Cell cell) {
		cell.setData(Binary.FALSE);
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(Binary.load(inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(Binary.class).save(outputStream);
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		cell.setData(cell.getData(Binary.class).not());
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		boolean powered = cell.getData(Binary.class).booleanValue;
		paintable.color(Color.BLACK);
		for(int i = 0; i < 4; i ++) 
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(powered? Color.GREEN : Color.RED);
		for(int i = 1; i <= 2; i ++) 
			for(int j = 1; j <= 2; j ++)
				paintable.set(i, j);
	}
	
	@Override
	public void tick(ItemFrame frame, Cell cell) {
		boolean powered = cell.getData(Binary.class).booleanValue;
		Facing.all(face -> cell.setLevel(face, powered? 32 : 0));
		if(powered) cell.setData(Binary.FALSE);
	}
}
