package net.aegistudio.mcb.bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Grid;

public class ComponentPlaceListener {
	private final Map<Material, List<ComponentPlacer>> placers
		= new TreeMap<Material, List<ComponentPlacer>>();
	private final Map<Component, ComponentPlacer> reverseMap
		= new HashMap<Component, ComponentPlacer>();
	
	public void add(ComponentPlacer placer) {
		List<ComponentPlacer> list = this.placers.get(placer.type);
		if(list == null) this.placers.put(placer.type, 
				list = new ArrayList<ComponentPlacer>());
		list.add(placer);
		reverseMap.put(placer.component, placer);
	}
	
	public void interact(Grid grid, int row, int column, Player player) {
		Component previousComponent = null;
		Cell previous = grid.getCell(row, column);
		if(previous != null) previousComponent = previous.getComponent();
		
		@SuppressWarnings("deprecation")
		ItemStack item = player.getItemInHand();
		if(item != null) {
			List<ComponentPlacer> suspicious = placers.get(item.getType());
			if(suspicious != null) for(ComponentPlacer placer : suspicious)
				if(placer.matches(item)) {
					if(previousComponent == placer.component) return;
					if(previousComponent != null)
						reverseMap.get(previousComponent).repay(player);
					
					placer.place(grid, player, row, column);
					grid.setCell(row, column, placer.component);
					return;
				}
		}
	}
}
