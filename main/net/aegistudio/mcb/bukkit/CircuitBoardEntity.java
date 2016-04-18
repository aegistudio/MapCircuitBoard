package net.aegistudio.mcb.bukkit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class CircuitBoardEntity implements Listener {
	private final MapCircuitBoard plugin;
	public CircuitBoardEntity(MapCircuitBoard plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onSpawnItem(ItemSpawnEvent e) {
		ItemStack item = e.getEntity().getItemStack();
		if(item.getType() != Material.MAP) return;
		short mapid = item.getDurability();
		PluginCanvasRegistry<CircuitBoardCanvas> canvas = 
				plugin.canvasService.get(plugin, "redstone", mapid, CircuitBoardCanvas.class);
		if(canvas != null) {
			plugin.circuitBoardItem.make(item, canvas.canvas().referred);
			e.getEntity().getNearbyEntities(.5, .5, .5)
				.forEach((entity) -> {
					if(entity.getType() == EntityType.DROPPED_ITEM) {
						if(((Item) entity).getItemStack().getType() == Material.ITEM_FRAME)
							entity.remove();
					}
					else if(entity.getType() == EntityType.ITEM_FRAME) {
						ItemStack mapitem = ((ItemFrame) entity).getItem();
						if(mapitem.getType() == Material.MAP)
							if(mapitem.getDurability() == mapid)
								entity.remove();
					}
				});
		}
	}
}
