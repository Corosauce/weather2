package weather2.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import CoroUtil.forge.CULog;
import net.minecraft.util.math.*;
import weather2.CommonProxy;
import weather2.block.BlockSandLayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import CoroUtil.util.Vec3;

/**
 * All stackable block code in this class considers "height" as a meta val height, not actual pixel height of AABB, basically 1 meta height = 2 pixel height, this is also used for all amount values
 * 
 * @author Corosus
 *
 */
public class WeatherUtilBlock {
	
	public static int layerableHeightPropMax = 8;

	public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
		fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4);
	}

	public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff) {
		
		/**
		 * for now, work in halves
		 * if "wall" is 4 height (aka 8 pixels high) or less, we can "go over it" aka continue onto next block past it
		 * 
		 * starting point needs to be air above solid
		 * 
		 * scan forward till not air or not placeable
		 * 
		 * get block height, if height < 4
		 * - place infront of wall
		 * if height >= 4
		 * - progress onto it and continue past it
		 * 
		 * 
		 * 
		 * - factor in height of current block we are on if its not air, aka half filled sand block vs next block
		 */
		
		//fix for starting on a layerable block
		IBlockState stateTest = world.getBlockState(posSource.toBlockPos());
		if (stateTest.getBlock() == blockLayerable) {
			int heightTest = getHeightForAnyBlock(stateTest);
			if (heightTest < 8) {
				//posSource = new Vec3(posSource.addVector(0, -1, 0));
			}
		}
		
		BlockPos posSourcei = posSource.toBlockPos();
		//int ySource = world.getHeight(posSourcei).getY();
		int y = posSourcei.getY();
		float tickStep = 0.75F;
		
		//float startScan = scanDistance;
		
		Vec3 posLastNonWall = new Vec3(posSource);
		Vec3 posWall = null;
		
		BlockPos lastScannedPosXZ = null;//new BlockPos(posSourcei);
		
		//System.out.println("Start block (should be air): " + world.getBlockState(posSourcei));
		
		int previousBlockHeight = 0;
		
		//looking for a proper wall we cant fly over as sand
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.xCoord + vecX);
    		int z = MathHelper.floor(posSource.zCoord + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		BlockPos posXZ = new BlockPos(x, 0, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		if (lastScannedPosXZ == null || !posXZ.equals(lastScannedPosXZ)) {
    		
	    		lastScannedPosXZ = new BlockPos(posXZ);

				AxisAlignedBB aabbCompare = new AxisAlignedBB(pos);
				List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
				state.addCollisionBoxToList(world, pos, aabbCompare, listAABBCollision, null, false);

				//if solid ground we can place on
	    		if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.PLANTS && (!state.getBlock().isReplaceable(world, pos) && !listAABBCollision.isEmpty())) {
	    			BlockPos posUp = new BlockPos(x, y + 1, z);
	    			IBlockState stateUp = world.getBlockState(posUp);
					//if above it is air
	    			if (stateUp.getMaterial() == Material.AIR) {
		    			int height = getHeightForAnyBlock(state);
		    			
		    			//if height of block minus block we are on/comparing against is short enough, we can continue onto it
		    			if (height - previousBlockHeight <= heightDiff) {
		    				//if block we are progressing to is a full block, reset height val
		    				if (height == 8) {
		    					previousBlockHeight = 0;
			    				y++;
		    				} else {
		    					previousBlockHeight = height;
		    				}
		    				
		    				posLastNonWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
		    				
		    				//System.out.println(posLastNonWall);
		    				
		    				continue;
		    			//too high, count as a wall
		    			} else {
		    				posWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
		    				break;
		    			}
		    		//hit a wall
	    			} else {
	    				posWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
	    				break;
	    			}
	    			
	    			//startScan = i;
	    			//posWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
	    			//break;
	    		} else {
	    			posLastNonWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
	    		}
	    		
    		} else {
    			continue;
    		}
		}
		
		if (posWall != null) {
			int amountWeHave = 1;
			int amountToAddPerXZ = 1;
			
			IBlockState state = world.getBlockState(posWall.toBlockPos());
			IBlockState state1 = world.getBlockState(posLastNonWall.toBlockPos().add(1, 0, 0));
			IBlockState state22 = world.getBlockState(posLastNonWall.toBlockPos().add(-1, 0, 0));
			IBlockState state3 = world.getBlockState(posLastNonWall.toBlockPos().add(0, 0, 1));
			IBlockState state4 = world.getBlockState(posLastNonWall.toBlockPos().add(0, 0, -1));

			//check all around place spot for cactus and cancel if true, to prevent cactus pop off when we place next to it
			if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
					state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS) {
				return;
			}
			
			BlockPos pos2 = new BlockPos(posLastNonWall.xCoord, posLastNonWall.yCoord, posLastNonWall.zCoord);
			IBlockState state2 = world.getBlockState(pos2);
			if (state2.getMaterial() == Material.WATER || state2.getMaterial() == Material.LAVA) {
				return;
			}
			
			amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.xCoord, posLastNonWall.yCoord, posLastNonWall.zCoord), amountWeHave, amountToAddPerXZ, 10, blockLayerable);
		} else {
			//System.out.println("no wall found");
		}
	}
	
	public static void fillAgainstWall(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		float thickness = 1F;
		float tickStep = 0.75F;
		//int fillPerTick = amountToTakeOrFill;
		//use snow for now, make sand block after
		
		//snow has 7 layers till its a full solid block (full solid on 8th layer)
		//0 is nothing, 1-7, 8 is full
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = WeatherUtilBlock.getPrecipitationHeightSafe(world, posSourcei).getY();
		int y = ySource;
		
		//override y so it scans from where ground at coord is
		y = (int) posSource.yCoord;
		//for sandstorm we might want to scan upwards in some scenarios.... but what
		
		float startScan = scanDistance;
		
		Vec3 posLastNonWall = new Vec3(posSource);
		Vec3 posWall = null;
		
		//scan outwards to find closest wall
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.xCoord + vecX);
    		int z = MathHelper.floor(posSource.zCoord + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			posWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
    			break;
    		} else {
    			posLastNonWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
    		}
		}
		
		double distFromSourceToWall = posSource.distanceTo(posLastNonWall);
		
		//new code, check wall is high enough
		if (posWall != null) {
			BlockPos posCheck = new BlockPos(posWall.toBlockPos());
			int heightOfWall = 0;
			int heightNeeded = 2;
			while (heightOfWall++ < heightNeeded) {
				posCheck = posCheck.add(0, 1, 0);
				IBlockState stateCheck = world.getBlockState(posCheck);
				if (!stateCheck.isSideSolid(world, posCheck, EnumFacing.UP)) {
					break;
				}
			}
			
			if (heightOfWall >= heightNeeded) {
				int amountWeHave = 4;
				int amountToAddPerXZ = 2;
				
				amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.xCoord, posLastNonWall.yCoord, posLastNonWall.zCoord), amountWeHave, amountToAddPerXZ, 10, blockLayerable);
			}
		}
		
		
	}

	public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius/*, float takeRadius*/, Block blockLayerable, int amountToTakeOrFill) {
		floodAreaWithLayerableBlock(world, posSource, directionYaw, scanDistance, fillRadius, -1, blockLayerable/*, false*/, amountToTakeOrFill);
	}
	
	/**
	 * Fill direction up with a block, as if flowing particles filled the area up
	 * 
	 * Optional takeRadius param, if -1, dont use, otherwise it will 
	 * 
	 * - calculate endpoint and try to fill that up first, the propegate back
	 * - only propegate back if theres still more to fill, however, with new take radius, make sure not to just refil where we took from
	 * 
	 * Its possible that scanDistance is either not far enough or cant scan far enough
	 * - if low, fillRadius might fill into area that takeRadius processed
	 * -- possibly detect this state and prevent method from running and eating cpu
	 * 
	 * - I guess have method still scan ahead first:
	 * -- if scanDistance effective > fillRadius + takeRadius
	 * --- process
	 * -- if not
	 * --- abort, or maybe just do single block and not full radial mode, for more fine processing
	 * ---- do this only if posSource != posSource + scanDistance effective found pos
	 * 
	 */
	public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, float takeRadius, Block blockLayerable, int amountToTakeOrRelocate/*, boolean transferSandEqually*/) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		float thickness = 1F;
		float tickStep = 0.75F;
		//int fillPerTick = amountToTakeOrFill;
		//use snow for now, make sand block after
		
		//snow has 7 layers till its a full solid block (full solid on 8th layer)
		//0 is nothing, 1-7, 8 is full
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = WeatherUtilBlock.getPrecipitationHeightSafe(world, posSourcei).getY();
		int y = ySource;
		
		//override y so it scans from where ground at coord is
		y = (int) posSource.yCoord;
		//for sandstorm we might want to scan upwards in some scenarios.... but what
		
		float startScan = scanDistance;
		
		Vec3 posLastNonWall = new Vec3(posSource);
		Vec3 posWall = null;
		
		//scan outwards to find closest wall
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor(posSource.xCoord + vecX);
    		int z = MathHelper.floor(posSource.zCoord + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			posWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
    			break;
    		} else {
    			posLastNonWall = new Vec3(posSource.xCoord + vecX, y, posSource.zCoord + vecZ);
    		}
		}
		
		double distFromSourceToWall = posSource.distanceTo(posLastNonWall);

		boolean doRadius = true;
		
		/**
		 * If in take mode, if distance between determined fill point and where we will take from is overlapping (close), dont use radial to prevent redundant take then fill
		 */
		if (takeRadius != -1) {
			if (distFromSourceToWall <= 2) {
				doRadius = false;
			}
		}
		
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
		
		float angleScanResolution = 1;
		float spreadDist = fillRadius;
		//int amountToFill = amountToTakeOrRelocate;
		int maxFallDist = 20;
		
		int amountToTakePerXZ = 2;
		int amountToAddPerXZ = 2;
		
		int amountWeHave = 0;
		if (takeRadius != -1) {
			amountWeHave = tryTakeFromPos(world, posSourcei, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
		} else {
			amountWeHave = amountToTakeOrRelocate;
		}
		
		//prevents trying to add sand to same position twice due to how trig code rounds to nearest block coord
		List<BlockPos> listProcessedFilter = new ArrayList<BlockPos>();
		
		//TODO: radius for taking
		if (doRadius) {
			for (float i = 1; i < takeRadius/* && amountWeHave > 0*/; i += 0.75F) {
				
				int amountToAddBasedOnDist = 2;
				
				//radial
				for (float angle = 0; angle <= 180/* && amountWeHave > 0*/; angle += angleScanResolution) {
					
					//left/right
					for (int mode = 0; mode <= 1/* && amountWeHave > 0*/; mode++) {
						
						float orientationMulti = 1F;
						if (mode == 1) {
							orientationMulti = -1F;
						}
						double vecX = (-Math.sin(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		double vecZ = (Math.cos(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		
			    		int x = MathHelper.floor(posSource.xCoord + vecX);
			    		int z = MathHelper.floor(posSource.zCoord + vecZ);
			    		
			    		//fix for derp y
			    		y = (int)posSource.yCoord;
			    		
			    		BlockPos pos = new BlockPos(x, y, z);
			    		
			    		IBlockState state = world.getBlockState(pos);
			    		if (!listProcessedFilter.contains(pos)) {
			    			listProcessedFilter.add(pos);
			    			//amountWeHave = trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
			    			amountWeHave = tryTakeFromPos(world, pos, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
			    		}
					}
				}
			}
		}
		
		listProcessedFilter.clear();
		//TEMP OVERRIDE!!!! set pos to player
		//posLastNonWall = posSource;
		
		amountWeHave = trySpreadOnPos2(world, new BlockPos(posLastNonWall.xCoord, posLastNonWall.yCoord, posLastNonWall.zCoord), amountWeHave, amountToAddPerXZ, maxFallDist, blockLayerable);
		
		//TEMP!!!!
		//doRadius = false;
		
		//distance
		if (doRadius) {
			for (float i = 1; i < spreadDist && amountWeHave > 0; i += 0.75F) {
				
				//int amountToAddBasedOnDist = (int) (((float)snowMetaMax / spreadDist) * (float)i);
				
				/**
				 * for making it add less sand to each block the more distant it is from where the sand "landed"
				 * TODO: make this formula not suck for other spreadDist sizes, currently manually tweaked
				 */
				int amountToAddBasedOnDist = (int) (((float)layerableHeightPropMax+1F) - (i*1.5F));
				if (amountToAddBasedOnDist < 1) amountToAddBasedOnDist = 1;
				
				//temp
				amountToAddBasedOnDist = 2;
				
				//radial
				for (float angle = 0; angle <= 180 && amountWeHave > 0; angle += angleScanResolution) {
					
					//left/right
					for (int mode = 0; mode <= 1 && amountWeHave > 0; mode++) {
						
						float orientationMulti = 1F;
						if (mode == 1) {
							orientationMulti = -1F;
						}
						double vecX = (-Math.sin(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		double vecZ = (Math.cos(Math.toRadians(directionYaw - (angle * orientationMulti))) * (i));
			    		
			    		int x = MathHelper.floor(posLastNonWall.xCoord + vecX);
			    		int z = MathHelper.floor(posLastNonWall.zCoord + vecZ);
			    		
			    		//fix for derp y
			    		y = (int)posLastNonWall.yCoord;
			    		
			    		BlockPos pos = new BlockPos(x, y, z);
			    		
			    		//scan a bit higher than positions for better result
			    		Vec3d sourceTest = posSource.addVector(0, 1D, 0).toMCVec();
			    		Vec3d destTest = new Vec3d(x + 0.5F, y + 1.5F, z + 0.5F);
			    		
			    		RayTraceResult destFound = world.rayTraceBlocks(sourceTest, destTest, false);
			    		
			    		if (destFound == null) {
				    		IBlockState state = world.getBlockState(pos);
				    		if (!listProcessedFilter.contains(pos)) {
				    			listProcessedFilter.add(pos);
				    			amountWeHave = trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
				    		}
			    		}
					}
				}
			}
		}
		
		CULog.dbg("leftover: " + amountWeHave);
	}
	
	public static int tryTakeFromPos(World world, BlockPos posTakeFrom, int amount, int amountAllowedToTakeForXZ, int maxDropAllowed, Block blockLayerable) {
		
		int amountTaken = 0;
		
		IBlockState statePos = world.getBlockState(posTakeFrom);
		
		//
		if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && statePos.getBlock() != Blocks.AIR) {
			return amount;
		}
		
		int dropDist = 0;
		BlockPos posScan = new BlockPos(posTakeFrom);
		while (statePos.getBlock() == Blocks.AIR && dropDist++ < maxDropAllowed) {
			posScan = posScan.add(0, -1, 0);
			statePos = world.getBlockState(posScan);
			if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && statePos.getBlock() != Blocks.AIR) {
				return amount;
			}
		}
		
		//statePos should now be blockLayerable type
		int amountLeftToTake = amountAllowedToTakeForXZ;
		while (amountTaken < amountAllowedToTakeForXZ) {
			int amountReturn = takeHeightFromLayerableBlock(world, posScan, blockLayerable/*statePos.getBlock()*/, amountLeftToTake);
			amountTaken += amountReturn;
			posScan = posScan.add(0, -1, 0);
			statePos = world.getBlockState(posScan);
			//see if we can continue to take more
			if (!isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable)) {
				break;
			}
		}
		
		//return amount we could take + original fed in for additiveness
		return amount + amountTaken;
		
	}
	
	public static int trySpreadOnPos2(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable) {
		
		if (amount <= 0) return amount;
		
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
			//if can be placed into, continue, as long as its not our block as it is replacable at layer height 1
			AxisAlignedBB aabbCompare = new AxisAlignedBB(posCheckPlaceable);
			List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
			stateCheckPlaceable.addCollisionBoxToList(world, posCheckPlaceable, aabbCompare, listAABBCollision, null, false);

			if (stateCheckPlaceable.getBlock() != blockLayerable && stateCheckPlaceable.getBlock().isReplaceable(world, posCheckPlaceable) && listAABBCollision.isEmpty()) {
				posCheckPlaceable = posCheckPlaceable.add(0, -1, 0);
				stateCheckPlaceable = world.getBlockState(posCheckPlaceable);
				distForPlaceableBlocks++;
				continue;
			//if its the kind of solid we want, break loop
			} else if (stateCheckPlaceable.isSideSolid(world, posCheckPlaceable, EnumFacing.UP) || 
					stateCheckPlaceable.getBlock() == blockLayerable) {
				break;
			//its something we cant stack onto
			} else {
				//System.out.println("found unstackable block: " + stateCheckPlaceable);
				return amount;
			}
		}
		
		//for some reason theres 10+ blocks of half solid blocks, lets just abort
		if (distForPlaceableBlocks >= 10) {
			return amount;
		}
		
		//at this point the block we are about to work with is solid facing up, or snow
		if (!stateCheckPlaceable.isSideSolid(world, posCheckPlaceable, EnumFacing.UP) && 
					stateCheckPlaceable.getBlock() != blockLayerable) {
			CULog.err("sandstorm: shouldnt be, failed a check somewhere!");
			return amount;
		}
		
		//lets clear out the blocks we found between air and solid or snow block
		for (int i = 0; i < distForPlaceableBlocks; i++) {
			world.setBlockState(posCheckNonAir.add(0, -i, 0), Blocks.AIR.getDefaultState());
		}
		
		BlockPos posPlaceLayerable = new BlockPos(posCheckPlaceable);
		IBlockState statePlaceLayerable = world.getBlockState(posPlaceLayerable);
		
		int amountToAdd = amountAllowedToAdd;
		
		//add in the amount of air blocks we found
		//distForPlaceableBlocks += depth;
		
		//just place while stuff to add and air above
		
		while (amountAllowedToAdd > 0 && world.getBlockState(posPlaceLayerable.add(0, 1, 0)).getMaterial() == Material.AIR) {
			//if no more layers to add
			if (amountAllowedToAdd <= 0) {
				break;
			}
			//if its snow we can add snow to
			if (statePlaceLayerable.getBlock() == blockLayerable && getHeightForLayeredBlock(statePlaceLayerable) < layerableHeightPropMax) {
				int height = getHeightForLayeredBlock(statePlaceLayerable);
				//if (height < snowMetaMax) {
					height += amountAllowedToAdd;
					if (height > layerableHeightPropMax) {
						amountAllowedToAdd = height - layerableHeightPropMax;
						height = layerableHeightPropMax;
						
					} else {
						amountAllowedToAdd = 0;
					}
					try {
						world.setBlockState(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					//if we maxed it, up the val
					if (height == layerableHeightPropMax) {
						posPlaceLayerable = posPlaceLayerable.add(0, 1, 0);
						statePlaceLayerable = world.getBlockState(posPlaceLayerable);
					}
				//}
			//solid block------------------- or air because we moved up 1 due to the previous being fully filled snow
			} else if (statePlaceLayerable.isSideSolid(world, posPlaceLayerable, EnumFacing.UP)) {
				posPlaceLayerable = posPlaceLayerable.add(0, 1, 0);
				statePlaceLayerable = world.getBlockState(posPlaceLayerable);
			//air
			} else if (statePlaceLayerable.getMaterial() == Material.AIR) {
				//copypasta, refactor/reduce once things work
				int height = amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;
					
				} else {
					amountAllowedToAdd = 0;
				}
				try {
					world.setBlockState(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//if we maxed it, up the val
				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.add(0, 1, 0);
					statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				}
			} else {
				CULog.dbg("wat! - " + statePlaceLayerable);
			}
		}
		
		if (amountAllowedToAdd < 0) {
			CULog.dbg("wat");
		}
		int amountAdded = amountToAdd - amountAllowedToAdd;
		amount -= amountAdded;
		return amount;
		
	}
	
	/**
	 * Checks if its the block layerable we want, or the vanilla 'full' height amount of it
	 * 
	 * @param state
	 * @param blockLayerable
	 * @return
	 */
	public static boolean isLayeredOrVanillaVersionOfBlock(IBlockState state, Block blockLayerable) {
		Block block = state.getBlock();
		if (block == blockLayerable) {
			return true;
		}
		if (blockLayerable == CommonProxy.blockSandLayer && block == Blocks.SAND) {
			return true;
		}
		if (blockLayerable == Blocks.SNOW_LAYER && block == Blocks.SNOW) {
			return true;
		}
		return false;
	}
	
	public static int getHeightForAnyBlock(IBlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.SNOW_LAYER) {
			return ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
		} else if (block == CommonProxy.blockSandLayer) {
			return ((Integer)state.getValue(BlockSandLayer.LAYERS)).intValue();
		} else if (block == Blocks.SAND || block == Blocks.SNOW) {
			return 8;
		} else if (block instanceof BlockSlab) {
			return 4;
		} else if (block == Blocks.AIR) {
			return 0;
		} else {
			return 8;
		}
	}
	
	public static int getHeightForLayeredBlock(IBlockState state) {
		if (state.getBlock() == Blocks.SNOW_LAYER) {
			return ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
		} else if (state.getBlock() == CommonProxy.blockSandLayer) {
			return ((Integer)state.getValue(BlockSandLayer.LAYERS)).intValue();
		} else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW) {
			return 8;
		} else {
			//missing implementation
			return 0;
		}
	}
	
	public static IBlockState setBlockWithLayerState(Block block, int height) {
		boolean solidBlockUnderMode = true;
		if (block == Blocks.SNOW_LAYER) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SNOW.getDefaultState();
			} else {
				return block.getDefaultState().withProperty(BlockSnow.LAYERS, height);
			}
		} else if (block == CommonProxy.blockSandLayer) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SAND.getDefaultState();
			} else {
				return block.getDefaultState().withProperty(BlockSandLayer.LAYERS, height);
			}
		} else {
			//means missing implementation
			return null;
		}
	}
	
	/**
	 * This method is bad, logic like this is difficult to do
	 * 
	 * @param state
	 * @param worldIn
	 * @param pos
	 * @param blockIn
	 * @return
	 */
	public static boolean divideToNeighborCheck(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		boolean foundSpotToSpread = false;
		
		int heightToUse = getHeightForLayeredBlock(state);

		CULog.dbg("try smooth out");
		
		if (heightToUse > 2) {
			for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
	        {
				if (heightToUse > 2) {
					BlockPos posCheck = pos.offset(enumfacing);
		            IBlockState stateCheck = worldIn.getBlockState(posCheck);
		            
	
	            	int addAmount = 1;
		            
		            //do height comparison if its sand already
		            if (stateCheck.getBlock() == state.getBlock()) {
		            	int heightCheck = getHeightForLayeredBlock(stateCheck);
			            if (heightCheck + 2 <= heightToUse) {
			            	heightToUse -= addAmount;
			            	addHeightToLayerableBLock(worldIn, posCheck, stateCheck.getBlock(), heightCheck, addAmount);
			            	foundSpotToSpread = true;
			            }
			        //else, do the usual
		            }/* else if (stateCheck.getMaterial() == Material.AIR) {
		            	int returnVal = trySpreadOnPos2(worldIn, posCheck, addAmount, addAmount, 10, stateCheck.getBlock());
		            	//TODO: factor in partial addition not just fully used
		            	if (returnVal == 0) {
		            		heightToUse -= addAmount;
		            		foundSpotToSpread = true;
		            	}
		            }*/
				}
	        }
		}
		
		if (foundSpotToSpread) {
			worldIn.setBlockState(pos, setBlockWithLayerState(state.getBlock(), heightToUse));
		}
		
		return foundSpotToSpread;
	}
	
	/**
	 * Simple helper method, returns amount it couldnt add
	 * @param world
	 * @param pos
	 * @param block
	 * @param amount
	 * @return
	 */
	public static int addHeightToLayerableBLock(World world, BlockPos pos, Block block, int sourceAmount, int amount) {
		IBlockState state = world.getBlockState(pos);
		int curAmount = sourceAmount;//getHeightForLayeredBlock(state);
		curAmount += amount;
		int leftOver = 0;
		if (curAmount > layerableHeightPropMax) {
			leftOver = curAmount - layerableHeightPropMax;
			curAmount = layerableHeightPropMax;
		} else {
			leftOver = 0;
		}
		try {
			world.setBlockState(pos, setBlockWithLayerState(block, curAmount));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return leftOver;
	}
	
	public static int takeHeightFromLayerableBlock(World world, BlockPos pos, Block block, int amount) {
		IBlockState state = world.getBlockState(pos);
		int height = getHeightForLayeredBlock(state);
		int amountReceived = 0;
		int newHeight = 0;
		//if fully remove
		if (height <= amount) {
			newHeight = 0;
			amountReceived = height;
		//if partial remove
		} else {
			newHeight = height - amount;
			amountReceived = amount;
		}
		
		if (newHeight > 0) {
			try {
				world.setBlockState(pos, setBlockWithLayerState(block, newHeight));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			world.setBlockToAir(pos);
		}
		
		return amountReceived;
	}

	/**
	 * Safe version of World.getPrecipitationHeight that wont invoke chunkgen/chunkload if its requesting height in unloaded chunk
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static BlockPos getPrecipitationHeightSafe(World world, BlockPos pos) {
		if (world.isBlockLoaded(pos)) {
			return world.getPrecipitationHeight(pos);
		} else {
			return new BlockPos(pos.getX(), 0, pos.getZ());
		}
	}
}
