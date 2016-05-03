package net.aegistudio.mcb.unit;

import org.bukkit.Server;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mpp.Interaction;

/**
 * Called when the player interact with a command block
 * component.
 * 
 * @author aegistudio
 */

public interface CommandBlockEditor {
	public Server getServer();
	
	public void edit(Interaction interaction, CommandBlockData data, Cell cell);
	
	public void execute(ItemFrame frame, CommandBlockData data, Cell cell);
}
