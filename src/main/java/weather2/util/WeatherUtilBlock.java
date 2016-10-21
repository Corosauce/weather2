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
		int fillPerTick = 3000;
		//use snow for now, make sand block after
		
		//snow has 8 layers till its a full solid block (full solid on 8th layer)
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = world.getHeight(posSourcei).getY();
		int y = ySource;
		
		//override y so it scans from where player is
		y = (int) posSource.yCoord;
		
		float startScan = fillDistance;
		
		Vec3 posLastNonWall = new Vec3(posSource);
		
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
    		} else {
    			posLastNonWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
    		}
		}
		
		boolean radialWay = true;
		
		//make dynamic depending on dist, see particle code for algo
		
		
		/**
		 * Scan in a pattern that sand would spread in IRL
		 * needs to scan in an arc, 360, cant assume we actually hit wall, but scanning will avoid filling up a wall of course
		 * - hit wall, spread dist 1 block, scan forward
		 * - scan left and right of decreasing angle
		 * - after full angle scan, repeat with larger block dist, and smaller angle jump amount to account for distance from center (use even particle spread algo for that)
		 * - 
		 * 
		 * still needs code to support dropping sand down on lower blocks
		 */
		
		float angleScanRes = 1;
		float spreadDist = 20;
		int amountToFill = fillPerTick;
		int maxFallDist = 20;
		
		//prevents trying to add sand to same position twice due to how trig code rounds to nearest block coord
		List<BlockPos> listProcessedFilter = new ArrayList<BlockPos>();
		
		//TEMP OVERRIDE!!!! set pos to player
		//posLastNonWall = posSource;
		
		amountToFill = trySpreadOnPos2(world, new BlockPos(posLastNonWall.xCoord, posLastNonWall.yCoord, posLastNonWall.zCoord), amountToFill, 2, maxFallDist);
		
		//distance
		boolean doRadius = true;
		if (doRadius) {
			for (float i = 1; i < spreadDist && amountToFill > 0; i += 0.75F) {
				
				//int amountToAddBasedOnDist = (int) (((float)snowMetaMax / spreadDist) * (float)i);
				
				/**
				 * for making it add less sand to each block the more distant it is from where the sand "landed"
				 * TODO: make this formula not suck for other spreadDist sizes, currently manually tweaked
				 */
				int amountToAddBasedOnDist = (int) (((float)snowMetaMax+1F) - (i*1.5F));
				if (amountToAddBasedOnDist < 1) amountToAddBasedOnDist = 1;
				
				//temp
				amountToAddBasedOnDist = 2;
				
				//radial
				for (float angle = 0; angle <= 180 && amountToFill > 0; angle += angleScanRes) {
					
					//left/right
					for (int mode = 0; mode <= 1 && amountToFill > 0; mode++) {
						
						float orientationMulti = 1F;
						if (mode == 1) {
							orientationMulti = -1F;
						}
						double vecX = (-Math.sin(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		double vecZ = (Math.cos(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		
			    		int x = MathHelper.floor_double(posLastNonWall.xCoord + vecX);
			    		int z = MathHelper.floor_double(posLastNonWall.zCoord + vecZ);
			    		
			    		//fix for derp y
			    		y = (int)posLastNonWall.yCoord;
			    		
			    		BlockPos pos = new BlockPos(x, y, z);
			    		
			    		IBlockState state = world.getBlockState(pos);
			    		if (!listProcessedFilter.contains(pos)) {
			    			listProcessedFilter.add(pos);
			    			amountToFill = trySpreadOnPos2(world, pos, amountToFill, amountToAddBasedOnDist, maxFallDist);
			    		}
					}
				}
			}
		}
		
		System.out.println("leftover: " + amountToFill);
		
		if (radialWay) return;
		
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
    		/*if (tryNew) {
	    		if (state.getMaterial() == Material.AIR || state.getBlock() == Blocks.SNOW_LAYER) {
	    			int extraFill = fillPerTick;
	    			int height = 0;
	    			if (state.getBlock() == Blocks.SNOW_LAYER) {
	    				height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
	    			}
	    			int leftover = recurseSpreadSand(world, pos, pos, pos, extraFill, 1);
	    			break;
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
    		}*/
		}
	}
	
	public static int trySpreadOnPos2(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed) {
		
		/**
		 * - check pos for solid
		 * - if air, tick down till not air or drop limit
		 * - at first non air, find first block with up face solid or snow block
		 * - set air to everything between not air and up face solid or snow block (2 high tall grass removal)
		 * - 
		 * - run code that sets snow, deals with solid up face or existing snow, fully or partially layered
		 * 
		 * 
		 */
		
		//must have clear air above first spots
		//TODO: might need special case so we can fill up a partially layered snow block
		if (world.getBlockState(posSpreadTo.add(0, 1, 0)).getMaterial() != Material.AIR) {
			return amount;
		}
		
		IBlockState statePos = world.getBlockState(posSpreadTo);
		
		BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
		IBlockState stateCheckNonAir = world.getBlockState(posCheckNonAir);
		
		int depth = 0;
		
		//find first non air
		while (stateCheckNonAir.getMaterial() == Material.AIR) {
			posCheckNonAir = posCheckNonAir.add(0, -1, 0);
			stateCheckNonAir = world.getBlockState(posCheckNonAir);
			depth++;
			//bail if drop too far, aka sand/snow fully particleizes
			if (depth > maxDropAllowed) {
				return amount;
			}
		}
		
		BlockPos posCheckPlaceable = new BlockPos(posCheckNonAir);
		IBlockState stateCheckPlaceable = world.getBlockState(posCheckPlaceable);
		
		int distForPlaceableBlocks = 0;
		
		while (true && distForPlaceableBlocks < 10) {
			//if can be placed into, continue
			if (stateCheckPlaceable.getBlock().isReplaceable(world, posCheckPlaceable)) {
				posCheckPlaceable = posCheckPlaceable.add(0, -1, 0);
				stateCheckPlaceable = world.getBlockState(posCheckPlaceable);
				distForPlaceableBlocks++;
				continue;
			//if its the kind of solid we want, break loop
			} else if (stateCheckPlaceable.isSideSolid(world, posCheckPlaceable, EnumFacing.UP) || 
					stateCheckPlaceable.getBlock() == Blocks.SNOW_LAYER) {
				break;
			//its something we cant stack onto
			} else {
				System.out.println("found unstackable block: " + stateCheckPlaceable);
				return amount;
			}
		}
		
		//for some reason theres 10+ blocks of half solid blocks, lets just abort
		if (distForPlaceableBlocks >= 10) {
			return amount;
		}
		
		//at this point the block we are about to work with is solid facing up, or snow
		if (!stateCheckPlaceable.isSideSolid(world, posCheckPlaceable, EnumFacing.UP) && 
					stateCheckPlaceable.getBlock() != Blocks.SNOW_LAYER) {
			System.out.println("shouldnt be, failed a check somewhere!");
			return amount;
		}
		
		//lets clear out the blocks we found between air and solid or snow block
		for (int i = 0; i < distForPlaceableBlocks; i++) {
			world.setBlockState(posCheckNonAir.add(0, -i, 0), Blocks.AIR.getDefaultState());
		}
		
		BlockPos posPlaceSnow = new BlockPos(posCheckPlaceable);
		IBlockState statePlaceSnow = world.getBlockState(posPlaceSnow);
		
		int amountToAdd = amountAllowedToAdd;
		
		//add in the amount of air blocks we found
		//distForPlaceableBlocks += depth;
		
		//just place while stuff to add and air above
		
		while (amountAllowedToAdd > 0 && world.getBlockState(posCheckPlaceable.add(0, 1, 0)).getMaterial() == Material.AIR) {
			//if no more layers to add
			if (amountAllowedToAdd <= 0) {
				break;
			}
			//if its snow we can add snow to
			if (statePlaceSnow.getBlock() == Blocks.SNOW_LAYER && ((Integer)statePlaceSnow.getValue(BlockSnow.LAYERS)).intValue() < snowMetaMax) {
				int height = ((Integer)statePlaceSnow.getValue(BlockSnow.LAYERS)).intValue();
				//if (height < snowMetaMax) {
					height += amountAllowedToAdd;
					if (height > snowMetaMax) {
						amountAllowedToAdd = height - snowMetaMax;
						height = snowMetaMax;
						
					} else {
						amountAllowedToAdd = 0;
					}
					try {
						world.setBlockState(posPlaceSnow, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					//if we maxed it, up the val
					if (height == snowMetaMax) {
						posPlaceSnow = posPlaceSnow.add(0, 1, 0);
						statePlaceSnow = world.getBlockState(posPlaceSnow);
					}
				//}
			//solid block------------------- or air because we moved up 1 due to the previous being fully filled snow
			} else if (statePlaceSnow.isSideSolid(world, posPlaceSnow, EnumFacing.UP)) {
				posPlaceSnow = posPlaceSnow.add(0, 1, 0);
				statePlaceSnow = world.getBlockState(posPlaceSnow);
			//air
			} else if (statePlaceSnow.getMaterial() == Material.AIR) {
				//copypasta, refactor/reduce once things work
				int height = amountAllowedToAdd;
				if (height > snowMetaMax) {
					amountAllowedToAdd = height - snowMetaMax;
					height = snowMetaMax;
					
				} else {
					amountAllowedToAdd = 0;
				}
				try {
					world.setBlockState(posPlaceSnow, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//if we maxed it, up the val
				if (height == snowMetaMax) {
					posPlaceSnow = posPlaceSnow.add(0, 1, 0);
					statePlaceSnow = world.getBlockState(posPlaceSnow);
				}
			} else {
				System.out.println("wat! - " + statePlaceSnow);
			}
		}
		
		if (amountAllowedToAdd < 0) {
			System.out.println("wat");
		}
		int amountAdded = amountToAdd - amountAllowedToAdd;
		amount -= amountAdded;
		return amount;
		
	}
	
	public static int trySpreadOnPos(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed) {
		
		amount -= amountAllowedToAdd;
		
		BlockPos posSpreadToNew = canSpreadToOrGetAdjustedPos(world, posSpreadTo, snowMetaMax+1, maxDropAllowed);
		//verifies its snow or air with solid under it, use val snowMetaMax+1 to enforce always spread
		if (posSpreadToNew != null) {
			IBlockState state = world.getBlockState(posSpreadToNew);
			int height = 0;
			if (state.getBlock() == Blocks.SNOW_LAYER) {
				height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
			}
			//int extraFill = amount;
			if (height < snowMetaMax) {
				height += amountAllowedToAdd;
				if (height > snowMetaMax) {
					amountAllowedToAdd = height - snowMetaMax;
					height = snowMetaMax;
				} else {
					amountAllowedToAdd = 0;
				}
				try {
					world.setBlockState(posSpreadToNew, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
		}
		
		return amount + amountAllowedToAdd;
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
	/*public static int recurseSpreadSand(World world, BlockPos posOriginalSource, BlockPos posSpreadFrom, BlockPos posSpreadTo, int amount, int currentRecurseDepth) {
		
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
	}*/
	
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
	/*public static BlockPos getPosToSpreadOn(World world, Random rand, BlockPos source, BlockPos exclude, int sourceHeight) {
		List<BlockPos> listPositions = new ArrayList<BlockPos>();
		listPositions.add(new BlockPos(source.add(-1, 0, 0)));
		listPositions.add(new BlockPos(source.add(1, 0, 0)));
		listPositions.add(new BlockPos(source.add(0, 0, -1)));
		listPositions.add(new BlockPos(source.add(0, 0, 1)));
		//BlockPos pos = null;
		
		while (listPositions.size() > 0) {
			int randVal = rand.nextInt(listPositions.size());
			BlockPos pos = listPositions.get(randVal);
			if ((exclude != null && pos.equals(exclude)) || !canSpreadToOrGetAdjustedPos(world, pos, sourceHeight)) {
				listPositions.remove(randVal);
			} else {
				return pos;
			}
		}
		
		return null;
	}*/
	
	/**
	 * Originally named canSpreadTo with a boolean return, now returns null for false, and returns adjusted BlockPos for "falling down" areas where its a cliff with lots of air blocks
	 * 
	 * @param world
	 * @param pos
	 * @param sourceAmount
	 * @return
	 */
	public static BlockPos canSpreadToOrGetAdjustedPos(World world, BlockPos pos, int sourceAmount, int maxDropAllowed) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.SNOW_LAYER && ((Integer)state.getValue(BlockSnow.LAYERS)).intValue() < snowMetaMax) {
			return pos;
			//int height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
			/*if (height < sourceAmount) {
				return pos;
			}*/
		//pretty sure we can assume its solid under for this one
		} else if (state.getBlock().isReplaceable(world, pos) && state.isSideSolid(world, pos.add(0, -1, 0), EnumFacing.UP)) {
			return pos;
		} else if (state.getMaterial() == Material.AIR) {
			BlockPos bestPos = new BlockPos(pos);
			int dropDist = 0;
			boolean foundSolid = false;
			while (dropDist++ < maxDropAllowed) {
				
				IBlockState checkState = world.getBlockState(bestPos.add(0, -1, 0));
				if (checkState.getMaterial() == Material.AIR) {
					bestPos = bestPos.add(0, -1, 0);
					continue;
				} else {
					if (checkState.isSideSolid(world, pos.add(0, -1, 0), EnumFacing.UP) || 
							(checkState.getBlock() == Blocks.SNOW_LAYER && ((Integer)checkState.getValue(BlockSnow.LAYERS)).intValue() == snowMetaMax)) {
						//return spot above solid
						return bestPos;
					} else if (state.getBlock().isReplaceable(world, pos)) {
						//return the assumed solid block under it
						return bestPos.add(0, -1, 0);
					}
				}
				
			}
			/*if (world.getBlockState(pos.add(0, -1, 0)).isSideSolid(world, pos.add(0, -1, 0), EnumFacing.UP)) {
				return pos;
			}*/
		}
		return null;
	}
}
