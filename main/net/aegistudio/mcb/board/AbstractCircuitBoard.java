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
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.TickableBoard;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PlaceSensitive;
import net.aegistudio.mpp.export.PluginCanvas;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public abstract class AbstractCircuitBoard implements PluginCanvas, PlaceSensitive, TickableBoard {
	public final Context context;
	public final MapCircuitBoard plugin;

	public Location location;
	
	public ActualGrid grid;
	public Grid getGrid() {
		return grid;
	}
	
	public void refer(Location location) {
		this.location = location;
	}
	
	public void defer() {
		this.location = null;
	}
	
	public AbstractCircuitBoard(MapCircuitBoard plugin, Context context) {
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
				
				this.grid = this.makeActualGrid(din);
				this.grid.load(input, plugin.factory);
				this.grid.add();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			this.defer();
		}
	}
	
	protected abstract ActualGrid makeActualGrid(DataInputStream din) throws Exception;
	
	protected abstract void saveAbstractGrid(DataOutputStream dout) throws Exception;
	
	//XXX override this when you want to change the case judging 
	//when there is no need to write the  grid data.
	protected boolean isInvalid() {	return location == null; }
	
	@Override
	public void save(OutputStream output) {
		try {
			DataOutputStream dout = new DataOutputStream(output);
			if(isInvalid())
				dout.writeUTF("");
			else {
				dout.writeUTF(location.getWorld().getName());
				dout.writeInt(location.getBlockX());
				dout.writeInt(location.getBlockY());
				dout.writeInt(location.getBlockZ());
				
				this.saveAbstractGrid(dout);
				grid.save(output, plugin.factory);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public @Override void paint(Interaction i, Color c) {	}
	
	// XXX Please make this 'canvas' separate from its subtypes.
	public PluginCanvasRegistry<? extends PluginCanvas> canvas;
	public @Override void add(PluginCanvasRegistry<? extends PluginCanvas> arg0) {
		this.canvas = arg0;
	}
	
	public @Override void remove(PluginCanvasRegistry<? extends PluginCanvas> arg0) {	
		if(this.grid != null) this.grid.remove();
	}
	
	public ItemFrame frame;
	@Override
	public ItemFrame getFrame() {
		return this.frame;
	}
	
	@Override
	public void tick() {
		// Check map in-place.
		whereami();
		
		// Update reference.
		this.makeGrid();
		this.destroyGrid();
		
		// Do repaint.
		if(this.location != null)
			this.repaint();
	}
	
	protected abstract void makeGrid();
	protected abstract void destroyGrid();
	
	public void whereami() {
		ItemFrame target = null;
		if(location != null) {
			if(!location.getChunk().isLoaded()) return;
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
		frame = target;
	}
	
	public void propagateIn() {
		if(frame != null && grid != null)
			Facing.all(face -> 
				plugin.propagate.in(transform(face, frame.getFacing(), 
						frame.getLocation().clone()), face, this, frame));
	}
	
	public void clockTick() {
		if(grid != null && frame != null)
			grid.tick(frame);
	}
	
	public void propagateOut() {
		if(frame != null && grid != null)
			Facing.all(face -> 
				plugin.propagate.out(transform(face, frame.getFacing(), 
						frame.getLocation().clone()), face, this, frame));
	}
	
	public void repaint() {
		context.color(null);
		context.clear();
		if(grid != null) 
			grid.paint(context);
		context.repaint();
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
	
	@Override
	public void place(Location arg0, BlockFace arg1) {	
		plugin.getServer().getScheduler().runTaskLater(plugin, 
				() ->	this.refer(arg0), 1);
	}

	@Override
	public void unplace(Item arg0) {
		this.defer();
	}
}
