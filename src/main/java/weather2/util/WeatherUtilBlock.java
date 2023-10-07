package weather2.util;

import com.corosus.coroutil.util.CoroUtilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import weather2.WeatherBlocks;
import weather2.block.SandLayerBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * All stackable block code in this class considers "height" as a meta val height, not actual pixel height of AABB, basically 1 meta height = 2 pixel height, this is also used for all amount values
 * 
 * @author Corosus
 *
 */
public class WeatherUtilBlock {

	//TODO: 1.14 restore removed methods from previous git commits


	public static int layerableHeightPropMax = 8;

	public static void fillAgainstWallSmoothly(Level world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int maxBlockStackingAllowed) {
		fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4, maxBlockStackingAllowed);
	}

	public static void fillAgainstWallSmoothly(Level world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff, int maxBlockStackingAllowed) {

		//layerableHeightPropMax = 8;

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
		BlockState stateTest = world.getBlockState(CoroUtilBlock.blockPos(posSource));
		if (stateTest.getBlock() == blockLayerable) {
			int heightTest = getHeightForAnyBlock(stateTest);
			if (heightTest < 8) {
				//posSource = new Vector3d(posSource.addVector(0, -1, 0));
			}
		}

		BlockPos posSourcei = CoroUtilBlock.blockPos(posSource);
		//int ySource = world.getHeight(posSourcei).getY();
		int y = posSourcei.getY();
		float tickStep = 0.75F;

		//float startScan = scanDistance;

		Vec3 posLastNonWall = new Vec3(posSource.x, posSource.y, posSource.z);
		Vec3 posWall = null;

		BlockPos lastScannedPosXZ = null;//new BlockPos(posSourcei);

		//System.out.println("Start block (should be air): " + world.getBlockState(posSourcei));

		int previousBlockHeight = 0;

		/*System.out.println("directionYaw: " + directionYaw);
		System.out.println("x: " + -Math.sin(Math.toRadians(directionYaw)));
		System.out.println("z: " + Math.cos(Math.toRadians(directionYaw)));*/

		//looking for a proper wall we cant fly over as sand
		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
			double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));

			int x = Mth.floor(posSource.x + vecX);
			int z = Mth.floor(posSource.z + vecZ);

			BlockPos pos = new BlockPos(x, y, z);
			BlockPos posXZ = new BlockPos(x, 0, z);
			BlockState state = world.getBlockState(pos);

			if (lastScannedPosXZ == null || !posXZ.equals(lastScannedPosXZ)) {

				lastScannedPosXZ = new BlockPos(posXZ);

				AABB aabbCompare = new AABB(pos);
				List<AABB> listAABBCollision = new ArrayList<>();
				VoxelShape voxelshape = Shapes.create(aabbCompare);
				//boolean collided = VoxelShapes.compare(state.getCollisionShapeUncached(world, pos).withOffset((double)l1, (double)k2, (double)i2), voxelshape, IBooleanFunction.AND);
				boolean collided = Shapes.joinIsNotEmpty(state.getCollisionShape(world, pos).move(pos.getX(), pos.getY(), pos.getZ()), voxelshape, BooleanOp.AND);
				//state.addCollisionBoxToList(world, pos, aabbCompare, listAABBCollision, null, false);

				//TODO: isReplaceable would require a fake player, see if we can avoid using isReplaceable, it let us place into things like grass? maybe?

				/*System.out.println("try: " + pos + " - collided: " + collided);
				if (pos.getX() == 80 && pos.getY() == 87 && pos.getZ() == 153) {
					System.out.println("!!!!: " + pos + " - collided: " + collided);
				}*/

				//if solid ground we can place on
				if (!state.isAir() && state.getBlock().defaultMapColor() == MapColor.PLANT &&
						/*(!state.getBlock().isReplaceable(world, pos) && */collided) {
					BlockPos posUp = new BlockPos(x, y + 1, z);
					BlockState stateUp = world.getBlockState(posUp);
					//if above it is air
					if (stateUp.isAir()) {
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

							posLastNonWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);

							//System.out.println(posLastNonWall);

							continue;
							//too high, count as a wall
						} else {
							posWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
							break;
						}
						//hit a wall
					} else {
						posWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
						break;
					}

					//startScan = i;
					//posWall = new Vector3d(posSource.x + vecX, y, posSource.z + vecZ);
					//break;
				} else {
					posLastNonWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
				}

			} else {
				continue;
			}
		}

		if (posWall != null) {
			int amountWeHave = 1;
			int amountToAddPerXZ = 1;

			BlockState state = world.getBlockState(CoroUtilBlock.blockPos(posWall));
			BlockState state1 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(1, 0, 0));
			BlockState state22 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(-1, 0, 0));
			BlockState state3 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(0, 0, 1));
			BlockState state4 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(0, 0, -1));

			//check all around place spot for cactus and cancel if true, to prevent cactus pop off when we place next to it
			if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
					state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS) {
				return;
			}

			BlockPos pos2 = CoroUtilBlock.blockPos(posLastNonWall.x, posLastNonWall.y, posLastNonWall.z);
			BlockState state2 = world.getBlockState(pos2);
			if (state2.getBlock().defaultMapColor() == MapColor.WATER || state2.getBlock().defaultMapColor() == MapColor.FIRE) {
				return;
			}

			amountWeHave = trySpreadOnPos2(world, CoroUtilBlock.blockPos(posLastNonWall.x, posLastNonWall.y, posLastNonWall.z), amountWeHave, amountToAddPerXZ, 10, blockLayerable, maxBlockStackingAllowed);
		} else {
			//System.out.println("no wall found");
		}
	}

	public static int trySpreadOnPos2(Level world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable, int maxBlockStackingAllowed) {

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
		if (!world.getBlockState(posSpreadTo.offset(0, 1, 0)).isAir()) {
			return amount;
		}

		BlockState statePos = world.getBlockState(posSpreadTo);

		BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
		BlockState stateCheckNonAir = world.getBlockState(posCheckNonAir);

		int depth = 0;

		//find first non air
		while (stateCheckNonAir.isAir()) {
			posCheckNonAir = posCheckNonAir.offset(0, -1, 0);
			stateCheckNonAir = world.getBlockState(posCheckNonAir);
			depth++;
			//bail if drop too far, aka sand/snow fully particleizes
			if (depth > maxDropAllowed) {
				return amount;
			}
		}

		BlockPos posCheckPlaceable = new BlockPos(posCheckNonAir);
		BlockState stateCheckPlaceable = world.getBlockState(posCheckPlaceable);

		//new check to limit how high snow/sand can stack
		if (maxBlockStackingAllowed > 0) {
			boolean sandMode = false;
			if (blockLayerable == Blocks.SNOW) {
				sandMode = false;
			} else if (blockLayerable == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
				sandMode = true;
			}
			int foundBlocks = 0;
			BlockPos posCheckDownForStacks = new BlockPos(posCheckPlaceable);
			BlockState stateCheckDownForStacks = world.getBlockState(posCheckPlaceable);
			if ((!sandMode && stateCheckPlaceable.getBlock() == Blocks.SNOW_BLOCK) || (sandMode && stateCheckPlaceable.getBlock() == Blocks.SAND)) {
				while ((!sandMode && stateCheckDownForStacks.getBlock() == Blocks.SNOW_BLOCK) || (sandMode && stateCheckDownForStacks.getBlock() == Blocks.SAND)) {
					foundBlocks++;
					if (foundBlocks >= maxBlockStackingAllowed) {
						//System.out.println("max snow stack allowed, bail");
						return amount;
					}
					posCheckDownForStacks = posCheckDownForStacks.offset(0, -1, 0);
					stateCheckDownForStacks = world.getBlockState(posCheckDownForStacks);
				}
			}
		}


		int distForPlaceableBlocks = 0;

		while (true && distForPlaceableBlocks < 10) {
			//if can be placed into, continue, as long as its not our block as it is replacable at layer height 1
			AABB aabbCompare = new AABB(posCheckPlaceable);
			//List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
			VoxelShape voxelshape = Shapes.create(aabbCompare);
			boolean collided = Shapes.joinIsNotEmpty(stateCheckPlaceable.getCollisionShape(world, posCheckPlaceable).move(posCheckPlaceable.getX(), posCheckPlaceable.getY(), posCheckPlaceable.getZ()), voxelshape, BooleanOp.AND);
			//stateCheckPlaceable.addCollisionBoxToList(world, posCheckPlaceable, aabbCompare, listAABBCollision, null, false);

			//TODO: isReplaceable would require a fake player, see if we can avoid using isReplaceable, it let us place into things like grass? maybe?

			if (stateCheckPlaceable.getBlock() != blockLayerable && /*stateCheckPlaceable.getBlock().isReplaceable(world, posCheckPlaceable) && */!collided && !stateCheckPlaceable.liquid()) {
				posCheckPlaceable = posCheckPlaceable.offset(0, -1, 0);
				stateCheckPlaceable = world.getBlockState(posCheckPlaceable);
				distForPlaceableBlocks++;
				continue;
				//if its the kind of solid we want, break loop
			} else if (stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) ||
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
		if (!stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) &&
				stateCheckPlaceable.getBlock() != blockLayerable) {
			System.out.println("shouldnt be, failed a check somewhere!");
			return amount;
		}

		//lets clear out the blocks we found between air and solid or snow block
		for (int i = 0; i < distForPlaceableBlocks; i++) {
			//System.out.println("clear out pos: " + posCheckNonAir);
			//System.out.println("clear out pos: " + world.getBlockState(posCheckNonAir));
			world.setBlockAndUpdate(posCheckNonAir.offset(0, -i, 0), Blocks.AIR.defaultBlockState());
		}

		BlockPos posPlaceLayerable = new BlockPos(posCheckPlaceable);
		BlockState statePlaceLayerable = world.getBlockState(posPlaceLayerable);

		int amountToAdd = amountAllowedToAdd;

		//add in the amount of air blocks we found
		//distForPlaceableBlocks += depth;

		//just place while stuff to add and air above

		while (amountAllowedToAdd > 0 && world.getBlockState(posPlaceLayerable.offset(0, 1, 0)).isAir()) {
			//if no more layers to add
			if (amountAllowedToAdd <= 0) {
				break;
			}
			//if its snow we can add snow to
			if (statePlaceLayerable.getBlock() == blockLayerable && getHeightForLayeredBlock(statePlaceLayerable) < layerableHeightPropMax) {
				int height = getHeightForLayeredBlock(statePlaceLayerable);
				//System.out.println("old height: " + height);
				//if (height < snowMetaMax) {
				height += amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;

				} else {
					amountAllowedToAdd = 0;
				}
				try {
					//System.out.println("new height: " + height);
					world.setBlockAndUpdate(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}

				//if we maxed it, up the val
				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
					statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				}
				//}
				//solid block------------------- or air because we moved up 1 due to the previous being fully filled snow
			} else if (statePlaceLayerable.isFaceSturdy(world, posPlaceLayerable, Direction.UP)) {
				posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
				statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				//air
			} else if (statePlaceLayerable.isAir()) {
				//copypasta, refactor/reduce once things work
				int height = amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;

				} else {
					amountAllowedToAdd = 0;
				}
				try {
					//System.out.println("air logic");
					//TODO: if other idea fails, put the stacks of snow count check here
					world.setBlockAndUpdate(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}

				//if we maxed it, up the val
				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
					statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				}
			} else {
				System.out.println("wat! - " + statePlaceLayerable);
			}
		}

		if (amountAllowedToAdd < 0) {
			System.out.println("wat");
		}
		int amountAdded = amountToAdd - amountAllowedToAdd;
		amount -= amountAdded;
		return amount;

	}

	public static int getHeightForAnyBlock(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.SNOW) {
			return state.getValue(SnowLayerBlock.LAYERS).intValue();
		} else if (block == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			return state.getValue(SandLayerBlock.LAYERS).intValue();
		} else if (block == Blocks.SAND || block == Blocks.SNOW_BLOCK) {
			return 8;
		} else if (block instanceof SlabBlock) {
			return 4;
		} else if (block == Blocks.AIR) {
			return 0;
		} else {
			return 8;
		}
	}

	public static int getHeightForLayeredBlock(BlockState state) {
		if (state.getBlock() == Blocks.SNOW) {
			return (state.getValue(SnowLayerBlock.LAYERS)).intValue();
		} else if (state.getBlock() == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			return state.getValue(SandLayerBlock.LAYERS).intValue();
		} else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW_BLOCK) {
			return 8;
		} else {
			//missing implementation
			return 0;
		}
	}

	public static BlockState setBlockWithLayerState(Block block, int height) {
		boolean solidBlockUnderMode = true;
		if (block == Blocks.SNOW) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SNOW_BLOCK.defaultBlockState();
			} else {
				return block.defaultBlockState().setValue(SnowLayerBlock.LAYERS, height);
			}
		} else if (block == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SAND.defaultBlockState();
			} else {
				return block.defaultBlockState().setValue(SandLayerBlock.LAYERS, height);
			}
		} else {
			//means missing implementation
			return null;
		}
	}

	public static BlockPos getPrecipitationHeightSafe(Level world, BlockPos pos) {
		return getPrecipitationHeightSafe(world, pos, Heightmap.Types.MOTION_BLOCKING);
	}

	/**
	 * Safe version of World.getPrecipitationHeight that wont invoke chunkgen/chunkload if its requesting height in unloaded chunk
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static BlockPos getPrecipitationHeightSafe(Level world, BlockPos pos, Heightmap.Types heightmapType) {
		if (world.hasChunkAt(pos)) {
			return world.getHeightmapPos(heightmapType, pos);
		} else {
			return new BlockPos(pos.getX(), -255, pos.getZ());
		}
	}
}
