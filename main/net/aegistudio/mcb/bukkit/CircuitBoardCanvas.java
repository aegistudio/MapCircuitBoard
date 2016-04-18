package net.aegistudio.mcb.bukkit;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.board.ActualGrid;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PluginCanvas;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class CircuitBoardCanvas implements PluginCanvas {
	public final Context context;
	public final MapCircuitBoard plugin;

	public Location location;
	public PluginCanvasRegistry<SchemeCanvas> referred;
	public ActualGrid grid;
	
	public void setReference(Location location, PluginCanvasRegistry<SchemeCanvas> reference) {
		this.location = location;
		this.referred = reference;
	}
	
	public void defer() {
		this.location = null;
		this.referred = null;
	}
	
	public CircuitBoardCanvas(MapCircuitBoard plugin, Context context) {
		this.context = context;
		this.plugin = plugin;
	}

	@Override
	public boolean interact(Interaction i) {
		if(this.grid != null) {
			Cell cell = this.grid.getCell(i.y / 4, i.x / 4);
			if(cell != null) 
				cell.getComponent().interact(cell, i);
		}
		return true;
	}

	@Override
	public void load(InputStream input) {
		try {
			DataInputStream din = new DataInputStream(input);
			String world = din.readUTF();
			if(world.length() > 0) {
				World worldInstance = plugin.getServer().getWorld(world);
				int blockX = din.readInt();
				int blockY = din.readInt();
				int blockZ = din.readInt();
				
				this.location = worldInstance
						.getBlockAt(blockX, blockY, blockZ).getLocation();
				
				this.referred = this.plugin.schemes.get((int)din.readShort());
				
				this.grid = new ActualGrid(referred.canvas().scheme);
				this.grid.load(input, plugin.factory);
				this.grid.add();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			this.defer();
		}
	}

	@Override
	public void save(OutputStream output) {
		try {
			DataOutputStream dout = new DataOutputStream(output);
			if(location == null)
				dout.writeUTF("");
			else {
				dout.writeUTF(location.getWorld().getName());
				dout.writeInt(location.getBlockX());
				dout.writeInt(location.getBlockY());
				dout.writeInt(location.getBlockZ());
				
				dout.writeShort(referred.mapid());
				grid.save(output, plugin.factory);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public @Override void paint(Interaction i, Color c) {	}
	
	public PluginCanvasRegistry<CircuitBoardCanvas> canvas;
	
	@SuppressWarnings("unchecked")
	public @Override void add(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
		this.canvas = (PluginCanvasRegistry<CircuitBoardCanvas>) arg0;
	}
	
	public @Override void remove(PluginCanvasRegistry<? extends PluginCanvas> arg0) {	
		if(this.grid != null) this.grid.remove();
	}
	
	@Override
	public void tick() {
		// Check map in-place.
		if(location != null) {
			boolean contains = false;
			for(Entity entity : location.getWorld().getNearbyEntities(location, 1.6, 1.6, 1.6))
				if(entity instanceof ItemFrame) {
					ItemFrame frame = (ItemFrame) entity;
					ItemStack internalItem = frame.getItem();
					if(internalItem != null) 
						if(internalItem.getType() == Material.MAP)
							if(internalItem.getDurability() == canvas.mapid()) {
								contains = true;
								break;
							}
				}
			if(!contains) defer();
		}
		
		// Update reference.
		if(this.location != null && this.referred != null)
			if(this.grid == null) {
				this.grid = new ActualGrid(referred.canvas().scheme);
				this.grid.add();

				if(plugin.circuit.containsKey(this.canvas.mapid()))
					plugin.circuit.remove(this.canvas.mapid());
			}
		
		if(this.location == null && this.referred == null) 
			if(this.grid != null) {
				this.grid.remove();
				this.grid = null;
				
				if(!plugin.circuit.containsKey(this.canvas.mapid()))
					plugin.circuit.put(this.canvas.mapid(), this.canvas);
			}
		
		// Actually tick.
		if(this.grid != null) {
			for(int i = 0; i < plugin.internalTick; i ++)
				grid.tick();
			
			context.color(null);
			context.clear();
			grid.paint(context);
			context.repaint();
		}
	}
}
