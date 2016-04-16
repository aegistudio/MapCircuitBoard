package net.aegistudio.mcb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.aegistudio.mcb.unit.Button;
import net.aegistudio.mcb.unit.Lever;
import net.aegistudio.mcb.unit.Torch;
import net.aegistudio.mcb.wire.FullDirectionalWire;

public class ComponentFactory {
	private final ArrayList<Component> instance = new ArrayList<Component>();
	
	public ComponentFactory() {
		instance.add(Air.INSTANCE);
		instance.add(FullDirectionalWire.INSTANCE);
		Facing.all(face -> instance.add(Torch.INSTANCES[face.ordinal()]));
		instance.add(Lever.INSTANCE);
		instance.add(Button.INSTANCE);
	}
	
	public int id(Component component) {
		return instance.indexOf(component);
	}
	
	public Component get(int index) {
		return instance.get(index);
	}
	
	public List<Component> all() {
		return Collections.unmodifiableList(instance);
	}
}
