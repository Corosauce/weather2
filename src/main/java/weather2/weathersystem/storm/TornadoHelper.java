package weather2.weathersystem.storm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.Weather;
import weather2.config.ConfigTornado;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilBlock;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilSound;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;

public class TornadoHelper {
	
	public StormObject storm;
	
	public int blockCount = 0;
	
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
     * this update queue isnt perfect, created to reduce chunk updates on client, but not removing block right away messes with block rip logic:
     * - wont dig for blocks under this block until current is removed
     * - initially, entries were spam added as the block still existed, changed list to hashmap to allow for blockpos hash lookup before adding another entry
     * - entity creation relocated to queue processing to initially prevent entity spam, but with entry lookup, not needed, other issues like collision are now the reason why we still relocated entity creation to queue process
     */
    private HashMap<BlockPos, BlockUpdateSnapshot> listBlockUpdateQueue = new HashMap<BlockPos, BlockUpdateSnapshot>();
    private int queueProcessRate = 10;
    
    public static class BlockUpdateSnapshot {
    	private int dimID;
    	private IBlockState state;
    	private IBlockState statePrev;
		private BlockPos pos;
    	private boolean createEntityForBlockRemoval;

		public BlockUpdateSnapshot(int dimID, IBlockState state, IBlockState statePrev, BlockPos pos, boolean createEntityForBlockRemoval) {
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

		public IBlockState getState() {
			return state;
		}

		public void setState(IBlockState state) {
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
		
    	public IBlockState getStatePrev() {
			return statePrev;
		}

		public void setStatePrev(IBlockState statePrev) {
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
			if (parWorld.getTotalWorldTime() % queueProcessRate == 0) {
				Iterator<BlockUpdateSnapshot> it = listBlockUpdateQueue.values().iterator();
				int count = 0;
				int entityCreateStaggerRate = 3;
				while (it.hasNext()) {
					BlockUpdateSnapshot snapshot = it.next();
					World world = DimensionManager.getWorld(snapshot.getDimID());
					if (world != null) {
						world.setBlockState(snapshot.getPos(), snapshot.getState(), 3);
						if (snapshot.getState().getBlock() == Blocks.AIR) {
							if (count % entityCreateStaggerRate == 0) {
								EntityMovingBlock mBlock = new EntityMovingBlock(parWorld, snapshot.getPos().getX(), snapshot.getPos().getY(), snapshot.getPos().getZ(), snapshot.statePrev.getBlock(), storm);
								/*if (mBlock != null) {
			                    	mBlock.setPosition(tryX, tryY, tryZ);
			                    }*/
								parWorld.spawnEntity(mBlock);
							}
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

        if (!parWorld.isRemote && ConfigTornado.Storm_Tornado_grabBlocks/*getStorm().grabsBlocks*/)
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
            //TODO: 1.10 make sure minHeight/maxHeight converted to baseHeight/heightVariation is correct, guessing we can just not factor in variation
        	if (bgb != null && bgb.getBaseHeight()/* + bgb.getHeightVariation()*/ <= 0.7) {
        		
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
	                    	
	                    	IBlockState state = parWorld.getBlockState(pos);
	                        Block blockID = state.getBlock();
	                        
	                        boolean performed = false;
	
	                        if (!CoroUtilBlock.isAir(blockID) && canGrab(parWorld, blockID)/* && Block.blocksList[blockID].blockMaterial == Material.ground*//* && world.getHeightValue(tryX, tryZ)-1 == tryY*/)
	                        {
	                            /*if (blockID != 0 && canGrab(blockID) && (world.getBlockStateId(tryX,tryY+1,tryZ) == 0 ||
	                                    world.getBlockStateId(tryX+1,tryY,tryZ) == 0 ||
	                                    world.getBlockStateId(tryX,tryY,tryZ+1) == 0 ||
	                                    world.getBlockStateId(tryX-1,tryY,tryZ) == 0 ||
	                                    world.getBlockStateId(tryX,tryY,tryZ-1) == 0)) {*/
	                            tryRipCount++;
	                            seesLight = tryRip(parWorld, tryX, tryY, tryZ);
	                            
	                            performed = seesLight;
	                        }
	                        
	                        if (!performed && ConfigTornado.Storm_Tornado_RefinedGrabRules) {
	                        	if (blockID == Blocks.GRASS) {
	                        		//parWorld.setBlockState(new BlockPos(tryX, tryY, tryZ), Blocks.dirt.getDefaultState());
	                        		if (!listBlockUpdateQueue.containsKey(pos)) {
	                        			listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.provider.getDimension(), Blocks.DIRT.getDefaultState(), state, pos, false));
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
	                	
	                    int tryX = (int)storm.pos.xCoord + rand.nextInt(randSize) - 20;
	                    int tryY = (int)spawnYOffset - 2 + rand.nextInt(8);
	                    int tryZ = (int)storm.pos.zCoord + rand.nextInt(randSize) - 20;
	
	                    double d0 = storm.pos.xCoord - tryX;
	                    double d2 = storm.pos.zCoord - tryZ;
	                    double dist = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
	                    
	                    if (dist < tornadoBaseSize/2 + randSize/2 && tryRipCount < tryRipMax)
	                    {
	                    	BlockPos pos = new BlockPos(tryX, tryY, tryZ);
	                        Block blockID = parWorld.getBlockState(pos).getBlock();


							if (!storm.isFirenado) {
								if (!CoroUtilBlock.isAir(blockID) && canGrab(parWorld, blockID))
								{
									tryRipCount++;
									tryRip(parWorld, tryX, tryY, tryZ);
								}

							} else {
								BlockPos posUp = pos.add(0, 1, 0);
								if (!CoroUtilBlock.isAir(blockID) && CoroUtilBlock.isAir(parWorld.getBlockState(posUp).getBlock())) {
									tryRipCount++;
									parWorld.setBlockState(posUp, Blocks.FIRE.getDefaultState());

									EntityMovingBlock mBlock = new EntityMovingBlock(parWorld, posUp.getX(), posUp.getY() + 10, posUp.getZ(), Blocks.FIRE, storm);
									parWorld.spawnEntity(mBlock);
								}
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
		//relocated to be created upon snapshot update (so 
        boolean createEntity = false;
        boolean tryRip = true;
		BlockPos pos = new BlockPos(tryX, tryY, tryZ);
		if (listBlockUpdateQueue.containsKey(pos)) {
			return true;
		}
        
        if (!tryRip) return true;
		
        if (!ConfigTornado.Storm_Tornado_grabBlocks) return true;
        
        if (isNoDigCoord(tryX, tryY, tryZ)) return true;

        boolean seesLight = false;
        IBlockState state = parWorld.getBlockState(pos);
        Block blockID = state.getBlock();

        //System.out.println(parWorld.getHeightValue(tryX, tryZ));
        if (( /*(canGrab(blockID)) &&blockID != 0 ||*/
                ((WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ)).getY() - 1 == tryY) ||
						WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX + 1, 0, tryZ)).getY() - 1 < tryY ||
						WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ + 1)).getY() - 1 < tryY ||
						WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX - 1, 0, tryZ)).getY() - 1 < tryY ||
						WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(tryX, 0, tryZ - 1)).getY() - 1 < tryY))
                /*(parWorld.getBlockStateId(tryX,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockStateId(tryX+1,tryY,tryZ) == 0 ||
                 parWorld.getBlockStateId(tryX,tryY,tryZ+1) == 0 ||
                 parWorld.getBlockStateId(tryX-1,tryY,tryZ) == 0 ||
                 parWorld.getBlockStateId(tryX,tryY,tryZ-1) == 0 ||
                 parWorld.getBlockStateId(tryX+1,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockStateId(tryX,tryY+1,tryZ+1) == 0 ||
                 parWorld.getBlockStateId(tryX-1,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockStateId(tryX,tryY+1,tryZ-1) == 0)*/
           )
        {
            

            if (parWorld.isBlockLoaded(new BlockPos(storm.pos.xCoord, 128, storm.pos.zCoord)) && /*mod_EntMover.getFPS() > mod_EntMover.safetyCutOffFPS && */blockCount <= ConfigTornado.Storm_Tornado_maxBlocksPerStorm && lastGrabTime < System.currentTimeMillis() && tickGrabCount < ConfigTornado.Storm_Tornado_maxBlocksGrabbedPerTick)
            {
                lastGrabTime = System.currentTimeMillis() - 5;
                //int blockMeta = this.parWorld.getBlockStateMetadata(tryX,tryY,tryZ);
                //rip noise, nm, forces particles
                //parWorld.playAuxSFX(2001, tryX, tryY, tryZ, blockID + blockMeta * 256);

                if (blockID != Blocks.SNOW && blockID != Blocks.GLASS)
                {
                    EntityMovingBlock mBlock = null;

                    if (parWorld.getClosestPlayer(storm.posBaseFormationPos.xCoord, storm.posBaseFormationPos.yCoord, storm.posBaseFormationPos.zCoord, 140, false) != null) {
                    	if (createEntity) {
		                    if (blockID == Blocks.GRASS)
		                    {
		                        mBlock = new EntityMovingBlock(parWorld, tryX, tryY, tryZ, Blocks.DIRT, storm);
		                    }
		                    else
		                    {
		                        mBlock = new EntityMovingBlock(parWorld, tryX, tryY, tryZ, blockID, storm);
		                    }
                    	}
	                    
	                    blockCount++;
	                    
	                    //if (WeatherMod.debug && parWorld.getWorldTime() % 60 == 0) System.out.println("ripping, count: " + WeatherMod.blockCount);

	                    if (mBlock != null) {
	                    	mBlock.setPosition(tryX, tryY, tryZ);
	                    }
	                    
	                    if (createEntity) {
		                    if (!parWorld.isRemote)
		                    {
		                        parWorld.spawnEntity(mBlock);
		                    }
	                    }

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
	                    if (mBlock != null) {
	                    	mBlock.type = 0;
	                    }
	                    seesLight = true;
                    }

                    
                }
                else
                {
                    if (blockID == Blocks.GLASS)
                    {
                        parWorld.playSound(null, new BlockPos(tryX, tryY, tryZ), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.AMBIENT, 5.0F, 1.0F);
                    }
                }
            }
            
            if (WeatherUtil.shouldRemoveBlock(blockID))
            {
                removeCount++;

                /*if (notify)
                {
                    parWorld.setBlockState(pos, Blocks.air.getDefaultState(), 3);
                }
                else
                {
                    parWorld.setBlockState(pos, Blocks.air.getDefaultState(), 0);
                }*/
                
                listBlockUpdateQueue.put(pos, new BlockUpdateSnapshot(parWorld.provider.getDimension(), Blocks.AIR.getDefaultState(), state, pos, true));
            }
        }

        return seesLight;
    }

    public boolean canGrab(World parWorld, Block blockID)
    {
        if (!CoroUtilBlock.isAir(blockID) && WeatherUtil.shouldGrabBlock(parWorld, blockID))
        {
            return true;
        }

        return false;
    }

    public boolean canGrabEntity(Entity ent) {
    	if (ent instanceof EntityPlayer) {
			if (ConfigTornado.Storm_Tornado_grabPlayer) {
				return true;
			}
		} else {
    		if (ConfigTornado.Storm_Tornado_grabPlayersOnly) {
    			return false;
			}
			if (ent instanceof INpc && ConfigTornado.Storm_Tornado_grabVillagers) {
				return true;
			}

			if (ent instanceof IMob && ConfigTornado.Storm_Tornado_grabMobs) {
				return true;
			}

			if (ent instanceof EntityAnimal && ConfigTornado.Storm_Tornado_grabAnimals) {
				return true;
			}
		}
		return false;
	}
	
    public boolean forceRotate(World parWorld/*Entity entity*/)
    {
    	
    	//changed for weather2:
    	//canEntityBeSeen commented out till replaced with coord one, might cause issues
    	
        double dist = grabDist;
        AxisAlignedBB aabb = new AxisAlignedBB(storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord, storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord);
        List list = parWorld.getEntitiesWithinAABB(Entity.class, aabb.expand(dist, this.storm.maxHeight * 3, dist));
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
						if ((entity1 instanceof EntityMovingBlock && !((EntityMovingBlock)entity1).collideFalling)/* || canEntityBeSeen(entity, entity1)*/)
						{
							storm.spinEntity(entity1);
							//spin(entity, conf, entity1);
							foundEnt = true;
						} else {
							if (entity1 instanceof EntityPlayer) {
								//dont waste cpu on server side doing LOS checks, since player movement is client side only, in all situations ive seen
								//actually we need to still change its motion var, otherwise weird things happen
								//if (entity1.world.isRemote) {
								if (WeatherUtilEntity.isEntityOutside(entity1)) {
									//Weather.dbg("entity1.motionY: " + entity1.motionY);
									storm.spinEntity(entity1);
									//spin(entity, conf, entity1);
									foundEnt = true;

									//Weather.dbg("spin player! client side?: " + entity1.world.isRemote);

								}
								//} else {

								//this should match the amount in spinEntity
                        			/*if (entity1.motionY > -0.8) {
                                        entity1.fallDistance = 0F;
                                    }*/
								//}
							} else if (entity1 instanceof EntityLivingBase && WeatherUtilEntity.isEntityOutside(entity1, true)) {//OldUtil.canVecSeeCoords(parWorld, storm.pos, entity1.posX, entity1.posY, entity1.posZ)/*OldUtil.canEntSeeCoords(entity1, entity.posX, entity.posY + 80, entity.posZ)*/) {
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

                /*if ((!(entity1 instanceof EntityPlayer) || ConfigTornado.Storm_Tornado_grabPlayer))
                {
                	if (!(entity1 instanceof EntityPlayer) && ConfigTornado.Storm_Tornado_grabPlayersOnly) {
                		continue;
                	}

                }*/

                if (entity1 instanceof EntityMovingBlock && !entity1.isDead)
                {
                    int var3 = MathHelper.floor(entity1.posX);
                    int var4 = MathHelper.floor(entity1.posZ);
                    byte var5 = 32;
                    /*if(!entity1.world.checkChunksExist(var3 - var5, 0, var4 - var5, var3 + var5, 128, var4 + var5) || !entity1.addedToChunk) {
                        entity1.setEntityDead();
                        mod_EntMover.blockCount--;
                    }*/
                }

                /*if (entity instanceof EntTornado)
                {*/
                    if (entity1 instanceof EntityMovingBlock)
                    {
                        if (blockCount + 5 > ConfigTornado.Storm_Tornado_maxBlocksPerStorm)
                        {
                            if (entity1.posY > 255)
                            {
                                entity1.setDead();
                                //System.out.println(blockCount);
                            }
                        }

                        /*if (entity1.motionX < 0.3F && entity1.motionY < 0.3F && entity1.motionZ < 0.3F && getFPS() < 20 && killCount < 20)
                        {
                            killCount++;
                            entity1.setDead();
                        }*/
                    }
                //}

                //deactivated for weather2
                //if (entity1 instanceof EntityItem && player != null)
                //{
                    //if (entity1.getDistanceToEntity(player) > 32F)
                    //{
                        //if ((((EntityItem) entity).item.itemID) == Block.sand.blockID) {
                        //entity1.setDead();
                        //}
                    //}
                //}
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
    
    @SideOnly(Side.CLIENT)
    public void soundUpdates(boolean playFarSound, boolean playNearSound)
    {
    	
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
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
            if (playFarSound) tryPlaySound(WeatherUtilSound.snd_wind_far, 2, mc.player, volScaleFar, far);
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);
            if (playNearSound) tryPlaySound(WeatherUtilSound.snd_wind_close, 1, mc.player, volScaleClose, close);

            if (storm.levelCurIntensityStage >= storm.STATE_FORMING && storm.stormType == storm.TYPE_LAND/*getStorm().type == getStorm().TYPE_TORNADO*/)
            {
                tryPlaySound(WeatherUtilSound.snd_tornado_dmg_close, 0, mc.player, volScaleClose, close);
            }
        }

        /*if (distToPlayer < far && WeatherUtilSound.lastSoundPositionUpdate < System.currentTimeMillis())
        {
            //System.out.println(sndSys);
            //int j = (int)(field.getFloat(item)
        	WeatherUtilSound.lastSoundPositionUpdate = System.currentTimeMillis() + 100;

            //float gameVol = mc.gameSettings.soundVolume;
            if (WeatherUtilSound.soundID[0] > -1 && WeatherUtilSound.soundTimer[0] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[0]).toString(), volScaleClose);
            }

            if (WeatherUtilSound.soundID[1] > -1 && WeatherUtilSound.soundTimer[1] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[1]).toString(), volScaleClose);
            }

            if (WeatherUtilSound.soundID[2] > -1 && WeatherUtilSound.soundTimer[2] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[2]).toString(), volScaleFar);
            }
        }*/

        //System.out.println(volScaleClose);
        //System.out.println(distToPlayer);
        //world.playRecord("destruction2", (int)posX, (int)posY, (int)posZ);
        //world.playSoundEffect(posX, posY, posZ, "tornado.destruction", 1F, 1.0F);
        //world.playSoundAtEntity(mc.player, "tornado.destruction", 1.0F, 1.0F);
        //world.playRecord("tornado.destruction", (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
        //mc.ingameGUI.recordPlayingUpFor = 0;
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
}
