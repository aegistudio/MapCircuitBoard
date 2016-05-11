package net.aegistudio.mcb.board;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.bukkit.Location;
import org.bukkit.entity.Item;

import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.layout.SchemeCanvas;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PluginCanvasRegistry;

public class CircuitBoardCanvas extends AbstractCircuitBoard {
	public PluginCanvasRegistry<SchemeCanvas> referred;

	public CircuitBoardCanvas(MapCircuitBoard plugin, Context context) {
		super(plugin, context);
	}
	
	public void refer(Location location, PluginCanvasRegistry<SchemeCanvas> reference) {
		this.location = location;
		this.referred = reference;
	}
	
	public void defer() {
		this.location = null;
		this.referred = null;
	}
	
	@Override
	protected ActualGrid makeActualGrid(DataInputStream din) throws Exception{
		this.referred = this.plugin.schemes.get((int)din.readShort());
		return new SpectatedActualGrid(referred.canvas().scheme);
	}

	public boolean isInvalid() {
		return location == null || referred == null || 
				referred.canvas().registry.mapid() < 0;
	}
	
	@Override
	protected void saveAbstractGrid(DataOutputStream dout) throws Exception {
		dout.writeShort(referred.mapid());
	}
	
	public void makeGrid() {
		if(this.location != null && this.referred != null) {
			if(this.grid == null) {
				//this.grid = new ActualGrid(referred.canvas().scheme);
				this.grid = new SpectatedActualGrid(referred.canvas().scheme);
				this.grid.add();
			}
		}
	}
	
	@Override
	protected void destroyGrid() {
		if(this.location == null && this.referred == null) {
			if(this.grid != null) {
				this.grid.remove();
				this.grid = null;
			}
			plugin.canvasService.destroy(canvas);
		}
	}
	
	public void repaint() {
		if(this.referred == null) return;
		super.repaint();
	}
	
	@Override
	public void unplace(Item arg0) {
		this.plugin.circuitBoardItem.make(arg0.getItemStack(), referred);
		super.unplace(arg0);
	}
}
