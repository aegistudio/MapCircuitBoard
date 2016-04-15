package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

public class Air implements Component {
	public static final Air INSTANCE = new Air();
	
	private Air() {}
	
	@Override
	public void init(Cell cell) {	cell.setData(null);		}

	@Override
	public void load(Cell cell, InputStream inputStream) {			}

	@Override
	public void save(Cell cell, OutputStream outputStream) {			}

	@Override
	public void interact(Cell cell, Interaction interaction) {			}

	@Override
	public void paint(Cell cell, Paintable paintable) {		}
}
