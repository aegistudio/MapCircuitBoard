package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.entity.ItemFrame;

/**
 * Cell is inside a grid, either a prototype / scheme
 * or a actual implementing circuit.
 * 
 * It is regarded as the mediator between circuit cells.
 * 
 * @author aegistudio
 */

public interface Cell {
	/**
	 * At which row is this cell placed.
	 */
	public int getRow();
	
	/**
	 * At which column is this cell placed.
	 */
	public int getColumn();
	
	/**
	 * This cell is inside which grid.
	 */
	public Grid getGrid();
	
	/**
	 * The inner component of this cell.
	 */
	public Component getComponent();
	
	/**
	 * The voltage level of this cell.
	 */
	public int getLevel(Facing port);
	
	/**
	 * Set the voltage level of this cell.
	 * @param port
	 */
	public void setLevel(Facing port, int level);
	
	/**
	 * @param direction
	 * @return
	 */
	public Cell adjacence(Facing port);
	
	/**
	 * The stored data for the component.
	 */
	public <T extends Data> T getData(Class<T> dataClazz);
	
	/**
	 * @param data the data to set.
	 */
	public <T extends Data> void setData(T data);
	
	/**
	 * @param input
	 */
	public void load(InputStream input, ComponentFactory table) throws Exception;
	
	/**
	 * @param output
	 */
	public void save(OutputStream output, ComponentFactory table) throws Exception;
	
	/**
	 * When the cell ticks.
	 */
	public void tick(ItemFrame location);
}
