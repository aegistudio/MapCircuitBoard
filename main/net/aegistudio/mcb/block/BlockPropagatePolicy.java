package net.aegistudio.mcb.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.metadata.FixedMetadataValue;

import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.MapCircuitBoard;
import net.aegistudio.mcb.board.CircuitBoardCanvas;
import net.aegistudio.mcb.board.PropagatePolicy;
import net.aegistudio.mcb.board.Propagator;

public class BlockPropagatePolicy implements PropagatePolicy {
	public final MapCircuitBoard plugin;
	
	public BlockPropagatePolicy(MapCircuitBoard circuitBoard) {
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new SimplePowerListener(Material.REDSTONE_TORCH_OFF, i -> i != 0, circuitBoard), circuitBoard);
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new SimplePowerListener(Material.DIODE_BLOCK_ON, i -> i == 0, circuitBoard), circuitBoard);
		//circuitBoard.getServer().getPluginManager()
		//	.registerEvents(new SimplePowerListener(Material.REDSTONE_COMPARATOR_ON, i -> i == 0, circuitBoard), circuitBoard);
		
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new LampPowerListener(circuitBoard), circuitBoard);
		
		// Maybe the predictor could be optimized.
		circuitBoard.getServer().getPluginManager()
			.registerEvents(new SimplePowerListener(Material.REDSTONE_WIRE, i -> true, circuitBoard), circuitBoard);
		
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
		Consumer<BlockState> redstoneInventory = null;		// Used only in Dropper and Dispenser.
		
		Function<Integer, Material> redstoneOnOff = null;
		Function<Integer, Integer> shouldPower = null;		// Used only in comparator, repeater and torch.
		
		switch(block.getType()) {
			case REDSTONE_WIRE:
				block.setData((byte)Math.min(15, power), true);
				block.setMetadata(REDSTONE_STATE, new FixedMetadataValue(plugin, Math.min(15, power)));
			break;
			
			case REDSTONE_COMPARATOR_OFF:
			case REDSTONE_COMPARATOR_ON:
				/*
				BlockState comparatorState = block.getState();
				Comparator comparator = (Comparator) comparatorState.getData();
				System.out.println(comparator.getFacing());
				BlockFace face = block.getFace(from.getBlock());
				*/
			break;
					
			case DIODE_BLOCK_OFF:
			case DIODE_BLOCK_ON:
				if(redstoneOnOff == null)
					redstoneOnOff = p -> p > 0? 
							Material.DIODE_BLOCK_ON:
							Material.DIODE_BLOCK_OFF;
				if(shouldPower == null)
					shouldPower = p -> p > 0? 15 : 0;
					
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
				if(redstoneOnOff == null)
					redstoneOnOff = p -> p > 0? 
							Material.REDSTONE_TORCH_OFF:
							Material.REDSTONE_TORCH_ON;
				if(shouldPower == null)
						shouldPower = p -> p > 0? 0 : 15;
				byte meta = block.getData();
				
				block.setTypeIdAndData(redstoneOnOff.apply(power).getId(), meta, true);
				
				block = block.getLocation().getBlock();
				block.getState().update(true, true);
				
				block.setMetadata(REDSTONE_STATE, new FixedMetadataValue(plugin, shouldPower.apply(power)));
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
			
			case DROPPER:
				if(redstoneInventory == null)
					redstoneInventory = state -> ((Dropper)state).drop();
			case DISPENSER:
				if(redstoneInventory == null)
					redstoneInventory = state -> ((Dispenser)state).dispense();
				try {
					final Consumer<BlockState> capturedRedstoneInventory = redstoneInventory;
					final BlockState capturedBlockState = block.getState();
					if(block.getData() < 8 && power > 0)
						plugin.getServer().getScheduler().runTask(plugin, 
								() -> capturedRedstoneInventory.accept(capturedBlockState));
				}
				catch(Throwable e) {
					e.printStackTrace();
				}
				block.setData((byte)((power > 0? 8 : 0) | (0x07 & block.getData())));
			break;
			
			case REDSTONE_LAMP_ON:
			case REDSTONE_LAMP_OFF:
				if(power > 0) 
					block.setType(Material.REDSTONE_LAMP_ON, true);
				else block.setType(Material.REDSTONE_LAMP_OFF, true);
				
				block = block.getLocation().getBlock();
				block.getState().update(true, true);
				block.setMetadata(REDSTONE_STATE, new FixedMetadataValue(plugin, power));
			break;
			
			case PISTON_BASE:
			case PISTON_STICKY_BASE:
				/*
				BlockState state = block.getState();
				PistonBaseMaterial base = (PistonBaseMaterial) state.getData();
				base.setPowered(power > 0? true : false);
				state.setData(base);
				state.update(true, true);
				*/
				
				BlockState state = block.getState();
				PistonBaseMaterial base = (PistonBaseMaterial) state.getData();
				List<Block> blockList = new ArrayList<Block>();
				BlockFace face = base.getFacing();
				Block pointerBlock = block.getRelative(face);
				boolean nonBlock = true;
				for(int i = 0; i < 15; i ++) 
					if(pointerBlock.getPistonMoveReaction() == PistonMoveReaction.BLOCK) {
						nonBlock = false;
						break;
					}
					else {
						blockList.add(pointerBlock);
						if(pointerBlock.getPistonMoveReaction() == PistonMoveReaction.BREAK)
							break;
						else pointerBlock = pointerBlock.getRelative(face);
					}
				
				if(nonBlock)
					plugin.getServer().getPluginManager().callEvent(power > 0?
							new BlockPistonExtendEvent(block, 15, face):
							new BlockPistonRetractEvent(block, blockList, face));
			break;
			
			default:
			break;
		}
		
		if(cascade) {
			if(!block.getType().isTransparent()) {
				BiConsumer<BlockFace, Location> offset =
						(b, l) -> {
							Location target = l.clone().add(b.getModX(), b.getModY(), b.getModZ());
							setBlockPower(l, target.getBlock(), power, false);
						};
						
				offset.accept(BlockFace.UP, block.getLocation());
				offset.accept(BlockFace.DOWN, block.getLocation());
				offset.accept(BlockFace.EAST, block.getLocation());
				offset.accept(BlockFace.WEST, block.getLocation());
				offset.accept(BlockFace.NORTH, block.getLocation());
				offset.accept(BlockFace.SOUTH, block.getLocation());
			}
		}
	}
}
