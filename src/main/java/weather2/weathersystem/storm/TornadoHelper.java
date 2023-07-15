package weather2.weathersystem.storm;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.config.ClientConfigData;
import weather2.config.ConfigMisc;
import weather2.config.ConfigStorm;
import weather2.config.ConfigTornado;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilBlock;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.tornado.simple.Layer;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;

import java.util.*;

public class TornadoHelper {
	
	public StormObject storm;
	
	//public int blockCount = 0;
	
	public int ripCount = 0;

    public long lastGrabTime = 0;
    public int tickGrabCount = 0;
    public int removeCount = 0;
    public int tryRipCount = 0;
    
    public int tornadoBaseSize = 5;
    public int grabDist = 100;
    
    //potentially an issue var
    public boolean lastTickPlayerClose;
    
    /**
     * this tick queue isnt perfect, created to reduce chunk updates on client, but not removing block right away messes with block rip logic:
     * - wont dig for blocks under this block until current is removed
     * - initially, entries were spam added as the block still existed, changed list to hashmap to allow for blockpos hash lookup before adding another entry
     * - entity creation relocated to queue processing to initially prevent entity spam, but with entry lookup, not needed, other issues like collision are now the reason why we still relocated entity creation to queue process
     */
    private HashMap<BlockPos, BlockUpdateSnapshot> listBlockUpdateQueue = new HashMap<BlockPos, BlockUpdateSnapshot>();
    private int queueProcessRate = 40;

    //for client player, for use of playing sounds
	public static boolean isOutsideCached = false;

	//for caching query on if a block damage preventer block is nearby, also assume blocked at first for safety
	public boolean isBlockGrabbingBlockedCached = true;
	public long isBlockGrabbingBlockedCached_LastCheck = 0;

	//static because its a shared list for the whole dimension
	//public static HashMap<Integer, Long> flyingBlock_LastQueryTime = new HashMap<>();
	//public static HashMap<Integer, Integer> flyingBlock_LastCount = new HashMap<>();

	public static GameProfile fakePlayerProfile = null;
    
    public static class BlockUpdateSnapshot {
    	//private int dimID;
		private ResourceKey<Level> dimension;
    	private BlockState state;
    	private BlockState statePrev;
		private BlockPos pos;
    	private boolean createEntityForBlockRemoval;

		public BlockUpdateSnapshot(ResourceKey<Level> dimension, BlockState state, BlockState statePrev, BlockPos pos, boolean createEntityForBlockRemoval) {
			this.dimension = dimension;
			this.state = state;
			this.statePrev = statePrev;
			this.pos = pos;
			this.createEntityForBlockRemoval = createEntityForBlockRemoval;
		}

		public ResourceKey<Level> getDimension() {
			return dimension;
		}

		public void setDimension(ResourceKey<Level> dimension) {
			this.dimension = dimension;
		}

		public BlockState getState() {
			return state;
		}

		public void setState(BlockState state) {
			this.state = state;
		}

		public BlockPos getPos() {
			return pos;
		}

		public void setPos(BlockPos pos) {
			this.pos = pos;
		}
    	
    	public boolean isCreateEntityForBlockRemoval() {
			return createEntityForBlockRemoval;
		}

		public void setCreateEntityForBlockRemoval(boolean createEntityForBlockRemoval) {
			this.createEntityForBlockRemoval = createEntityForBlockRemoval;
		}
		
    	public BlockState getStatePrev() {
			return statePrev;
		}

		public void setStatePrev(BlockState statePrev) {
			this.statePrev = statePrev;
		}
    	
    }
	
	public TornadoHelper(StormObject parStorm) {
		storm = parStorm;
	}
	
