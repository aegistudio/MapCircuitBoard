package net.aegistudio.mcb.board;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Facing;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class BoardPropagatePolicy implements PropagatePolicy {

	@Override
	public boolean in(Location location, Facing face, CircuitBoardCanvas canvas) {
		Collection<Entity> collection = location.getWorld().getNearbyEntities(location, 0.3, 0.3, 0.3);
		
		for(Entity entity : collection) {
			if(entity.getType() != EntityType.ITEM_FRAME) continue;
			ItemFrame frame = (ItemFrame) entity;
			if(frame.getItem().getType() != Material.MAP) continue;
			PluginCanvasRegistry<CircuitBoardCanvas> circuitBoardRegistry = canvas.plugin.canvasService.get(canvas.plugin, "redstone", 
					frame.getItem().getDurability(), CircuitBoardCanvas.class);
			if(circuitBoardRegistry == null) continue;
			if(circuitBoardRegistry.canvas().grid == null) continue;
			
			Propagator propagator = new Propagator();
			propagator.retrieveVoltage(circuitBoardRegistry.canvas().grid, face.opposite());
			propagator.propagateVoltage(canvas.grid, face);
			return true;
		};
		return false;
	}

	@Override
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas) {
		return false;
	}

}
