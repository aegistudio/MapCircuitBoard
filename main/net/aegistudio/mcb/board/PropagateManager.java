package net.aegistudio.mcb.board;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Location;

import net.aegistudio.mcb.Facing;

public class PropagateManager implements PropagatePolicy {
	/** Using strategic pattern **/
	public Collection<PropagatePolicy> propagates; {
		propagates = new ArrayList<PropagatePolicy>();
		propagates.add(new BlockPropagatePolicy());
		propagates.add(new BoardPropagatePolicy());
		propagates.add(new AirPropagatePolicy());
	}
	
	@Override
	public boolean in(Location location, Facing face, CircuitBoardCanvas canvas) {
		for(PropagatePolicy policy : propagates)
			if(policy.in(location, face, canvas)) return true;
		return false;
	}

	@Override
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas) {
		for(PropagatePolicy policy : propagates)
			if(policy.out(location, face, canvas)) return true;
		return false;
	}
}
