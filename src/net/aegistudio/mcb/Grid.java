package net.aegistudio.mcb;

import java.io.InputStream;

import net.aegistudio.mpp.algo.Paintable;

public interface Grid {
	public Cell getCell(int row, int column);
	
	public void setCell(int row, int column, Component component);
	
	public void load(InputStream inputStream) throws Exception;

	public void paint(Paintable paintable);
	
	public void save(InputStream outputStream) throws Exception;
}
