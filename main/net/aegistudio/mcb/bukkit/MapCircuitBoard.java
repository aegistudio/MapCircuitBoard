package net.aegistudio.mcb.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mpp.export.PluginCanvasService;

public class MapCircuitBoard extends JavaPlugin {
	public final ComponentFactory factory = new ComponentFactory();
	
	public ComponentFactory getComponentTable() {
		return this.factory;
	}
	
	public PluginCanvasService service;
	public void onLoad() {
		try {
			service = super.getServer().getServicesManager()
					.getRegistration(PluginCanvasService.class).getProvider();
			
			service.register(this, "scheme", (context) -> new SchemeCanvas(this, context));
			service.register(this, "redstone", (context) -> new CircuitBoardCanvas(this, context));
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
