package net.aegistudio.mcb.board;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Location;

import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.layout.SchemeCanvas;
import net.aegistudio.mpp.Interaction;
import net.aegistudio.mpp.export.Context;
import net.aegistudio.mpp.export.PlaceSensitive;

public class IntegratedCanvas extends AbstractCircuitBoard implements PlaceSensitive {
	public SchemeCanvas layout;
	public IntegratedCanvas(MapCircuitBoard plugin, Context context) {
		super(plugin, context);
		this.layout = new SchemeCanvas(plugin, null);
	}

	@Override
	protected ActualGrid makeActualGrid(DataInputStream din) throws Exception {
		return new SpectatedActualGrid(layout.scheme);
	}

	@Override
	protected void saveAbstractGrid(DataOutputStream dout) throws Exception {	}

	@Override
	public boolean interact(Interaction interaction) {
		return this.layout.interact(interaction);
	}
	
	public void load(InputStream input) {
		this.layout.load(input);
		super.load(input);
	}
	
	public void save(OutputStream output) {
		this.layout.save(output);
		super.save(output);
	}
	
	@Override
	protected void makeGrid() {
		if(location != null && super.grid == null) {
			super.grid = new SpectatedActualGrid(layout.scheme);
			super.grid.add();
		}
	}

	@Override
	protected void destroyGrid() {
		if(location == null && super.grid != null) {
			super.grid.remove();
			super.grid = null;
		}
	}
	
	public void refer(Location location) {
		if(super.frame != null) {
			super.frame.remove();
			super.frame = null;
		}
		super.refer(location);
	}
}
