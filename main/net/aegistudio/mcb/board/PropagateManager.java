package net.aegistudio.mcb.board;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.TickableBoard;
import net.aegistudio.mcb.block.BlockPropagatePolicy;

public class PropagateManager implements PropagatePolicy {
	/** Using strategic pattern **/
	public Collection<PropagatePolicy> propagates; 
	
	public PropagateManager(MapCircuitBoard plugin){
		propagates = new ArrayList<PropagatePolicy>();
		propagates.add(new BlockPropagatePolicy(plugin));
		propagates.add(new BoardPropagatePolicy(plugin));
		propagates.add(new AirPropagatePolicy());
	}
	
	@Override
	public boolean in(Location location, Facing face, TickableBoard canvas, ItemFrame frame) {
		for(PropagatePolicy policy : propagates)
			if(policy.in(location, face, canvas, frame)) return true;
		return false;
	}

	@Override
	public boolean out(Location location, Facing face, TickableBoard canvas, ItemFrame frame) {
		for(PropagatePolicy policy : propagates)
			if(policy.out(location, face, canvas, frame)) return true;
		return false;
	}
}
