package net.aegistudio.mcb;

import org.bukkit.entity.ItemFrame;

public interface TickableBoard {
	public ItemFrame getFrame();
	
	public Grid getGrid();
	
	public void whereami();
	
	public void propagateIn();
	
	public void clockTick();
	
	public void propagateOut();
}
