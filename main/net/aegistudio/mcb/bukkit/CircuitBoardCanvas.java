package net.aegistudio.mcb.bukkit;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PluginCanvas;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class CircuitBoardCanvas implements PluginCanvas {
	public final Context context;
	public final MapCircuitBoard plugin;
	
	public CircuitBoardCanvas(MapCircuitBoard plugin, Context context) {
		this.context = context;
		this.plugin = plugin;
	}

	@Override
	public boolean interact(Interaction i) {
		return false;
	}

	@Override
	public void load(InputStream input) {
		
	}

	@Override
	public void paint(Interaction i, Color c) {
		
	}

	@Override
	public void save(OutputStream output) {
		
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void add(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
		
	}

	@Override
	public void remove(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
	
	}
}
