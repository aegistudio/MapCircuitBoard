package net.aegistudio.mcb.layout;

import java.util.Properties;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.MapCircuitBoard;

public class CommandBlockPlacer extends ComponentPlacer {
	Properties locale;
	public CommandBlockPlacer(MapCircuitBoard board, Material type, Component component) {
		super(type, component);
		this.locale = board.locale;
	}

	public boolean place(Grid grid, Player who, int row, int column) {
		if(!who.hasPermission("mcb.cmdblock")) {
			who.sendMessage(locale.getProperty("cmdblock.nopermission"));
			return false;
		}
		return super.place(grid, who, row, column);
	}
	
	public boolean unplace(Grid grid, Player who, int row, int column) {
		if(!who.hasPermission("mcb.cmdblock")) {
			who.sendMessage(locale.getProperty("cmdblock.nopermission"));
			return false;
		}
		return super.unplace(grid, who, row, column);
	}
}
