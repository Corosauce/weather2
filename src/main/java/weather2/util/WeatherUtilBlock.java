package weather2.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import CoroUtil.util.Vec3;

public class WeatherUtilBlock {
	
	public static int snowMetaMax = 8;

	/**
	 * Fill direction up with a block, as if flowing particles filled the area up
	 * 
	 * - calculate endpoint and try to fill that up first, the propegate back
	 * - 
	 * 
	 */
	public static void floodAreaWithSand(World world, Vec3 posSource, float directionYaw, float fillDistance, float fillWideness) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		float thickness = 1F;
		float tickStep = 0.75F;
		int fillPerTick = 100;
		//use snow for now, make sand block after
		
		//snow has 8 layers till its a full solid block (full solid on 8th layer)
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = world.getHeight(posSourcei).getY();
		int y = ySource;
		float startScan = fillDistance;
		
		//scan outwards to find closest wall
		for (float i = 0; i < fillDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor_double(posSource.xCoord + vecX);
    		int z = MathHelper.floor_double(posSource.zCoord + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			break;
    		}
		}
		
		//scan inwards from the non air block we found
		for (float i = startScan; i > 0; i -= tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor_double(posSource.xCoord + vecX);
    		int z = MathHelper.floor_double(posSource.zCoord + vecZ);
    		
    		//world.getHeight(new BlockPos(x, 0, z)).getY();
    		
    		boolean foundSpotToFill = false;
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		//IBlockState state = world.getBlockState(new BlockPos(xxx + x, setBlockHeight, zzz + z));
    		/*int meta = state.getBlock().getMetaFromState(state);
    		if (meta < snowMetaMax) {
        		meta += 1;
    		}*/
    		
    		boolean tryNew = true;
    		
    		//new
    		if (tryNew) {
	    		if (state.getMaterial() == Material.AIR || state.getBlock() == Blocks.SNOW_LAYER) {
	    			int extraFill = fillPerTick;
	    			int height = 0;
	    			if (state.getBlock() == Blocks.SNOW_LAYER) {
	    				height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
	    			}
	    			int leftover = recurseSpreadSand(world, pos, pos, pos, extraFill, 1);
	    			break;
					//BlockPos nextPos = getPosToSpreadOn(world, world.rand, pos, null, height);
					/*if (nextPos != null) {
						
					}*/
	    		}
    		} else {
    		
	    		if (state.getMaterial() == Material.AIR) {
	    			world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, fillPerTick));
	    			foundSpotToFill = true;
	    			break;
	    		} else if (state.getBlock() == Blocks.SNOW_LAYER) {
	    			foundSpotToFill = true;
	    			//1 - 8
	    			int height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
	    			int extraFill = 0;
	    			if (height <= snowMetaMax-1) {
	    				height += fillPerTick;
	    				if (height > snowMetaMax) {
	    					extraFill = height - snowMetaMax;
	    					height = snowMetaMax;
	    				}
	    				world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
	    				if (extraFill > 0) {
	    					
	    					BlockPos nextPos = getPosToSpreadOn(world, world.rand, pos, null, height);
	    					if (nextPos != null) {
	    						int leftover = recurseSpreadSand(world, pos, pos, nextPos, extraFill, 1);
	    					}
	    				}
	    				System.out.println("extra to fill: " + extraFill);
	    				break;
	    			} else {
	    				//full from the start, treat like a wall
	    				//extraFill = fillPerTick;
	    				extraFill = fillPerTick;
	    				BlockPos nextPos = getPosToSpreadOn(world, world.rand, pos, null, height);
						if (nextPos != null) {
							int leftover = recurseSpreadSand(world, pos, pos, nextPos, extraFill, 1);
						}
	    			}
	    			
	    			//world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, fillPerTick));
	    			
	    		}
    		}
		}
	}
	
	/**
	 * Spreads out sand, since spread will be circular, use posOriginalSource to determine how far out this method has gone to prevent endless recursion in some scenarios
	 * 
	 * to start out, it just tries the blocks next to it, if they are all equal or higher of height, the method stops
	 * in future, try to "push out" those heights more, having it continue to search around
	 * 
	 * @param world
	 * @param posOriginalSource
	 * @param posSpreadFrom
	 * @param posSpreadTo Either LAYER block of lower height meta or air
	 * @param amount
	 * @param currentRecurseDepth
	 */
	public static int recurseSpreadSand(World world, BlockPos posOriginalSource, BlockPos posSpreadFrom, BlockPos posSpreadTo, int amount, int currentRecurseDepth) {
		
		IBlockState state = world.getBlockState(posSpreadTo);
		int height = 0;
		if (state.getBlock() == Blocks.SNOW_LAYER) {
			height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
		}
		//int extraFill = amount;
		if (height <= snowMetaMax-1) {
			height += amount;
			if (height > snowMetaMax) {
				amount = height - snowMetaMax;
				height = snowMetaMax;
			} else {
				amount = 0;
			}
			world.setBlockState(posSpreadTo, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
			
		}
		
		if (amount > 0) {
			BlockPos nextPos = getPosToSpreadOn(world, world.rand, posSpreadTo, posSpreadFrom, height);
			if (nextPos != null) {
				amount = recurseSpreadSand(world, posOriginalSource, posSpreadTo, nextPos, amount, currentRecurseDepth+1);
			}
		}
		
		return amount;
	}
	
	/*public static BlockPos getNextRandomPosition(Random rand, BlockPos source, BlockPos exclude) {
		BlockPos pos = null;
		while (pos == null || pos.equals(exclude)) {
			int randVal = rand.nextInt(4);
			if (randVal == 1) {
				pos = new BlockPos(source.add(-1, 0, 0));
			} else if (randVal == 2) {
				pos = new BlockPos(source.add(1, 0, 0));
			} else if (randVal == 3) {
				pos = new BlockPos(source.add(0, 0, -1));
			} else if (randVal == 4) {
				pos = new BlockPos(source.add(0, 0, 1));
			}
		}
		return pos;
	}*/
	
	/**
	 * TODO: what about drops down where theres air for a few blocks before theres ground?
	 * 
	 * @param world
	 * @param rand
	 * @param source
	 * @param exclude
	 * @param sourceHeight
	 * @return
	 */
	public static BlockPos getPosToSpreadOn(World world, Random rand, BlockPos source, BlockPos exclude, int sourceHeight) {
		List<BlockPos> listPositions = new ArrayList<BlockPos>();
		listPositions.add(new BlockPos(source.add(-1, 0, 0)));
		listPositions.add(new BlockPos(source.add(1, 0, 0)));
		listPositions.add(new BlockPos(source.add(0, 0, -1)));
		listPositions.add(new BlockPos(source.add(0, 0, 1)));
		//BlockPos pos = null;
		
		while (listPositions.size() > 0) {
			int randVal = rand.nextInt(listPositions.size());
			BlockPos pos = listPositions.get(randVal);
			if ((exclude != null && pos.equals(exclude)) || !canSpreadTo(world, pos, sourceHeight)) {
				listPositions.remove(randVal);
			} else {
				return pos;
			}
		}
		
		return null;
		
		/*while (pos == null || pos.equals(exclude)) {
			int randVal = rand.nextInt(4);
			if (randVal == 1) {
				pos = new BlockPos(source.add(-1, 0, 0));
			} else if (randVal == 2) {
				pos = new BlockPos(source.add(1, 0, 0));
			} else if (randVal == 3) {
				pos = new BlockPos(source.add(0, 0, -1));
			} else if (randVal == 4) {
				pos = new BlockPos(source.add(0, 0, 1));
			}
		}
		return pos;*/
	}
	
	public static boolean canSpreadTo(World world, BlockPos pos, int sourceAmount) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.SNOW_LAYER) {
			int height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
			if (height < sourceAmount) {
				return true;
			}
		} else if (state.getMaterial() == Material.AIR) {
			if (world.getBlockState(pos.add(0, -1, 0)).isSideSolid(world, pos.add(0, -1, 0), EnumFacing.UP)) {
				return true;
			}
		}
		return false;
	}
}
