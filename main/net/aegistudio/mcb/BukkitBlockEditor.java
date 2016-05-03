package net.aegistudio.mcb;

import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import net.aegistudio.mcb.unit.CommandBlockData;
import net.aegistudio.mcb.unit.CommandBlockEditor;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.CommandHandle;
import net.aegistudio.mpp.export.PluginCommandService;

public class BukkitBlockEditor implements CommandBlockEditor {
	private final MapCircuitBoard plugin;
	public final TreeMap<String, Cell> commandBlock;
	
	public BukkitBlockEditor(MapCircuitBoard plugin) {
		this.plugin = plugin;
		this.commandBlock = new TreeMap<String, Cell>();
	}
	
	@Override
	public Server getServer() {
		return plugin.getServer();
	}

	@Override
	public void edit(Interaction interaction, CommandBlockData data, Cell cell) {
		if(!(interaction.sender instanceof Player)) return;
		Player player = (Player) interaction.sender;
		player.sendMessage(ChatColor.AQUA + "Raw command: " + ChatColor.RESET + data.command);
		player.sendMessage("Actual command: " + data.translated);
		player.sendMessage("Last output: " + data.lastOutput);
		player.sendMessage("Emit power: " + (data.lastOutputState? 
				(ChatColor.GREEN + "yes") : (ChatColor.RED + "no")));
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
		commandService.register(plugin, "control/cmdblock/info", new CommandHandle<MapCircuitBoard>() {
			@Override
			public String description() {
				return plugin.locale.getProperty("info.description");
			}

			@Override
			public boolean handle(MapCircuitBoard arg0, String arg1, CommandSender arg2, String[] arg3) {
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
