package net.aegistudio.mcb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.aegistudio.mcb.unit.Button;
import net.aegistudio.mcb.unit.Lever;
import net.aegistudio.mcb.unit.MonitorPin;
import net.aegistudio.mcb.unit.OriginatorPin;
import net.aegistudio.mcb.unit.Torch;
import net.aegistudio.mcb.wire.FullDirectionalWire;
import net.aegistudio.mcb.wire.BiInsulatedWire;

public class ComponentFactory {
	private final ArrayList<Component> instance = new ArrayList<Component>();
	
	public ComponentFactory() {
		instance.add(Air.INSTANCE);
		instance.add(FullDirectionalWire.INSTANCE);
		Facing.all(face -> instance.add(Torch.INSTANCES[face.ordinal()]));
		instance.add(Lever.INSTANCE);
		instance.add(Button.INSTANCE);
		instance.add(MonitorPin.INSTANCE);
		instance.add(OriginatorPin.INSTANCE);
		for(BiInsulatedWire wire : BiInsulatedWire.INSTANCES) instance.add(wire);
	}
	
	public int id(Component component) {
		return instance.indexOf(component);
	}
	
	public int id(Class<? extends Component> clazz) {
		for(int i = 0; i < instance.size(); i ++)
			if(clazz == instance.get(i).getClass()) return i;
		return -1;
	}
	
	public Component get(int index) {
		return instance.get(index);
	}
	
	public List<Component> all() {
		return Collections.unmodifiableList(instance);
	}
	
	public void all(Class<? extends Component> clazz, 
			Consumer<Component> consumer) {
		for(int i = 0; i < instance.size(); i ++)
			if(clazz == instance.get(i).getClass())
				consumer.accept(instance.get(i));
	}
}
