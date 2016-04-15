package net.aegistudio.mcb.wire;

import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Facing;

public interface Wire extends Component {
	/**
	 * How far is this cell to another cell.
	 * Usually return 1 if the power descend by 1 is propagate.
	 * Return Short.MAX_VALUE if it's not adjacent.
	 * Return 0 if power don't go down.
	 * 
	 * @param indirection the input direction
	 * @return the distance
	 */
	public short distance(Facing outdirection);
}
