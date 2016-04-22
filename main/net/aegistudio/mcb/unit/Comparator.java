package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Comparator implements Unit {
	public static final Comparator[][] INSTANCES = new Comparator[Facing.values().length][2]; static {
		Facing.all(f -> new Comparator(f, false));
		Facing.all(f -> new Comparator(f, true));
	}
	
	Facing inputSide;
	boolean subtractive;
	public Comparator(Facing inputSide, boolean subtractive) {
		this.inputSide = inputSide;
		this.subtractive = subtractive;
		INSTANCES[inputSide.ordinal()][subtractive? 1 : 0] = this;
	}
	
	@Override
	public void init(Cell cell) {			}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {			}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {			}

	@SuppressWarnings("deprecation")
	@Override
	public void interact(Cell cell, Interaction interaction) {
		boolean toggle = false;
		if(interaction.sender instanceof Player) {
			toggle = ((Player) interaction.sender).getItemInHand()
					.getType() == Material.REDSTONE_COMPARATOR;
		}
		
		Comparator newComparator;
		if(toggle)
			newComparator = INSTANCES[inputSide.ordinal()][subtractive? 0 : 1];
		else 
			newComparator = INSTANCES[inputSide.nextQuad().ordinal()][subtractive? 1 : 0];
		
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), newComparator);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(Color.GRAY);
		for(int i = 0; i < 4; i ++)
			for(int j = 0; j < 4; j ++)
				paintable.set(i, j);
		
		paintable.color(new Color(level(getInputLevel(cell, inputSide)), 0, 0));
		inputSide.side((x, y) -> paintable.set(x, y), 0, 3, 3);
		inputSide.side((x, y) -> paintable.set(x + 1, y + 1), 0, 1, 1);
		
		paintable.color(new Color(0, 0, level(getInputLevel(cell, inputSide.previousQuad()))));
		inputSide.previousQuad().side((x, y) -> paintable.set(x, y), 1, 2, 3);
		
		paintable.color(new Color(0, 0, level(getInputLevel(cell, inputSide.nextQuad()))));
		inputSide.nextQuad().side((x, y) -> paintable.set(x, y), 1, 2, 3);
		
		paintable.color(new Color(subtractive? 1.0f : 0.4f, subtractive? 1.0f : 0.4f, 0));
		inputSide.opposite().side((x, y) -> paintable.set(x + 1, y + 1), 0, 1, 1);
		
		paintable.color(new Color(level(cell.getLevel(inputSide.opposite())), 0, 0));
		inputSide.opposite().side((x, y) -> paintable.set(x, y), 1, 2, 3);
	}

	public int getInputLevel(Cell cell, Facing facing) {
		Cell adjcell = cell.adjacence(facing);
		if(adjcell == null) return 0;
		return adjcell.getLevel(facing.opposite());
	}
	
	private float level(int level) {
		return 0.2f + 0.8f * level / 32;
	}
	
	@Override
	public void tick(Cell cell) {
		int aLevel = getInputLevel(cell, inputSide);
		int bLevel = Math.max(getInputLevel(cell, inputSide.previousQuad()), 
				getInputLevel(cell, inputSide.nextQuad()));
		cell.setLevel(inputSide.opposite(), subtractive? 
				Math.max(aLevel - bLevel, 0) : (aLevel >= bLevel? aLevel : 0));
	}
}
