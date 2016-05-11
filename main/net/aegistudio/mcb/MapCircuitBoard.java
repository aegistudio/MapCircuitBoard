package net.aegistudio.mcb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import net.aegistudio.mcb.board.CircuitBoardCanvas;
import net.aegistudio.mcb.board.CircuitBoardItem;
import net.aegistudio.mcb.board.IntegratedCanvas;
import net.aegistudio.mcb.board.PropagateManager;
import net.aegistudio.mcb.clock.Asynchronous;
import net.aegistudio.mcb.clock.Clocking;
import net.aegistudio.mcb.clock.Synchronous;
import net.aegistudio.mcb.layout.CommandBlockPlacer;
import net.aegistudio.mcb.layout.ComponentPlaceListener;
import net.aegistudio.mcb.layout.ComponentPlacer;
import net.aegistudio.mcb.layout.SchemeCanvas;
import net.aegistudio.mcb.mcinject.CraftMinecraftServer;
import net.aegistudio.mcb.mcinject.MinecraftServer;
import net.aegistudio.mcb.unit.Button;
import net.aegistudio.mcb.unit.CommandBlock;
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
	
	public CircuitBoardItem circuitBoardItem;
	
	public PropagateManager propagate;
	public BukkitBlockEditor editor;
	
	public static final String SOURCE_RAW_URL = "https://raw.githubusercontent.com/aegistudio/MapCircuitBoard/master/";
	public Properties locale = new Properties();
	public MinecraftServer server;
	
	public void onEnable() {
		try {
			this.server = new CraftMinecraftServer(getServer());
		}
		catch(Exception e) {
			e.printStackTrace();
			this.setEnabled(false);
		}
		new Thread(() -> {
			try {
				this.locale = new Properties();
				final Properties defaultLocale = new Properties();
				
				Thread fetchLocale = new Thread(() -> {
					try {
						URL localeMapUrl = new URL(SOURCE_RAW_URL + "locale/map.properties");
				    	Properties localeMap = new Properties();
				    	localeMap.load(localeMapUrl.openConnection().getInputStream());
				    	String mappedLanguage = localeMap.getProperty(Locale.getDefault().toString());
				    	if(mappedLanguage == null) mappedLanguage = localeMap.getProperty("en_US");
				    	
				    	URL mappedUrl = new URL(SOURCE_RAW_URL + "locale/" + mappedLanguage);
						defaultLocale.load(mappedUrl.openConnection().getInputStream());
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				});
				
				fetchLocale.start();
				File locale = new File(this.getDataFolder(), "locale.properties");
				if(locale.exists())
					this.locale.load(new FileInputStream(locale));
				
				fetchLocale.join();
				
				int presize = this.locale.keySet().size();
				defaultLocale.forEach((k, v) -> MapCircuitBoard.this.locale.putIfAbsent(k, v));
				int postsize = this.locale.keySet().size();
				
				if(postsize > presize)
					this.locale.store(new FileOutputStream(locale), null);
				
				this.locale.replaceAll((k, v) -> {
					String current = (String) v;
					for(ChatColor color : ChatColor.values()) 
						current = current.replace("${" + color.name() + "}", color.toString());
					return current;
				});
				
				sendConsole("Successfully fetched locale file!");
			}
			catch(Exception e) {
				sendConsole("Failed in fetching locale file.");
				e.printStackTrace();
			}
		}).start();
		
		factory = new ComponentFactory(editor = new BukkitBlockEditor(this));
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
		factory.all(CommandBlock.class, command -> placeListener.add(new CommandBlockPlacer(this, Material.COMMAND, command)));
		
		propagate = new PropagateManager(this);
		
		try {
			canvasService = super.getServer().getServicesManager()
					.getRegistration(PluginCanvasService.class).getProvider();
			
			schemes = new TreeMap<Integer, PluginCanvasRegistry<SchemeCanvas>>();
			canvasService.register(this, "scheme", (context) -> new SchemeCanvas(this, context));
			canvasService.register(this, "redstone", (context) -> new CircuitBoardCanvas(this, context));
			canvasService.register(this, "integrated", (context) -> new IntegratedCanvas(this, context));
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
				public @Override String description() {		return locale.getProperty("scheme.description");		}
				
				public @Override boolean handle(MapCircuitBoard arg0, CommandSender arg1, 
						String[] arg2, SchemeCanvas arg3) {
					if(arg1.hasPermission("mcb.scheme"))
						return true;
					arg1.sendMessage(locale.getProperty("scheme.nopermission"));
					return false;
				}
				
				public @Override String paramList() {	return "";	}
			});
			
			commandService.registerControl(this, "create/circuit", "scheme", SchemeCanvas.class, 
					new CanvasCommandHandle<MapCircuitBoard, SchemeCanvas>() {
				public @Override String description() {		return locale.getProperty("circuit.description");		}
				
				public @Override boolean handle(MapCircuitBoard arg0, CommandSender arg1, String[] arg2, SchemeCanvas arg3) {
					if(!(arg1 instanceof Player)) return false;
					if(!arg1.hasPermission("mcb.circuit")) {
						arg1.sendMessage(locale.getProperty("circuit.nopermission"));
						return true;
					}
					
					ItemStack boardItem = new ItemStack(Material.MAP, 1);
					circuitBoardItem.make(boardItem, arg3.registry);
					Player player = (Player) arg1;
					player.getLocation().getWorld().dropItem(player.getLocation(), boardItem);
					return true;
				}
				
				public @Override String paramList() {	return "";	}
			});
			
			commandService.registerCreate(this, "create/integrated", "integrated", 
					new CanvasCommandHandle<MapCircuitBoard, IntegratedCanvas>() {

				public @Override String description() {	return locale.getProperty("integrated.description");	}

				public @Override boolean handle(MapCircuitBoard arg0, CommandSender arg1, 
						String[] arg2, IntegratedCanvas arg3) {
					if(arg1.hasPermission("mcb.integrated"))
						return true;
					arg1.sendMessage(locale.getProperty("integrated.nopermission"));
					return false;
				}

				public @Override String paramList() {	return "";	}
			});
			
			editor.registerCommands(commandService);
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		
		/**
		 * Sending metric message to mcstats.
		 */
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (Exception e) {
	        
	    }
		
		this.load();
	
		clocking.start(this);
		
	    /**
	     * Check whether there's new version of this plugin.
	     */

		new Thread(() -> {
	    	String currentVersion = this.getDescription().getVersion();
		    try {
		    	URL masterUrl = new URL(SOURCE_RAW_URL + "build.properties");
		    	Properties masterBuild = new Properties();
		    	masterBuild.load(masterUrl.openConnection().getInputStream());
		    	String masterVersion = masterBuild.getProperty("version");
	
		    	if(!masterVersion.equals(currentVersion)) {
		    		sendConsole(ChatColor.AQUA + "The newest version (" + ChatColor.GREEN + masterVersion + ChatColor.AQUA + ") has been published!");
		    		
		    		sendConsole(ChatColor.AQUA + "Downloads: ");
		    		String downloadJava8 = masterBuild.getProperty("download.java8");
		    		if(downloadJava8 != null) sendConsole(ChatColor.AQUA + "#   java8: " + 
		    				ChatColor.GREEN + ChatColor.UNDERLINE + downloadJava8);
		    		
		    		String downloadJava7 = masterBuild.getProperty("download.java7");
		    		if(downloadJava7 != null) sendConsole(ChatColor.AQUA + "#   java7: " + 
		    				ChatColor.GREEN + ChatColor.UNDERLINE + downloadJava7);
		    		
		    		sendConsole(ChatColor.AQUA + "Forums: ");
	    			sendConsole(ChatColor.AQUA + "#   spigotmc: " + ChatColor.GREEN + ChatColor.UNDERLINE + "https://www.spigotmc.org/resources/22074/");
	    			sendConsole(ChatColor.AQUA + "#   mcbbs: " + ChatColor.GREEN + ChatColor.UNDERLINE + "http://www.mcbbs.net/thread-576750-1-1.html");
		    	}
		    	else sendConsole("Congratulations! The plugin is of the newest version now!");
		    } catch(Exception e) {
		    	sendConsole("Cannot fetch information of the newest version, I'm sorry. :-(");
		    }
		}).start();
	}
	
	private void sendConsole(String data) {
		ConsoleCommandSender console = this.getServer().getConsoleSender();
		console.sendMessage("[" + this.getName() + "] " + data);
	}
	
	public void onDisable() {
		clocking.stop(this);
	}
	
	public void load() {
		this.reloadConfig();
		FileConfiguration config = getConfig();
		
		if(config.contains(INTERNAL_TICK)) 
			internalTick = config.getInt(INTERNAL_TICK);
		else config.set(INTERNAL_TICK, internalTick);
		
		
		if(config.contains(POLICY)) 
			policy = config.getString(POLICY);
		else config.set(POLICY, policy);
		
		switch(policy) {
			case "async":
				clocking = new Asynchronous();
			default:
				clocking = new Synchronous();
		}
		
		this.saveConfig();
	}
	
	public static final String INTERNAL_TICK = "internalTick";
	public int internalTick = 1;
	
	public static final String POLICY = "policy";
	public String policy = "sync";
	public Clocking clocking;
	
	public void clock() {
		forCircuitBoards(redstone -> {if(redstone.getFrame() == null) redstone.whereami();});
		for(int i = 0; i < internalTick; i ++) {
			forCircuitBoards(redstone -> redstone.propagateIn());
			forCircuitBoards(redstone -> redstone.clockTick());
			forCircuitBoards(redstone -> redstone.propagateOut());
		}
	}
	
	public void forCircuitBoards(Consumer<TickableBoard> todo) {
		Deque<WorkingThread> workingThreadQueue = new LinkedList<WorkingThread>();
		Consumer<TickableBoard> boardConsumer = (board) -> {
			WorkingThread thread = new WorkingThread(todo, board);
			workingThreadQueue.addFirst(thread);
			thread.start();
		};
		
		canvasService.getPluginCanvases(this, "redstone", CircuitBoardCanvas.class)
			.forEach(registry -> boardConsumer.accept(registry.canvas()));
		canvasService.getPluginCanvases(this, "integrated", IntegratedCanvas.class)
			.forEach(registry -> boardConsumer.accept(registry.canvas()));
			
		for(WorkingThread thread : workingThreadQueue)
			try { thread.join(); } catch(Exception e) {	e.printStackTrace(); }
	}
	
	class WorkingThread extends Thread{
		private final Consumer<TickableBoard> consumer;
		private final TickableBoard canvas;
		public WorkingThread(Consumer<TickableBoard> consumer, TickableBoard canvas) {
			this.consumer = consumer;
			this.canvas = canvas;
		}
		
		public void run() {	
			try {	this.consumer.accept(canvas);	} catch(Throwable t) {			}
		}
	}
}
