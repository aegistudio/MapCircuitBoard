package net.aegistudio.mcb.board;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.ItemFrame;
import org.bukkit.metadata.FixedMetadataValue;

import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.MapCircuitBoard;

public class BlockPropagatePolicy implements PropagatePolicy {
	public final MapCircuitBoard plugin;
	
	public BlockPropagatePolicy(MapCircuitBoard circuitBoard) {
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new TorchPowerListener(circuitBoard), circuitBoard);
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new LampPowerListener(circuitBoard), circuitBoard);
		
		this.plugin = circuitBoard;
	}
	
	@Override
	public boolean in(Location location, Facing side, CircuitBoardCanvas canvas, ItemFrame frame) {
		Block block = location.getBlock();
		if(block.getType() == Material.AIR) return false;
		
		int power = block.getBlockPower();
		Propagator propagate = new Propagator();
		propagate.setVoltage(2 * power);
		propagate.propagateVoltage(canvas.grid, side);
		return true;
	}

	@Override
	public boolean out(Location location, Facing face, CircuitBoardCanvas canvas, ItemFrame frame) {
		Block block = location.getBlock();
		if(block.getType() == Material.AIR) return false;
		
		Propagator propagate = new Propagator();
		propagate.setVoltage(-1);
		propagate.retrieveVoltage(canvas.grid, face);
		if(propagate.getVoltage() < 0) return true;
		
		int newVoltage = propagate.getVoltage() / 2;
		setBlockPower(frame.getLocation(), block, newVoltage, true);
		
		return true;
	}
	
	public static final String REDSTONE_STATE = "redstoneState";
	
	@SuppressWarnings("deprecation")
	public void setBlockPower(Location from, Block block, int power, boolean cascade) {
		switch(block.getType()) {
			case REDSTONE_WIRE:
				block.setData((byte)Math.min(15, power));
			break;
			
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
				byte meta = block.getData();
				
				block.setType(power > 0? 
					Material.REDSTONE_TORCH_OFF: 
					Material.REDSTONE_TORCH_ON, true);
				
				block = block.getLocation().getBlock();
				
				block.setData(meta, true);
				block.setMetadata(REDSTONE_STATE, new FixedMetadataValue(plugin, power > 0? 0 : 15));
			break;

			case WOODEN_DOOR:
			case BIRCH_DOOR:
			case ACACIA_DOOR:
			case JUNGLE_DOOR:
			case DARK_OAK_DOOR:
			case IRON_DOOR:
				byte data = block.getData();
				block.setData((byte) ((data & 0x03) | (power != 0? 4 : 0)));
			break;
			
			case DISPENSER:
				Dispenser state = (Dispenser) block.getState();
				if(block.getData() == 4 && power > 0)
					state.dispense();
				block.setData((byte)(power > 0? 12 : 4));
			break;
			
			case REDSTONE_LAMP_ON:
			case REDSTONE_LAMP_OFF:
				if(power > 0) {
					BlockState fromstate = from.getBlock().getState();
					Material material = fromstate.getType();
					byte rawdata = fromstate.getRawData();
					
					from.getBlock().setType(Material.REDSTONE_BLOCK, false);
					
					block.setType(Material.REDSTONE_LAMP_ON);
					
					from.getBlock().setTypeIdAndData(material.getId(),
							rawdata, false);
				}
				else block.setType(Material.REDSTONE_LAMP_OFF, true);
				
				block = block.getLocation().getBlock();
				block.setMetadata(REDSTONE_STATE, new FixedMetadataValue(plugin, power));
			break;
			
			default:
				
			break;
		}
	}
}
