package weather2.weathersystem.storm;

import CoroUtil.forge.CULog;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import weather2.ClientConfigData;
import weather2.ClientTickHandler;
import weather2.config.ConfigMisc;
import weather2.config.ConfigStorm;
import weather2.config.ConfigTornado;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilBlock;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilSound;

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
    private int queueProcessRate = 10;

    //for client player, for use of playing sounds
	public static boolean isOutsideCached = false;

	//for caching query on if a block damage preventer block is nearby, also assume blocked at first for safety
	public boolean isBlockGrabbingBlockedCached = true;
	public long isBlockGrabbingBlockedCached_LastCheck = 0;

	//static because its a shared list for the whole dimension
	public static HashMap<Integer, Long> flyingBlock_LastQueryTime = new HashMap<>();
	public static HashMap<Integer, Integer> flyingBlock_LastCount = new HashMap<>();

	public static GameProfile fakePlayerProfile = null;
    
    public static class BlockUpdateSnapshot {
    	private int dimID;
    	private BlockState state;
    	private BlockState statePrev;
		private BlockPos pos;
    	private boolean createEntityForBlockRemoval;

		public BlockUpdateSnapshot(int dimID, BlockState state, BlockState statePrev, BlockPos pos, boolean createEntityForBlockRemoval) {
			this.dimID = dimID;
			this.state = state;
			this.statePrev = statePrev;
			this.pos = pos;
			this.createEntityForBlockRemoval = createEntityForBlockRemoval;
		}

		public int getDimID() {
			return dimID;
		}

		public void setDimID(int dimID) {
			this.dimID = dimID;
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
	
	public void tick(World parWorld) {
		
		if (!parWorld.isRemote) {
			if (parWorld.getGameTime() % queueProcessRate == 0) {
				Iterator<BlockUpdateSnapshot> it = listBlockUpdateQueue.values().iterator();
				int count = 0;
				int entityCreateStaggerRate = 3;
				Random rand = new Random();
				while (it.hasNext()) {
					BlockUpdateSnapshot snapshot = it.next();
					World world = WeatherUtil.getWorld(snapshot.getDimID());
					if (world != null) {

						//TODO: 1.14 uncomment
						/*if (snapshot.getState().getBlock() == Blocks.AIR && ConfigTornado.Storm_Tornado_grabbedBlocksRepairOverTime && UtilMining.canConvertToRepairingBlock(world, snapshot.statePrev)) {*/
						if (false) {
							//TODO: 1.14 uncomment
							//TileEntityRepairingBlock.replaceBlockAndBackup(world, snapshot.getPos(), ConfigTornado.Storm_Tornado_TicksToRepairBlock);
							//world.setBlockState(snapshot.getPos(), Blocks.LEAVES.getDefaultState(), 3);
						} else {
							CULog.dbg("cant use repairing block on: " + snapshot.statePrev);
							world.setBlockState(snapshot.getPos(), snapshot.getState(), 3);
						}

						if (snapshot.isCreateEntityForBlockRemoval()) {
							//TODO: 1.14 uncomment
							/*EntityMovingBlock mBlock = new EntityMovingBlock(parWorld, snapshot.getPos().getX(), snapshot.getPos().getY(), snapshot.getPos().getZ(), snapshot.statePrev, storm);
							double speed = 1D;
							mBlock.motionX += (rand.nextDouble() - rand.nextDouble()) * speed;
							mBlock.motionZ += (rand.nextDouble() - rand.nextDouble()) * speed;
							mBlock.motionY = 1D;
							parWorld.addEntity(mBlock);*/
						}
					}
					count++;
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
        //int firesPerTick = 0;
		int firesPerTickMax = 1;

        if (storm.isFirenado) {
        	//tryRipMax = 1;
		}

        //startDissipate();
        
        //tornado profile changing from storm data
        tornadoBaseSize = getTornadoBaseSize();
        
        if (storm.stormType == storm.TYPE_WATER) {
        	tornadoBaseSize *= 3;
        }
        
        //Weather.dbg("getTornadoBaseSize: " + tornadoBaseSize + " - " + storm.levelCurIntensityStage);
        
        /*if (parWorld.isRemote) {
        	soundUpdates();
        }*/
        
        forceRotate(parWorld);
        
        Random rand = new Random();
        
        //confirm this is correct, changing to formation use!
        //int spawnYOffset = (int) storm.currentTopYBlock;
        int spawnYOffset = (int) storm.posBaseFormationPos.yCoord;

        if (!parWorld.isRemote && (ConfigTornado.Storm_Tornado_grabBlocks || storm.isFirenado)/*getStorm().grabsBlocks*/)
        {
            int yStart = 00;
            int yEnd = (int)storm.pos.yCoord/* + 72*/;
            int yInc = 1;

            //commented out for weather2
            /*if (getStorm().type == getStorm().TYPE_HURRICANE)
            {
                yStart = 10;
                yEnd = 40;
            }*/
            Biome bgb = parWorld.getBiome(new BlockPos(MathHelper.floor(storm.pos.xCoord), 0, MathHelper.floor(storm.pos.zCoord)));
        	
            //prevent grabbing in high areas (hills)
            //TODO: 1.10 make sure minHeight/maxHeight converted to baseHeight/scale is correct, guessing we can just not factor in variation
        	if (bgb != null && (bgb.getDepth()/* + bgb.getScale()*/ <= 0.7 || storm.isFirenado)) {
        		
	            for (int i = yStart; i < yEnd; i += yInc)
	            {
	                int YRand = i;//rand.nextInt(126)+2;
	                int ii = YRand / 4;
	
	                if (i > 20 && rand.nextInt(2) != 0)
	                {
	                    continue;
	                }
	
	                if (tryRipCount > tryRipMax)
	                {
	                    break;
	                }
	                
	                
	                int extraTry = (int) ((storm.levelCurIntensityStage+1 - storm.levelStormIntensityFormingStartVal) * 5);
	                int loopAmount = 5 + ii + extraTry;
	                
	                if (storm.stormType == StormObject.TYPE_WATER) {
	                	loopAmount = 1 + ii/2;
	                }
	
	                for (int k = 0; k < loopAmount; k++)
	                {
	                    //for (int k = 0; k < mod_EntMover.tornadoBaseSize/2+(ii/2); k++) {
	                    //for (int l = 0; l < mod_EntMover.tornadoBaseSize/2+(ii/2); l++) {
	                    //if (rand.nextInt(3) != 0) { continue; }
	                    if (tryRipCount > tryRipMax)
	                    {
	                        break;
	                    }
	
	                    int tryY = (int)(spawnYOffset + YRand - 1.5D); //mod_EntMover.tornadoBaseSize;
	
	                    if (tryY > 255)
	                    {
	                        tryY = 255;
	                    }
	
	                    //System.out.println(posY);
	                    //int tryX = (int)posX+k-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    //int tryZ = (int)posZ+l-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    int tryX = (int)storm.pos.xCoord + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));
	                    int tryZ = (int)storm.pos.zCoord + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));
	
	                    double d0 = storm.pos.xCoord - tryX;
	                    double d2 = storm.pos.zCoord - tryZ;
	                    double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
	                    BlockPos pos = new BlockPos(tryX, tryY, tryZ);
	                    
	                    if (dist < tornadoBaseSize/2 + ii/2 && tryRipCount < tryRipMax)
	                    {
	                    	
	                    	BlockState state = parWorld.getBlockState(pos);
	                        Block blockID = state.getBlock();
	                        
	                        boolean performed = false;
	
	                        if (canGrab(parWorld, state, pos))
	                        {
	                            tryRipCount++;
	                            seesLight = tryRip(parWorld, tryX, tryY, tryZ);
	                            
	                            performed = seesLight;
	                        }
	                        
	                        if (!performed && ConfigTornado.Storm_Tornado_RefinedGrabRules) {
	                        	if (blockID == Blocks.GRASS/* && canGrab(parWorld, state, pos)*/) {
	                        		//parWorld.setBlockState(new BlockPos(tryX, tryY, tryZ), Blocks.dirt.getDefaultState());
	                        		if (!listBlockUpdateQueue.containsKey(pos)) {
	                        			listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.getDimension().getType().getId(), Blocks.DIRT.getDefaultState(), state, pos, false));
	                        		}
	                        		
	                        	}
	                        }
	                    	
	                    }
	
	                    /*tryX = (int)posX-k+((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    tryZ = (int)posZ-l+((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	
	                    if (tryRipCount < tryRipMax) {
	                    	int blockID = this.world.getBlockStateId(tryX,tryY,tryZ);
	                    	if (blockID != 0 && canGrab(blockID)) {
	                    		tryRipCount++;
	                    		seesLight = tryRip(tryX,tryY,tryZ, true);
	                    	}
	                    }*/
	                    //}
	                    //int tryX = (int)posX+this.rand.nextInt(mod_EntMover.tornadoBaseSize+(ii))-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    //int tryZ = (int)posZ+this.rand.nextInt(mod_EntMover.tornadoBaseSize+(ii))-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                }
	            }
	
	            /*if (getStorm().type == getStorm().TYPE_TORNADO)
	            {*/
	                for (int k = 0; k < 10; k++)
	                {
	                	int randSize = 40;

						//if (storm.isFirenado) {
							randSize = 10;
						//}
	                	
	                    int tryX = (int)storm.pos.xCoord + rand.nextInt(randSize) - randSize/2;
	                    int tryY = (int)spawnYOffset - 2 + rand.nextInt(8);
	                    int tryZ = (int)storm.pos.zCoord + rand.nextInt(randSize) - randSize/2;
	
	                    double d0 = storm.pos.xCoord - tryX;
	                    double d2 = storm.pos.zCoord - tryZ;
	                    double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
	                    
	                    if (dist < tornadoBaseSize/2 + randSize/2 && tryRipCount < tryRipMax)
	                    {
	                    	BlockPos pos = new BlockPos(tryX, tryY, tryZ);
	                    	BlockState state = parWorld.getBlockState(pos);
	                        Block blockID = state.getBlock();

							if (canGrab(parWorld, state, pos))
							{
								tryRipCount++;
								tryRip(parWorld, tryX, tryY, tryZ);
							}

	

	                    }
	                }
	            //}
	
	            /*if (tryRipCount >= tryRipMax)
	            {
	                hitMaxTriesLastTick = true;
	            }
	            else
	            {
	                hitMaxTriesLastTick = false;
	            }*/
        	}
        }
        else
        {
            seesLight = true;
        }

        if (Math.abs((spawnYOffset - storm.pos.yCoord)) > 5)
        {
            seesLight = true;
        }

		if (!parWorld.isRemote && storm.isFirenado) {
        	if (storm.levelCurIntensityStage >= storm.STATE_STAGE1)
			for (int i = 0; i < firesPerTickMax; i++) {
				BlockPos posUp = new BlockPos(storm.posGround.xCoord, storm.posGround.yCoord + rand.nextInt(30), storm.posGround.zCoord);
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

			int tryX = (int)storm.pos.xCoord + rand.nextInt(randSize) - randSize/2;

			int tryZ = (int)storm.pos.zCoord + rand.nextInt(randSize) - randSize/2;
			int tryY = parWorld.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(tryX, 0, tryZ)).getY() - 1;

			double d0 = storm.pos.xCoord - tryX;
			double d2 = storm.pos.zCoord - tryZ;
			double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

			if (dist < tornadoBaseSize/2 + randSize/2 && tryRipCount < tryRipMax) {
				BlockPos pos = new BlockPos(tryX, tryY, tryZ);
				Block block = parWorld.getBlockState(pos).getBlock();
				BlockPos posUp = new BlockPos(tryX, tryY+1, tryZ);
				Block blockUp = parWorld.getBlockState(posUp).getBlock();

				if (!CoroUtilBlock.isAir(block) && CoroUtilBlock.isAir(blockUp))
				{
					parWorld.setBlockState(posUp, Blocks.FIRE.getDefaultState());
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

	public boolean tryRip(World parWorld, int tryX, int tryY, int tryZ/*, boolean notify*/)
    {

		//performance debug testing vars:
		//relocated to be created upon snapshot tick (so 

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

		//CULog.dbg("tryRip: " + blockID);

        //System.out.println(parWorld.getHeightValue(tryX, tryZ));
        if ((((WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ)).getY() - 1 == tryY) ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX + 1, 0, tryZ)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ + 1)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX - 1, 0, tryZ)).getY() - 1 < tryY ||
		WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ - 1)).getY() - 1 < tryY))) {

        	int blockCount = getBlockCountForDim(parWorld);

			//old per storm blockCount seems glitched... lets use a global we cache count of
            if (parWorld.isBlockLoaded(new BlockPos(storm.pos.xCoord, 128, storm.pos.zCoord)) &&
				lastGrabTime < System.currentTimeMillis() &&
				tickGrabCount < ConfigTornado.Storm_Tornado_maxBlocksGrabbedPerTick) {

                lastGrabTime = System.currentTimeMillis() - 5;

                if (blockID != Blocks.SNOW)
                {
                	boolean playerClose = parWorld.getClosestPlayer(storm.posBaseFormationPos.xCoord, storm.posBaseFormationPos.yCoord, storm.posBaseFormationPos.zCoord, 140, false) != null;
                    if (playerClose) {
	                    
	                    //blockCount++;
	                    
	                    //if (WeatherMod.debug && parWorld.getDayTime() % 60 == 0) System.out.println("ripping, count: " + WeatherMod.blockCount);

	                    //this.activeBlocks.add(mBlock);
	                    tickGrabCount++;
	                    ripCount++;

	                    if (ripCount % 10 == 0)
	                    {
	                        //System.out.println(ripCount);
	                    }
	                    else
	                    {
	                        //System.out.print(ripCount + " - ");
	                    }

	                    //mBlock.controller = this;
	                    seesLight = true;




                    }

					if (WeatherUtil.shouldRemoveBlock(blockID))
					{
						removeCount++;

						boolean shouldEntityify = blockCount <= ConfigTornado.Storm_Tornado_maxFlyingEntityBlocks;
						listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.getDimension().getType().getId(), Blocks.AIR.getDefaultState(), state, pos, playerClose && shouldEntityify));
					}
                }
				if (blockID == Blocks.GLASS)
				{
					parWorld.playSound(null, new BlockPos(tryX, tryY, tryZ), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.AMBIENT, 5.0F, 1.0F);
				}
            }
        }

        return seesLight;
    }

    public boolean canGrab(World parWorld, BlockState state, BlockPos pos)
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

    public boolean canGrabEventCheck(World world, BlockState state, BlockPos pos) {
    	if (!ConfigMisc.blockBreakingInvokesCancellableEvent) return true;
    	if (world instanceof ServerWorld) {
			if (fakePlayerProfile == null) {
				fakePlayerProfile = new GameProfile(UUID.fromString("1396b887-2570-4948-86e9-0633d1d22946"), "weather2FakePlayer");
			}
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, FakePlayerFactory.get((ServerWorld) world, fakePlayerProfile));
			MinecraftForge.EVENT_BUS.post(event);
			return !event.isCanceled();
		} else {
    		return false;
		}
	}

    public boolean canGrabEntity(Entity ent) {
		if (ent.world.isRemote) {
			return canGrabEntityClient(ent);
		} else {
			if (ent instanceof PlayerEntity) {
				if (ConfigTornado.Storm_Tornado_grabPlayer) {
					return true;
				} else {
					return false;
				}
			} else {
				if (ConfigTornado.Storm_Tornado_grabPlayersOnly) {
					return false;
				}
				if (ent instanceof INPC) {
					return ConfigTornado.Storm_Tornado_grabVillagers;
				}
				if (ent instanceof ItemEntity) {
					return ConfigTornado.Storm_Tornado_grabItems;
				}
				if (ent instanceof IMob) {
					return ConfigTornado.Storm_Tornado_grabMobs;
				}
				if (ent instanceof AnimalEntity) {
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
		if (ent instanceof PlayerEntity) {
			if (clientConfig.Storm_Tornado_grabPlayer) {
				return true;
			} else {
				return false;
			}
		} else {
			if (clientConfig.Storm_Tornado_grabPlayersOnly) {
				return false;
			}
			if (ent instanceof INPC) {
				return clientConfig.Storm_Tornado_grabVillagers;
			}
			if (ent instanceof ItemEntity) {
				return clientConfig.Storm_Tornado_grabItems;
			}
			if (ent instanceof IMob) {
				return clientConfig.Storm_Tornado_grabMobs;
			}
			if (ent instanceof AnimalEntity) {
				return clientConfig.Storm_Tornado_grabAnimals;
			}
		}
		//for moving blocks, other non livings
		return true;
	}
	
    public boolean forceRotate(World parWorld/*Entity entity*/)
    {
    	
    	//changed for weather2:
    	//canEntityBeSeen commented out till replaced with coord one, might cause issues
    	
        double dist = grabDist;
        AxisAlignedBB aabb = new AxisAlignedBB(storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord, storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord);
        aabb = aabb.grow(dist, this.storm.maxHeight * 3, dist);
        List list = parWorld.getEntitiesWithinAABB(Entity.class, aabb);
        boolean foundEnt = false;
        int killCount = 0;

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Entity entity1 = (Entity)list.get(i);

                if (canGrabEntity(entity1)) {
					if (getDistanceXZ(storm.posBaseFormationPos, entity1.posX, entity1.posY, entity1.posZ) < dist)
					{
						//TODO: 1.14 uncomment and remove false
						if (false/* && (entity1 instanceof EntityMovingBlock && !((EntityMovingBlock)entity1).collideFalling)*/)
						{
							storm.spinEntity(entity1);
							//spin(entity, conf, entity1);
							foundEnt = true;
						} else {
							if (entity1 instanceof PlayerEntity) {
								//dont waste cpu on server side doing LOS checks, since player movement is client side only, in all situations ive seen
								//actually we need to still change its motion var, otherwise weird things happen
								//if (entity1.world.isRemote) {
								if (WeatherUtilEntity.isEntityOutside(entity1)) {
									//Weather.dbg("entity1.motionY: " + entity1.motionY);
									storm.spinEntity(entity1);
									foundEnt = true;
								}
							} else if ((entity1 instanceof LivingEntity || entity1 instanceof ItemEntity) && WeatherUtilEntity.isEntityOutside(entity1, true)) {//OldUtil.canVecSeeCoords(parWorld, storm.pos, entity1.posX, entity1.posY, entity1.posZ)/*OldUtil.canEntSeeCoords(entity1, entity.posX, entity.posY + 80, entity.posZ)*/) {
								//trying only server side to fix warp back issue (which might mean client and server are mismatching for some rules)
								//if (!entity1.world.isRemote) {
								storm.spinEntity(entity1);
								//spin(entity, conf, entity1);
								foundEnt = true;
								//}
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
        double var7 = parVec.xCoord - var1;
        //double var9 = ent.posY - var3;
        double var11 = parVec.zCoord - var5;
        return (double)MathHelper.sqrt(var7 * var7/* + var9 * var9*/ + var11 * var11);
    }
    
    public double getDistanceXZ(Entity ent, double var1, double var3, double var5)
    {
        double var7 = ent.posX - var1;
        //double var9 = ent.posY - var3;
        double var11 = ent.posZ - var5;
        return (double)MathHelper.sqrt(var7 * var7/* + var9 * var9*/ + var11 * var11);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void soundUpdates(boolean playFarSound, boolean playNearSound)
    {
    	
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
        Vec3 plPos = new Vec3(mc.player.posX, mc.player.posY, mc.player.posZ);
        
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
				if (mc.world.getGameTime() % 40 == 0) {
					isOutsideCached = WeatherUtilEntity.isPosOutside(mc.world,
							new Vec3(mc.player.getPosition().getX()+0.5F, mc.player.getPosition().getY()+0.5F, mc.player.getPosition().getZ()+0.5F));
				}
				if (isOutsideCached) {
					tryPlaySound(WeatherUtilSound.snd_wind_far, 2, mc.player, volScaleFar, far);
				}
			}
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);
            if (playNearSound) tryPlaySound(WeatherUtilSound.snd_wind_close, 1, mc.player, volScaleClose, close);

            if (storm.levelCurIntensityStage >= storm.STATE_FORMING && storm.stormType == storm.TYPE_LAND/*getStorm().type == getStorm().TYPE_TORNADO*/)
            {
                tryPlaySound(WeatherUtilSound.snd_tornado_dmg_close, 0, mc.player, volScaleClose, close);
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

	/**
	 * Will abort out of counting if it hits the min amount required as per config
	 *
	 * @param world
	 * @return
	 */
	public static int getBlockCountForDim(World world) {
    	int queryRate = 20;
    	boolean perform = false;
		int flyingBlockCount = 0;
    	int dimID = world.getDimension().getType().getId();
    	if (!flyingBlock_LastCount.containsKey(dimID) || !flyingBlock_LastQueryTime.containsKey(dimID)) {
			//System.out.println("perform for missing");
			perform = true;
		} else if (flyingBlock_LastQueryTime.get(dimID) + queryRate < world.getGameTime()) {
			//System.out.println("perform for time");
			perform = true;
		}

		if (perform) {

			//Weather.dbg("getting moving block count");

			//TODO: 1.14 uncomment
			/*List<Entity> field_76702_h = world.loadedEntityList;
    		for (int i = 0; i < field_76702_h.size(); i++) {
    			Entity ent = field_76702_h.get(i);
				if (ent instanceof EntityMovingBlock) {
					flyingBlockCount++;

					if (flyingBlockCount > ConfigTornado.Storm_Tornado_maxFlyingEntityBlocks) {
						break;
					}
					//save time if we already hit the max
				}
			}*/

			flyingBlock_LastQueryTime.put(dimID, world.getGameTime());
    		flyingBlock_LastCount.put(dimID, flyingBlockCount);
		}

		return flyingBlock_LastCount.get(dimID);
	}

	public boolean isBlockGrabbingBlocked(World world, BlockState state, BlockPos pos) {
		int queryRate = 40;
		if (isBlockGrabbingBlockedCached_LastCheck + queryRate < world.getGameTime()) {
			isBlockGrabbingBlockedCached_LastCheck = world.getGameTime();

			isBlockGrabbingBlockedCached = false;

			for (Long hash : storm.manager.getListWeatherBlockDamageDeflector()) {
				BlockPos posDeflect = BlockPos.fromLong(hash);

				if (pos.distanceSq(posDeflect) < ConfigStorm.Storm_Deflector_RadiusOfStormRemoval * ConfigStorm.Storm_Deflector_RadiusOfStormRemoval) {
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
