package weather2.weathersystem.storm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.player.PlayerData;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;
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
	public List<EntityRotFX> listParticlesGround;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	
	public int sizeMaxFunnelParticles = 600;
	
	//public WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(1);
	//this was pulled over from weather1 i believe
	//public int curWeatherType = 1; //NEEDS SYNCING
	
	//basic info
	public static int static_YPos_layer0 = ConfigMisc.Cloud_Layer0_Height;
	public static int static_YPos_layer1 = 350;
	public static int static_YPos_layer2 = 500;
	public static List<Integer> layers = new ArrayList<Integer>(Arrays.asList(static_YPos_layer0, static_YPos_layer1, static_YPos_layer2));
	public int layer = 0;
	public Vec3 pos = new Vec3(0, static_YPos_layer0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);
	
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
	//public float levelStormIntensityMax = 0; //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	
	//revision, ints for each stage of intensity, and a float for the intensity of THAT current stage
	public int levelCurIntensityStage = 0; //since we want storms to build up to a climax still, this will start from 0 and peak to levelStormIntensityMax
	public float levelCurStagesIntensity = 0;
	//public boolean isRealStorm = false;
	public boolean hasStormPeaked = false;
	
	public int maxIntensityStage = STATE_STAGE5;
	
	//used to mark difference between land and water based storms
	public int stormType = TYPE_LAND;
	public static int TYPE_LAND = 0; //for tornados
	public static int TYPE_WATER = 1; //for tropical cyclones / hurricanes
	
	//used to mark intensity stages
	public static int STATE_NORMAL = 0; //probably unused!
	public static int STATE_THUNDER = 1;
	public static int STATE_HIGHWIND = 2;
	public static int STATE_HAIL = 3;
	public static int STATE_FORMING = 4; //forming tornado for land, for water... stage 0 or something?
	public static int STATE_STAGE1 = 5; //these are for both tornados (land) and tropical cyclones (water)
	public static int STATE_STAGE2 = 6;
	public static int STATE_STAGE3 = 7;
	public static int STATE_STAGE4 = 8;
	public static int STATE_STAGE5 = 9; //counts as hurricane for water
	
	//helper val, adjust with flags method
	public static float levelStormIntensityFormingStartVal = STATE_FORMING;
	
	
	//spin speed for potential tornado formations, should go up with intensity increase;
	public double spinSpeed = 0.02D;
	
	//PENDING REVISION \\ - use based on levelStormIntensityCur ???
	
	//states that combine all lesser states
	//public int state = STATE_NORMAL;
	
	
	//used for sure, rain is dependant on water level values
	public boolean attrib_precipitation = false;
	public boolean attrib_waterSpout = false;
	
	//copied from EntTornado
	//buildup var - unused in new system currently, but might be needed for touchdown effect
	
	//unused tornado scale, always 1F
	public float scale = 1F;
	public float strength = 100;
	public int maxHeight = 60;
	
	public int currentTopYBlock = -1;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	//public Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
	public int updateLCG = (new Random()).nextInt();
    
    public float formingStrength = 0; //for transition from 0 (in clouds) to 1 (touch down)
    
    public Vec3 posBaseFormationPos = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord); //for formation / touchdown progress, where all the ripping methods scan from
    
    public boolean naturallySpawned = true;
    public boolean canSnowFromCloudTemperature = false;
    public boolean alwaysProgresses = false;
    public boolean isDead = false;
    
    //to let client know server is raining (since we override client side raining state for render changes)
    //public boolean overCastModeAndRaining = false;
    
    /*@SideOnly(Side.CLIENT)
    public RenderCubeCloud renderBlock;*/
    
    //there is an issue with rainstorms sometimes never going away, this is a patch to mend the underlying issue i cant find yet
    public long ticksSinceLastPacketReceived = 0;
	
    //public static long lastStormFormed = 0;
    
	public StormObject(WeatherManagerBase parManager) {
		manager = parManager;
		
		if (parManager.getWorld().isRemote) {
			listParticlesCloud = new ArrayList<EntityRotFX>();
			listParticlesFunnel = new ArrayList<EntityRotFX>();
			listParticlesGround = new ArrayList<EntityRotFX>();
			//renderBlock = new RenderCubeCloud();
		}
	}
	
	//not used yet
	public void initFirstTime() {
		ID = StormObject.lastUsedStormID++;
		
		Biome bgb = manager.getWorld().getBiomeGenForCoords(new BlockPos(MathHelper.floor_double(pos.xCoord), 0, MathHelper.floor_double(pos.zCoord)));

		
		float temp = 1;
		
		if (bgb != null) {
			temp = bgb.getFloatTemperature(new BlockPos(MathHelper.floor_double(pos.xCoord), MathHelper.floor_double(pos.yCoord), MathHelper.floor_double(pos.zCoord)));
		}
		
		//initial setting, more apparent than gradual adjustments
		if (naturallySpawned) {
			levelTemperature = getTemperatureMCToWeatherSys(temp);
		}
		//levelWater = 0;
		levelWindMomentum = 0;
		
		//Weather.dbg("initialize temp to: " + levelTemperature + " - biome: " + bgb.biomeName);
		
		
		 
	}
	
	public boolean isPrecipitating() {
		return attrib_precipitation;
	}
	
	public void setPrecipitating(boolean parVal) {
		attrib_precipitation = parVal;
	}
	
	public boolean isRealStorm() {
		return levelCurIntensityStage > STATE_NORMAL;
	}
	
	public boolean isTornadoFormingOrGreater() {
		return stormType == TYPE_LAND && levelCurIntensityStage >= STATE_FORMING;
	}
	
	public boolean isCycloneFormingOrGreater() {
		return stormType == TYPE_WATER && levelCurIntensityStage >= STATE_FORMING;
	}
	
	public boolean isSpinning() {
		return levelCurIntensityStage >= STATE_HIGHWIND;
	}
	
	public boolean isTropicalCyclone() {
		return levelCurIntensityStage >= STATE_STAGE1;
	}
	
	public boolean isHurricane() {
		return levelCurIntensityStage >= STATE_STAGE5;
	}
	
	public void readFromNBT(NBTTagCompound var1)
    {
		nbtSyncFromServer(var1);
		
		motion = new Vec3(var1.getDouble("vecX"), var1.getDouble("vecY"), var1.getDouble("vecZ"));
		angleIsOverridden = var1.getBoolean("angleIsOverridden");
		angleMovementTornadoOverride = var1.getFloat("angleMovementTornadoOverride");
    }
	
	public NBTTagCompound writeToNBT()
    {
		
		NBTTagCompound nbt = nbtSyncForClient();
		
		nbt.setDouble("vecX", motion.xCoord);
		nbt.setDouble("vecY", motion.yCoord);
		nbt.setDouble("vecZ", motion.zCoord);
		nbt.setBoolean("angleIsOverridden", angleIsOverridden);
		nbt.setFloat("angleMovementTornadoOverride", angleMovementTornadoOverride);
		
		
		return nbt;
    }
	
	//receiver method
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		ID = parNBT.getLong("ID");
		//Weather.dbg("StormObject " + ID + " receiving sync");
		
		pos = new Vec3(parNBT.getInteger("posX"), parNBT.getInteger("posY"), parNBT.getInteger("posZ"));
		size = parNBT.getInteger("size");
		maxSize = parNBT.getInteger("maxSize");
		
		//state = parNBT.getInteger("state");
		
		//attrib_tornado_severity = parNBT.getInteger("attrib_tornado_severity");
		
		//attrib_highwind = parNBT.getBoolean("attrib_highwind");
		//attrib_tornado = parNBT.getBoolean("attrib_tornado");
		//attrib_hurricane = parNBT.getBoolean("attrib_hurricane");
		attrib_precipitation = parNBT.getBoolean("attrib_rain");
		attrib_waterSpout = parNBT.getBoolean("attrib_waterSpout");
		
		currentTopYBlock = parNBT.getInteger("currentTopYBlock");
		
		levelTemperature = parNBT.getFloat("levelTemperature");
		levelWater = parNBT.getInteger("levelWater");
		
		layer = parNBT.getInteger("layer");
		
		//curWeatherType = parNBT.getInteger("curWeatherType");
		
		//formingStrength = parNBT.getFloat("formingStrength");
		
		levelCurIntensityStage = parNBT.getInteger("levelCurIntensityStage");
		levelCurStagesIntensity = parNBT.getFloat("levelCurStagesIntensity");
		stormType = parNBT.getInteger("stormType");
		
		hasStormPeaked = parNBT.getBoolean("hasStormPeaked");
		
		//overCastModeAndRaining = parNBT.getBoolean("overCastModeAndRaining");
		
		isDead = parNBT.getBoolean("isDead");
		
		ticksSinceLastPacketReceived = 0;//manager.getWorld().getTotalWorldTime();
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
		
		//data.setInteger("state", state);
		
		//data.setInteger("attrib_tornado_severity", attrib_tornado_severity);
		
		//data.setBoolean("attrib_highwind", attrib_highwind);
		//data.setBoolean("attrib_tornado", attrib_tornado);
		//data.setBoolean("attrib_hurricane", attrib_hurricane);
		data.setBoolean("attrib_rain", attrib_precipitation);
		data.setBoolean("attrib_waterSpout", attrib_waterSpout);
		
		data.setInteger("currentTopYBlock", currentTopYBlock);
		
		data.setFloat("levelTemperature", levelTemperature);
		data.setInteger("levelWater", levelWater);
		
		data.setInteger("layer", layer);
		
		//data.setInteger("curWeatherType", curWeatherType);
		
		//data.setFloat("formingStrength", formingStrength);
		
		data.setInteger("levelCurIntensityStage", levelCurIntensityStage);
		data.setFloat("levelCurStagesIntensity", levelCurStagesIntensity);
		data.setInteger("stormType", stormType);
		
		data.setBoolean("hasStormPeaked", hasStormPeaked);
		
		//data.setBoolean("overCastModeAndRaining", overCastModeAndRaining);
		
		data.setBoolean("isDead", isDead);
		
		return data;
	}
	
	public NBTTagCompound nbtForIMC() {
		//we basically need all the same data minus a few soooo whatever
		return nbtSyncForClient();
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick) {
		//renderBlock.doRenderClouds(this, 0, 0, 0, 0, partialTick);
		/*if (layer == 1) {
			renderBlock.doRenderClouds(this, pos.xCoord, pos.yCoord, pos.zCoord, 0, partialTick);
		}*/
	}
	
	public void tick() {
		//Weather.dbg("ticking storm " + ID + " - manager: " + manager);
		

		//TODO: 1.10.2 TEMP OFF REENABLE
		if (true) return;
		
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
		posGround.yCoord = currentTopYBlock;
		
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			if (!WeatherUtil.isPaused()) {
				
				ticksSinceLastPacketReceived++;
				
				if (layer == 0) {
					tickClient();
				}
				
				if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
					tornadoHelper.tick(manager.getWorld());
				}
				
				if (levelCurIntensityStage >= STATE_HIGHWIND) {
					if (manager.getWorld().isRemote) {
						tornadoHelper.soundUpdates(true, isTornadoFormingOrGreater() || isCycloneFormingOrGreater());
			        }
				}
			}
		} else {

			if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
				tornadoHelper.tick(manager.getWorld());
			}
			
			if (levelCurIntensityStage >= STATE_HIGHWIND) {
				if (manager.getWorld().isRemote) {
					tornadoHelper.soundUpdates(true, isTornadoFormingOrGreater() || isCycloneFormingOrGreater());
		        }
			}
			
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
				tickProgression();
				tickSnowFall();
			}
			
			//overCastModeAndRaining = ConfigMisc.overcastMode && manager.getWorld().isRaining();
	        
		}
		
		if (layer == 0) {
	        //sync X Y Z, Y gets changed below
	        posBaseFormationPos = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
	
	        if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal) {
	        	if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal + 1) {
	        		formingStrength = 1;
	        		posBaseFormationPos.yCoord = posGround.yCoord;
	        	} else {
	        		
	        		//make it so storms touchdown at 0.5F intensity instead of 1 then instantly start going back up, keeps them down for a full 1F worth of intensity val
	        		float intensityAdj = Math.min(1F, levelCurStagesIntensity * 2F);
	        		
	        		//shouldnt this just be intensityAdj?
			        float val = (levelCurIntensityStage + intensityAdj) - StormObject.levelStormIntensityFormingStartVal;
			        formingStrength = val;
			        double yDiff = pos.yCoord - posGround.yCoord;
			        posBaseFormationPos.yCoord = pos.yCoord - (yDiff * formingStrength);
	        	}
	        } else {
	        	if (levelCurIntensityStage == STATE_HIGHWIND) {
	        		formingStrength = 1;
	        		posBaseFormationPos.yCoord = posGround.yCoord;
	        	} else {
		        	formingStrength = 0;
		        	posBaseFormationPos.yCoord = pos.yCoord;
	        	}
	        }
	        
			
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
		
		float cloudSpeedAmp = 0.2F;
		
		
		
		float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;
		
		if (levelCurIntensityStage >= STATE_FORMING) {
			finalSpeed = 0.2F;
		} else if (levelCurIntensityStage >= STATE_THUNDER) {
			finalSpeed = 0.05F;
		}
		
		if (levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
			finalSpeed /= ((float)(levelCurIntensityStage-levelStormIntensityFormingStartVal+1F));
		}
		
		if (finalSpeed < 0.03F) {
			finalSpeed = 0.03F;
		}
		
		if (finalSpeed > 0.3F) {
			finalSpeed = 0.3F;
		}
		
		if (manager.getWorld().getTotalWorldTime() % 100 == 0 && levelCurIntensityStage >= STATE_FORMING) {
			
			//finalSpeed = 0.5F;
			
			//Weather.dbg("storm ID: " + this.ID + ", stage: " + levelCurIntensityStage + ", storm speed: " + finalSpeed);
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
		
		currentTopYBlock = world.getHeight(new BlockPos(MathHelper.floor_double(pos.xCoord), 0, MathHelper.floor_double(pos.zCoord))).getY();
		//Weather.dbg("currentTopYBlock: " + currentTopYBlock);
		if (levelCurIntensityStage >= STATE_THUNDER) {
			if (rand.nextInt((int)Math.max(1, ConfigMisc.Storm_LightningStrikeBaseValueOddsTo1 - (levelCurIntensityStage * 10))) == 0) {
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				int y = world.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
				if (world.isBlockLoaded(new BlockPos(x, y, z))) {
					//if (world.canLightningStrikeAt(x, y, z)) {
						addWeatherEffectLightning(new EntityLightningBolt(world, (double)x, (double)y, (double)z));
					//}
				}
			}
		}
		
		//dont forget, this doesnt account for storm size, so small storms have high concentration of hail, as it grows, it appears to lessen in rate
		if (isPrecipitating() && levelCurIntensityStage == STATE_HAIL && stormType == TYPE_LAND) {
			//if (rand.nextInt(1) == 0) {
			for (int i = 0; i < Math.max(1, ConfigMisc.Storm_HailPerTick * (size/maxSize)); i++) {
				int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
				if (world.isBlockLoaded(new BlockPos(x, static_YPos_layer0, z)) && (world.getClosestPlayer(x, 50, z, 80, false) != null)) {
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
		
		if (!ConfigMisc.Snow_PerformSnowfall) return;
		
		if (!isPrecipitating()) return;
		
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
		            
		            //afterthought, for weather 2.3.7
		            if (!world.isBlockLoaded(new BlockPos(x, 128, z))) {
		            	continue;
		            }
		            
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
			            
			            setBlockHeight = world.getPrecipitationHeight(new BlockPos(xxx + x, 0, zzz + z)).getY();
			            
			            
			
			            if (canSnowAtBody(xxx + x, setBlockHeight, zzz + z) && Blocks.SNOW.canPlaceBlockAt(world, new BlockPos(xxx + x, setBlockHeight, zzz + z))) {
			            //if (entP != null && entP.getDistance(xx, entP.posY, zz) < 16) {
			            	boolean perform = false;
			            	Block id = world.getBlockState(new BlockPos(xxx + x, setBlockHeight, zzz + z)).getBlock();
			            	int meta = 0;
			            	if (id.getMaterial(id.getDefaultState()) == Material.SNOW) {
			            		if (ConfigMisc.Snow_ExtraPileUp) {
			            			IBlockState state = world.getBlockState(new BlockPos(xxx + x, setBlockHeight, zzz + z));
				            		meta = state.getBlock().getMetaFromState(state);
				            		if (meta < snowMetaMax) {
				            			perform = true;
					            		meta += 1;
				            		} else {
				            			if (ConfigMisc.Snow_MaxBlockBuildupHeight > 1) {
				            				int i;
				            				int originalSetBlockHeight = setBlockHeight;
				            				for (i = 0; i < ConfigMisc.Snow_MaxBlockBuildupHeight; i++) {
				            					Block checkID = world.getBlockState(new BlockPos(xxx + x, originalSetBlockHeight + i, zzz + z)).getBlock();
				            					if (checkID.getMaterial(checkID.getDefaultState()) == Material.SNOW) {
				            						IBlockState state2 = world.getBlockState(new BlockPos(xxx + x, originalSetBlockHeight + i, zzz + z));
				            						meta = state2.getBlock().getMetaFromState(state2);
				            						if (meta < snowMetaMax) {
				            							setBlockHeight = originalSetBlockHeight + i;
				    			            			perform = true;
				    				            		meta += 1;
				    				            		break;
				            						} else {
				            							//let it continue to next height
				            						}
				            					} else if (CoroUtilBlock.isAir(checkID)) {
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
			            		world.setBlockState(new BlockPos(xxx + x, setBlockHeight, zzz + z), id.getStateFromMeta(meta), 3);
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
		return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
	}
	
	//return relative values, id 0 (to mark its ok to start snow here) or id snow (to mark check meta), and meta of detected snow if snow (dont increment it, thats handled after this)
	public ChunkCoordinatesBlock getSnowfallEvenOutAdjust(int x, int y, int z, int sourceMeta) {
		
		//only check down once, if air, check down one more time, if THAT is air, we dont allow spread out, because we dont want to loop all the way down to bottom of some cliff
		//could use getHeight but then we'd have to difference check the height and that might complicate things...
		
		int metaToSet = 0;
		
		World world = manager.getWorld();
		Block checkID = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		//check for starting with no snow
		if (CoroUtilBlock.isAir(checkID)) {
			Block checkID2 = world.getBlockState(new BlockPos(x, y-1, z)).getBlock();
			//make sure somethings underneath it - we shouldnt need to check deeper because we spread out while meta of snow is halfway, before it can start a second pile
			if (CoroUtilBlock.isAir(checkID2)) {
				//Weather.dbg("1");
				return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
			} else {
				//Weather.dbg("2");
				//return that its an open area to start snow at
				return new ChunkCoordinatesBlock(x, y, z, Blocks.AIR, 0);
			}
		} else if (checkID == Blocks.SNOW) {
			IBlockState state = world.getBlockState(new BlockPos(x, y, z));
			int checkMeta = state.getBlock().getMetaFromState(state);
			//if detected snow is shorter, return with detected meta val!
			//adjusting to <=
			if (checkMeta < sourceMeta) {
				//Weather.dbg("3 - checkMeta: " + checkMeta + " vs sourceMeta: " + sourceMeta);
				return new ChunkCoordinatesBlock(x, y, z, checkID, checkMeta);
			}
		} else {
			return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
		}
		return new ChunkCoordinatesBlock(0, 0, 0, Blocks.AIR, 0);
	}
	
	public boolean canSnowAtBody(int par1, int par2, int par3)
    {
		World world = manager.getWorld();
		
		Biome biomegenbase = world.getBiomeGenForCoords(new BlockPos(par1, 0, par3));
        
        if (biomegenbase == null) return false;
        
        float f = biomegenbase.getFloatTemperature(new BlockPos(par1, par2, par3));

        if ((canSnowFromCloudTemperature && levelTemperature > 0) || (!canSnowFromCloudTemperature && biomegenbase.getFloatTemperature(new BlockPos(par1, par2, par3)) > 0.15F))
        {
            return false;
        }
        else
        {
            if (par2 >= 0 && par2 < 256 && world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(par1, par2, par3)) < 10)
            {
                Block l = world.getBlockState(new BlockPos(par1, par2 - 1, par3)).getBlock();
                Block i1 = world.getBlockState(new BlockPos(par1, par2, par3)).getBlock();

                if ((CoroUtilBlock.isAir(i1) || i1 == Blocks.SNOW)/* && Block.snow.canPlaceBlockAt(world, par1, par2, par3)*/ && CoroUtilBlock.isAir(l) && l != Blocks.ICE && l.getMaterial(l.getDefaultState()).blocksMovement())
                {
                    return true;
                }
            }

            return false;
        }
    }
	
	public void tickProgression() {
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
		//int randomChanceOfRain = ConfigMisc.Player_Storm_Rain_OddsTo1;
		
		boolean isInOcean = false;
		boolean isOverWater = false;
		
		if (world.getTotalWorldTime() % ConfigMisc.Storm_AllTypes_TickRateDelay == 0) {
			
			NBTTagCompound playerNBT = PlayerData.getPlayerNBT(userSpawnedFor);
			
			long lastStormDeadlyTime = playerNBT.getLong("lastStormDeadlyTime");
			//long lastStormRainTime = playerNBT.getLong("lastStormRainTime");
			
			Biome bgb = world.getBiomeGenForCoords(new BlockPos(MathHelper.floor_double(pos.xCoord), 0, MathHelper.floor_double(pos.zCoord)));
			
			//temperature scan
			if (bgb != null) {
				
				isInOcean = bgb.getBiomeName().contains("Ocean") || bgb.getBiomeName().contains("ocean");
				
				float biomeTempAdj = getTemperatureMCToWeatherSys(bgb.getFloatTemperature(new BlockPos(MathHelper.floor_double(pos.xCoord), MathHelper.floor_double(pos.yCoord), MathHelper.floor_double(pos.zCoord))));
				if (levelTemperature > biomeTempAdj) {
					levelTemperature -= tempAdjustRate;
				} else {
					levelTemperature += tempAdjustRate;
				}
			}
			
			boolean performBuildup = false;
			
			Random rand = new Random();
			
			if (!isPrecipitating() && rand.nextInt(randomChanceOfWaterBuildFromNothing) == 0) {
				performBuildup = true;
			}
			
			Block blockID = world.getBlockState(new BlockPos(MathHelper.floor_double(pos.xCoord), currentTopYBlock-1, MathHelper.floor_double(pos.zCoord))).getBlock();
			if (!CoroUtilBlock.isAir(blockID)) {
				//Block block = Block.blocksList[blockID];
				if (blockID.getMaterial(blockID.getDefaultState()) instanceof MaterialLiquid) {
					isOverWater = true;
				}
			}
			
			//water scan - dont build up if raining already
			if (!performBuildup && !isPrecipitating() && rand.nextInt(randomChanceOfWaterBuildFromWater) == 0) {
				if (isOverWater) {
					performBuildup = true;
				}
				
				if (!performBuildup && bgb != null && (isInOcean || bgb.getBiomeName().contains("Swamp") || bgb.getBiomeName().contains("Jungle") || bgb.getBiomeName().contains("River"))) {
					performBuildup = true;
				}
			}
			
			if (performBuildup) {
				//System.out.println("RAIN BUILD TEMP OFF");
				levelWater += levelWaterBuildRate;
				//Weather.dbg("building rain: " + levelWater);
			}
			
			//water values adjust when raining
			if (isPrecipitating()) {
				levelWater -= levelWaterSpendRate;
				
				//TEMP!!!
				/*System.out.println("TEMP!!!");
				levelWater = 0;*/
				
				if (levelWater < 0) levelWater = 0;
				
				if (levelWater <= 0) {
					setPrecipitating(false);
					Weather.dbg("ending raining for: " + ID);
				}
			} else {
				if (!ConfigMisc.overcastMode || manager.getWorld().isRaining()) {
					if (levelWater >= levelWaterStartRaining) {
						if (ConfigMisc.Player_Storm_Rain_OddsTo1 != -1 && rand.nextInt(ConfigMisc.Player_Storm_Rain_OddsTo1) == 0) {
							setPrecipitating(true);
							Weather.dbg("starting raining for: " + ID);
						}
					}
				}
			}
			
			//actual storm formation chance
			
			WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
			
			boolean tryFormStorm = false;
			
			if (ConfigMisc.Server_Storm_Deadly_UseGlobalRate) {
				if (ConfigMisc.Server_Storm_Deadly_TimeBetweenInTicks != -1) {
					if (wm.lastStormFormed == 0 || wm.lastStormFormed + ConfigMisc.Server_Storm_Deadly_TimeBetweenInTicks < world.getTotalWorldTime()) {
						tryFormStorm = true;
					}
				}
			} else {
				if (ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks != -1) {
					if (lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks < world.getTotalWorldTime()) {
						tryFormStorm = true;
					}
				}
			}
			
			if (((ConfigMisc.overcastMode && manager.getWorld().isRaining()) || !ConfigMisc.overcastMode) && WeatherUtilConfig.listDimensionsStorms.contains(manager.getWorld().provider.getDimension()) && tryFormStorm) {
				//if (lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks < world.getTotalWorldTime()) {
					int stormFrontCollideDist = ConfigMisc.Storm_Deadly_CollideDistance;
					int randomChanceOfCollide = ConfigMisc.Player_Storm_Deadly_OddsTo1;
					
					if (isInOcean && (ConfigMisc.Storm_OddsTo1OfOceanBasedStorm > 0 && rand.nextInt(ConfigMisc.Storm_OddsTo1OfOceanBasedStorm) == 0)) {
						EntityPlayer entP = world.getPlayerEntityByName(userSpawnedFor);
						
						if (entP != null) {
							initRealStorm(entP, null);
						} else {
							initRealStorm(null, null);
						}
						
						if (ConfigMisc.Server_Storm_Deadly_UseGlobalRate) {
							wm.lastStormFormed = world.getTotalWorldTime();
						} else {
							playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());
						}
					} else if (!isInOcean && ConfigMisc.Storm_OddsTo1OfLandBasedStorm > 0 && rand.nextInt(ConfigMisc.Storm_OddsTo1OfLandBasedStorm) == 0) {
						EntityPlayer entP = world.getPlayerEntityByName(userSpawnedFor);
						
						if (entP != null) {
							initRealStorm(entP, null);
						} else {
							initRealStorm(null, null);
						}
						
						if (ConfigMisc.Server_Storm_Deadly_UseGlobalRate) {
							wm.lastStormFormed = world.getTotalWorldTime();
						} else {
							playerNBT.setLong("lastStormDeadlyTime", world.getTotalWorldTime());
						}
					} else if (rand.nextInt(randomChanceOfCollide) == 0) {
						for (int i = 0; i < manager.getStormObjects().size(); i++) {
							StormObject so = manager.getStormObjects().get(i);
							
							boolean startStorm = false;
							
							if (so.ID != this.ID && so.levelCurIntensityStage <= 0) {
								if (so.pos.distanceTo(pos) < stormFrontCollideDist) {
									if (this.levelTemperature < 0) {
										if (so.levelTemperature > 0) {
											startStorm = true;
										}
									} else if (this.levelTemperature > 0) {
										if (so.levelTemperature < 0) {
											startStorm = true;
										}
									}
								}
							}
							
							if (startStorm) {
								
								//Weather.dbg("start storm!");
								
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
				//}
			}
			
			if (isRealStorm()) {
				
				//force storms to die if its no longer raining while overcast mode is active
				if (ConfigMisc.overcastMode) {
					if (!manager.getWorld().isRaining()) {
						hasStormPeaked = true;
					}
				}
				
				//force rain on while real storm and not dying
				if (!hasStormPeaked) {
					levelWater = levelWaterStartRaining;
					setPrecipitating(true);
				}
				
				if ((levelCurIntensityStage == STATE_HIGHWIND || levelCurIntensityStage == STATE_HAIL) && isOverWater) {
					if (ConfigMisc.Storm_OddsTo1OfHighWindWaterSpout != 0 && rand.nextInt(ConfigMisc.Storm_OddsTo1OfHighWindWaterSpout) == 0) {
						attrib_waterSpout = true;
					}
				} else {
					attrib_waterSpout = false;
				}
				
				float levelStormIntensityRate = 0.02F;
				float minIntensityToProgress = 0.6F;
				int oddsTo1OfIntensityProgressionBase = ConfigMisc.Storm_OddsTo1OfProgressionBase;
				
				//speed up forming and greater progression
				if (levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
					levelStormIntensityRate *= 3;
					oddsTo1OfIntensityProgressionBase /= 3;
				}

				int oddsTo1OfIntensityProgression = oddsTo1OfIntensityProgressionBase + (levelCurIntensityStage * ConfigMisc.Storm_OddsTo1OfProgressionStageMultiplier);
				
				if (!hasStormPeaked) {
					
					levelCurStagesIntensity += levelStormIntensityRate;
					
					if (levelCurIntensityStage < maxIntensityStage && (!ConfigMisc.Storm_NoTornadosOrCyclones || levelCurIntensityStage < STATE_FORMING-1)) {
						if (levelCurStagesIntensity >= minIntensityToProgress) {
							//Weather.dbg("storm ID: " + this.ID + " trying to hit next stage");
							if (alwaysProgresses || rand.nextInt(oddsTo1OfIntensityProgression) == 0) {
								stageNext();
								Weather.dbg("storm ID: " + this.ID + " - growing, stage: " + levelCurIntensityStage);
								//mark is tropical cyclone if needed! and never unmark it!
								if (isInOcean) {
									//make it ONLY allow to change during forming stage, so it locks in
									if (levelCurIntensityStage == STATE_FORMING) {
										Weather.dbg("storm ID: " + this.ID + " marked as tropical cyclone!");
										stormType = TYPE_WATER;
									}
								}
							}
						}
					}
					
					
					//Weather.dbg("storm ID: " + this.ID + " - growing, stage: " + levelCurIntensityStage + " at intensity: " + levelCurStagesIntensity);
					
					if (levelCurStagesIntensity >= 1F) {
						Weather.dbg("storm peaked at: " + levelCurIntensityStage);
						hasStormPeaked = true;
					}
				} else {
					
					if (ConfigMisc.overcastMode && manager.getWorld().isRaining()) {
						levelCurStagesIntensity -= levelStormIntensityRate * 0.9F;
					} else {
						levelCurStagesIntensity -= levelStormIntensityRate * 0.3F;
					}
					
					
					if (levelCurStagesIntensity <= 0) {
						stagePrev();
						Weather.dbg("storm ID: " + this.ID + " - dying, stage: " + levelCurIntensityStage);
						if (levelCurIntensityStage <= 0) {
							setNoStorm();
						}
					}
					
					
				}
				
				//levelStormIntensityCur value ranges and what they influence
				//revised to remove rain and factor in tropical storm / hurricane
				//1 = thunderstorm (and more rain???)
				//2 = high wind
				//3 = hail
				//4 = tornado forming OR tropical cyclone (forming?) - logic splits off here where its marked as hurricane if its over water
				//5 = F1 OR TC 2
				//6 = F2 OR TC 3
				//7 = F3 OR TC 4
				//8 = F4 OR TC 5
				//9 = F5 OR hurricane ??? (perhaps hurricanes spawn differently, like over ocean only, and sustain when hitting land for a bit)
				
				//what about tropical storm? that is a mini hurricane, perhaps also ocean based
				
				//levelWindMomentum = rate of increase of storm??? (in addition to the pre storm system speeds)
				
				
				
				//POST DEV NOTES READ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:
				
				//it might be a good idea to make something else determine increase from high winds to tornado and higher
				//using temperatures is a little unstable at such a large range of variation....
				
				//updateStormFlags();
				//curWeatherType = Math.min(WeatherTypes.weatherEntTypes.size()-1, Math.max(1, levelCurIntensityStage - 1));
			} else {
				if (ConfigMisc.overcastMode) {
					if (!manager.getWorld().isRaining()) {
						if (attrib_precipitation) {
							setPrecipitating(false);
						}
					}
				}
			}
			
			
		}
	}
	
	public WeatherEntityConfig getWeatherEntityConfigForStorm() {
		//default spout
		WeatherEntityConfig weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		if (levelCurIntensityStage >= STATE_STAGE5) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(5);
		} else if (levelCurIntensityStage >= STATE_STAGE4) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(4);
		} else if (levelCurIntensityStage >= STATE_STAGE3) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(3);
		} else if (levelCurIntensityStage >= STATE_STAGE2) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(2);
		} else if (levelCurIntensityStage >= STATE_STAGE1) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(1);
		} else if (levelCurIntensityStage >= STATE_FORMING) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		}
		return weatherConfig;
	}
	
	public void stageNext() {
		levelCurIntensityStage++;
		levelCurStagesIntensity = 0F;
		if (ConfigMisc.Storm_Tornado_aimAtPlayerOnSpawn) {
			if (!hasStormPeaked && levelCurIntensityStage == STATE_FORMING) {
				aimStormAtClosestOrProvidedPlayer(null);
			}
		}
	}
	
	public void stagePrev() {
		levelCurIntensityStage--;
		levelCurStagesIntensity = 1F;
	}
	
	public void initRealStorm(EntityPlayer entP, StormObject stormToAbsorb) {
		
		//new way of storm progression
		levelCurIntensityStage = STATE_THUNDER;
		
		
		//isRealStorm = true;
		float diff = 4;
		if (stormToAbsorb != null) {
			diff = this.levelTemperature - stormToAbsorb.levelTemperature;
		}
		if (naturallySpawned) {
			this.levelWater = this.levelWaterStartRaining * 2;
			/*this.levelStormIntensityMax = (float) (diff * ConfigMisc.Storm_IntensityAmplifier);
			if (levelStormIntensityMax < ConfigMisc.Storm_Deadly_MinIntensity) {
				levelStormIntensityMax = (float)ConfigMisc.Storm_Deadly_MinIntensity;
			}*/
		}
		this.attrib_precipitation = true;

		if (stormToAbsorb != null) {
			Weather.dbg("stormfront collision happened between ID " + this.ID + " and " + stormToAbsorb.ID);
			manager.removeStormObject(stormToAbsorb.ID);
			((WeatherManagerServer)manager).syncStormRemove(stormToAbsorb);
		} else {
			Weather.dbg("ocean storm happened, ID " + this.ID);
		}
		
		if (ConfigMisc.Storm_Tornado_aimAtPlayerOnSpawn) {
			
			if (entP != null) {
				aimStormAtClosestOrProvidedPlayer(entP);
			}
			
		}
	}
	
	public void aimStormAtClosestOrProvidedPlayer(EntityPlayer entP) {
		
		if (entP == null) {
			entP = manager.getWorld().getClosestPlayer(pos.xCoord, pos.yCoord, pos.zCoord, -1, false);
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
			
			Weather.dbg("stormfront aimed at player " + CoroUtilEntity.getName(entP));
		}
	}
	
	/*public void updateStormFlags() {
		
		boolean flagDbg = true;
		
		if (levelCurIntensityStage >= 9) {
			attrib_hurricane = true;
		} else if (levelCurIntensityStage >= 9) {
			attrib_tornado_severity = ATTRIB_F5;
		} else if (levelCurIntensityStage >= 8) {
			attrib_tornado_severity = ATTRIB_F4;
		} else if (levelCurIntensityStage >= 7) {
			attrib_tornado_severity = ATTRIB_F3;
		} else if (levelCurIntensityStage >= 6) {
			attrib_tornado_severity = ATTRIB_F2;
		} else if (levelCurIntensityStage >= 5) {
			attrib_tornado_severity = ATTRIB_F1;
		} else if (levelCurIntensityStage >= 4) {
			//once again aim the storm back at player after forming if it overshot them, to solve some taking too long to buildup and passing over them >:D
			if (ConfigMisc.Storm_Tornado_aimAtPlayerOnSpawn) {
				if (!hasStormPeaked && attrib_tornado_severity != ATTRIB_FORMINGTORNADO) {
					aimStormAtClosestOrProvidedPlayer(null);
				}
			}
			attrib_tornado_severity = ATTRIB_FORMINGTORNADO;
			state = this.STATE_SPINNING;
		} else if (levelCurIntensityStage >= 3) {
			state = this.STATE_HAIL;
		} else if (levelCurIntensityStage >= 2) {
			attrib_highwind = true;
		} else if (levelCurIntensityStage >= 1) {
			state = this.STATE_THUNDER;
		} else if (levelCurIntensityStage > 0) {
			//already added rain when combining storms - but what about commands?
			attrib_precipitation = true;
			state = this.STATE_NORMAL;
		} else {
			setNoStorm();
		}
		
		if (!naturallySpawned) {
			if (flagDbg) Weather.dbg("flags updated for " + ID + ", state: " + state);
		}
		
		//TEEEEEEEESSSSSSSSTTTTTTTTTTTTTT
		//aimStormAtClosestOrProvidedPlayer(null);
		
		curWeatherType = Math.min(WeatherTypes.weatherEntTypes.size()-1, Math.max(1, attrib_tornado_severity - 1));
	}*/
	
	//FYI rain doesnt count as storm
	public void setNoStorm() {
		Weather.dbg("storm ID: " + this.ID + " - ended storm event");
		levelCurIntensityStage = STATE_NORMAL;
		levelCurStagesIntensity = 0;
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(new Vec3(pos.xCoord, pos.yCoord, pos.zCoord));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
				particleBehaviorFog.tickUpdateList();
			}
		}
        
		EntityPlayer entP = Minecraft.getMinecraft().thePlayer;
		
		spinSpeed = 0.02D;
		double spinSpeedMax = 0.4D;
		/*if (isHurricane()) {
			spinSpeed = spinSpeedMax * 1.2D;
			Weather.dbg("spin speed: " + spinSpeed);
		} else */if (isCycloneFormingOrGreater()) {
			spinSpeed = spinSpeedMax * 0.00D + ((levelCurIntensityStage-levelStormIntensityFormingStartVal+1) * spinSpeedMax * 0.2D);
			//Weather.dbg("spin speed: " + spinSpeed);
		} else if (isTornadoFormingOrGreater()) {
			spinSpeed = spinSpeedMax * 0.2D;
		} else if (levelCurIntensityStage >= STATE_HIGHWIND) {
			spinSpeed = spinSpeedMax * 0.05D;
		} else {
			spinSpeed = spinSpeedMax * 0.02D;
		}
		
		//bonus!
		if (isHurricane()) {
			spinSpeed += 0.1D;
		}
		
		if (size == 0) size = 1;
		int delay = Math.max(1, (int)(100F / size * 1F));
		int loopSize = 1;//(int)(1 * size * 0.1F);
		
		int extraSpawning = 0;
		
		if (isSpinning()) {
			loopSize += 4;
			extraSpawning = 300;
		}
		
		//adjust particle creation rate for upper tropical cyclone work
		if (stormType == TYPE_WATER) {
			if (levelCurIntensityStage >= STATE_STAGE5) {
				loopSize = 10;
				extraSpawning = 800;
			} else if (levelCurIntensityStage >= STATE_STAGE4) {
				loopSize = 8;
				extraSpawning = 700;
			} else if (levelCurIntensityStage >= STATE_STAGE3) {
				loopSize = 6;
				extraSpawning = 500; 
			} else if (levelCurIntensityStage >= STATE_STAGE2) {
				loopSize = 4;
				extraSpawning = 400;
			} else {
				extraSpawning = 300;
			}
		}
		
		//Weather.dbg("size: " + size + " - delay: " + delay); 
		
		Random rand = new Random();
		
		Vec3 playerAdjPos = new Vec3(entP.posX, pos.yCoord, entP.posZ);
		double maxSpawnDistFromPlayer = 512;
		
		//spawn clouds
		if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigMisc.Cloud_ParticleSpawnDelay) == 0) {
			for (int i = 0; i < loopSize; i++) {
				if (listParticlesCloud.size() < size + extraSpawning) {
					double spawnRad = size;
					
					if (layer != 0) {
						spawnRad = size * 5;
					}
					
					//Weather.dbg("listParticlesCloud.size(): " + listParticlesCloud.size());
					
					Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layers.get(layer), pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
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
		
		//ground effects
		if (levelCurIntensityStage >= STATE_HIGHWIND) {
			for (int i = 0; i < (stormType == TYPE_WATER ? 50 : 3)/*loopSize/2*/; i++) {
				if (listParticlesGround.size() < (stormType == TYPE_WATER ? 600 : 150)/*size + extraSpawning*/) {
					double spawnRad = size/4*3;
					
					if (stormType == TYPE_WATER) {
						spawnRad = size*3;
					}
					
					//Weather.dbg("listParticlesCloud.size(): " + listParticlesCloud.size());
					
					Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), posGround.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						int groundY = manager.getWorld().getHeight(new BlockPos((int)tryPos.xCoord, 0, (int)tryPos.zCoord)).getY();
						EntityRotFX particle = spawnFogParticle(tryPos.xCoord, groundY + 3, tryPos.zCoord, 2);
						
						particle.setScale(100);
						particle.rotationYaw = rand.nextInt(360);
						particle.rotationPitch = rand.nextInt(360);
						
						listParticlesGround.add(particle);
						
						//Weather.dbg("ground fog!");
						
				    	/*if (layer == 0) {
				    		particle.particleScale = 500;
				    	} else {
				    		particle.particleScale = 2000;
				    	}*/
						
						//listParticlesCloud.add(particle);
					}
				}
				
				
			}
		}
		
		delay = 1;
		loopSize = 2;
		
		double spawnRad = size/48;
		
		if (levelCurIntensityStage >= STATE_STAGE5) {
			spawnRad = 200;
			loopSize = 10;
			sizeMaxFunnelParticles = 1200;
		} else if (levelCurIntensityStage >= STATE_STAGE4) {
			spawnRad = 150;
			loopSize = 8;
			sizeMaxFunnelParticles = 1000;
		} else if (levelCurIntensityStage >= STATE_STAGE3) {
			spawnRad = 100;
			loopSize = 6;
			sizeMaxFunnelParticles = 800; 
		} else if (levelCurIntensityStage >= STATE_STAGE2) {
			spawnRad = 50;
			loopSize = 4;
			sizeMaxFunnelParticles = 600;
		} else {
			sizeMaxFunnelParticles = 600;
		}
		
		//spawn funnel
		if (isTornadoFormingOrGreater() || (attrib_waterSpout)) {
			if (this.manager.getWorld().getTotalWorldTime() % (delay + ConfigMisc.Storm_ParticleSpawnDelay) == 0) {
				for (int i = 0; i < loopSize; i++) {
					//temp comment out
					//if (attrib_tornado_severity > 0) {
					
					//Weather.dbg("spawn");
					
					//trim!
					if (listParticlesFunnel.size() >= sizeMaxFunnelParticles) {
						listParticlesFunnel.get(0).setExpired();
						listParticlesFunnel.remove(0);
					}
					
					if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {
						
						
						Vec3 tryPos = new Vec3(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						//int y = entP.worldObj.getPrecipitationHeight((int)tryPos.xCoord, (int)tryPos.zCoord);
						
						if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
							EntityRotFX particle = spawnFogParticle(tryPos.xCoord, posBaseFormationPos.yCoord, tryPos.zCoord, 3);
							
							//move these to a damn profile damnit!
							particle.setMaxAge(150 + ((levelCurIntensityStage-1) * 100) + rand.nextInt(100));
							
							float baseBright = 0.3F;
							float randFloat = (rand.nextFloat() * 0.6F);
							
							particle.rotationYaw = rand.nextInt(360);
							
							float finalBright = Math.min(1F, baseBright+randFloat);
							
							//highwind aka spout in this current code location
							if (levelCurIntensityStage == STATE_HIGHWIND) {
								particle.setScale(150);
								particle.setRBGColorF(finalBright-0.2F, finalBright-0.2F, finalBright);
							} else {
								particle.setScale(250);
								particle.setRBGColorF(finalBright, finalBright, finalBright);
							}
							
							
							listParticlesFunnel.add(particle);
						}
					} else {
						//Weather.dbg("particles maxed");
					}
				}
			}
		}
		
		for (int i = 0; i < listParticlesFunnel.size(); i++) {
			EntityRotFX ent = listParticlesFunnel.get(i);
			if (!ent.isAlive()) {
				listParticlesFunnel.remove(ent);
			} else if (ent.getPosY() > pos.yCoord) {
				ent.setExpired();
				listParticlesFunnel.remove(ent);
			} else {
				 double var16 = this.pos.xCoord - ent.getPosX();
                 double var18 = this.pos.zCoord - ent.getPosZ();
                 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                 ent.rotationYaw += ent.getEntityId() % 90;
                 ent.rotationPitch = -30F;
                 
                 //fade spout blue to grey
                 if (levelCurIntensityStage == STATE_HIGHWIND) {
                	 int fadingDistStart = 30;
                	 if (ent.getPosY() > posGround.yCoord + fadingDistStart) {
		                 float maxVal = ent.getBlueColorF();
		                 float fadeRate = 0.002F;
		                 ent.setRBGColorF(Math.min(maxVal, ent.getRedColorF()+fadeRate), Math.min(maxVal, ent.getGreenColorF()+fadeRate), maxVal);
                	 }
                 }
                 
                 spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive()) {
				listParticlesCloud.remove(ent);
			} else {
				//ent.posX = pos.xCoord + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.getEntityId());
				float posZ = (float) Math.cos(ent.getEntityId());
				ent.setPosition(pos.xCoord + posX*radius, ent.posY, pos.zCoord + posZ*radius);*/
		        
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				
				double curDist = ent.getDistance(pos.xCoord, ent.getPosY(), pos.zCoord);

				float dropDownRange = 15F;
		        
		        float extraDropCalc = 0;
		        if (curDist < 200 && ent.getEntityId() % 20 < 5) {
			        //cyclone and hurricane dropdown modifications here
		        	extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
		        	if (isCycloneFormingOrGreater()) {
		        		extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange * 5F);
		        		//Weather.dbg("extraDropCalc: " + extraDropCalc);
		        	}
		        }
		        
		        
				
				if (isSpinning()) {
					double speed = spinSpeed + (rand.nextDouble() * 0.01D);
					double distt = size;//300D;
					
					
					double vecX = ent.getPosX() - pos.xCoord;
			        double vecZ = ent.getPosZ() - pos.zCoord;
			        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
			        //System.out.println("angle: " + angle);
			        
			        //fix speed causing inner part of formation to have a gap
			        angle += speed * 50D;
			        //angle += 20;
			        
			        angle -= (ent.getEntityId() % 10) * 3D;
			        
			        //random addition
			        angle += rand.nextInt(10) - rand.nextInt(10);
			        
			        if (curDist > distt) {
			        	//System.out.println("curving");
			        	angle += 40;
			        	//speed = 1D;
			        }
			        
			        //keep some near always - this is the lower formation part
			        if (ent.getEntityId() % 20 < 5) {
			        	if (levelCurIntensityStage >= STATE_FORMING) {
			        		if (stormType == TYPE_WATER) {
			        			angle += 40 + ((ent.getEntityId() % 5) * 4);
			        			if (curDist > 150 + ((levelCurIntensityStage-levelStormIntensityFormingStartVal+1) * 30)) {
			        				angle += 10;
			        			}
			        		} else {
			        			angle += 30 + ((ent.getEntityId() % 5) * 4);
			        		}
			        		
			        	} else {
			        		//make a wider spinning lower area of cloud, for high wind
			        		if (curDist > 150) {
			        			angle += 50 + ((ent.getEntityId() % 5) * 4);
			        		}
			        	}
			        	
			        	double var16 = this.pos.xCoord - ent.getPosX();
		                double var18 = this.pos.zCoord - ent.getPosZ();
		                ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
		                ent.rotationPitch = -20F - (ent.getEntityId() % 10);
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
			        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
				        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
			        }
				} else {
					float cloudMoveAmp = 0.2F * (1 + layer);
					
					float speed = getAdjustedSpeed() * cloudMoveAmp;
					float angle = getAdjustedAngle();
					
					dropDownRange = 5;
			        if (/*curDist < 200 && */ent.getEntityId() % 20 < 5) {
			        	extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
			        }
					
					if (curSpeed < speed * 1D) {
			        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
				        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
			        }
				}
		        
				if (Math.abs(ent.getPosY() - (pos.yCoord - extraDropCalc)) > 2F) {
			        if (ent.getPosY() < pos.yCoord - extraDropCalc) {
		        		ent.setMotionY(ent.getMotionY() + 0.1D);
		        	} else {
		        		ent.setMotionY(ent.getMotionY() + 0.1D);
		        	}
				}
		        
				float dropDownSpeedMax = 0.15F;
				
				if (isCycloneFormingOrGreater()) {
					dropDownSpeedMax = 0.9F;
				}
				
		        if (ent.getMotionY() < -dropDownSpeedMax) {
		        	ent.setMotionY(-dropDownSpeedMax);
		        }
		        
		        if (ent.getMotionY() > dropDownSpeedMax) {
		        	ent.setMotionY(dropDownSpeedMax);
		        }
		        
		        //double distToGround = ent.worldObj.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
		        
		        //ent.setPosition(ent.posX, pos.yCoord, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.setDead();
				listParticles.remove(ent);
			}*/
		}
		
		for (int i = 0; i < listParticlesGround.size(); i++) {
			EntityRotFX ent = listParticlesGround.get(i);
			
			double curDist = ent.getDistance(pos.xCoord, ent.getPosY(), pos.zCoord);
			
			if (!ent.isAlive()) {
				listParticlesGround.remove(ent);
			} else {
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
			
				double speed = Math.max(0.2F, 5F * spinSpeed) + (rand.nextDouble() * 0.01D);
				double distt = size;//300D;
				
				
				double vecX = ent.getPosX() - pos.xCoord;
		        double vecZ = ent.getPosZ() - pos.zCoord;
		        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
		        
		        angle += 85;
		        
		        int maxParticleSize = 60;
		        
		        if (stormType == TYPE_WATER) {
		        	maxParticleSize = 150;
		        	speed /= 5D;
				}
		        
		        ent.setScale((float) Math.min(maxParticleSize, curDist * 2F));
		        
		        if (curDist < 20) {
		        	ent.setExpired();
		        }

	        	double var16 = this.pos.xCoord - ent.getPosX();
                double var18 = this.pos.zCoord - ent.getPosZ();
		        //ent.rotationYaw += 5;//(float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                //ent.rotationPitch = 0;//-20F - (ent.getEntityId() % 10);
                
                if (curSpeed < speed * 20D) {
		        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
			        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
		        }
			}
		}
		
		//System.out.println("size: " + listParticlesCloud.size());
	}
	
	public float getAdjustedSpeed() {
		return manager.windMan.getWindSpeedForClouds();
	}
	
	public float getAdjustedAngle() {
		float angle = manager.windMan.getWindAngleForClouds();
		
		float angleAdjust = Math.max(10, Math.min(45, 45F * levelTemperature * 0.2F));
		float targetYaw = 0;
		
		//coldfronts go south to 0, warmfronts go north to 180
		if (levelTemperature > 0) {
			//Weather.dbg("warmer!");
			targetYaw = 180;
		} else {
			//Weather.dbg("colder!");
			targetYaw = 0;
		}
		
		float bestMove = MathHelper.wrapDegrees(targetYaw - angle);
		
		if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}
		
		//Weather.dbg("ID: " + ID + " - " + manager.windMan.getWindAngleForClouds() + " - final angle: " + angle);
		
		return angle;
	}
	
	public void spinEntity(Object entity1) {
		
		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = getWeatherEntityConfigForStorm();//WeatherTypes.weatherEntTypes.get(curWeatherType);
		
		Random rand = new Random();
		
    	/*if (entity instanceof EntTornado) {
    		entT = (EntTornado) entity;
    	}*/
    
    	boolean forTornado = true;//entT != null;
    	
    	//TODO: 1.10.2 will this break client code?
    	World world = DimensionManager.getWorld(this.manager.dim);
    	long worldTime = world.getTotalWorldTime();
    	
    	Entity ent = null;
    	if (entity1 instanceof Entity) {
    		ent = (Entity) entity1;
    	}
    	
        //ConfigTornado.Storm_Tornado_height;
        double radius = 10D;
        double scale = conf.tornadoWidthScale;
        double d1 = entity.pos.xCoord - CoroUtilEntOrParticle.getPosX(entity1);
        double d2 = entity.pos.zCoord - CoroUtilEntOrParticle.getPosZ(entity1);
        
        if (conf.type == conf.TYPE_SPOUT) {
        	float range = 30F * (float) Math.sin((Math.toRadians(((worldTime * 0.5F) + (ID * 50)) % 360)));
        	float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(entity1) - posGround.yCoord) / (pos.yCoord - posGround.yCoord)));
        	float posOffsetX = (float) Math.sin((Math.toRadians(heightPercent * 360F)));
        	float posOffsetZ = (float) -Math.cos((Math.toRadians(heightPercent * 360F)));
        	//Weather.dbg("posOffset: " + posOffset);
        	//d1 += 50F*heightPercent*posOffset;
        	d1 += range*posOffsetX;
        	d2 += range*posOffsetZ;
        }
        
        float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
        float f1;

        for (f1 = f; f1 < -180F; f1 += 360F) { }

        for (; f1 >= 180F; f1 -= 360F) { }

        double distY = entity.pos.yCoord - CoroUtilEntOrParticle.getPosY(entity1);
        double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

        if (CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.yCoord < 0.0D)
        {
            distY = 1.0D;
        }
        else
        {
            distY = CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.yCoord;
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
        
        //Weather.dbg("TEMP!!!!");
        //WeatherTypes.initWeatherTypes();

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

            if (CoroUtilEntOrParticle.getMotionY(entity1) > -0.8)
            {
            	//System.out.println(entity1.motionY);
                ent.fallDistance = 0F;
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
            int airTime = ent.getEntityData().getInteger("timeInAir");
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

            if (ent.motionY > -1.5)
            {
                ent.fallDistance = 0F;
            }
            
            if (ent.motionY > 0.3F) ent.motionY = 0.3F;

            if (forTornado) ent.onGround = false;
            
            //System.out.println(adjPull);
        }
        
        
        grab += conf.relTornadoSize;
        
        double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
        
        f1 = (float)((double)f1 + profileAngle);
        
        //debug - dont do this here, breaks server
        /*if (entity1 instanceof EntityIconFX) {
        	if (entity1.getEntityId() % 20 < 5) {
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
        if (entity1 instanceof EntityPlayer && conf.type != 0) {
        	//System.out.println("grab: " + f5);
        	if (ent.onGround) {
        		f5 *= 10.5F;
        	} else {
        		f5 *= 5F;
        	}
        	//if (entity1.worldObj.rand.nextInt(2) == 0) entity1.onGround = false;
        } else if (entity1 instanceof EntityLivingBase && conf.type != 0) {
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
        if (entity1 instanceof Entity) {
	        long lastPullTime = ent.getEntityData().getLong("lastPullTime");
	        if (lastPullTime == worldTime) {
	        	//System.out.println("preventing double pull");
	        	pullY = 0;
	        }
	        ent.getEntityData().setLong("lastPullTime", worldTime);
        }
        
        setVel(entity1, -moveX, pullY, moveZ);
	}
	
	public void setVel(Object entity, float f, float f1, float f2)
    {
        /*entity.motionX += f;
        entity.motionY += f1;
        entity.motionZ += f2;*/
        CoroUtilEntOrParticle.setMotionX(entity, CoroUtilEntOrParticle.getMotionX(entity) + f);
		CoroUtilEntOrParticle.setMotionY(entity, CoroUtilEntOrParticle.getMotionY(entity) + f1);
		CoroUtilEntOrParticle.setMotionZ(entity, CoroUtilEntOrParticle.getMotionZ(entity) + f2);

        if (entity instanceof EntitySquid)
        {
        	Entity ent = (Entity) entity;
        	ent.setPosition(ent.posX + ent.motionX * 5F, ent.posY, ent.posZ + ent.motionZ * 5F);
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
		entityfx.setCanCollide(false);
    	entityfx.callUpdatePB = false;
    	
    	boolean debug = false;
    	
    	if (debug) {
    		//entityfx.setMaxAge(50 + rand.nextInt(10));
    	} else {
	    	
    	}
    	
    	if (levelCurIntensityStage == STATE_NORMAL) {
    		entityfx.setMaxAge(300 + rand.nextInt(100));
    	} else {
    		entityfx.setMaxAge((size/2) + rand.nextInt(100));
    	}
    	
		//pieces that move down with funnel need render order shift, also only for relevant storm formations
		if (entityfx.getEntityId() % 20 < 5 && isSpinning()) {
			entityfx.renderOrder = 3;
			
			entityfx.setMaxAge((size) + rand.nextInt(100));
		}
    	
    	float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.7F;
		if (levelCurIntensityStage > STATE_NORMAL) {
			baseBright = 0.2F;
		} else if (attrib_precipitation) {
			baseBright = 0.2F;
		} else if (manager.isVanillaRainActiveOnServer) {
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
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT/*manager.getWorld().isRemote*/) {
			cleanupClient();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		manager = null;
		if (tornadoHelper != null) tornadoHelper.storm = null;
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
	
	//notes moved to bottom\\
	
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
