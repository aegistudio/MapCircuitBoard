package net.aegistudio.mcb.layout;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aegistudio.mcb.Component;
import net.aegistudio.mcb.Grid;

public class ComponentPlacer {
	public final Material type;
	public final Component component;
	public ComponentPlacer(Material type, Component component) {
		this.type = type;
		this.component = component;
	}
	
	public boolean matches(ItemStack item) {
		if(type == null || type == Material.AIR)
			return item == null || item.getType() == Material.AIR;
		return item.getType() == type;
	}
	
	public boolean place(Grid grid, Player who, int row, int column) {
		consume(who);
		return true;
	}
	
	public boolean unplace(Grid grid, Player who, int row, int column) {
		repay(who);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	protected void consume(Player who) {
		if(who.getGameMode() != GameMode.SURVIVAL) return;
		ItemStack itemStack = who.getItemInHand();
		itemStack.setAmount(itemStack.getAmount() - 1);
		if(itemStack.getAmount() == 0)
			who.setItemInHand(null);
	}
	
	protected void repay(Player who) {
		if(who.getGameMode() == GameMode.SURVIVAL)
			who.getLocation().getWorld().dropItem(who.getLocation(), 
					new ItemStack(type));
	}
}
