package net.aegistudio.mcb;

import java.util.TreeMap;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;

import net.aegistudio.mcb.unit.CommandBlockData;
import net.aegistudio.mcb.unit.CommandBlockEditor;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.CommandHandle;
import net.aegistudio.mpp.export.PluginCommandService;

public class BukkitBlockEditor implements CommandBlockEditor {
	private final MapCircuitBoard plugin;
	public CommandHandle<MapCircuitBoard> infoHandle;
	
	public BukkitBlockEditor(MapCircuitBoard plugin) {
		this.plugin = plugin;
		this.selected = new TreeMap<String, Pair>();
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

	@Override
	public void execute(ItemFrame frame, CommandBlockData data, Cell cell) {
		if(data.command.length() == 0) return;
		// translate
		data.translated = data.command;
		
		data.lastOutputState = plugin.getServer()
				.dispatchCommand(data, data.translated);
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
					arg2.sendMessage(plugin.locale.getProperty("info.nopermission"));
					return true;
				}
				
				StringBuilder builder = new StringBuilder();
				for(int i = 0; i < arg3.length; i ++) {
					if(i > 0) builder.append(' ');
					builder.append(arg3[i]);
				}
				
				pair.data.command = new String(builder);
				pair.cell.setData(pair.data);
				
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
				return true;
			}
			
		});
	}
}
