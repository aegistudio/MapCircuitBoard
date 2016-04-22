package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class OriginatorPin implements Pin {
	public static final Pin INSTANCE = new OriginatorPin();
	private OriginatorPin() {}
	
	@Override
	public void tick(Cell cell) {
		Voltage voltage = cell.getData(Voltage.class);
		Facing.all(f -> cell.setLevel(f, voltage.voltage));
	}

	@Override
	public void init(Cell cell) {
		cell.setData(new Voltage());
	}

	@Override
	public void load(Cell cell, InputStream inputStream) throws Exception {
		cell.setData(Voltage.load(inputStream));
	}

	@Override
	public void save(Cell cell, OutputStream outputStream) throws Exception {
		cell.getData(Voltage.class).save(outputStream);
	}

	@Override
	public void interact(Cell cell, Interaction interaction) {
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), MonitorPin.INSTANCE);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(new Color(0.2f + 0.8f * cell.getData(Voltage.class).voltage / 32, 0, 0));
		paintable.set(1, 1);		paintable.set(1, 2);
		paintable.set(2, 1);		paintable.set(2, 2);
		
		paintable.color(Color.YELLOW.darker());
		for(int i = 0; i <= 3; i ++) {
			paintable.set(i, 0);
			paintable.set(i, 3);
			paintable.set(0, i);
			paintable.set(3, i);
		}
	}

	@Override
	public void setPinVoltage(Cell cell, int voltage) {
		cell.getData(Voltage.class).voltage = voltage;
	}

	@Override
	public int getPinVoltage(Cell cell) {
		return -1;
	}
}
