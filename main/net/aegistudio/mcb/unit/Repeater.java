package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Repeater implements Unit {
	public static final Repeater[][] INSTANCES = new Repeater[Facing.values().length][4];
	static {
		Facing.all(face -> new Repeater(face, 0, 0x01, 0x01));
		Facing.all(face -> new Repeater(face, 1, 0x06, 0x07));
		Facing.all(face -> new Repeater(face, 2, 0x28, 0x2f));
		Facing.all(face -> new Repeater(face, 3, 0xf0, 0xff));
	}
	private final Facing inputSide;
	private final int queueMask, tick, powerMask;
	private Repeater(Facing facing, int tick, int stateMask, int dataMask) {
		this.inputSide = facing;
		this.tick = tick;
		this.queueMask = dataMask;
		this.powerMask = stateMask;
		INSTANCES[facing.ordinal()][tick] = this;
	}
	
	@Override
	public void init(Cell cell) {
		cell.setData(new RepeaterQueue());
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(RepeaterQueue.read(inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(RepeaterQueue.class).save(outputStream);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void interact(Cell cell, Interaction interaction) {
		boolean tickChange = false;
		if(interaction.sender instanceof Player) {
			tickChange = ((Player)interaction.sender)
					.getItemInHand().getType() == Material.DIODE;
		}
		
		Component newComponent;
		if(tickChange)
			newComponent = Repeater.INSTANCES[inputSide.ordinal()][(tick + 1) % 4];
		else {
			Facing newQuad = interaction.rightHanded? inputSide.nextQuad() : inputSide.previousQuad();
			newComponent = Repeater.INSTANCES[newQuad.ordinal()][tick];
		}
		
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), newComponent);
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
		
		paintable.color(cell.getData(RepeaterQueue.class)
				.powered(powerMask)? ACTIVATED : DEACTIVATED);
		inputSide.opposite().side((i, j) -> paintable.set(i, j), 1, 2, 3);
	}
	
	@Override
	public void tick(Cell cell) {
		RepeaterQueue data = cell.getData(RepeaterQueue.class);

		Cell adjCell = cell.adjacence(inputSide);
		int level = 0;
		if(adjCell != null) 
			level = adjCell.getLevel(inputSide.opposite());
		data.enqueue(level != 0, queueMask);
		
		for(Facing face : Facing.values()) {
			if(face == inputSide.opposite())
				cell.setLevel(face, data.powered(powerMask)? 32 : 0);
			else cell.setLevel(face, 0);
		}
	}
}
