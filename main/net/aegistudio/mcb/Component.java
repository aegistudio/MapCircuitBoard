package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.algo.Paintable;

/**
 * Component defines a 4x4 area on the map.
 * 
 * @author aegistudio
 */

public interface Component {
	/**
	 * Invoked when the component added to the grid.
	 * 
	 * @param cell the cell
	 */
	public void init(Cell cell);
	
	/**
	 * Invoked when there's a need to load data.
	 * 
	 * @param cell the cell
	 * @param inputStream the input data stream.
	 * @throws Exception 
	 */
	public void load(Cell cell, InputStream inputStream) throws Exception;
	
	/**
	 * Invoked when there's a need to save data.
	 * 
	 * @param cell the cell
	 * @param outputStream the output data stream.
	 */
	public void save(Cell cell, OutputStream outputStream) throws Exception;
	
	/**
	 * Invoked when player interact with the cell.
	 * 
	 * @param cell the cell
	 * @param interaction the interaction
	 */
	public void interact(Cell cell, Interaction interaction);
	
	/**
	 * Invoked when there's need to display the component.
	 * 
	 * @param cell
	 * @param paintable
	 */
	public void paint(Cell cell, Paintable paintable);
}
