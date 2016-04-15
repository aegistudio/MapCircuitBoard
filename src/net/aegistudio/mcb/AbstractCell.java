package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractCell<G extends Grid, C extends Component> implements Cell {
	private final int row;		public @Override int getRow() {	return row;	}
	private final int column;	public @Override int getColumn() {	return column;	}
	private final G grid;		public @Override G getGrid() { return grid; }
	
	protected int[] level = new int[Facing.values().length];
	public @Override int getLevel(Facing port) {		return level[port.ordinal()];		}
	public @Override void setLevel(Facing port, int level) {	this.level[port.ordinal()] = level;		}
	
	protected AbstractCell(G grid, int row, int column) {
		this.grid = grid;
		this.row = row;
		this.column = column;
	}

	protected C component;
	public @Override C getComponent() {	return component;	}
	protected void setComponent(C component) {
		if(this.component != component) {
			this.component = component;
			this.component.init(this);
		}
	}
	
	@Override
	public Cell adjacence(Facing port) {	return port.call(row, column, (r, c) -> grid.getCell(r, c));	}

	private Cloneable data;
	@SuppressWarnings("unchecked")
	public @Override <T extends Cloneable> T getData(Class<T> dataClazz) {	return (T)data;	}
	public @Override <T extends Cloneable> void setData(T data) {	this.data = data;	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(InputStream input) throws Exception {
		this.component = (C) ComponentFactory.get(input.read());
		this.component.load(this, input);
	}

	@Override
	public void save(OutputStream output) throws Exception {
		output.write(ComponentFactory.id(component));
		component.save(this, output);
	}
	
	public void tick() {
		// Don't tick by default.
	}
}
