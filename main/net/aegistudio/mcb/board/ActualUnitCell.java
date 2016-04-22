package net.aegistudio.mcb.board;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.AbstractCell;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Data;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.layout.LayoutUnitCell;
import net.aegistudio.mcb.unit.Unit;

public class ActualUnitCell extends AbstractCell<ActualGrid, Unit> {
	public final int[] bufferedLevel = new int[Facing.values().length];
	protected ActualUnitCell(ActualGrid grid, LayoutUnitCell cell) {
		super(grid, cell.getRow(), cell.getColumn());
		super.component = cell.getComponent();
		
		Data data = cell.getData(Data.class);
		if(data != null) this.setData(data.duplicate());
		else this.setData(null);
	}
	
	public void load(InputStream inputStream, ComponentFactory factory) throws Exception {
		component.load(this, inputStream);
		DataInputStream din = new DataInputStream(inputStream);
		for(Facing f : Facing.values()) super.setLevel(f, din.readInt());
	}
	
	public void save(OutputStream outputStream, ComponentFactory factory) throws Exception {
		component.save(this, outputStream);
		DataOutputStream dout = new DataOutputStream(outputStream);
		for(Facing f : Facing.values()) dout.writeInt(super.getLevel(f));
	}
	
	public int getLevel(Facing face) {
		Cell cell = super.adjacence(face);
		if(cell == null) return super.getLevel(face);
		if(cell instanceof ActualUnitCell) return bufferedLevel[face.ordinal()];
		return super.getLevel(face);
	}
	
	public void backupLevel() {
		System.arraycopy(level, 0, bufferedLevel, 0, Facing.values().length);
	}
	
	public void tick() {
		super.component.tick(this);
	}
}
