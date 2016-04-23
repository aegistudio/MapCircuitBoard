package net.aegistudio.mcb;

import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import net.aegistudio.mcb.board.CircuitBoardCanvas;
import net.aegistudio.mcb.board.CircuitBoardItem;
import net.aegistudio.mcb.board.PropagateManager;
import net.aegistudio.mcb.layout.ComponentPlaceListener;
import net.aegistudio.mcb.layout.ComponentPlacer;
import net.aegistudio.mcb.layout.SchemeCanvas;
import net.aegistudio.mcb.unit.Button;
import net.aegistudio.mcb.unit.Comparator;
import net.aegistudio.mcb.unit.Lever;
import net.aegistudio.mcb.unit.MonitorPin;
import net.aegistudio.mcb.unit.OriginatorPin;
import net.aegistudio.mcb.unit.Repeater;
import net.aegistudio.mcb.unit.Torch;
import net.aegistudio.mcb.wire.BiInsulatedWire;
import net.aegistudio.mcb.wire.FullDirectionalWire;
import net.aegistudio.mpp.export.CanvasCommandHandle;
import net.aegistudio.mpp.export.PluginCanvasRegistry;
import net.aegistudio.mpp.export.PluginCanvasService;
import net.aegistudio.mpp.export.PluginCommandService;

public class MapCircuitBoard extends JavaPlugin {
	public ComponentFactory factory;
	
	public ComponentFactory getComponentTable() {
		return this.factory;
	}
	
	public PluginCanvasService canvasService;
	public PluginCommandService commandService;
	public TreeMap<Integer, PluginCanvasRegistry<SchemeCanvas>> schemes;

	public ComponentPlaceListener placeListener
			= new ComponentPlaceListener();
	
	public int internalTick = 1;
	public CircuitBoardItem circuitBoardItem;
	
	public PropagateManager propagate;
	
	public void onEnable() {
		factory = new ComponentFactory();
		placeListener.add(new ComponentPlacer(Material.AIR, factory.get(factory.id(Air.class))));
		placeListener.add(new ComponentPlacer(Material.REDSTONE, factory.get(factory.id(FullDirectionalWire.class))));
		factory.all(Torch.class, torch -> placeListener.add(new ComponentPlacer(Material.REDSTONE_TORCH_ON, torch)));
		placeListener.add(new ComponentPlacer(Material.LEVER, factory.get(factory.id(Lever.class))));
		placeListener.add(new ComponentPlacer(Material.WOOD_BUTTON, factory.get(factory.id(Button.class))));
		placeListener.add(new ComponentPlacer(Material.STONE_BUTTON, factory.get(factory.id(MonitorPin.class))));
		placeListener.add(new ComponentPlacer(Material.STONE_BUTTON, factory.get(factory.id(OriginatorPin.class))));
		factory.all(BiInsulatedWire.class, insulated -> placeListener.add(new ComponentPlacer(Material.POWERED_RAIL, insulated)));
		factory.all(Repeater.class, repeater -> placeListener.add(new ComponentPlacer(Material.DIODE, repeater)));
		factory.all(Comparator.class, comparator -> placeListener.add(new ComponentPlacer(Material.REDSTONE_COMPARATOR, comparator)));
		
		propagate = new PropagateManager(this);
		
		try {
			canvasService = super.getServer().getServicesManager()
					.getRegistration(PluginCanvasService.class).getProvider();
			
			schemes = new TreeMap<Integer, PluginCanvasRegistry<SchemeCanvas>>();
			canvasService.register(this, "scheme", (context) -> new SchemeCanvas(this, context));
			
			canvasService.register(this, "redstone", (context) -> new CircuitBoardCanvas(this, context));
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		
		circuitBoardItem = new CircuitBoardItem(this);
		getServer().getPluginManager().registerEvents(circuitBoardItem, this);
		
		try {
			commandService = super.getServer().getServicesManager()
					.getRegistration(PluginCommandService.class).getProvider();
			commandService.registerCreate(this, "create/scheme", "scheme", 
					new CanvasCommandHandle<MapCircuitBoard, SchemeCanvas>() {
				public @Override String description() {		return "create a circuit scheme!";		}
				
				public @Override boolean handle(MapCircuitBoard arg0, CommandSender arg1, 
						String[] arg2, SchemeCanvas arg3) {
					return true;
				}
				
				public @Override String paramList() {	return "";	}
			});
			
			commandService.registerControl(this, "create/circuit", "scheme", SchemeCanvas.class, 
					new CanvasCommandHandle<MapCircuitBoard, SchemeCanvas>() {
				public @Override String description() {		return "get a circuit board item!";		}
				
				public @Override boolean handle(MapCircuitBoard arg0, CommandSender arg1, String[] arg2, SchemeCanvas arg3) {
					if(!(arg1 instanceof Player)) return false;
					ItemStack boardItem = new ItemStack(Material.MAP, 1);
					circuitBoardItem.make(boardItem, arg3.registry);
					Player player = (Player) arg1;
					player.getLocation().getWorld().dropItem(player.getLocation(), boardItem);
					return true;
				}
				
				public @Override String paramList() {	return "";	}
			});
			
			/**
			 * Sending metric message to mcstats.
			 */
		    try {
		        Metrics metrics = new Metrics(this);
		        metrics.start();
		    } catch (Exception e) {
		        
		    }
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			canvasService.getPluginCanvases(this, "redstone", CircuitBoardCanvas.class)
				.forEach(redstone -> {if(redstone.canvas().frame == null) redstone.canvas().whereami();});
			for(int i = 0; i < internalTick; i ++) {
				canvasService.getPluginCanvases(this, "redstone", CircuitBoardCanvas.class)
					.forEach(redstone -> redstone.canvas().propagateIn());
				canvasService.getPluginCanvases(this, "redstone", CircuitBoardCanvas.class)
					.forEach(redstone -> redstone.canvas().clockTick());
				canvasService.getPluginCanvases(this, "redstone", CircuitBoardCanvas.class)
					.forEach(redstone -> redstone.canvas().propagateOut());
			}
		},1, 0);
	}
}
