package weather2.weathersystem.storm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.player.PlayerData;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;
import CoroUtil.util.ChunkCoordinatesBlock;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;

public class StormObject {

	//used on both server and client side, mark things SideOnly where needed
	
	//size, state
	
	//should they extend entity?
	
	//management stuff
	public static long lastUsedStormID = 0; //ID starts from 0 for each game start, no storm nbt disk reload for now
	public long ID; //loosely accurate ID for tracking, but we wanted to persist between world reloads..... need proper UUID??? I guess add in UUID later and dont persist, start from 0 per game run
	public WeatherManagerBase manager;
	public String userSpawnedFor = "";
	
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	
	public int sizeMaxFunnelParticles = 600;
	
	//public WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(1);
	//this was pulled over from weather1 i believe
	public int curWeatherType = 1; //NEEDS SYNCING
	
	//basic info
	public static int static_YPos_layer0 = 200;
	public static int static_YPos_layer1 = 350;
	public static int static_YPos_layer2 = 500;
	public static List<Integer> layers = new ArrayList<Integer>(Arrays.asList(static_YPos_layer0, static_YPos_layer1, static_YPos_layer2));
	public int layer = 0;
	public Vec3 pos = Vec3.createVectorHelper(0, static_YPos_layer0, 0);
	public Vec3 posGround = Vec3.createVectorHelper(0, 0, 0);
	public Vec3 motion = Vec3.createVectorHelper(0, 0, 0);
	
	public boolean angleIsOverridden = false;
	public float angleMovementTornadoOverride = 0;
	
	//growth / progression info
	public int size = 50;
	public int maxSize = ConfigMisc.Storm_MaxRadius;
	public boolean isGrowing = true;
	
	//cloud formation data, helps storms
	public int levelWater = 0; //builds over water and humid biomes, causes rainfall (not technically a storm)
	public float levelWindMomentum = 0; //high elevation builds this, plains areas lowers it, 0 = no additional speed ontop of global speed
	public float levelTemperature = 0; //negative for cold, positive for warm, we subtract 0.7 from vanilla values to make forest = 0, plains 0.1, ocean -0.5, etc
	//public float levelWindDirectionAdjust = 0; //for persistant direction change i- wait just calculate on the fly based on temperature
	
	public int levelWaterStartRaining = 100;
	
	//storm data, used when its determined a storm will happen from cloud front collisions
	public float levelStormIntensityMax = 0; //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	public float levelStormIntensityCur = 0; //since we want storms to build up to a climax still, this will start from 0 and peak to levelStormIntensityMax
	public boolean isRealStorm = false;
	public boolean hasStormPeaked = false;
	
	//helper val, adjust with flags method
	public static float levelStormIntensityFormingStartVal = 4;
	
	
	//spin speed for potential tornado formations, should go up with intensity increase;
	public double spinSpeed = 0.02D;
	
	//PENDING REVISION \\ - use based on levelStormIntensityCur ???
	
	//states that combine all lesser states
	public int state = STATE_NORMAL;
	public static int STATE_NORMAL = 0; //no spin
	//public static int STATE_RAIN = 1; //no spin
	public static int STATE_THUNDER = 2; //no spin
	public static int STATE_SPINNING = 3; //does spin
	public static int STATE_HAIL = 4; //does spin
	
	//used for sure, rain is dependant on water level values
	public boolean attrib_precipitation = false;
	
	//attributes that can happen independantly - revise maybe
	
	public boolean attrib_highwind = false;
	//public boolean attrib_tornado = false;
	public boolean attrib_hurricane = false;
	//F1 - F5 severity states (changes size and damage radius of tornado)
	public int attrib_tornado_severity = 0;
	public static int ATTRIB_FORMINGTORNADO = 1; //the tornado version of forming, not cloud forming
	public static int ATTRIB_F1 = 2;
	public static int ATTRIB_F2 = 3;
	public static int ATTRIB_F3 = 4;
	public static int ATTRIB_F4 = 5;
	public static int ATTRIB_F5 = 6;
	
	//PENDING REVISION //
	
	//TORNADO RIPPING REVISIONS:
	//dont carve into ground, just rip up exposed stuff
	//replace grass with dirt
	//F1 doesnt rip logs, just leaves
	//only F2 and higher rips up trees
	
	//copied from EntTornado
	
	//buildup var - unused in new system currently, but might be needed for touchdown effect
	public float strength = 100;
	
	//unused tornado scale, always 1F
	public float scale = 1F;
	
	public int currentTopYBlock = -1;
	
	public int maxHeight = 60;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	public Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
	public int updateLCG = (new Random()).nextInt();
    
    public float formingStrength = 0; //for transition from 0 (in clouds) to 1 (touch down)
    
