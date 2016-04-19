package net.aegistudio.mcb.unit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class MonitorPin implements Pin {
	public static final Pin INSTANCE = new MonitorPin();
	private MonitorPin() {}
	
	@Override
	public void tick(Cell cell) {
		Voltage voltage = cell.getData(Voltage.class);
		voltage.voltage = 0;
		Facing.all(f -> { 
			Cell adj = cell.adjacence(f);
			if(adj == null) return;
			if(voltage.voltage < adj.getLevel(f.opposite())) {
				voltage.voltage = adj.getLevel(f.opposite());
			}
		});
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
		cell.getGrid().setCell(cell.getRow(), cell.getColumn(), OriginatorPin.INSTANCE);
	}

	@Override
	public void paint(Cell cell, Paintable paintable) {
		paintable.color(Color.GRAY.darker());
		for(int i = 0; i <= 3; i ++) {
			paintable.set(i, 0);
			paintable.set(i, 3);
			paintable.set(0, i);
			paintable.set(3, i);
		}
	}

	@Override
	public void setPinVoltage(Cell cell, int voltage) {
		// monitor only.
	}

	@Override
	public int getPinVoltage(Cell cell) {
		return cell.getData(Voltage.class).voltage;
	}
}
