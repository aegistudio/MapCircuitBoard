package net.aegistudio.mcb.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import net.aegistudio.mcb.AbstractCell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.wire.Wire;

public class LayoutWireCell extends AbstractCell<LayoutGrid, Wire> {
	public LayoutWireCell(LayoutGrid grid, Wire wire, int row, int column) {
		super(grid, row, column);
		this.component = wire;
	}
	
	Map<LayoutUnitCell, short[]> map = new HashMap<LayoutUnitCell, short[]>();
	void setDistance(LayoutUnitCell cell, Facing facing, short value) {
		short[] distanceVector = map.get(cell);
		if(distanceVector == null) {
			int length = Facing.values().length;
			map.put(cell, distanceVector = new short[length]);
			for(int i = 0; i < distanceVector.length; i ++)
				distanceVector[i] = Short.MAX_VALUE;
		}
		distanceVector[facing.ordinal()] = value;
	}
	
	public Set<LayoutUnitCell> allAdjacentUnit() {
		return Collections.unmodifiableSet(map.keySet());
	}
	
	public void allReachablePort(BiConsumer<LayoutUnitCell, Facing> consumer) {
		Map<LayoutUnitCell, short[]> interleaved = new HashMap<LayoutUnitCell, short[]>();
		for(Entry<LayoutUnitCell, short[]> input : map.entrySet()) {
			int length = Facing.values().length;
			short[] value = new short[length];
			System.arraycopy(input.getValue(), 0, value, 0, length);
			interleaved.put(input.getKey(), value);
		}
		for(Entry<LayoutUnitCell, short[]> output : interleaved.entrySet()) {
			Facing.all(face -> {
				if(output.getValue()[face.ordinal()] <= Short.MAX_VALUE)
					consumer.accept(output.getKey(), face);
			});
		}
	}
	
	boolean removeDistance(LayoutUnitCell cell, Facing facing) {
		short[] vector = map.get(cell);
		if(vector == null) return false;
		short previous = vector[facing.ordinal()];
		if(previous == Short.MAX_VALUE) return false;
		vector[facing.ordinal()] = Short.MAX_VALUE;
		for(short value : vector) 
			if(value < Short.MAX_VALUE) return true;
		map.remove(cell);
		return true;
	}
	
	public short getDistance(LayoutUnitCell cell, Facing facing) {
		short[] distanceVector = map.get(cell);
		if(distanceVector == null) return Short.MAX_VALUE;
		return distanceVector[facing.ordinal()];
	}
}
