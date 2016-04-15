package net.aegistudio.mcb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.aegistudio.mcb.unit.Torch;
import net.aegistudio.mcb.wire.FullDirectionalWire;

public class ComponentFactory {
	private static final ArrayList<Component> instance = new ArrayList<Component>();
	static {
		instance.add(Air.INSTANCE);
		instance.add(FullDirectionalWire.INSTANCE);
		instance.add(Torch.INSTANCE);
	}
	
	public static int id(Component component) {
		return instance.indexOf(component);
	}
	
	public static Component get(int index) {
		return instance.get(index);
	}
	
	public static List<Component> all() {
		return Collections.unmodifiableList(instance);
	}
}
