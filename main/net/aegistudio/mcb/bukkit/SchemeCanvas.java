package net.aegistudio.mcb.bukkit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.entity.Player;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.layout.LayoutGrid;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PluginCanvas;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class SchemeCanvas implements PluginCanvas {
	public final LayoutGrid scheme;
	public final Context context;
	public final MapCircuitBoard plugin;
	
	public SchemeCanvas(MapCircuitBoard plugin, Context context) {
		this.context = context;
		this.plugin = plugin;
		this.scheme = new LayoutGrid();
	}
	
	public @Override void tick() {	}
	public @Override void paint(Interaction i, Color c) {		}

	@Override
	public boolean interact(Interaction i) {
		int row = i.y / 4;	int column = i.x / 4;
		if(i.rightHanded) {
			// Interacting
			Cell target = this.scheme.getCell(row, column);
			if(target != null){
				target.getComponent().interact(target, i);
				repaint();
			}
		}
		else {
			// Placing
			if(i.sender instanceof Player) {
				//Player player = (Player) i.sender;
				//player.getItemInHand();
				this.scheme.setCell(row, column, null);
				repaint();
			}
		}
		return true;
	}

	public void repaint() {
		context.color(null);
		context.clear();
		this.scheme.paint(context);
		context.repaint();
	}
	
	@Override
	public void load(InputStream input) {
		try {
			this.scheme.load(input, plugin.getComponentTable());
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void save(OutputStream output) {
		try {
			this.scheme.save(output, plugin.getComponentTable());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void add(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
		
	}

	@Override
	public void remove(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
		
	}
}
