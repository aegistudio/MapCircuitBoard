package net.aegistudio.mcb.board;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.layout.SchemeCanvas;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PlaceSensitive;
import net.aegistudio.mpp.export.PluginCanvas;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class CircuitBoardCanvas implements PluginCanvas, PlaceSensitive {
	public final Context context;
	public final MapCircuitBoard plugin;

	public Location location;
	public PluginCanvasRegistry<SchemeCanvas> referred;
	public ActualGrid grid;
	
	public void refer(Location location, PluginCanvasRegistry<SchemeCanvas> reference) {
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
		if(!i.rightHanded) return false;
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
			if(location == null || referred == null || 
					referred.canvas().registry.mapid() < 0)
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
		ItemFrame target = null;
		if(location != null) {
			for(Entity entity : location.getWorld().getNearbyEntities(location, 1.6, 1.6, 1.6))
				if(entity instanceof ItemFrame) {
					ItemFrame frame = (ItemFrame) entity;
					ItemStack internalItem = frame.getItem();
					if(internalItem != null) 
						if(internalItem.getType() == Material.MAP)
							if(internalItem.getDurability() == canvas.mapid()) {
								target = frame;
								break;
							}
				}
			if(target == null) defer();
		}
		
		// Update reference.
		if(this.location != null && this.referred != null) {
			if(this.grid == null) {
				this.grid = new ActualGrid(referred.canvas().scheme);
				this.grid.add();
			}
		}
		
		if(this.location == null && this.referred == null) {
			if(this.grid != null) {
				this.grid.remove();
				this.grid = null;
			}
			plugin.canvasService.destroy(canvas);
		}
		
		final ItemFrame finalFrame = target;
		
		// Actually tick.
		if(this.grid != null) {
			for(int i = 0; i < plugin.internalTick; i ++) {
				// Take in signal.
				Facing.all(f -> propagateIn(finalFrame, f));
				
				// Do tick.
				grid.tick();
				
				// Emit out signal.
				Facing.all(f -> propagateOut(finalFrame, f));
			}
			
			context.color(null);
			context.clear();
			grid.paint(context);
			context.repaint();
		}
	}
	
	public Location transform(Facing facing, BlockFace face, Location location) {
		int mx = face.getModX();		int mz = face.getModZ();
		if(facing.offsetColumn == 0) 
			return location.add(0, facing.offsetRow, 0);
		else {
			/**
			 * |i	j	k |
			 * |0	1	0 | = mzi + 0j + -mxk.
			 * |mx	0	mz|
			 */
			return location.add(mz * facing.offsetColumn, 0, -mx * facing.offsetColumn);
		}
	}
	
	public void propagateOut(ItemFrame frame, Facing face) {
		plugin.propagate.out(transform(face, frame.getFacing(), frame.getLocation().clone()), face, this, frame);
	}
	
	public void propagateIn(ItemFrame frame, Facing face) {
		plugin.propagate.in(transform(face, frame.getFacing(), frame.getLocation().clone()), face, this, frame);
	}
	
	@Override
	public void place(Location arg0, BlockFace arg1) {	
		this.location = arg0;
	}

	@Override
	public void unplace(Item arg0) {
		plugin.circuitBoardItem.make(arg0.getItemStack(), this.referred);
		this.defer();
	}
}