    public Vec3 posBaseFormationPos = Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord); //for formation / touchdown progress, where all the ripping methods scan from
    
    public boolean naturallySpawned = true;
    public boolean isDead = false;
	
	public StormObject(WeatherManagerBase parManager) {
		manager = parManager;
		
		if (parManager.getWorld().isRemote) {
			listParticlesCloud = new ArrayList<EntityRotFX>();
			listParticlesFunnel = new ArrayList<EntityRotFX>();
		}
	}
	
	//not used yet
	public void initFirstTime() {
		ID = StormObject.lastUsedStormID++;
		
		BiomeGenBase bgb = manager.getWorld().getBiomeGenForCoords(MathHelper.floor_double(pos.xCoord), MathHelper.floor_double(pos.zCoord));

		
		float temp = bgb.getFloatTemperature();
		
		//initial setting, more apparent than gradual adjustments
		levelTemperature = getTemperatureMCToWeatherSys(bgb.getFloatTemperature());
		levelWater = 0;
		levelWindMomentum = 0;
		
		//Weather.dbg("initialize temp to: " + levelTemperature + " - biome: " + bgb.biomeName);
		
		//defaults are 0.5
		
		/*
		
		0.5  - ocean
		0.5  - river
		0.5  - sky (end)
		
		0.8  - plains
		2.0  - desert
		0.2  - extreme hills
		0.7  - forest
		0.05 - taiga
		0.8  - swampland
		2.0  - hell
		
		0.0  - frozen river
		0.0  - frozen ocean
		0.0  - ice plains
		0.0  - ice mountains
		0.2  - mushroom island
		0.9  - mushroom island shore
		
		0.8  - beach
		2.0  - desert hills
		0.7  - forest hills
		0.05 - taiga hills
		0.2  - extreme hills edge
		1.2  - jungle
		1.2  - jungle hills
		
		
		reorganized temperatures:
		
		0.0
		---
		frozen river
		frozen ocean
		ice plains
		ice mountains
		
		0.05
		---
		taiga
		taiga hills
		
		0.2
		---
		extreme hills
		extreme hills edge
		mushroom island
		
		0.5 (default val)
		---
		ocean
		river
		sky (end)
		
		0.7
		---
		forest
		forest hills
		
		0.8
		---
		plains
		swampland
		beach (we might not have to ignore beach, value seems sane)
		
		0.9
		---
		mushroom island shore
		
		1.2
		---
		jungle
		jungle hills
		
		2.0
		---
		desert
		desert hills
		hell
		
		*/
		 
	}
	
	//not used yet
	public void readFromNBT(NBTTagCompound var1)
    {
		
    }
	
	//not used yet
	public void writeToNBT(NBTTagCompound var1)
    {
		
    }
	
	//receiver method
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		ID = parNBT.getLong("ID");
		//Weather.dbg("StormObject " + ID + " receiving sync");
		
		pos = Vec3.createVectorHelper(parNBT.getInteger("posX"), parNBT.getInteger("posY"), parNBT.getInteger("posZ"));
		size = parNBT.getInteger("size");
		maxSize = parNBT.getInteger("maxSize");
		
		state = parNBT.getInteger("state");
		
		attrib_tornado_severity = parNBT.getInteger("attrib_tornado_severity");
		
		attrib_highwind = parNBT.getBoolean("attrib_highwind");
		//attrib_tornado = parNBT.getBoolean("attrib_tornado");
		attrib_hurricane = parNBT.getBoolean("attrib_hurricane");
		attrib_precipitation = parNBT.getBoolean("attrib_rain");
		
		currentTopYBlock = parNBT.getInteger("currentTopYBlock");
		
		levelTemperature = parNBT.getFloat("levelTemperature");
		levelWater = parNBT.getInteger("levelWater");
		
		layer = parNBT.getInteger("layer");
		
		curWeatherType = parNBT.getInteger("curWeatherType");
		
		//formingStrength = parNBT.getFloat("formingStrength");
		
		levelStormIntensityCur = parNBT.getFloat("levelStormIntensityCur");
	}
	
	//compose nbt data for packet (and serialization in future)
	public NBTTagCompound nbtSyncForClient() {
		NBTTagCompound data = new NBTTagCompound();
		
		data.setInteger("posX", (int)pos.xCoord);
		data.setInteger("posY", (int)pos.yCoord);
		data.setInteger("posZ", (int)pos.zCoord);
		
		data.setLong("ID", ID);
		data.setInteger("size", size);
		data.setInteger("maxSize", maxSize);
		
		data.setInteger("state", state);
		
		data.setInteger("attrib_tornado_severity", attrib_tornado_severity);
		
		data.setBoolean("attrib_highwind", attrib_highwind);
		//data.setBoolean("attrib_tornado", attrib_tornado);
		data.setBoolean("attrib_hurricane", attrib_hurricane);
		data.setBoolean("attrib_rain", attrib_precipitation);
		
		data.setInteger("currentTopYBlock", currentTopYBlock);
		
		data.setFloat("levelTemperature", levelTemperature);
		data.setInteger("levelWater", levelWater);
		
		data.setInteger("layer", layer);
		
		data.setInteger("curWeatherType", curWeatherType);
		
		//data.setFloat("formingStrength", formingStrength);
		
		data.setFloat("levelStormIntensityCur", levelStormIntensityCur);
		
		return data;
	}
	
	public void tick() {
		//Weather.dbg("ticking storm " + ID + " - manager: " + manager);
		
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord);
		posGround.yCoord = currentTopYBlock;
		
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			if (!WeatherUtil.isPaused()) {
				tickClient();
			}
		} else {

			//debug \\
			
			//maxSize = 200;
			//isGrowing = true;
			
			/*maxSize = 200;
			//size = maxSize;
			isGrowing = true;
			//state = STATE_HAIL;
			state = STATE_NORMAL;
			attrib_hurricane = false;
			attrib_tornado = true;
			attrib_tornado = false;
			attrib_highwind = false;
			attrib_tornado_severity = 0;*/
			//attrib_tornado_severity = ATTRIB_F1;
			//debug //
			
			
			
			tickMovement();
			
			//System.out.println("cloud motion: " + motion + " wind angle: " + angle);
			
			if (layer == 0) {
				tickWeatherEvents();
				tickCloudProgression();
				tickSnowFall();
			}
	        
		}
		
        //sync X Y Z, Y gets changed below
        posBaseFormationPos = Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord);

        if (levelStormIntensityCur >= StormObject.levelStormIntensityFormingStartVal) {
        	if (levelStormIntensityCur >= StormObject.levelStormIntensityFormingStartVal + 1) {
        		formingStrength = 1;
        		posBaseFormationPos.yCoord = posGround.yCoord;
        	} else {
		        float val = levelStormIntensityCur - StormObject.levelStormIntensityFormingStartVal;
		        formingStrength = val;
		        double yDiff = pos.yCoord - posGround.yCoord;
		        posBaseFormationPos.yCoord = pos.yCoord - (yDiff * formingStrength);
        	}
        } else {
        	formingStrength = 0;
        	posBaseFormationPos.yCoord = pos.yCoord;
        }
        
		if (attrib_tornado_severity >= ATTRIB_FORMINGTORNADO) {
			tornadoHelper.tick(manager.getWorld());
		}
		
	}
	
	public void tickMovement() {

		//storm movement via wind
		float angle = getAdjustedAngle();
		
		if (angleIsOverridden) {
			angle = angleMovementTornadoOverride;
			//debug
			/*if (manager.getWorld().getTotalWorldTime() % 20 == 0) {
				EntityPlayer entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1);
				if (entP != null) {
					
					//even more debug, heat seak test
					//Random rand = new Random();
					double var11 = entP.posX - pos.xCoord;
		            double var15 = entP.posZ - pos.zCoord;
		            float yaw = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;
		            //weather override!
		            //yaw = weatherMan.wind.direction;
		            //int size = ConfigMisc.Storm_Tornado_aimAtPlayerAngleVariance;
		            //yaw += rand.nextInt(size) - (size / 2);
					
					angleMovementTornadoOverride = yaw;
					
					Weather.dbg("angle override: " + angle + " - dist from player: " + entP.getDistance(pos.xCoord, pos.yCoord, pos.zCoord));
				}
					
			}*/
		}
		
		//Weather.dbg("cur angle: " + angle);
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		
		float cloudSpeedAmp = 5F;
		
		cloudSpeedAmp /= ((float)attrib_tornado_severity+1F);
		
		float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;
		
		if (finalSpeed > 0.3F) {
			finalSpeed = 0.3F;
		}
		
		if (attrib_tornado_severity > 0) {
			if (finalSpeed > 0.1F) {
				finalSpeed = 0.1F;
			}
			//Weather.dbg("storm speed: " + finalSpeed);
		}
		
		
		motion.xCoord = vecX * finalSpeed;
		motion.zCoord = vecZ * finalSpeed;
		
		double max = 0.2D;
		//max speed
		
		/*if (motion.xCoord < -max) motion.xCoord = -max;
		if (motion.xCoord > max) motion.xCoord = max;
		if (motion.zCoord < -max) motion.zCoord = -max;
		if (motion.zCoord > max) motion.zCoord = max;*/
		
		//actually move storm
		pos.xCoord += motion.xCoord;
		pos.zCoord += motion.zCoord;
	}
	
	public void tickWeatherEvents() {
		Random rand = new Random();
		World world = manager.getWorld();
		
		currentTopYBlock = world.getHeightValue(MathHelper.floor_double(pos.xCoord), MathHelper.floor_double(pos.zCoord));
		//Weather.dbg("currentTopYBlock: " + currentTopYBlock);
		if (state >= STATE_THUNDER) {
			if (rand.nextInt((int)Math.max(1, ConfigMisc.Storm_LightningStrikeBaseValueOddsTo1 - (levelStormIntensityCur * 10))) == 0) {
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				int y = world.getPrecipitationHeight(x, z);
				if (world.checkChunksExist(x, y, z, x, y, z)) {
					//if (world.canLightningStrikeAt(x, y, z)) {
						addWeatherEffectLightning(new EntityLightningBolt(world, (double)x, (double)y, (double)z));
					//}
				}
			}
		}
		
		if (attrib_precipitation && state >= STATE_HAIL) {
			//if (rand.nextInt(1) == 0) {
			for (int i = 0; i < 10; i++) {
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				if (world.checkChunksExist(x, static_YPos_layer0, z, x, static_YPos_layer0, z) && (world.getClosestPlayer(x, 50, z, 80) != null)) {
					//int y = world.getPrecipitationHeight(x, z);
					//if (world.canLightningStrikeAt(x, y, z)) {
					EntityIceBall hail = new EntityIceBall(world);
					hail.setPosition(x, layers.get(layer), z);
					world.spawnEntityInWorld(hail);
					//world.addWeatherEffect(new EntityLightningBolt(world, (double)x, (double)y, (double)z));
					//}
					
					//System.out.println("spawned hail: " );
				} else {
					//System.out.println("nope");
				}
			}
		}
		
		
	}
	
	public void tickSnowFall() {
		
		if (!attrib_precipitation) return;
		
		World world = manager.getWorld();
		
		//CHANGE THIS PART TO ITERATE OVER THE STORM SIZE, NOT ENTIRE ACTIVE CHUNKS!
		/*Iterator iterator = world.activeChunkSet.iterator();
        doneChunks.retainAll(world.activeChunkSet);
        if (doneChunks.size() == world.activeChunkSet.size())
        {
            doneChunks.clear();
        }

        

        while (iterator.hasNext())*/
		
		final long startTime = System.nanoTime();
		
		int xx = 0;
		int zz = 0;
		
		//Weather.dbg("set size: " + size);
		
		//EntityPlayer entP = world.getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1);
		
		//if (entP != null) {
		
			for (xx = (int) (pos.xCoord - size/2); xx < pos.xCoord + size/2; xx+=16) {
				for (zz = (int) (pos.zCoord - size/2); zz < pos.zCoord + size/2; zz+=16) {
			/*for (xx = (int) (entP.posX - size/2); xx < entP.posX + size/2; xx+=16) {
				for (zz = (int) (entP.posZ - size/2); zz < entP.posZ + size/2; zz+=16) {*/
		        	//ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)iterator.next();
					
					//temp override test
					/*if (entP != null) {
						xx = (int) entP.posX;
						zz = (int) entP.posZ;
					}*/
					
		        	int chunkX = xx / 16;
		        	int chunkZ = zz / 16;
		            int x = chunkX * 16;
		            int z = chunkZ * 16;
		            //world.theProfiler.startSection("getChunk");
		            Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		            //world.moodSoundAndLightCheck(k, l, chunk);
		            //world.theProfiler.endStartSection("tickChunk");
		            //Limits and evenly distributes the lighting update time
		            /*if (System.nanoTime() - startTime <= 4000000 && doneChunks.add(chunkcoordintpair))
		            {
		                chunk.updateSkylight();
		            }*/
		            int i1;
		            int xxx;
		            int zzz;
		            int setBlockHeight;
		            
		            int i2;
		            
					if (world.provider.canDoRainSnowIce(chunk) && (ConfigMisc.Snow_RarityOfBuildup == 0 || world.rand.nextInt(ConfigMisc.Snow_RarityOfBuildup) == 0))
			        {
			            updateLCG = updateLCG * 3 + 1013904223;
			            i1 = updateLCG >> 2;
			            xxx = i1 & 15;
			            zzz = i1 >> 8 & 15;
			

			
						double d0 = pos.xCoord - (xx + xxx);
				        double d2 = pos.zCoord - (zz + zzz);
				        if ((double)MathHelper.sqrt_double(d0 * d0 + d2 * d2) > size) continue;
			            
			            //j1 = 1;
			            //k1 = 1;
			            
			            int snowMetaMax = 7; //snow loops past 6 for some reason
			            
			            setBlockHeight = world.getPrecipitationHeight(xxx + x, zzz + z);
			            
			            
			
			            if (canSnowAtBody(xxx + x, setBlockHeight, zzz + z)) {
			            //if (entP != null && entP.getDistance(xx, entP.posY, zz) < 16) {
			            	boolean perform = false;
			            	int id = world.getBlockId(xxx + x, setBlockHeight, zzz + z);
			            	int meta = 0;
			            	if (id == Block.snow.blockID) {
			            		if (ConfigMisc.Snow_ExtraPileUp) {
				            		meta = world.getBlockMetadata(xxx + x, setBlockHeight, zzz + z);
				            		if (meta < snowMetaMax) {
				            			perform = true;
					            		meta += 1;
				            		} else {
				            			if (ConfigMisc.Snow_MaxBlockBuildupHeight > 1) {
				            				int i;
				            				int originalSetBlockHeight = setBlockHeight;
				            				for (i = 0; i < ConfigMisc.Snow_MaxBlockBuildupHeight; i++) {
				            					int checkID = world.getBlockId(xxx + x, originalSetBlockHeight + i, zzz + z);
				            					if (checkID == Block.snow.blockID) {
				            						meta = world.getBlockMetadata(xxx + x, originalSetBlockHeight + i, zzz + z);
				            						if (meta < snowMetaMax) {
				            							setBlockHeight = originalSetBlockHeight + i;
				    			            			perform = true;
				    				            		meta += 1;
				    				            		break;
				            						} else {
				            							//let it continue to next height
				            						}
				            					} else if (checkID == 0) {
				            						meta = 0;
				            						setBlockHeight = originalSetBlockHeight + i;
			    			            			perform = true;
			    			            			break;
				            					}
				            				}
				            				//if the loop went past the max height
				            				if (i == ConfigMisc.Snow_MaxBlockBuildupHeight) {
				            					perform = false;
				            				}
				            			}
				            		}
			            		}
			            	} else {
			            		perform = true;
			            	}
			            	if (perform) {
			            		//Weather.dbg("set data: " + setBlockHeight + " - meta: " + meta);
			            		if (ConfigMisc.Snow_SmoothOutPlacement) {
			            			//spread out as it was trying to go from ...
			            			int origMeta = Math.max(0, meta-1);
			            			if (origMeta > snowMetaMax - 4/*snowMetaMax / 2*/) {
			            				//Weather.dbg("SMOOTHING TRY!");
				            			ChunkCoordinatesBlock coords = getSnowfallEvenOutAdjustCheck(xxx + x, setBlockHeight, zzz + z, origMeta);
				            			//if detected a smooth out requirement
				            			if (coords.posX != 0 || coords.posZ != 0) {
				            				if (meta != coords.meta + 1) {
					            				//Weather.dbg("SMOOTHING PERFORM! - meta was: " + origMeta + " - is now coords.meta: " + coords.meta);
					            				xxx = coords.posX;
					            				zzz = coords.posZ;
					            				meta = coords.meta + 1;
				            				} else {
				            					perform = false;
				            					//Weather.dbg("false positive! wasted work!");
				            				}
				            			} else {
				            				//Weather.dbg("SMOOTHING DENY!");
				            			}
			            			}
			            		}
			            	}
			            	
			            	if (perform) {
			            		world.setBlock(xxx + x, setBlockHeight, zzz + z, Block.snow.blockID, meta, 3);
			            	}
			            }
			        }
				}
			}
			
		//}
	}
	
	//questionably efficient code, but really there isnt much better options
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjustCheck(int x, int y, int z, int sourceMeta) {
		//filter out diagonals
		ChunkCoordinatesBlock attempt;
		attempt = getSnowfallEvenOutAdjust(x-1, y, z, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
		attempt = getSnowfallEvenOutAdjust(x+1, y, z, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
		attempt = getSnowfallEvenOutAdjust(x, y, z-1, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
		attempt = getSnowfallEvenOutAdjust(x, y, z+1, sourceMeta);
		if (attempt.posX != 0 || attempt.posZ != 0) return attempt;
		return new ChunkCoordinatesBlock(0, 0, 0, 0, 0);
	}
	
	//return relative values, id 0 (to mark its ok to start snow here) or id snow (to mark check meta), and meta of detected snow if snow (dont increment it, thats handled after this)
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjust(int x, int y, int z, int sourceMeta) {
		
		//only check down once, if air, check down one more time, if THAT is air, we dont allow spread out, because we dont want to loop all the way down to bottom of some cliff
		//could use getHeight but then we'd have to difference check the height and that might complicate things...
		
		int metaToSet = 0;
		
		World world = manager.getWorld();
		int checkID = world.getBlockId(x, y, z);
		//check for starting with no snow
		if (checkID == 0) {
			int checkID2 = world.getBlockId(x, y-1, z);
			//make sure somethings underneath it - we shouldnt need to check deeper because we spread out while meta of snow is halfway, before it can start a second pile
			if (checkID2 == 0) {
				//Weather.dbg("1");
				return new ChunkCoordinatesBlock(0, 0, 0, 0, 0);
			} else {
				//Weather.dbg("2");
				//return that its an open area to start snow at
				return new ChunkCoordinatesBlock(x, y, z, 0, 0);
			}
		} else if (checkID == Block.snow.blockID) {
			int checkMeta = world.getBlockMetadata(x, y, z);
			//if detected snow is shorter, return with detected meta val!
			//adjusting to <=
			if (checkMeta < sourceMeta) {
				//Weather.dbg("3 - checkMeta: " + checkMeta + " vs sourceMeta: " + sourceMeta);
				return new ChunkCoordinatesBlock(x, y, z, checkID, checkMeta);
			}
		} else {
			return new ChunkCoordinatesBlock(0, 0, 0, 0, 0);
		}
		return new ChunkCoordinatesBlock(0, 0, 0, 0, 0);
	}
	
	public boolean canSnowAtBody(int par1, int par2, int par3)
    {
		World world = manager.getWorld();
		
        BiomeGenBase biomegenbase = world.getBiomeGenForCoords(par1, par3);
        //float f = biomegenbase.getFloatTemperature();

        if (levelTemperature > 0/*f > 0.15F*/)
        {
            return false;
        }
        else
        {
            if (par2 >= 0 && par2 < 256 && world.getSavedLightValue(EnumSkyBlock.Block, par1, par2, par3) < 10)
            {
                int l = world.getBlockId(par1, par2 - 1, par3);
                int i1 = world.getBlockId(par1, par2, par3);

                if ((i1 == 0 || i1 == Block.snow.blockID)/* && Block.snow.canPlaceBlockAt(world, par1, par2, par3)*/ && l != 0 && l != Block.ice.blockID && Block.blocksList[l].blockMaterial.blocksMovement())
                {
                    return true;
                }
            }

            return false;
        }
    }
	
	public void tickCloudProgression() {
		World world = manager.getWorld();
		
		//storm progression, heavy WIP
		if (world.getTotalWorldTime() % 3 == 0) {
			if (isGrowing) {
				if (size < maxSize) {
					size++;
				} else {
					//isGrowing = false;
				}
			} else {
				/*if (size > 0) {
					size--;
				} else if (size <= 0) {
					//kill
					//manager.removeStormObject(ID);
				}*/
			}

			//System.out.println("cur size: " + size);
		}
		
		float tempAdjustRate = (float)ConfigMisc.Storm_TemperatureAdjustRate;//0.1F;
		int levelWaterBuildRate = ConfigMisc.Storm_Rain_WaterBuildUpRate;
		int levelWaterSpendRate = ConfigMisc.Storm_Rain_WaterSpendRate;
		int randomChanceOfWaterBuildFromWater = ConfigMisc.Storm_Rain_WaterBuildUpOddsTo1FromSource;
		int randomChanceOfWaterBuildFromNothing = ConfigMisc.Storm_Rain_WaterBuildUpOddsTo1FromNothing;
		int randomChanceOfRain = ConfigMisc.Player_Storm_Rain_OddsTo1;
		
		if (world.getTotalWorldTime() % ConfigMisc.Storm_AllTypes_TickRateDelay == 0) {
			
			NBTTagCompound playerNBT = PlayerData.getPlayerNBT(userSpawnedFor);
			
			long lastStormDeadlyTime = playerNBT.getLong("lastStormDeadlyTime");
			//long lastStormRainTime = playerNBT.getLong("lastStormRainTime");
			
			BiomeGenBase bgb = world.getBiomeGenForCoords(MathHelper.floor_double(pos.xCoord), MathHelper.floor_double(pos.zCoord));
			
			//temperature scan
			float biomeTempAdj = getTemperatureMCToWeatherSys(bgb.getFloatTemperature());
			if (levelTemperature > biomeTempAdj) {
				levelTemperature -= tempAdjustRate;
			} else {
				levelTemperature += tempAdjustRate;
			}
			
			boolean performBuildup = false;
			
			Random rand = new Random();
			
			if (!attrib_precipitation && rand.nextInt(randomChanceOfWaterBuildFromNothing) == 0) {
				performBuildup = true;
			}
			
			//water scan - dont build up if raining already
			if (!performBuildup && !attrib_precipitation && rand.nextInt(randomChanceOfWaterBuildFromWater) == 0) {
				int blockID = world.getBlockId(MathHelper.floor_double(pos.xCoord), currentTopYBlock-1, MathHelper.floor_double(pos.zCoord));
				if (blockID != 0) {
					Block block = Block.blocksList[blockID];
					if (block.blockMaterial instanceof MaterialLiquid) {
						performBuildup = true;
					}
				}
				
				if (!performBuildup && bgb.biomeName.contains("Ocean") || bgb.biomeName.contains("ocean") || bgb.biomeName.contains("Swamp") || bgb.biomeName.contains("Jungle") || bgb.biomeName.contains("River")) {
					performBuildup = true;
				}
			}
			
			if (performBuildup) {
				//System.out.println("RAIN BUILD TEMP OFF");
				levelWater += levelWaterBuildRate;
				//Weather.dbg("building rain: " + levelWater);
			}
			
			//water values adjust when raining
			if (attrib_precipitation) {
				levelWater -= levelWaterSpendRate;
				
				if (levelWater < 0) levelWater = 0;
				
				if (levelWater <= 0) {
					attrib_precipitation = false;
					Weather.dbg("ending raining for: " + ID);
				}
			} else {
				if (levelWater >= levelWaterStartRaining) {
					if (rand.nextInt(randomChanceOfRain) == 0) {
						attrib_precipitation = true;
						Weather.dbg("starting raining for: " + ID);
					}
				}
			}
			
			if (lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks < world.getTotalWorldTime()) {
				int stormFrontCollideDist = ConfigMisc.Storm_Deadly_CollideDistance;
				int randomChanceOfCollide = ConfigMisc.Player_Storm_Deadly_OddsTo1;
				
				if (rand.nextInt(randomChanceOfCollide) == 0) {
					for (int i = 0; i < manager.getStormObjects().size(); i++) {
						StormObject so = manager.getStormObjects().get(i);
						
						boolean startStorm = false;
						
						if (so.ID != this.ID && so.levelStormIntensityCur <= 0) {
							if (so.pos.distanceTo(pos) < stormFrontCollideDist) {
								if (this.levelTemperature < 0) {
									if (so.levelTemperature > 0) {
									}
								} else if (this.levelTemperature > 0) {
									if (so.levelTemperature < 0) {
										startStorm = true;
									}
								}
							}
						}
						
						if (startStorm) {
							
							playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());
							
							//EntityPlayer entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1);
							EntityPlayer entP = world.getPlayerEntityByName(userSpawnedFor);
							
							if (entP != null) {
								initRealStorm(entP, so);
							} else {
								initRealStorm(null, so);
								//can happen, chunkloaded emtpy overworld, let the storm do what it must without a player
								//Weather.dbg("Weather2 WARNING!!!! Failed to get a player object for new tornado, this shouldnt happen");
							}
							
							break;
						}
					}
				}
			}
			
			if (isRealStorm) {
				float levelStormIntensityRate = 0.1F;
				
				if (!hasStormPeaked) {
					levelStormIntensityCur += levelStormIntensityRate;
					
					Weather.dbg("storm ID: " + this.ID + " - growing at intensity: " + levelStormIntensityCur);
					
					if (levelStormIntensityCur > levelStormIntensityMax) {
						Weather.dbg("storm peaked at: " + levelStormIntensityMax);
						hasStormPeaked = true;
					}
				} else {
					if (levelStormIntensityCur > 0) {
						levelStormIntensityCur -= (levelStormIntensityRate * 0.3F);
					
						Weather.dbg("storm ID: " + this.ID + " - dying at intensity: " + levelStormIntensityCur);
						
						if (levelStormIntensityCur < 0) {
							levelStormIntensityCur = 0;
							
							//maybe unneeded here
							setNoStorm();
						}
					} else {
						
					}
				}
				
				//levelStormIntensityCur value ranges and what they influence
				//0 - 1+ = rain - thunderstorm
				//1 - 2+ = thunderstorm - high wind (and more rain???)
				//2 - 3+ = high wind - hail
				//3 - 4+ = hail - tornado forming
				//4 - 5+ = tornado forming - F1 tornado
				//5 - 6+ = F1 - F2
				//6 - 7+ = F2 - F3
				//7 - 8+ = F3 - F4
				//8 - 9+ = F4 - F5
				//9 - 10+ = F5 - hurricane ??? (perhaps hurricanes spawn differently, like over ocean only, and sustain when hitting land for a bit)
				
				//what about tropical storm? that is a mini hurricane, perhaps also ocean based
				
				//levelWindMomentum = rate of increase of storm??? (in addition to the pre storm system speeds)
				
				
				
				//POST DEV NOTES READ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
				
				//it might be a good idea to make something else determine increase from high winds to tornado and higher
				//using temperatures is a little unstable at such a large range of variation....
				
				updateStormFlags();
			}
			
			
		}
	}
	
	public void initRealStorm(EntityPlayer entP, StormObject stormToAbsorb) {
		
		
		isRealStorm = true;
		float diff = 4;
		if (stormToAbsorb != null) {
			diff = this.levelTemperature - stormToAbsorb.levelTemperature;
		}
		if (naturallySpawned) {
			this.levelWater = this.levelWaterStartRaining * 2;
			this.levelStormIntensityMax = (float) (diff * ConfigMisc.Storm_IntensityAmplifier);
			if (levelStormIntensityMax < ConfigMisc.Storm_Deadly_MinIntensity) {
				levelStormIntensityMax = (float)ConfigMisc.Storm_Deadly_MinIntensity;
			}
		}
		this.attrib_precipitation = true;

		if (stormToAbsorb != null) {
			Weather.dbg("stormfront collision happened between ID " + this.ID + " and " + stormToAbsorb.ID + " - intensity max: " + levelStormIntensityMax);
			manager.removeStormObject(stormToAbsorb.ID);
		}
		
		if (ConfigMisc.Storm_Tornado_aimAtPlayerOnSpawn) {
			
			if (entP != null) {
				aimStormAtClosestOrProvidedPlayer(entP);
			}
			
		}
	}
	
	public void aimStormAtClosestOrProvidedPlayer(EntityPlayer entP) {
		
		if (entP == null) {
			entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1);
		}
		
		if (entP != null) {
			Random rand = new Random();
			double var11 = entP.posX - pos.xCoord;
            double var15 = entP.posZ - pos.zCoord;
            float yaw = -(float)(Math.atan2(var11, var15) * 180.0D / Math.PI);
            //weather override!
            //yaw = weatherMan.wind.direction;
            int size = ConfigMisc.Storm_Tornado_aimAtPlayerAngleVariance;
            if (size > 0) {
            	yaw += rand.nextInt(size) - (size / 2);
            }
            
            angleIsOverridden = true;
			angleMovementTornadoOverride = yaw;
			
			Weather.dbg("stormfront aimed at player " + entP.username);
		}
	}
	
	public void updateStormFlags() {
		if (levelStormIntensityCur >= 9) {
			attrib_hurricane = true;
		} else if (levelStormIntensityCur >= 9) {
			attrib_tornado_severity = ATTRIB_F5;
		} else if (levelStormIntensityCur >= 8) {
			attrib_tornado_severity = ATTRIB_F4;
		} else if (levelStormIntensityCur >= 7) {
			attrib_tornado_severity = ATTRIB_F3;
		} else if (levelStormIntensityCur >= 6) {
			attrib_tornado_severity = ATTRIB_F2;
		} else if (levelStormIntensityCur >= 5) {
			attrib_tornado_severity = ATTRIB_F1;
		} else if (levelStormIntensityCur >= 4) {
			//once again aim the storm back at player after forming if it overshot them, to solve some taking too long to buildup and passing over them >:D
			if (ConfigMisc.Storm_Tornado_aimAtPlayerOnSpawn) {
				if (!hasStormPeaked && attrib_tornado_severity != ATTRIB_FORMINGTORNADO) {
					aimStormAtClosestOrProvidedPlayer(null);
				}
			}
			attrib_tornado_severity = ATTRIB_FORMINGTORNADO;
			state = this.STATE_SPINNING;
		} else if (levelStormIntensityCur >= 3) {
			state = this.STATE_HAIL;
		} else if (levelStormIntensityCur >= 2) {
			attrib_highwind = true;
		} else if (levelStormIntensityCur >= 1) {
			state = this.STATE_THUNDER;
		} else if (levelStormIntensityCur > 0) {
			//already added rain when combining storms - but what about commands?
			attrib_precipitation = true;
			state = this.STATE_NORMAL;
		} else {
			setNoStorm();
		}
		
		//TEEEEEEEESSSSSSSSTTTTTTTTTTTTTT
		//aimStormAtClosestOrProvidedPlayer(null);
		
		curWeatherType = Math.min(WeatherTypes.weatherEntTypes.size()-1, Math.max(1, attrib_tornado_severity - 1));
	}
	
	//FYI rain doesnt count as storm
	public void setNoStorm() {
		Weather.dbg("storm ID: " + this.ID + " - ended storm event");
		state = this.STATE_NORMAL;
		attrib_highwind = false;
		attrib_tornado_severity = 0;
		isRealStorm = false;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
				particleBehaviorFog.tickUpdateList();
			}
		}
        
		EntityPlayer entP = Minecraft.getMinecraft().thePlayer;
		
		spinSpeed = 0.02D;
		double spinSpeedMax = 0.4D;
		if (attrib_hurricane) {
			spinSpeed = spinSpeedMax * 0.4D;
		} else if (attrib_tornado_severity > 0) {
			spinSpeed = spinSpeedMax * 0.2D;
		} else if (attrib_highwind) {
			spinSpeed = spinSpeedMax * 0.1D;
		} else {
			spinSpeed = spinSpeedMax * 0.05D;
		}
		
		if (size == 0) size = 1;
		int delay = Math.max(1, (int)(100F / size * 1F));
		int loopSize = 1;//(int)(1 * size * 0.1F);
		
		int extraSpawning = 0;
		
		if (state >= STATE_SPINNING) {
			loopSize += 4;
			extraSpawning = 300;
		}
		
		//Weather.dbg("size: " + size + " - delay: " + delay); 
		
		Random rand = new Random();
		
		Vec3 playerAdjPos = Vec3.createVectorHelper(entP.posX, pos.yCoord, entP.posZ);
		double maxSpawnDistFromPlayer = 512;
		
		//spawn clouds
		if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigMisc.Cloud_ParticleSpawnDelay) == 0) {
			for (int i = 0; i < loopSize; i++) {
				if (listParticlesCloud.size() < size + extraSpawning) {
					double spawnRad = size;
					
					if (layer != 0) {
						spawnRad = size * 5;
					}
					
					Vec3 tryPos = Vec3.createVectorHelper(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layers.get(layer), pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						EntityRotFX particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord, 2);
						
				    	/*if (layer == 0) {
				    		particle.particleScale = 500;
				    	} else {
				    		particle.particleScale = 2000;
				    	}*/
						
						listParticlesCloud.add(particle);
					}
				}
				
				
			}
		}
		
		delay = 1;
		loopSize = 2;
		
		double spawnRad = size/48;
		
		if (curWeatherType+1 >= ATTRIB_F5) {
			spawnRad = 200;
			loopSize = 10;
			sizeMaxFunnelParticles = 1200;
		} else if (curWeatherType+1 >= ATTRIB_F4) {
			spawnRad = 150;
			loopSize = 8;
			sizeMaxFunnelParticles = 1000;
		} else if (curWeatherType+1 >= ATTRIB_F3) {
			spawnRad = 100;
			loopSize = 6;
			sizeMaxFunnelParticles = 800; 
		} else if (curWeatherType+1 >= ATTRIB_F2) {
			spawnRad = 50;
			loopSize = 4;
			sizeMaxFunnelParticles = 600;
		} else {
			sizeMaxFunnelParticles = 600;
		}
		
		//spawn funnel
		if (attrib_tornado_severity != 0) {
			if (this.manager.getWorld().getTotalWorldTime() % delay == 0) {
				for (int i = 0; i < loopSize; i++) {
					//temp comment out
					//if (attrib_tornado_severity > 0) {
					
					//trim!
					if (listParticlesFunnel.size() >= sizeMaxFunnelParticles) {
						listParticlesFunnel.get(0).setDead();
						listParticlesFunnel.remove(0);
					}
					
					if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {
						
						
						Vec3 tryPos = Vec3.createVectorHelper(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						//int y = entP.worldObj.getPrecipitationHeight((int)tryPos.xCoord, (int)tryPos.zCoord);
						
						if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
							EntityRotFX particle = spawnFogParticle(tryPos.xCoord, posBaseFormationPos.yCoord, tryPos.zCoord, 3);
							
							//move these to a damn profile damnit!
							particle.setMaxAge(150 + (curWeatherType * 100) + rand.nextInt(100));
							
							float baseBright = 0.3F;
							float randFloat = (rand.nextFloat() * 0.6F);
							
							float finalBright = Math.min(1F, baseBright+randFloat);
							particle.setRBGColorF(finalBright, finalBright, finalBright);
							
							particle.particleScale = 250;
							
							listParticlesFunnel.add(particle);
						}
					} else {
						Weather.dbg("particles maxed");
					}
				}
			}
		}
		
		for (int i = 0; i < listParticlesFunnel.size(); i++) {
			EntityRotFX ent = listParticlesFunnel.get(i);
			if (ent.isDead) {
				listParticlesFunnel.remove(ent);
			} else {
				 double var16 = this.pos.xCoord - ent.posX;
                 double var18 = this.pos.zCoord - ent.posZ;
                 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                 ent.rotationPitch = -30F;
                 
                 spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (ent.isDead) {
				listParticlesCloud.remove(ent);
			} else {
				//ent.posX = pos.xCoord + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.entityId);
				float posZ = (float) Math.cos(ent.entityId);
				ent.setPosition(pos.xCoord + posX*radius, ent.posY, pos.zCoord + posZ*radius);*/
		        
				double curSpeed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
				
				double curDist = ent.getDistance(pos.xCoord, ent.posY, pos.zCoord);

				float dropDownRange = 15F;
		        
		        float extraDropCalc = 0;
		        if (curDist < 200 && ent.entityId % 20 < 5) {
		        	extraDropCalc = ((ent.entityId % 20) * dropDownRange);
		        }
				
				if (state >= STATE_SPINNING) {
					double speed = spinSpeed + (rand.nextDouble() * 0.01D);
					double distt = size;//300D;
					
					
					double vecX = ent.posX - pos.xCoord;
			        double vecZ = ent.posZ - pos.zCoord;
			        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
			        //System.out.println("angle: " + angle);
			        
			        //fix speed causing inner part of formation to have a gap
			        angle += speed * 50D;
			        //angle += 20;
			        
			        angle -= (ent.entityId % 10) * 3D;
			        
			        //random addition
			        angle += rand.nextInt(10) - rand.nextInt(10);
			        
			        if (curDist > distt) {
			        	//System.out.println("curving");
			        	angle += 40;
			        	//speed = 1D;
			        }
			        
			        //keep some near always
			        if (ent.entityId % 20 < 5) {
			        	angle += 30 + ((ent.entityId % 5) * 4);
			        	
			        	double var16 = this.pos.xCoord - ent.posX;
		                double var18 = this.pos.zCoord - ent.posZ;
		                ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
		                ent.rotationPitch = -20F - (ent.entityId % 10);
			        }
			        
			        
			        
	
			        
			        /*if (curDist < 30) {
			        	ent.motionY -= 0.2D;
			        	if (ent.rotationPitch > 0) {
			        		ent.rotationPitch--;
			        	} else if (ent.rotationPitch < 0) {
			        		ent.rotationPitch++;
			        	} else {
			        		ent.rotationPitch = 0;
			        	}
			        	
			        	angle = -45;
			        } else {
			        	if (ent.rotationPitch > 90) {
			        		ent.rotationPitch--;
			        	} else if (ent.rotationPitch < 90) {
			        		ent.rotationPitch++;
			        	} else {
			        		ent.rotationPitch = 90;
			        	}
			        	//angle = 90;
			        	
			        }*/
			        
			        
			        
			        if (curSpeed < speed * 20D) {
			        	ent.motionX += -Math.sin(Math.toRadians(angle)) * speed;
				        ent.motionZ += Math.cos(Math.toRadians(angle)) * speed;
			        }
				} else {
					float cloudMoveAmp = 0.2F * (1 + layer);
					
					float speed = getAdjustedSpeed() * cloudMoveAmp;
					float angle = getAdjustedAngle();
					
					dropDownRange = 5;
			        if (/*curDist < 200 && */ent.entityId % 20 < 5) {
			        	extraDropCalc = ((ent.entityId % 20) * dropDownRange);
			        }
					
					if (curSpeed < speed * 1D) {
			        	ent.motionX += -Math.sin(Math.toRadians(angle)) * speed;
				        ent.motionZ += Math.cos(Math.toRadians(angle)) * speed;
			        }
				}
		        
				if (Math.abs(ent.posY - (pos.yCoord - extraDropCalc)) > 2F) {
			        if (ent.posY < pos.yCoord - extraDropCalc) {
		        		ent.motionY += 0.1D;
		        	} else {
		        		ent.motionY -= 0.1D;
		        	}
				}
		        
		        if (ent.motionY < -0.15F) {
		        	ent.motionY = -0.15F;
		        }
		        
		        if (ent.motionY > 0.15F) {
		        	ent.motionY = 0.15F;
		        }
		        
		        //double distToGround = ent.worldObj.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
		        
		        //ent.setPosition(ent.posX, pos.yCoord, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.setDead();
				listParticles.remove(ent);
			}*/
		}
		
		//System.out.println("size: " + listParticlesCloud.size());
	}
	
	public float getAdjustedSpeed() {
		return manager.windMan.getWindSpeedForClouds();
	}
	
	public float getAdjustedAngle() {
		float angle = manager.windMan.getWindAngleForClouds();
		
		float angleAdjust = Math.max(45, 45F * levelTemperature * 0.2F);
		float targetYaw = 0;
		
		//coldfronts go south to 0, warmfronts go north to 180
		if (levelTemperature > 0) {
			//Weather.dbg("warmer!");
			targetYaw = 180;
		} else {
			//Weather.dbg("colder!");
			targetYaw = 0;
		}
		
		float bestMove = MathHelper.wrapAngleTo180_float(targetYaw - angle);
		
		if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}
		
		//Weather.dbg(manager.windMan.getWindAngleForClouds() + " - final angle: " + angle);
		
		return angle;
	}
	
	public void spinEntity(Entity entity1) {
		
		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(curWeatherType);
		
		Random rand = new Random();
		
    	/*if (entity instanceof EntTornado) {
    		entT = (EntTornado) entity;
    	}*/
    
    	boolean forTornado = entT != null;
    	
        //ConfigTornado.Storm_Tornado_height;
        double radius = 10D;
        double scale = conf.tornadoWidthScale;
        double d1 = entity.pos.xCoord - entity1.posX;
        double d2 = entity.pos.zCoord - entity1.posZ;
        float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
        float f1;

        for (f1 = f; f1 < -180F; f1 += 360F) { }

        for (; f1 >= 180F; f1 -= 360F) { }

        double distY = entity.pos.yCoord - entity1.posY;
        double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

        if (entity1.posY - entity.pos.yCoord < 0.0D)
        {
            distY = 1.0D;
        }
        else
        {
            distY = entity1.posY - entity.pos.yCoord;
        }

        if (distY > maxHeight)
        {
            distY = maxHeight;
        }

        double grab = (10D / WeatherUtilEntity.getWeight(entity1, forTornado))/* / ((distY / maxHeight) * 1D)*/ * ((Math.abs((maxHeight - distY)) / maxHeight));
        float pullY = 0.0F;

        //some random y pull
        if (rand.nextInt(5) != 0)
        {
            //pullY = 0.035F;
        }

        if (distXZ > 5D)
        {
            grab = grab * (radius / distXZ);
        }

        pullY += (float)(conf.tornadoLiftRate / (WeatherUtilEntity.getWeight(entity1, forTornado) / 2F)/* * (Math.abs(radius - distXZ) / radius)*/);
        
        
        if (entity1 instanceof EntityPlayer)
        {
            double adjPull = 0.2D / ((WeatherUtilEntity.getWeight(entity1, forTornado) * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            double adjGrab = (10D * (((float)(((double)WeatherUtilEntity.playerInAirTime + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }
            
            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (entity1.motionY > -0.8)
            {
            	//System.out.println(entity1.motionY);
                entity1.fallDistance = 0F;
            } else if (entity1.motionY > -1.5) {
            	//entity1.fallDistance = 5F;
            	//System.out.println(entity1.fallDistance);
            }

            
        }
        else if (entity1 instanceof EntityLivingBase)
        {
            double adjPull = 0.005D / ((WeatherUtilEntity.getWeight(entity1, forTornado) * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            int airTime = entity1.getEntityData().getInteger("timeInAir");
            double adjGrab = (10D * (((float)(((double)(airTime) + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }
            
            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (entity1.motionY > -2.0)
            {
                entity1.fallDistance = 0F;
            }

            if (forTornado) entity1.onGround = false;
            
            //System.out.println(adjPull);
        }
        
        
        grab += conf.relTornadoSize;
        
        double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
        
        f1 = (float)((double)f1 + profileAngle);
        
        //debug - dont do this here, breaks server
        /*if (entity1 instanceof EntityIconFX) {
        	if (entity1.entityId % 20 < 5) {
        		if (((EntityIconFX) entity1).renderOrder != -1) {
        			if (entity1.worldObj.getTotalWorldTime() % 40 == 0) {
        				//Weather.dbg("final grab angle: " + profileAngle);
        			}
        		}
        	}
        }*/
        
        if (entT != null) {
        	
        	if (entT.scale != 1F) f1 += 20 - (20 * entT.scale);
        }
        
        float f3 = (float)Math.cos(-f1 * 0.01745329F - (float)Math.PI);
        float f4 = (float)Math.sin(-f1 * 0.01745329F - (float)Math.PI);
        float f5 = conf.tornadoPullRate * 1;
        
        if (entT != null) {
        	if (entT.scale != 1F) f5 *= entT.scale * 1.2F;
        }

        if (entity1 instanceof EntityLivingBase)
        {
            f5 /= (WeatherUtilEntity.getWeight(entity1, forTornado) * ((distXZ + 1D) / radius));
        }
        
        //if player and not spout
        if (entity1 instanceof EntityPlayer && curWeatherType != 0) {
        	//System.out.println("grab: " + f5);
        	if (entity1.onGround) {
        		f5 *= 10.5F;
        	} else {
        		f5 *= 5F;
        	}
        	//if (entity1.worldObj.rand.nextInt(2) == 0) entity1.onGround = false;
        } else if (entity1 instanceof EntityLivingBase && curWeatherType != 0) {
        	f5 *= 1.5F;
        }

        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof EntityLivingBase) {
        	f5 *= 0.3F;
        }
        
        float moveX = f3 * f5;
        float moveZ = f4 * f5;
        //tornado strength changes
        float str = 1F;

        /*if (entity instanceof EntTornado)
        {
            str = ((EntTornado)entity).strength;
        }*/
        
        str = strength;
        
        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof EntityLivingBase) {
        	str *= 0.3F;
        }

        pullY *= str / 100F;
        
        if (entT != null) {
        	if (entT.scale != 1F) {
        		pullY *= entT.scale * 1.0F;
        		pullY += 0.002F;
        	}
        }
        
        //prevent double+ pull on entities
        long lastPullTime = entity1.getEntityData().getLong("lastPullTime");
        if (lastPullTime == entity1.worldObj.getTotalWorldTime()) {
        	//System.out.println("preventing double pull");
        	pullY = 0;
        }
        entity1.getEntityData().setLong("lastPullTime", entity1.worldObj.getTotalWorldTime());
        
        setVel(entity1, -moveX, pullY, moveZ);
	}
	
	public void setVel(Entity entity, float f, float f1, float f2)
    {
        entity.motionX += f;
        entity.motionY += f1;
        entity.motionZ += f2;

        if (entity instanceof EntitySquid)
        {
            entity.setPosition(entity.posX + entity.motionX * 5F, entity.posY, entity.posZ + entity.motionZ * 5F);
        }
    }
	
	
	
	@SideOnly(Side.CLIENT)
    public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder) {
    	double speed = 0D;
		Random rand = new Random();
    	EntityRotFX entityfx = particleBehaviorFog.spawnNewParticleIconFX(Minecraft.getMinecraft().theWorld, ParticleRegistry.cloud256, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, parRenderOrder);
		particleBehaviorFog.initParticle(entityfx);
		
		//lock y
		//entityfx.spawnY = (float) entityfx.posY;
		//entityfx.spawnY = ((int)200 - 5) + rand.nextFloat() * 5;
		entityfx.noClip = true;
    	entityfx.callUpdatePB = false;
    	
    	boolean debug = false;
    	
    	if (debug) {
    		//entityfx.setMaxAge(50 + rand.nextInt(10));
    	} else {
	    	
    	}
    	
    	if (state == STATE_NORMAL) {
    		entityfx.setMaxAge(300 + rand.nextInt(100));
    	} else {
    		entityfx.setMaxAge((size/2) + rand.nextInt(100));
    	}
    	
		//pieces that move down with funnel need render order shift, also only for relevant storm formations
		if (entityfx.entityId % 20 < 5 && state >= STATE_SPINNING) {
			entityfx.renderOrder = 3;
			
			entityfx.setMaxAge((size) + rand.nextInt(100));
		}
    	
    	float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.7F;
		if (state > STATE_NORMAL) {
			baseBright = 0.2F;
		} else if (attrib_precipitation) {
			baseBright = 0.2F;
		} else {
			float adj = Math.min(1F, levelWater / levelWaterStartRaining) * 0.6F;
			baseBright -= adj;
		}
		
		if (layer == 1) {
			baseBright = 0.1F;
		}
		
		float finalBright = Math.min(1F, baseBright+randFloat);
		entityfx.setRBGColorF(finalBright, finalBright, finalBright);
		
		//entityfx.setRBGColorF(1, 1, 1);
		
		//DEBUG
		if (debug) {
			if (levelTemperature < 0) {
				entityfx.setRBGColorF(0, 0, finalBright);
			} else if (levelTemperature > 0) {
				entityfx.setRBGColorF(finalBright, 0, 0);
			}
		}
    	
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		//entityfx.spawnAsWeatherEffect();
		particleBehaviorFog.particles.add(entityfx);
		return entityfx;
    }
	
	public void reset() {
		setDead();
	}
	
	public void setDead() {
		//Weather.dbg("storm killed, ID: " + ID);
		
		isDead = true;
		
		//cleanup memory
		if (manager.getWorld().isRemote) {
			cleanupClient();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		manager = null;
		tornadoHelper.storm = null;
		tornadoHelper = null;
	}
	
	@SideOnly(Side.CLIENT)
	public void cleanupClient() {
		listParticlesCloud.clear();
		listParticlesFunnel.clear();
		if (particleBehaviorFog != null && particleBehaviorFog.particles != null) particleBehaviorFog.particles.clear();
		particleBehaviorFog = null;
	}
	
	public float getTemperatureMCToWeatherSys(float parOrigVal) {
		//Weather.dbg("orig val: " + parOrigVal);
		//-0.7 to make 0 be the middle average
		parOrigVal -= 0.7;
		//multiply by 2 for an increased difference, for more to work with
		parOrigVal *= 2F;
		//Weather.dbg("final val: " + parOrigVal);
		return parOrigVal;
	}
	
	public void addWeatherEffectLightning(EntityLightningBolt parEnt) {
		//manager.getWorld().addWeatherEffect(parEnt);
		manager.getWorld().weatherEffects.add(parEnt);
		((WeatherManagerServer)manager).syncLightningNew(parEnt);
	}
}
