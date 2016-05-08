package net.aegistudio.mcb;

import java.util.HashMap;
import java.util.TreeMap;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.mcinject.tileentity.TileEntityCommand;
import net.aegistudio.mcb.mcinject.world.BlockPosition;
import net.aegistudio.mcb.mcinject.world.World;
import net.aegistudio.mcb.reflect.clazz.SamePackageClass;
import net.aegistudio.mcb.reflect.method.AbstractExecutor;
import net.aegistudio.mcb.reflect.method.LengthedExecutor;
import net.aegistudio.mcb.reflect.method.NamedExecutor;
import net.aegistudio.mcb.unit.CommandBlockData;
import net.aegistudio.mcb.unit.CommandBlockEditor;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.CommandHandle;
import net.aegistudio.mpp.export.PluginCommandService;

public class BukkitBlockEditor implements CommandBlockEditor {
	private final MapCircuitBoard plugin;
	public CommandHandle<MapCircuitBoard> infoHandle;
	public CommandMap commandMap;
	public final AbstractExecutor proxiedNativeCommandSender;
	public final VanillaExecutorProxy vanillaProxy;
	
	public BukkitBlockEditor(MapCircuitBoard plugin) {
		this.plugin = plugin;
		this.selected = new TreeMap<String, Pair>();
		
		try {
			commandMap = (CommandMap)new NamedExecutor(plugin.server.getBukkitServerClass().method(), 
					"getCommandMap").invoke(this.plugin.getServer());
			net.aegistudio.mcb.reflect.clazz.Class proxiedNativeCommandSender = 
					new SamePackageClass(plugin.server.getBukkitServerClass(), "command.ProxiedNativeCommandSender");
			this.proxiedNativeCommandSender = new LengthedExecutor(proxiedNativeCommandSender.constructor(), 3);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

		this.vanillaProxy = new VanillaExecutorProxy(plugin.server);
	}
	
	@Override
	public Server getServer() {
		return plugin.getServer();
	}

	@Override
	public void edit(Interaction interaction, CommandBlockData data, Cell cell) {
		Pair value = new Pair();
		value.data = data; value.cell = cell;
		selected.put(interaction.sender.getName(), value);
		
		interaction.sender.sendMessage("");
		infoHandle.handle(plugin, "", interaction.sender, new String[0]);
	}
	
	TreeMap<String, Pair> selected;
	class Pair {
		CommandBlockData data;
		Cell cell;
	}

	class Proxier {
		TileEntityCommand command;
		CommandBlockData data;
		CommandSender sender;
	}
	
	HashMap<CommandBlockData, Proxier> proxier = new HashMap<CommandBlockData, Proxier>();
	public Proxier retrieve(ItemFrame frame, CommandBlockData data) {
		Proxier result = proxier.get(data);
		if(result != null && result.data == data) return result;
		Proxier value = new Proxier();
		
		value.command = new TileEntityCommand(plugin.server);
		value.command.setWorld(new World(plugin.server, frame.getWorld()));
		value.command.setPosition(new BlockPosition(plugin.server, frame.getLocation()));
		
		CommandSender sender = (CommandSender)proxiedNativeCommandSender
				.invoke(null, value.command.getCommandBlock().thiz, data, data);

		value.data = data;	value.sender = sender;
		proxier.put(data, value);
		return value;
	}
	
	@Override
	public void execute(ItemFrame frame, CommandBlockData data, Cell cell) {
		if(data.command.length() == 0) {
			data.lastOutputState = false;
			data.translated = "";
			return;
		}
		
		// translate
		data.translated = data.command;
		if(data.translated.charAt(0) == '/') 
			data.translated = data.translated.substring(1);
		
		// run task.
		Runnable task = () -> {
			Proxier proxy = retrieve(frame, data);
			//plugin.getServer().dispatchCommand(retrieve(frame, data)
			//		.sender, data.translated);
			
			String[] splitted = data.translated.split(" ");
			String[] trimmed = new String[splitted.length - 1];
			System.arraycopy(splitted, 1, trimmed, 0, trimmed.length);
			int successfulCount = vanillaProxy.executeSuccess(this.commandMap.getCommand(splitted[0]), 
					proxy.sender, splitted[0], trimmed);
			data.lastOutputState = successfulCount > 0;
		};

		
		// filter asynchronous command.
		if(data.translated.startsWith("summon"))
			plugin.getServer().getScheduler().runTask(plugin, task);
		else task.run();
	}
	
	public void registerCommands(PluginCommandService commandService) throws Exception {
		// advance plugin command.
		commandService.registerGroup(plugin, "control/cmdblock", this.plugin.locale.getProperty("cmdblock.description"));
		commandService.register(plugin, "control/cmdblock/info", infoHandle = new CommandHandle<MapCircuitBoard>() {
			@Override
			public String description() {
				return plugin.locale.getProperty("info.description");
			}

			@Override
			public boolean handle(MapCircuitBoard arg0, String arg1, CommandSender arg2, String[] arg3) {
				Pair pair = selected.get(arg2.getName());
				if(pair == null){
					arg2.sendMessage(plugin.locale.getProperty("info.notselected"));
					return true;
				}
				
				CommandBlockData data = pair.data;
				arg2.sendMessage(plugin.locale.getProperty("info.lastedited") + 
						(data.lastEdited != null && data.lastEdited.length() > 0? data.lastEdited:
							plugin.locale.getProperty("info.none")));
				arg2.sendMessage(plugin.locale.getProperty("info.rawcmd") + 
						(data.command != null && data.command.length() > 0? data.command:
							plugin.locale.getProperty("info.none")));
				
				if(data.translated != null && data.translated.length() > 0) 
					arg2.sendMessage(plugin.locale.getProperty("info.actualcmd") + data.translated);
				
				arg2.sendMessage(plugin.locale.getProperty("info.lastoutput") + 
						(data.lastOutput != null && data.lastOutput.length() > 0? data.lastOutput:
							plugin.locale.getProperty("info.none")));
				
				arg2.sendMessage(plugin.locale.getProperty("info.laststate") + (data.lastOutputState? 
						plugin.locale.getProperty("info.yes") : plugin.locale.getProperty("info.no")));
				arg2.sendMessage(plugin.locale.getProperty("info.footer"));
				
				return true;
			}
		});
		
		commandService.register(plugin, "control/cmdblock/update", new CommandHandle<MapCircuitBoard>() {
			@Override
			public String description() {
				return plugin.locale.getProperty("update.description");
			}
			
			@Override
			public boolean handle(MapCircuitBoard arg0, String arg1, CommandSender arg2, String[] arg3) {
				Pair pair = selected.get(arg2.getName());
				if(pair == null){
					arg2.sendMessage(plugin.locale.getProperty("info.notselected"));
					return true;
				}
				if(!arg2.hasPermission("mcb.cmdblock")) {
					arg2.sendMessage(plugin.locale.getProperty("update.nopermission"));
					return true;
				}
				
				StringBuilder builder = new StringBuilder();
				for(int i = 0; i < arg3.length; i ++) {
					if(i > 0) builder.append(' ');
					builder.append(arg3[i]);
				}
				
				pair.data.command = new String(builder);
				pair.cell.setData(pair.data);
				pair.data.lastEdited = arg2.getName();
				pair.data.translated = "";
				if(pair.cell.getGrid() instanceof AbstractGrid)
					((AbstractGrid)pair.cell.getGrid()).update(pair.cell.getRow(), 
							pair.cell.getColumn(), pair.cell);
				
				return true;
			}
		});
		
		commandService.register(plugin, "control/cmdblock/exit", new CommandHandle<MapCircuitBoard>() {
			@Override
			public String description() {
				return plugin.locale.getProperty("exit.description");
			}

			@Override
			public boolean handle(MapCircuitBoard arg0, String arg1, CommandSender arg2, String[] arg3) {
				if(selected.remove(arg2.getName()) != null)
					arg2.sendMessage(plugin.locale.getProperty("exit.selected"));
				else arg2.sendMessage(plugin.locale.getProperty("info.notselected"));
				return true;
			}
		});
	}
}
