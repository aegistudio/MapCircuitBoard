package net.aegistudio.mcb.layout;

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
	
	public void unplace(Grid grid, int row, int column, Player player) {
		Cell previous = grid.getCell(row, column);
		if(previous == null) return;
		Component previousComponent = previous.getComponent();
		
		if(reverseMap.get(previousComponent).unplace(grid, player, row, column))
			grid.setCell(row, column, null);
	}
	
	public void place(Grid grid, int row, int column, Player player) {
		Cell previous = grid.getCell(row, column);
		if(previous != null) return;
		
		@SuppressWarnings("deprecation")
		ItemStack item = player.getItemInHand();
		if(item != null) {
			List<ComponentPlacer> suspicious = placers.get(item.getType());
			if(suspicious != null)
				for(ComponentPlacer placer : suspicious)
					if(placer.matches(item)) {
						if(placer.place(grid, player, row, column))
							grid.setCell(row, column, placer.component);
						return ;
					}
		}
	}
}