	public int getTornadoBaseSize() {
        int sizeChange = 10;
		if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE5) {
        	return sizeChange * 9;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE4) {
        	return sizeChange * 7;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE3) {
        	return sizeChange * 5;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE2) {
        	return sizeChange * 4;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE1) {
        	return sizeChange * 3;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_FORMING) {
        	return sizeChange * 1;
        } else {
        	return 5;
        }
	}



	public void tick(Level parWorld) {

		if (!parWorld.isClientSide()) {
			if (parWorld.getGameTime() % queueProcessRate == 0) {
				Iterator<BlockUpdateSnapshot> it = listBlockUpdateQueue.values().iterator();
				while (it.hasNext()) {
					BlockUpdateSnapshot snapshot = it.next();
					Level world = WeatherUtil.getWorld(snapshot.getDimension());
					if (world != null) {
						world.setBlock(snapshot.getPos(), snapshot.getState(), 3);
						if (snapshot.isCreateEntityForBlockRemoval()) {
							//moved to where we add to the queue for less clunky visuals
							//((WeatherManagerServer)this.storm.manager).syncBlockParticleNew(snapshot.getPos(), snapshot.getStatePrev(), storm);
						}
					}
				}
				listBlockUpdateQueue.clear();
			}
		}

		if (storm == null) return;

		boolean seesLight = false;
		tickGrabCount = 0;
		removeCount = 0;
		tryRipCount = 0;
		int tryRipMax = 300;
		if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE5) {
			tryRipMax *= 2.5;
		} else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE4) {
			tryRipMax *= 1.8;
		} else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE3) {
			tryRipMax *= 1.5;
		} else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE2) {
			tryRipMax *= 1.2;
		}
		//tryRipMax = 1000;
		int firesPerTickMax = 1;
		//tornado profile changing from storm data
		tornadoBaseSize = getTornadoBaseSize();

		if (storm.stormType == storm.TYPE_WATER) {
			tornadoBaseSize *= 3;
		}

		forceRotate(parWorld);

		Random rand = new Random();
		if (!parWorld.isClientSide() && !Weather.isLoveTropicsInstalled() && (ConfigTornado.Storm_Tornado_grabBlocks || storm.isFirenado))
		{
			//int yStart = (int) (storm.posGround.y - 10);
			int yStart = 0;
			int yEnd = (int) storm.pos.y/* + 72*/;
			int yInc = 1;
			Biome bgb = parWorld.getBiome(new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(Mth.floor(storm.pos.x), 0, Mth.floor(storm.pos.z))))).get();

			//prevent grabbing in high areas (hills)
			//TODO: 1.10 make sure minHeight/maxHeight converted to baseHeight/scale is correct, guessing we can just not factor in variation
			//TODO: 1.18: getDepth formally base height, seems entirely gone now
			double depth = 0;
			//depth = bgb.getDepth();
			/* + bgb.getScale()*/
			if (depth/* + bgb.getScale()*/ <= 0.7 || storm.isFirenado) {

				boolean newTest = false;

				int tryCount = 0;

				if (newTest) {
					TornadoFunnelSimple tornadoFunnelSimple = storm.getTornadoFunnelSimple();
					int layers = tornadoFunnelSimple.listLayers.size();

					for (int i = 0; i < layers; i++) {
						float radius = tornadoFunnelSimple.getConfig().getRadiusOfBase() + (tornadoFunnelSimple.getConfig().getRadiusIncreasePerLayer() * (i));

						Layer layer = tornadoFunnelSimple.listLayers.get(i);

						float circumference = radius * 2 * Mth.PI;
						float particleSpaceOccupy = 1F;
						float scanResolution = (float) Math.floor(circumference / particleSpaceOccupy);
						float particleSpacingDegrees = 360 / scanResolution;
						//scanResolution = 1F;

						for (float deg = 0; deg < 360; deg += particleSpacingDegrees) {
							float radiusScanSize = 5F;
							//for (float radiusToUse = radius - radiusScanSize; radiusToUse <= radius; radiusToUse+=0.5F) {
							for (float radiusToUse = radius; radiusToUse <= radius; radiusToUse += 1F) {
								float x = (float) (layer.getPos().x + (-Math.sin(Math.toRadians(deg)) * radiusScanSize));
								float z = (float) (layer.getPos().z + (Math.cos(Math.toRadians(deg)) * radiusScanSize));
								float y = (float) layer.getPos().y;

								BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y) - 1, (int) Math.floor(z));

								boolean performed = false;

								BlockState state = parWorld.getBlockState(pos);

								if (parWorld.getGameTime() % 10 == 0 && radiusToUse == radius - radiusScanSize) {
									//((ServerLevel) parWorld).sendParticles(ParticleTypes.HEART, pos.getX(), pos.getY(), pos.getZ(), 1, 0.3D, 0D, 0.3D, 1D);
								}

								tryCount++;

								if (canGrab(parWorld, state, pos)) {
									tryRipCount++;
									seesLight = tryRip(parWorld, pos.getX(), pos.getY(), pos.getZ());
									performed = seesLight;

								}

								if (!performed && ConfigTornado.Storm_Tornado_RefinedGrabRules) {
									if (state.getBlock() == Blocks.GRASS_BLOCK) {
										if (!listBlockUpdateQueue.containsKey(pos)) {
											listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.dimension(), Blocks.DIRT.defaultBlockState(), state, pos, false));
										}
									}
								}
							}
						}
					}

					//CULog.dbg("tryCount: " + tryCount);
				} else {
					int ii = 2;

					int stageIntensity = (int) ((storm.levelCurIntensityStage+1 - storm.levelStormIntensityFormingStartVal));
					int loopAmount = stageIntensity * 500;

					if (storm.stormType == StormObject.TYPE_WATER) {
						loopAmount = 1 + ii/2;
					}

					for (int k = 0; k < loopAmount; k++)
					{
						if (tryRipCount > tryRipMax) {
							break;
						}

						int bottomY = (int) Math.max(parWorld.getMinBuildHeight(), storm.posBaseFormationPos.y - 10);
						int topY = (int) Math.max(parWorld.getMaxBuildHeight(), storm.getPosTop().y);
						if (bottomY >= topY) bottomY = topY - 1;
						int tryY = rand.nextInt(bottomY, topY);

						if (tryY > parWorld.getMaxBuildHeight()) {
							tryY = parWorld.getMaxBuildHeight();
						}

						int tryX = (int)storm.pos.x + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));
						int tryZ = (int)storm.pos.z + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));

						double d0 = storm.pos.x - tryX;
						double d2 = storm.pos.z - tryZ;
						double dist = Mth.sqrt((float) (d0 * d0 + d2 * d2));
						BlockPos pos = new BlockPos(tryX, tryY, tryZ);

						if (dist < tornadoBaseSize/2 + ii/2 && tryRipCount < tryRipMax)
						{

							BlockState state = parWorld.getBlockState(pos);
							Block blockID = state.getBlock();

							boolean performed = false;

							tryCount++;

							if (canGrab(parWorld, state, pos))
							{
								tryRipCount++;
								seesLight = tryRip(parWorld, tryX, tryY, tryZ);

								performed = seesLight;
							}

							if (!performed && ConfigTornado.Storm_Tornado_RefinedGrabRules) {
								if (blockID == Blocks.GRASS_BLOCK) {
									if (!listBlockUpdateQueue.containsKey(pos)) {
										listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.dimension(), Blocks.DIRT.defaultBlockState(), state, pos, false));
									}

								}
							}
						}
					}

					int spawnYOffset = (int) storm.posBaseFormationPos.y - 10;

					for (int k = 0; k < 10; k++) {
						int randSize = 40;

						randSize = 10;

						int tryX = (int)storm.pos.x + rand.nextInt(randSize) - randSize/2;
						int tryY = (int)spawnYOffset - 2 + rand.nextInt(8);
						int tryZ = (int)storm.pos.z + rand.nextInt(randSize) - randSize/2;

						double d0 = storm.pos.x - tryX;
						double d2 = storm.pos.z - tryZ;
						double dist = Mth.sqrt((float) (d0 * d0 + d2 * d2));

						if (dist < tornadoBaseSize/2 + randSize/2 && tryRipCount < tryRipMax) {
							BlockPos pos = new BlockPos(tryX, tryY, tryZ);
							BlockState state = parWorld.getBlockState(pos);

							tryCount++;

							if (canGrab(parWorld, state, pos))
							{
								tryRipCount++;
								tryRip(parWorld, tryX, tryY, tryZ);
							}
						}
					}

					//CULog.dbg("tryCount: " + tryCount);
				}


			}
		}
		else
		{
			seesLight = true;
		}

		/*if (Math.abs((spawnYOffset - storm.pos.y)) > 5)
		{
			seesLight = true;
		}*/

		if (!parWorld.isClientSide() && storm.isFirenado) {
			if (storm.levelCurIntensityStage >= storm.STATE_STAGE1)
				for (int i = 0; i < firesPerTickMax; i++) {
					BlockPos posUp = new BlockPos(storm.posGround.x, storm.posGround.y + rand.nextInt(30), storm.posGround.z);
					BlockState state = parWorld.getBlockState(posUp);
					if (CoroUtilBlock.isAir(state.getBlock())) {
						//parWorld.setBlockState(posUp, Blocks.FIRE.getDefaultState());

						//TODO: 1.14 uncomment
					/*EntityMovingBlock mBlock = new EntityMovingBlock(parWorld, posUp.getX(), posUp.getY(), posUp.getZ(), Blocks.FIRE.getDefaultState(), storm);
					mBlock.metadata = 15;
					double speed = 2D;
					mBlock.motionX += (rand.nextDouble() - rand.nextDouble()) * speed;
					mBlock.motionZ += (rand.nextDouble() - rand.nextDouble()) * speed;
					mBlock.motionY = 1D;
					mBlock.mode = 0;
					parWorld.addEntity(mBlock);*/
					}
				}


			int randSize = 10;

			int tryX = (int)storm.pos.x + rand.nextInt(randSize) - randSize/2;

			int tryZ = (int)storm.pos.z + rand.nextInt(randSize) - randSize/2;
			int tryY = parWorld.getHeight(Heightmap.Types.MOTION_BLOCKING, tryX, tryZ) - 1;

			double d0 = storm.pos.x - tryX;
			double d2 = storm.pos.z - tryZ;
			double dist = Mth.sqrt((float) (d0 * d0 + d2 * d2));

			if (dist < tornadoBaseSize/2 + randSize/2 && tryRipCount < tryRipMax) {
				BlockPos pos = new BlockPos(tryX, tryY, tryZ);
				Block block = parWorld.getBlockState(pos).getBlock();
				BlockPos posUp = new BlockPos(tryX, tryY+1, tryZ);
				Block blockUp = parWorld.getBlockState(posUp).getBlock();

				if (!CoroUtilBlock.isAir(block) && CoroUtilBlock.isAir(blockUp))
				{
					parWorld.setBlock(posUp, Blocks.FIRE.defaultBlockState(), 3);
				}
			}
		}
	}
	
	public boolean isNoDigCoord(int x, int y, int z) {

        // MCPC start
          /*org.bukkit.entity.Entity bukkitentity = this.getBukkitEntity();
          if ((bukkitentity instanceof Player)) {
            Player player = (Player)bukkitentity;
            BlockBreakEvent breakev = new BlockBreakEvent(player.getWorld().getBlockStateAt(x, y, z), player);
            Bukkit.getPluginManager().callEvent(breakev);
            if (breakev.isCancelled()) {
                return true;
            }
          }*/
          // MCPC end
          
          return false;
    }

	public boolean tryRip(Level parWorld, int tryX, int tryY, int tryZ/*, boolean notify*/)
    {
        boolean tryRip = true;
		BlockPos pos = new BlockPos(tryX, tryY, tryZ);
		if (listBlockUpdateQueue.containsKey(pos)) {
			return true;
		}
        
        if (!tryRip) return true;
        if (!ConfigTornado.Storm_Tornado_grabBlocks) return true;
        if (isNoDigCoord(tryX, tryY, tryZ)) return true;

        boolean seesLight = false;
        BlockState state = parWorld.getBlockState(pos);
        Block blockID = state.getBlock();
        if ((((WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ)).getY() - 1 == tryY) ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX + 1, 0, tryZ)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ + 1)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX - 1, 0, tryZ)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ - 1)).getY() - 1 < tryY))) {

        	int blockCount = 0;

			//old per storm blockCount seems glitched... lets use a global we cache count of
            if (parWorld.isLoaded(new BlockPos(storm.pos.x, 128, storm.pos.z)) &&
				lastGrabTime < System.currentTimeMillis() &&
				tickGrabCount < ConfigTornado.Storm_Tornado_maxBlocksGrabbedPerTick) {

                lastGrabTime = System.currentTimeMillis() - 5;

                if (blockID != Blocks.PACKED_ICE && blockID != Blocks.ICE && blockID != Blocks.SNOW_BLOCK && blockID != Blocks.SNOW && blockID != Blocks.POWDER_SNOW)
                {
                	boolean playerClose = parWorld.getNearestPlayer(storm.posBaseFormationPos.x, storm.posBaseFormationPos.y, storm.posBaseFormationPos.z, 140, false) != null;
                    if (playerClose) {
	                    tickGrabCount++;
	                    ripCount++;
	                    seesLight = true;
                    }

					if (WeatherUtil.shouldRemoveBlock(state))
					{
						removeCount++;
						boolean shouldEntityify = blockCount <= ConfigTornado.Storm_Tornado_maxFlyingEntityBlocks;
						listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.dimension(), Blocks.AIR.defaultBlockState(), state, pos, playerClose && shouldEntityify));
						if (playerClose && shouldEntityify && (state.canOcclude() || state.getMaterial().equals(Material.LEAVES))) {
							((WeatherManagerServer) this.storm.manager).syncBlockParticleNew(pos, state, storm);
						}
					}
                }
				if (blockID == Blocks.GLASS)
				{
					parWorld.playSound(null, new BlockPos(tryX, tryY, tryZ), SoundEvents.GLASS_BREAK, SoundSource.AMBIENT, 5.0F, 1.0F);
				}
            }
        }

        return seesLight;
    }

    public boolean canGrab(Level parWorld, BlockState state, BlockPos pos)
    {
        if (!CoroUtilBlock.isAir(state.getBlock()) &&
				state.getBlock() != Blocks.FIRE &&
				//TODO: 1.14 uncomment
				/*state.getBlock() != CommonProxy.blockRepairingBlock &&*/
				WeatherUtil.shouldGrabBlock(parWorld, state) &&
				!isBlockGrabbingBlocked(parWorld, state, pos))
        {
        	return canGrabEventCheck(parWorld, state, pos);
        }

        return false;
    }

    public boolean canGrabEventCheck(Level world, BlockState state, BlockPos pos) {
    	if (!ConfigMisc.blockBreakingInvokesCancellableEvent) return true;
    	if (world instanceof ServerLevel) {
			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("1396b887-2570-4948-86e9-0633d1d22946"), "weather2FakePlayer");
			}
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, FakePlayerFactory.get((ServerLevel) world, fakePlayerProfile));
			MinecraftForge.EVENT_BUS.post(event);
			return !event.isCanceled();
		} else {
    		return false;
		}
	}

    public boolean canGrabEntity(Entity ent) {
		if (ent.level.isClientSide()) {
			return canGrabEntityClient(ent);
		} else {
			if (ent instanceof Player) {
				if (!((Player) ent).isCreative()) {
					if (ConfigTornado.Storm_Tornado_grabPlayer) {
						if (storm.isPlayerControlled() && storm.spawnerUUID != null && storm.spawnerUUID.equals(ent.getUUID().toString())) {
							return false;
						}
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				if (ConfigTornado.Storm_Tornado_grabPlayersOnly) {
					return false;
				}
				if (ent instanceof Npc) {
					return ConfigTornado.Storm_Tornado_grabVillagers;
				}
				if (ent instanceof ItemEntity) {
					return ConfigTornado.Storm_Tornado_grabItems || storm.isPet();
				}
				if (ent instanceof Enemy) {
					return ConfigTornado.Storm_Tornado_grabMobs;
				}
				if (ent instanceof Animal) {
					return ConfigTornado.Storm_Tornado_grabAnimals;
				}
			}
			//for moving blocks, other non livings
			return true;
		}

	}

	@OnlyIn(Dist.CLIENT)
	public boolean canGrabEntityClient(Entity ent) {
		ClientConfigData clientConfig = ClientTickHandler.clientConfigData;
		if (ent instanceof Player) {
			if (!((Player) ent).isCreative()) {
				if (ConfigTornado.Storm_Tornado_grabPlayer) {
					if (storm.isPlayerControlled() && storm.spawnerUUID != null && storm.spawnerUUID.equals(ent.getUUID().toString())) {
						return false;
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			if (clientConfig.Storm_Tornado_grabPlayersOnly) {
				return false;
			}
			if (ent instanceof Npc) {
				return clientConfig.Storm_Tornado_grabVillagers;
			}
			if (ent instanceof ItemEntity) {
				return clientConfig.Storm_Tornado_grabItems || storm.isPet();
			}
			if (ent instanceof Enemy) {
				return clientConfig.Storm_Tornado_grabMobs;
			}
			if (ent instanceof Animal) {
				return clientConfig.Storm_Tornado_grabAnimals;
			}
		}
		//for moving blocks, other non livings
		return true;
	}

	public boolean forceRotate(Level parWorld)
	{
		return forceRotate(parWorld, false);
	}

    public boolean forceRotate(Level parWorld, boolean featherFallInstead)
    {
    	
    	//changed for weather2:
    	//canEntityBeSeen commented out till replaced with coord one, might cause issues
    	
        double dist = grabDist * 2;
		if (storm.isPet()) {
			dist = 3F;
		}
        AABB aabb = new AABB(storm.pos.x, storm.currentTopYBlock, storm.pos.z, storm.pos.x, storm.currentTopYBlock, storm.pos.z);
		if (storm.isPet()) {
			aabb = aabb.inflate(dist, 3, dist);
		} else {
			aabb = aabb.inflate(dist, this.storm.maxHeight * 3.8, dist);
		}

        List list = parWorld.getEntitiesOfClass(Entity.class, aabb);
        boolean foundEnt = false;
        int killCount = 0;

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Entity entity1 = (Entity)list.get(i);

                if (canGrabEntity(entity1)) {
					if (getDistanceXZ(storm.posBaseFormationPos, entity1.getX(), entity1.getY(), entity1.getZ()) < dist)
					{
						if (!storm.isPet()) {
							//TODO: 1.14 uncomment and remove false
							if (false/* && (entity1 instanceof EntityMovingBlock && !((EntityMovingBlock)entity1).collideFalling)*/) {
								storm.spinEntity(entity1);
								//spin(entity, conf, entity1);
								foundEnt = true;
							} else {
								if (entity1 instanceof Player) {
									//dont waste cpu on server side doing LOS checks, since player movement is client side only, in all situations ive seen
									//actually we need to still change its motion var, otherwise weird things happen
									//if (entity1.world.isClientSide()) {
									if (WeatherUtilEntity.isEntityOutside(entity1)) {
										//Weather.dbg("entity1.motionY: " + entity1.motionY);
										if (featherFallInstead) {
											((Player) entity1).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0, false, true, true));
										} else {
											storm.spinEntityv2(entity1);
										}
										foundEnt = true;
									}
								} else if ((entity1 instanceof LivingEntity) && WeatherUtilEntity.isEntityOutside(entity1, false)) {//OldUtil.canVecSeeCoords(parWorld, storm.pos, entity1.posX, entity1.posY, entity1.posZ)/*OldUtil.canEntSeeCoords(entity1, entity.posX, entity.posY + 80, entity.posZ)*/) {
									//trying only server side to fix warp back issue (which might mean client and server are mismatching for some rules)
									//if (!entity1.world.isClientSide()) {
									if (featherFallInstead) {
										((LivingEntity) entity1).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0, false, true, true));
									} else {
										storm.spinEntityv2(entity1);
									}
									//spin(entity, conf, entity1);
									foundEnt = true;
									//}
								}
							}
						} else {
							if (entity1 instanceof ItemEntity && storm.isPetGrabsItems()) {
								storm.spinEntityv2(entity1);
								foundEnt = true;
							}
						}
					}
				}
            }
        }

        return foundEnt;
    }
    
    public double getDistanceXZ(Vec3 parVec, double var1, double var3, double var5)
    {
        double var7 = parVec.x - var1;
        //double var9 = ent.posY - var3;
        double var11 = parVec.z - var5;
        return Mth.sqrt((float) (var7 * var7/* + var9 * var9*/ + var11 * var11));
    }
    
    public double getDistanceXZ(Entity ent, double var1, double var3, double var5)
    {
        double var7 = ent.getX() - var1;
        //double var9 = ent.posY - var3;
        double var11 = ent.getZ() - var5;
        return Mth.sqrt((float) (var7 * var7/* + var9 * var9*/ + var11 * var11));
    }
    
    @OnlyIn(Dist.CLIENT)
    public void soundUpdates(boolean playFarSound, boolean playNearSound)
    {
    	if (storm.isPet()) return;
    	Minecraft mc = Minecraft.getInstance();
    	
        if (mc.player == null)
        {
            return;
        }

        //close sounds
        int far = 200;
        int close = 120;
        if (storm.stormType == storm.TYPE_WATER) {
        	close = 200;
        }
        Vec3 plPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        double distToPlayer = this.storm.posGround.distanceTo(plPos);
        
        float volScaleFar = (float) ((far - distToPlayer/*this.getDistanceToEntity(mc.player)*/) / far);
        float volScaleClose = (float) ((close - distToPlayer/*this.getDistanceToEntity(mc.player)*/) / close);

        if (volScaleFar < 0F)
        {
            volScaleFar = 0.0F;
        }

        if (volScaleClose < 0F)
        {
            volScaleClose = 0.0F;
        }

        if (distToPlayer < close)
        {
            if (!lastTickPlayerClose)
            {
                /*this.soundTimer[0] = System.currentTimeMillis();
                this.soundTimer[1] = System.currentTimeMillis();
                tryPlaySound(snd_dmg_close, 0, mc.player, volScaleClose);
                tryPlaySound(snd_wind_close, 1, mc.player, volScaleClose);*/
            }

            lastTickPlayerClose = true;
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);
        }
        else
        {
            lastTickPlayerClose = false;
        }

        if (distToPlayer < far)
        {
            if (playFarSound) {
				if (mc.level.getGameTime() % 40 == 0) {
					isOutsideCached = WeatherUtilEntity.isPosOutside(mc.level,
							new Vec3(mc.player.getPosition(1).x()+0.5F, mc.player.getPosition(1).y()+0.5F, mc.player.getPosition(1).z()+0.5F));
				}
				if (isOutsideCached) {
					tryPlaySound(WeatherUtilSound.snd_wind_far, 2, mc.player, volScaleFar, far);
				}
			}
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);

			float quietTornadoTweak = 1F;
			if (Weather.isLoveTropicsInstalled()) {
				/*quietTornadoTweak = 0.01F;
				quietTornadoTweak = 0.0F;*/
				close = 7;
			}

            if (playNearSound) tryPlaySound(WeatherUtilSound.snd_wind_close, 1, mc.player, volScaleClose * quietTornadoTweak, close);

            if (storm.levelCurIntensityStage >= storm.STATE_FORMING && storm.stormType == storm.TYPE_LAND/*getStorm().type == getStorm().TYPE_TORNADO*/)
            {
                tryPlaySound(WeatherUtilSound.snd_tornado_dmg_close, 0, mc.player, volScaleClose * quietTornadoTweak, close);
            }
        }
    }

    public boolean tryPlaySound(String[] sound, int arrIndex, Entity source, float vol, float parCutOffRange)
    {
        Entity soundTarget = source;

		Random rand = new Random();
        
        // should i?
        //soundTarget = this;
        if (WeatherUtilSound.soundTimer[arrIndex] <= System.currentTimeMillis())
        {

			//CULog.dbg("sound: " + sound[0] + " vol: " + vol + " parCutOffRange: " + parCutOffRange);
            //world.playSoundAtEntity(soundTarget, new StringBuilder().append("tornado."+sound).toString(), 1.0F, 1.0F);
            //((IWorldAccess)this.worldAccesses.get(var5)).playSound(var2, var1.posX, var1.posY - (double)var1.yOffset, var1.posZ, var3, var4);
        	/*WeatherUtilSound.soundID[arrIndex] = */WeatherUtilSound.playMovingSound(storm, new StringBuilder().append("streaming." + sound[WeatherUtilSound.snd_rand[arrIndex]]).toString(), vol, 1.0F, parCutOffRange);
            //this.soundID[arrIndex] = mod_EntMover.getLastSoundID();
            //System.out.println(new StringBuilder().append("tornado."+sound[snd_rand[arrIndex]]).toString());
            //System.out.println(soundToLength.get(sound[snd_rand[arrIndex]]));
            int length = (Integer)WeatherUtilSound.soundToLength.get(sound[WeatherUtilSound.snd_rand[arrIndex]]);
            //-500L, for blending
            WeatherUtilSound.soundTimer[arrIndex] = System.currentTimeMillis() + length - 500L;
            WeatherUtilSound.snd_rand[arrIndex] = rand.nextInt(3);
        }

        return false;
    }

	public boolean isBlockGrabbingBlocked(Level world, BlockState state, BlockPos pos) {
		int queryRate = 40;
		if (isBlockGrabbingBlockedCached_LastCheck + queryRate < world.getGameTime()) {
			isBlockGrabbingBlockedCached_LastCheck = world.getGameTime();

			isBlockGrabbingBlockedCached = false;

			for (Long hash : storm.manager.getListWeatherBlockDamageDeflector()) {
				BlockPos posDeflect = BlockPos.of(hash);

				if (pos.distSqr(posDeflect) < ConfigStorm.Storm_Deflector_RadiusOfStormRemoval * ConfigStorm.Storm_Deflector_RadiusOfStormRemoval) {
					isBlockGrabbingBlockedCached = true;
					break;
				}
			}
		}

		return isBlockGrabbingBlockedCached;
	}

	public void cleanup() {
		listBlockUpdateQueue.clear();
		storm = null;
	}
}
