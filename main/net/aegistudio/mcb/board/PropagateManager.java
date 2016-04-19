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
		propagates.add(new AirPropagatePolicy());
	}
	
	@Override
	public void in(Location location, Facing face, CircuitBoardCanvas canvas) {
		propagates.forEach((p) -> p.in(location, face, canvas));
	}

	@Override
	public void out(Location location, Facing face, CircuitBoardCanvas canvas) {
		propagates.forEach((p) -> p.out(location, face, canvas));
	}
}
