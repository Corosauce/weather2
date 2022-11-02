package weather2.weathersystem.storm;

import com.corosus.coroutil.util.*;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.config.*;
import weather2.player.PlayerData;
import weather2.util.*;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.tornado.ActiveTornadoConfig;
import weather2.weathersystem.tornado.simple.Layer;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;

import java.util.*;

public class StormObject extends WeatherObject {

	//used on both server and client side, mark things SideOnly where needed
	
	//size, state
	
	//should they extend entity?
	
	//management stuff
	
	public String spawnerUUID = "";

	//newer cloud managing list for more strict render optimized positioning
	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloud;

	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloudLower;

	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesFunnel;

	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesGround;
	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@OnlyIn(Dist.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	
	public int sizeMaxFunnelParticles = 600;
	
	//public WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(1);
	//this was pulled over from weather1 i believe
	//public int curWeatherType = 1; //NEEDS SYNCING
	
	//basic info
	public static int static_YPos_layer0 = ConfigMisc.Cloud_Layer0_Height;
	public static int static_YPos_layer1 = ConfigMisc.Cloud_Layer1_Height;
	public static int static_YPos_layer2 = ConfigMisc.Cloud_Layer2_Height;
	public static List<Integer> layers = new ArrayList<Integer>(Arrays.asList(static_YPos_layer0, static_YPos_layer1, static_YPos_layer2));
	public int layer = 0;
	
	public boolean angleIsOverridden = false;
	public float angleMovementTornadoOverride = 0;
	
	//growth / progression info
	
	public boolean isGrowing = true;
	
	//cloud formation data, helps storms
	public int levelWater = 0; //builds over water and humid biomes, causes rainfall (not technically a storm)
	public float levelWindMomentum = 0; //high elevation builds this, plains areas lowers it, 0 = no additional speed ontop of global speed
	public float levelTemperature = 0; //negative for cold, positive for warm, we subtract 0.7 from vanilla values to make forest = 0, plains 0.1, ocean -0.5, etc
	//public float levelWindDirectionAdjust = 0; //for persistant direction change i- wait just calculate on the fly based on temperature
	
	public int levelWaterStartRaining = 100;
	
	//storm data, used when its determined a storm will happen from cloud front collisions
	public int levelStormIntensityMax = 0; //calculated from colliding warm and cold fronts, used to determine how crazy a storm _will_ get
	
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
	public static int STATE_NORMAL = 0;
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
	private TornadoFunnelSimple tornadoFunnelSimple;
	
	//public Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
	public int updateLCG = (new Random()).nextInt();
    
    public float formingStrength = 0; //for transition from 0 (in clouds) to 1 (touch down)
    
    public Vec3 posBaseFormationPos = new Vec3(pos.x, pos.y, pos.z); //for formation / touchdown progress, where all the ripping methods scan from

    public boolean naturallySpawned = true;
	//to prevent things like it progressing to next stage before weather machine undoes it
	public boolean weatherMachineControlled = false;
    public boolean canSnowFromCloudTemperature = false;
    public boolean alwaysProgresses = false;
    
    
    //to let client know server is raining (since we override client side raining state for render changes)
    //public boolean overCastModeAndRaining = false;
    
    /*@SideOnly(Side.CLIENT)
    public RenderCubeCloud renderBlock;*/
    
    //there is an issue with rainstorms sometimes never going away, this is a patch to mend the underlying issue i cant find yet
    public long ticksSinceLastPacketReceived = 0;
	
    //public static long lastStormFormed = 0;
    
    public boolean canBeDeadly = true;

	/**
	 * Populate sky with stormless/cloudless storm objects in order to allow clear skies with current design
	 */
	public boolean cloudlessStorm = false;

	//used to cache a scan for blocks ahead of storm, to move around
	public float cachedAngleAvoidance = 0;

	public boolean isFirenado = false;

	public List<LivingEntity> listEntitiesUnderClouds = new ArrayList<>();

	private boolean playerControlled = false;
	private int playerControlledTimeLeft = 20;
	private boolean baby = false;

	private boolean configNeedsSync = true;
    
	public StormObject(WeatherManager parManager) {
		super(parManager);
		
		pos = new Vec3(0, static_YPos_layer0, 0);
		maxSize = ConfigStorm.Storm_MaxRadius;
		
		if (parManager.getWorld().isClientSide()) {
			listParticlesCloud = new ArrayList<>();
			listParticlesFunnel = new ArrayList<>();
			listParticlesGround = new ArrayList<>();
			lookupParticlesCloud = new HashMap<>();
			lookupParticlesCloudLower = new HashMap<>();
			lookupParticlesFunnel = new HashMap<>();
			//renderBlock = new RenderCubeCloud();
		}
	}
	
	public void initFirstTime() {
		super.initFirstTime();
		
		Biome bgb = manager.getWorld().m_204166_(new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z))).m_203334_();

		
		float temp = 1;
		
		if (bgb != null) {
			//temp = bgb.getFloatTemperature(new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
			temp = CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), bgb, new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
		}
		
		//initial setting, more apparent than gradual adjustments
		if (naturallySpawned) {
			levelTemperature = getTemperatureMCToWeatherSys(temp);
		}
		//levelWater = 0;
		levelWindMomentum = 0;
		
		//Weather.dbg("initialize temp to: " + levelTemperature + " - biome: " + bgb.biomeName);
		
		
		 
	}

	public boolean isCloudlessStorm() {
		return cloudlessStorm;
	}

	public void setCloudlessStorm(boolean cloudlessStorm) {
		this.cloudlessStorm = cloudlessStorm;
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

	@Override
	public void read()
    {
		super.read();
		nbtSyncFromServer();

		CachedNBTTagCompound var1 = this.getNbtCache();
		

		angleIsOverridden = var1.getBoolean("angleIsOverridden");
		angleMovementTornadoOverride = var1.getFloat("angleMovementTornadoOverride");

		spawnerUUID = var1.getString("spawnerUUID");
    }

    @Override
	public void write()
    {
		super.write();
		nbtSyncForClient();

		CachedNBTTagCompound nbt = this.getNbtCache();
		

		nbt.putBoolean("angleIsOverridden", angleIsOverridden);
		nbt.putFloat("angleMovementTornadoOverride", angleMovementTornadoOverride);

		nbt.putString("spawnerUUID", spawnerUUID);

    }
	
	//receiver method
	@Override
	public void nbtSyncFromServer() {

		CachedNBTTagCompound parNBT = this.getNbtCache();

		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("Received payload from server; length=" + parNBT.getNewNBT().getAllKeys().size());
			Iterator iterator = parNBT.getNewNBT().getAllKeys().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("Received    " + keys);
		}

		super.nbtSyncFromServer();

		//state = parNBT.getInt("state");
		
		//attrib_tornado_severity = parNBT.getInt("attrib_tornado_severity");
		
		//attrib_highwind = parNBT.getBoolean("attrib_highwind");
		//attrib_tornado = parNBT.getBoolean("attrib_tornado");
		//attrib_hurricane = parNBT.getBoolean("attrib_hurricane");
		attrib_precipitation = parNBT.getBoolean("attrib_rain");
		attrib_waterSpout = parNBT.getBoolean("attrib_waterSpout");
		
		currentTopYBlock = parNBT.getInt("currentTopYBlock");
		
		levelTemperature = parNBT.getFloat("levelTemperature");
		levelWater = parNBT.getInt("levelWater");
		
		layer = parNBT.getInt("layer");
		
		//curWeatherType = parNBT.getInt("curWeatherType");
		
		//formingStrength = parNBT.getFloat("formingStrength");

		levelCurIntensityStage = parNBT.getInt("levelCurIntensityStage");
		levelStormIntensityMax = parNBT.getInt("levelStormIntensityMax");
		levelCurStagesIntensity = parNBT.getFloat("levelCurStagesIntensity");
		stormType = parNBT.getInt("stormType");
		
		hasStormPeaked = parNBT.getBoolean("hasStormPeaked");
		
		//overCastModeAndRaining = parNBT.getBoolean("overCastModeAndRaining");
		
		isDead = parNBT.getBoolean("isDead");

		cloudlessStorm = parNBT.getBoolean("cloudlessStorm");

		isFirenado = parNBT.getBoolean("isFirenado");
		
		ticksSinceLastPacketReceived = 0;//manager.getWorld().getGameTime();

		weatherMachineControlled = parNBT.getBoolean("weatherMachineControlled");

		//TODO: sync tornado config for size etc
		String prefix = "tornadoFunnelData_layer_";
		if (parNBT.contains(prefix + "count") && tornadoFunnelSimple != null) {
			int count = parNBT.getInt(prefix + "count");
			for (int i = 0; i < tornadoFunnelSimple.listLayers.size(); i++) {
				Layer layer = tornadoFunnelSimple.listLayers.get(i);
				Vec3 newPos = new Vec3(parNBT.getDouble(prefix + i + "_posX"), parNBT.getDouble(prefix + i + "_posY"), parNBT.getDouble(prefix + i + "_posZ"));
				layer.setPos(newPos);
			}
		}

		playerControlled = parNBT.getBoolean("playerControlled");
		spawnerUUID = parNBT.getString("spawnerUUID");

		if (parNBT.contains("config")) {
			tornadoFunnelSimple.setConfig(ActiveTornadoConfig.deserialize(parNBT.get("config")));
		}

		baby = parNBT.getBoolean("baby");
	}
	
	//compose nbt data for packet (and serialization in future)
	@Override
	public void nbtSyncForClient() {
		super.nbtSyncForClient();

		CachedNBTTagCompound data = this.getNbtCache();
		
		//data.putInt("state", state);
		
		//data.putInt("attrib_tornado_severity", attrib_tornado_severity);
		
		//data.putBoolean("attrib_highwind", attrib_highwind);
		//data.putBoolean("attrib_tornado", attrib_tornado);
		//data.putBoolean("attrib_hurricane", attrib_hurricane);
		data.putBoolean("attrib_rain", attrib_precipitation);
		data.putBoolean("attrib_waterSpout", attrib_waterSpout);
		
		data.putInt("currentTopYBlock", currentTopYBlock);
		
		data.putFloat("levelTemperature", levelTemperature);
		data.putInt("levelWater", levelWater);
		
		data.putInt("layer", layer);
		
		//data.putInt("curWeatherType", curWeatherType);
		
		//data.putFloat("formingStrength", formingStrength);
		
		data.putInt("levelCurIntensityStage", levelCurIntensityStage);
		data.putFloat("levelCurStagesIntensity", levelCurStagesIntensity);
		data.putFloat("levelStormIntensityMax", levelStormIntensityMax);
		data.putInt("stormType", stormType);
		
		data.putBoolean("hasStormPeaked", hasStormPeaked);
		
		//data.putBoolean("overCastModeAndRaining", overCastModeAndRaining);
		
		data.putBoolean("isDead", isDead);

		data.putBoolean("cloudlessStorm", cloudlessStorm);


		data.putBoolean("isFirenado", isFirenado);

		data.putBoolean("weatherMachineControlled", weatherMachineControlled);

		//sync the data heavy tornado funnel every 5 seconds
		if (manager != null && tornadoFunnelSimple != null && manager.getWorld().getGameTime() % 100 == 0) {
			String prefix = "tornadoFunnelData_layer_";
			data.putInt(prefix + "count", tornadoFunnelSimple.listLayers.size());
			for (int i = 0; i < tornadoFunnelSimple.listLayers.size(); i++) {
				Vec3 layerPos = tornadoFunnelSimple.listLayers.get(i).getPos();
				data.putDouble(prefix + i + "_posX", layerPos.x);
				data.putDouble(prefix + i + "_posY", layerPos.y);
				data.putDouble(prefix + i + "_posZ", layerPos.z);
			}
		}

		data.putBoolean("playerControlled", playerControlled);
		data.putString("spawnerUUID", spawnerUUID);

		if (configNeedsSync && tornadoFunnelSimple != null) {
			data.put("config", tornadoFunnelSimple.getConfig().serialize());
		}

		data.putBoolean("baby", baby);

	}
	
	public CompoundTag nbtForIMC() {
		//we basically need all the same data minus a few soooo whatever
		nbtSyncForClient();
		return getNbtCache().getNewNBT();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {
		super.tickRender(partialTick);



		//renderBlock.doRenderClouds(this, 0, 0, 0, 0, partialTick);
		/*if (layer == 1) {
			renderBlock.doRenderClouds(this, pos.x, pos.y, pos.z, 0, partialTick);
		}*/

		//TODO: consider only putting funnel in this method since its the fast part, the rest might be slow enough to only need to do per gametick

		if (!WeatherUtil.isPaused()) {

			int count = 8+1;


			//ParticleBehaviorFog.newCloudWay = true;

			Iterator<Map.Entry<Integer, EntityRotFX>> it = lookupParticlesCloud.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;
					double spawnRad = 120;//(ticksExisted % 100) + 10;
					double speed = 2D / (spawnRad);
					if (isSpinning()) {
						speed = 50D / (spawnRad);
					}
					ent.rotationSpeedAroundCenter = (float)speed;
					if (i == 0) {
						tryPos = new Vec3(pos.x, layers.get(layer), pos.z);
						ent.rotationYaw = ent.rotationAroundCenter;
					} else {
						double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
						double x = -Math.sin(rad) * spawnRad;
						double z = Math.cos(rad) * spawnRad;
						tryPos = new Vec3(pos.x + x, layers.get(layer), pos.z + z);

						double var16 = this.pos.x - ent.getPosX();
						double var18 = this.pos.z - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
						//ent.rotationPitch = -20F;// - (ent.getEntityId() % 10);

						//ent.setAge(100);


					}
					ent.setPosition(tryPos.x, tryPos.y, tryPos.z);
				}
			}



			count = 16*2;

			it = lookupParticlesCloudLower.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;

					ent.setScale(800 * 0.15F);

					double countPerLayer = 16;
					double rotPos = i % 16;
					int layerRot = i / 16;
					double spawnRad = 80;
					if (layerRot == 1) {
						spawnRad = 60;
						ent.setScale(600 * 0.15F);
					}
					double speed = 50D / (spawnRad * 2D);

					ent.rotationSpeedAroundCenter = (float)speed;
					double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
					double x = -Math.sin(rad) * spawnRad;
					double z = Math.cos(rad) * spawnRad;
					tryPos = new Vec3(pos.x + x, layers.get(layer) - 20, pos.z + z);

					ent.setPosition(tryPos.x, tryPos.y, tryPos.z);

					double var16 = this.pos.x - ent.getPosX();
					double var18 = this.pos.z - ent.getPosZ();
					ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					ent.rotationPitch = -20F;// - (ent.getEntityId() % 10);
				}
			}
		}
	}

	public void setupTornado() {
		ActiveTornadoConfig activeTornadoConfig;
		if (isBaby()) {
			activeTornadoConfig = new ActiveTornadoConfig()
					.setHeight(20)
					.setRadiusOfBase(5F + 0F)
					.setSpinSpeed(360F / 20F)
					.setRadiusIncreasePerLayer(0.2F)
					.setEntityPullDistXZ(20)
					.setEntityPullDistXZForY(5);
		} else {
			activeTornadoConfig = new ActiveTornadoConfig()
					.setHeight(150)
					.setRadiusOfBase(5F + 5F)
					.setSpinSpeed(360F / 20F)
					.setRadiusIncreasePerLayer(0.2F)
					.setEntityPullDistXZ(120)
					.setEntityPullDistXZForY(60);
		}
		tornadoFunnelSimple = new TornadoFunnelSimple(activeTornadoConfig, this);
	}
	
	public void tick() {
		super.tick();
		//Weather.dbg("ticking storm " + ID + " - manager: " + manager);
		
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = new Vec3(pos.x, currentTopYBlock, pos.z);

		if (isTornadoFormingOrGreater()) {
			if (tornadoFunnelSimple == null) {
				setupTornado();
			}

			tornadoFunnelSimple.pos = new Vec3(posGround.x, posGround.y, posGround.z);

			tornadoFunnelSimple.tick();
		}
		
		LogicalSide side = EffectiveSide.get();
		if (side == LogicalSide.CLIENT) {
			if (!WeatherUtil.isPaused()) {
				
				ticksSinceLastPacketReceived++;
				
				//if (layer == 0) {
					tickClient();
				//}
				
				if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
					tornadoHelper.tick(manager.getWorld());

					tornadoFunnelSimple.tickClient();
				}
				
				if (levelCurIntensityStage >= STATE_HIGHWIND) {
					if (manager.getWorld().isClientSide()) {
						tornadoHelper.soundUpdates(true, isTornadoFormingOrGreater() || isCycloneFormingOrGreater());
			        }
				}

				tickMovementClient();
			}
		} else {

			if (isCloudlessStorm()) {
				if (ConfigMisc.overcastMode && manager.getWorld().isRaining()) {
					this.setCloudlessStorm(false);
				}
			}

			if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
				tornadoHelper.tick(manager.getWorld());

				//TODO: TEMPPPPPPPPPPPP, make server logic split up
				//tornadoFunnelSimple.tickClient();
			}

			if (levelCurIntensityStage >= STATE_HIGHWIND) {
				if (manager.getWorld().isClientSide()) {
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
				if (!isCloudlessStorm()) {
					tickWeatherEvents();
					tickProgression();
					//tickSnowFall();
				}
			} else {
				//make layer 1 max size for visuals
				size = maxSize;
			}
			
			//overCastModeAndRaining = ConfigMisc.overcastMode && manager.getWorld().isRaining();
	        
		}
		
		if (layer == 0) {
	        //sync X Y Z, Y gets changed below
	        posBaseFormationPos = new Vec3(pos.x, pos.y, pos.z);
	
	        if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal) {
	        	if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal + 1) {
	        		formingStrength = 1;
	        		posBaseFormationPos = new Vec3(posBaseFormationPos.x, posGround.y, posBaseFormationPos.z);
	        	} else {
	        		
	        		//make it so storms touchdown at 0.5F intensity instead of 1 then instantly start going back up, keeps them down for a full 1F worth of intensity val
	        		float intensityAdj = Math.min(1F, levelCurStagesIntensity * 2F);
	        		
	        		//shouldnt this just be intensityAdj?
			        float val = (levelCurIntensityStage + intensityAdj) - StormObject.levelStormIntensityFormingStartVal;
			        formingStrength = val;
			        double yDiff = pos.y - posGround.y;
			        posBaseFormationPos = new Vec3(posBaseFormationPos.x, pos.y - (yDiff * formingStrength), posBaseFormationPos.z);
	        	}
	        } else {
	        	if (levelCurIntensityStage == STATE_HIGHWIND) {
	        		formingStrength = 1;
	        		posBaseFormationPos = new Vec3(posBaseFormationPos.x, posGround.y, posBaseFormationPos.z);
	        	} else {
		        	formingStrength = 0;
	        		posBaseFormationPos = new Vec3(posBaseFormationPos.x, pos.y, posBaseFormationPos.z);
	        	}
	        }
		}
		
	}
	
	public void tickMovement() {

		if (playerControlled) {
			Player entP = manager.getWorld().getPlayerByUUID(UUID.fromString(spawnerUUID));
			aimStormAtClosestOrProvidedPlayer(entP);
		}

		//storm movement via wind
		float angle = getAdjustedAngle();

		if (angleIsOverridden) {
			angle = angleMovementTornadoOverride;
			//debug
			/*if (manager.getWorld().getGameTime() % 20 == 0) {
				EntityPlayer entP = manager.getWorld().getClosestPlayer(pos.x, pos.y, pos.z, -1);
				if (entP != null) {

					//even more debug, heat seak test
					//Random rand = new Random();
					double var11 = entP.posX - pos.x;
		            double var15 = entP.posZ - pos.z;
		            float yaw = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;
		            //weather override!
		            //yaw = weatherMan.wind.direction;
		            //int size = ConfigMisc.Storm_Tornado_aimAtPlayerAngleVariance;
		            //yaw += rand.nextInt(size) - (size / 2);

					angleMovementTornadoOverride = yaw;

					Weather.dbg("angle override: " + angle + " - dist from player: " + entP.getDistance(pos.x, pos.y, pos.z));
				}

			}*/
		}

		//despite overridden angle, still avoid obstacles

		//slight randomness to angle
		Random rand = new Random();
		angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;

		//avoid large obstacles
		double scanDist = 50;
		double scanX = this.pos.x + (-Math.sin(Math.toRadians(angle)) * scanDist);
		double scanZ = this.pos.z + (Math.cos(Math.toRadians(angle)) * scanDist);

		int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

		if (this.pos.y < height) {
			float angleAdj = 45;
			if (this.ID % 2 == 0) {
				angleAdj = -45;
			}
			angle += angleAdj;
		}
		
		//Weather.dbg("cur angle: " + angle);
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		
		float cloudSpeedAmp = 0.2F;

		boolean love_tropics_tweaks = true;
		//tweaking for lt
		if (love_tropics_tweaks) cloudSpeedAmp = 3F;
		
		
		
		float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;
		
		if (levelCurIntensityStage >= STATE_FORMING) {
			finalSpeed = 0.2F;
		} else if (levelCurIntensityStage >= STATE_THUNDER) {
			finalSpeed = 0.05F;
		}
		
		if (!love_tropics_tweaks && levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
			finalSpeed /= ((float)(levelCurIntensityStage-levelStormIntensityFormingStartVal+1F));
		}
		
		if (finalSpeed < 0.03F) {
			finalSpeed = 0.03F;
		}
		
		if (!love_tropics_tweaks && finalSpeed > 0.3F) {
			finalSpeed = 0.3F;
		}

		if (love_tropics_tweaks) {
			finalSpeed = 0.1F;
			//finalSpeed = 0F;
		}

		if (playerControlled) {
			finalSpeed = 0.5F;
			Player player = getPlayer();
			if (player != null) {
				if (posGround.distanceTo(player.position()) > 30) {
					pos = new Vec3(player.position().x, player.position().y, player.position().z);
				}
			}
		}
		
		if (manager.getWorld().getGameTime() % 100 == 0 && levelCurIntensityStage >= STATE_FORMING) {
			
			//finalSpeed = 0.5F;
			
			//Weather.dbg("storm ID: " + this.ID + ", stage: " + levelCurIntensityStage + ", storm speed: " + finalSpeed);
		}
		

		if (!weatherMachineControlled) {
			motion = new Vec3(vecX * finalSpeed, 0, vecZ * finalSpeed);

			double max = 0.2D;
			//max speed

			/*if (motion.x < -max) motion.x = -max;
			if (motion.x > max) motion.x = max;
			if (motion.z < -max) motion.z = -max;
			if (motion.z > max) motion.z = max;*/

			//actually move storm
			pos = pos.add(motion);
		}
	}

	public void tickMovementClient() {
		if (!weatherMachineControlled) {
			pos = pos.add(motion);
		}
	}
	
	public void tickWeatherEvents() {
		Random rand = new Random();
		Level world = manager.getWorld();
		
		currentTopYBlock = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z))).getY();
		//Weather.dbg("currentTopYBlock: " + currentTopYBlock);
		if (levelCurIntensityStage >= STATE_THUNDER) {
			if (rand.nextInt((int)Math.max(1, ConfigStorm.Storm_LightningStrikeBaseValueOddsTo1 - (levelCurIntensityStage * 10))) == 0) {
				int x = (int) (pos.x + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.z + rand.nextInt(size) - rand.nextInt(size));
				if (world.isLoaded(new BlockPos(x, 0, z))) {
					int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
					//if (world.canLightningStrikeAt(x, y, z)) {
					CULog.dbg("todo fix lightning, register it");
						//addWeatherEffectLightning(new LightningBoltWeather(null, world, x, y, z), false);
					//}
				}
			}
		}
		
		//dont forget, this doesnt account for storm size, so small storms have high concentration of hail, as it grows, it appears to lessen in rate
		if (isPrecipitating() && levelCurIntensityStage == STATE_HAIL && stormType == TYPE_LAND) {
			//if (rand.nextInt(1) == 0) {
			for (int i = 0; i < Math.max(1, ConfigStorm.Storm_HailPerTick * (size/maxSize)); i++) {
				int x = (int) (pos.x + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) (pos.z + rand.nextInt(size) - rand.nextInt(size));
				if (world.isLoaded(new BlockPos(x, static_YPos_layer0, z)) && (world.getNearestPlayer(x, 50, z, 80, false) != null)) {
					//int y = world.getPrecipitationHeight(x, z);
					//if (world.canLightningStrikeAt(x, y, z)) {
					//TODO: 1.14 uncomment
					/*EntityIceBall hail = new EntityIceBall(world);
					hail.setPosition(x, layers.get(layer), z);
					world.addEntity(hail);*/
					//world.addWeatherEffect(new LightningBoltWeather(world, (double)x, (double)y, (double)z));
					//}
					
					//System.out.println("spawned hail: " );
				} else {
					//System.out.println("nope");
				}
			}
		}

		trackAndExtinguishEntities();
	}

	public void trackAndExtinguishEntities() {

		if (ConfigStorm.Storm_Rain_TrackAndExtinguishEntitiesRate <= 0) return;

		if (isPrecipitating()) {

			//efficient caching
			if ((manager.getWorld().getGameTime() + (ID * 20)) % ConfigStorm.Storm_Rain_TrackAndExtinguishEntitiesRate == 0) {
				listEntitiesUnderClouds.clear();
				BlockPos posBP = new BlockPos(posGround.x, posGround.y, posGround.z);
				List<LivingEntity> listEnts = manager.getWorld().getEntitiesOfClass(LivingEntity.class, new AABB(posBP).inflate(size));
				for (LivingEntity ent : listEnts) {
					if (ent.level.canSeeSky(ent.blockPosition())) {
						listEntitiesUnderClouds.add(ent);
					}
				}
			}

			for (LivingEntity ent : listEntitiesUnderClouds) {
				ent.clearFire();
			}
		}
	}
	
	public void tickProgression() {
		Level world = manager.getWorld();
		
		//storm progression, heavy WIP
		if (world.getGameTime() % 3 == 0) {
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
		
		float tempAdjustRate = (float)ConfigStorm.Storm_TemperatureAdjustRate;//0.1F;
		int levelWaterBuildRate = ConfigStorm.Storm_Rain_WaterBuildUpRate;
		int levelWaterSpendRate = ConfigStorm.Storm_Rain_WaterSpendRate;
		int randomChanceOfWaterBuildFromWater = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromSource;
		int randomChanceOfWaterBuildFromNothing = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromNothing;
		int randomChanceOfWaterBuildFromOvercastRaining = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromOvercastRaining;
		randomChanceOfWaterBuildFromOvercastRaining = 10;
		//int randomChanceOfRain = ConfigMisc.Player_Storm_Rain_OddsTo1;
		
		boolean isInOcean = false;
		boolean isOverWater = false;
		
		if (world.getGameTime() % ConfigStorm.Storm_AllTypes_TickRateDelay == 0) {

			//boolean debug = false;
			CompoundTag playerNBT = PlayerData.getPlayerNBT(spawnerUUID);
			
			long lastStormDeadlyTime = playerNBT.getLong("lastStormDeadlyTime");
			//long lastStormRainTime = playerNBT.getLong("lastStormRainTime");

			//Biome bgb = null;
			Biome bgb = world.m_204166_(new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z))).m_203334_();
			
			//temperature scan
			if (bgb != null) {

				isInOcean = bgb.getRegistryName().toString().toLowerCase().contains("ocean");
				
				//float biomeTempAdj = getTemperatureMCToWeatherSys(bgb.getFloatTemperature(new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z))));
				float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), bgb, new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z))));
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

			if (!isPrecipitating() && ConfigMisc.overcastMode && manager.getWorld().isRaining() &&
					rand.nextInt(randomChanceOfWaterBuildFromOvercastRaining) == 0) {
				performBuildup = true;
			}

			BlockPos tryPos = new BlockPos(Mth.floor(pos.x), currentTopYBlock - 1, Mth.floor(pos.z));
			if (world.isLoaded(tryPos)) {
				BlockState state = world.getBlockState(tryPos);
				if (!CoroUtilBlock.isAir(state.getBlock())) {
					//Block block = Block.blocksList[blockID];
					if (state.getMaterial() == Material.WATER) {
						isOverWater = true;
					}
				}
			}
			
			//water scan - dont build up if raining already
			if (!performBuildup && !isPrecipitating() && rand.nextInt(randomChanceOfWaterBuildFromWater) == 0) {
				if (isOverWater) {
					performBuildup = true;
				}

				if (bgb != null) {
					String biomecat = bgb.getRegistryName().toString();

					if (!performBuildup && (isInOcean || biomecat.contains("swamp") || biomecat.contains("jungle") || biomecat.contains("river"))) {
						performBuildup = true;
					}
				}
			}
			
			if (performBuildup) {
				//System.out.println("RAIN BUILD TEMP OFF");
				levelWater += levelWaterBuildRate;
				Weather.dbg("building rain: " + levelWater);
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
				if (levelWater >= levelWaterStartRaining) {
					if (ConfigMisc.overcastMode) {
						if (manager.getWorld().isRaining()) {
							if (ConfigStorm.Storm_Rain_Overcast_OddsTo1 != -1 && rand.nextInt(ConfigStorm.Storm_Rain_Overcast_OddsTo1) == 0) {
								setPrecipitating(true);
								Weather.dbg("starting raining for: " + ID);
							}
						}
					} else {
						if (ConfigStorm.Storm_Rain_OddsTo1 != -1 && rand.nextInt(ConfigStorm.Storm_Rain_OddsTo1) == 0) {
							setPrecipitating(true);
							Weather.dbg("starting raining for: " + ID);
						}
					}
				}

			}
			
			//actual storm formation chance
			
			WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(world.dimension());
			
			boolean tryFormStorm = false;
			boolean tempAlwaysFormStorm = false;
			
			if (this.canBeDeadly && this.levelCurIntensityStage == STATE_NORMAL) {
				if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
					if (ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks != -1) {
						if (wm.lastStormFormed == 0 || wm.lastStormFormed + ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks < world.getGameTime()) {
							tryFormStorm = true;
						}
					}
				} else {
					if (tempAlwaysFormStorm || ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks != -1) {
						if (tempAlwaysFormStorm || lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks < world.getGameTime()) {
							tryFormStorm = true;
						}
					}
				}
			}

			if (weatherMachineControlled) {
			    return;
            }

			String temp = manager.getWorld().dimension().location().toString();
			if (((ConfigMisc.overcastMode && manager.getWorld().isRaining()) || !ConfigMisc.overcastMode) && WeatherUtilConfig.listDimensionsStorms.contains(manager.getWorld().dimension().location().toString()) && tryFormStorm) {
				int stormFrontCollideDist = ConfigStorm.Storm_Deadly_CollideDistance;
				int randomChanceOfCollide = ConfigStorm.Player_Storm_Deadly_OddsTo1;

				if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
					randomChanceOfCollide = ConfigStorm.Server_Storm_Deadly_OddsTo1;
				}

				if (isInOcean && (ConfigStorm.Storm_OddsTo1OfOceanBasedStorm > 0 && rand.nextInt(ConfigStorm.Storm_OddsTo1OfOceanBasedStorm) == 0)) {
					Player entP = world.getPlayerByUUID(UUID.fromString(spawnerUUID));

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
						wm.lastStormFormed = world.getGameTime();
					} else {
						playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());
					}
				} else if (!isInOcean && ConfigStorm.Storm_OddsTo1OfLandBasedStorm > 0 && rand.nextInt(ConfigStorm.Storm_OddsTo1OfLandBasedStorm) == 0) {
					Player entP = world.getPlayerByUUID(UUID.fromString(spawnerUUID));

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
						wm.lastStormFormed = world.getGameTime();
					} else {
						playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());
					}
				} else if (rand.nextInt(randomChanceOfCollide) == 0) {
					for (int i = 0; i < manager.getStormObjects().size(); i++) {
						WeatherObject wo = manager.getStormObjects().get(i);

						if (wo instanceof StormObject) {
							StormObject so = (StormObject) wo;



							boolean startStorm = false;

							if (so.ID != this.ID && so.levelCurIntensityStage <= 0 && !so.isCloudlessStorm() && !so.weatherMachineControlled) {
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

								playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());

								//EntityPlayer entP = manager.getWorld().getClosestPlayer(pos.x, pos.y, pos.z, -1);
								Player entP = world.getPlayerByUUID(UUID.fromString(spawnerUUID));

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

				//temp
				//levelWater = 0;
				//setPrecipitating(false);
				
				if ((levelCurIntensityStage == STATE_HIGHWIND || levelCurIntensityStage == STATE_HAIL) && isOverWater) {
					if (ConfigStorm.Storm_OddsTo1OfHighWindWaterSpout != 0 && rand.nextInt(ConfigStorm.Storm_OddsTo1OfHighWindWaterSpout) == 0) {
						attrib_waterSpout = true;
					}
				} else {
					attrib_waterSpout = false;
				}
				
				float levelStormIntensityRate = 0.02F;
				float minIntensityToProgress = 0.6F;
				//change since storms have a predetermined max now, nevermind, storms take too long, limited simbox area
				//minIntensityToProgress = 0.8F;
				//int oddsTo1OfIntensityProgressionBase = ConfigStorm.Storm_OddsTo1OfProgressionBase;
				
				//speed up forming and greater progression when past forming state
				if (levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
					levelStormIntensityRate *= 3;
					//oddsTo1OfIntensityProgressionBase /= 3;
				}

				//int oddsTo1OfIntensityProgression = oddsTo1OfIntensityProgressionBase + (levelCurIntensityStage * ConfigStorm.Storm_OddsTo1OfProgressionStageMultiplier);

				if (!hasStormPeaked) {
					
					levelCurStagesIntensity += levelStormIntensityRate;
					
					if (levelCurIntensityStage < maxIntensityStage && (!ConfigTornado.Storm_NoTornadosOrCyclones || levelCurIntensityStage < STATE_FORMING-1)) {
						if (levelCurStagesIntensity >= minIntensityToProgress) {
							//Weather.dbg("storm ID: " + this.ID + " trying to hit next stage");
							if (alwaysProgresses || levelCurIntensityStage < levelStormIntensityMax/*rand.nextInt(oddsTo1OfIntensityProgression) == 0*/) {
								stageNext();
								Weather.dbg("storm ID: " + this.ID + " - growing, stage: " + levelCurIntensityStage + " pos: " + pos);
								//mark is tropical cyclone if needed! and never unmark it!
								if (isInOcean) {
									//make it ONLY allow to change during forming stage, so it locks in
									if (levelCurIntensityStage == STATE_FORMING) {
										Weather.dbg("storm ID: " + this.ID + " marked as tropical cyclone!");
										stormType = TYPE_WATER;

										//reroll dice on ocean storm since we only just define it here
										levelStormIntensityMax = rollDiceOnMaxIntensity();
										Weather.dbg("rerolled odds for ocean storm, max stage will be: " + levelStormIntensityMax);
									}
								}
							}
						}
					}
					
					
					Weather.dbg("storm ID: " + this.ID + " - growing, stage " + levelCurIntensityStage + " of max " + levelStormIntensityMax + ", at intensity: " + levelCurStagesIntensity + " pos: " + pos);
					
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

		if (playerControlled) {
			if (playerControlledTimeLeft > 0) {
				playerControlledTimeLeft--;

				if (playerControlledTimeLeft <= 0) {
					remove();
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
		if (ConfigTornado.Storm_Tornado_aimAtPlayerOnSpawn) {
			if (!hasStormPeaked && levelCurIntensityStage == STATE_FORMING) {
				aimStormAtClosestOrProvidedPlayer(null);
			}
		}
	}
	
	public void stagePrev() {
		levelCurIntensityStage--;
		levelCurStagesIntensity = 1F;
	}
	
	public void initRealStorm(Player entP, StormObject stormToAbsorb) {
		
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

		this.levelStormIntensityMax = rollDiceOnMaxIntensity();
		Weather.dbg("rolled odds for storm, unless it becomes ocean storm, max stage will be: " + levelStormIntensityMax);

		this.attrib_precipitation = true;

		if (stormToAbsorb != null) {
			Weather.dbg("stormfront collision happened between ID " + this.ID + " and " + stormToAbsorb.ID);
			manager.removeStormObject(stormToAbsorb.ID);
			((WeatherManagerServer)manager).syncStormRemove(stormToAbsorb);
		} else {
			Weather.dbg("ocean storm happened, ID " + this.ID);
		}
		
		if (ConfigTornado.Storm_Tornado_aimAtPlayerOnSpawn) {
			
			//if (entP != null) {
				aimStormAtClosestOrProvidedPlayer(entP);
			//}
			
		}
	}

	public int rollDiceOnMaxIntensity() {
		Random rand = new Random();
		int randVal = rand.nextInt(100);
		if (stormType == TYPE_LAND) {
			if (randVal <= ConfigStorm.Storm_PercentChanceOf_F5_Tornado) {
				return STATE_STAGE5;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F4_Tornado) {
				return STATE_STAGE4;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F3_Tornado) {
				return STATE_STAGE3;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F2_Tornado) {
				return STATE_STAGE2;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F1_Tornado) {
				return STATE_STAGE1;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F0_Tornado) {
				return STATE_FORMING;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_Hail) {
				return STATE_HAIL;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_HighWind) {
				return STATE_HIGHWIND;
			}
		} else if (stormType == TYPE_WATER) {
			if (randVal <= ConfigStorm.Storm_PercentChanceOf_C5_Cyclone) {
				return STATE_STAGE5;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C4_Cyclone) {
				return STATE_STAGE4;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C3_Cyclone) {
				return STATE_STAGE3;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C2_Cyclone) {
				return STATE_STAGE2;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C1_Cyclone) {
				return STATE_STAGE1;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C0_Cyclone) {
				return STATE_FORMING;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_Hail) {
				return STATE_HAIL;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_HighWind) {
				return STATE_HIGHWIND;
			}
		}

		return STATE_THUNDER;
	}
	
	public void aimStormAtClosestOrProvidedPlayer(Player entP) {
		
		if (entP == null) {
			entP = manager.getWorld().getNearestPlayer(pos.x, pos.y, pos.z, -1, false);
		}
		
		if (entP != null) {
			Random rand = new Random();
			double var11 = entP.getX() - pos.x;
            double var15 = entP.getZ() - pos.z;
            float yaw = -(float)(Math.atan2(var11, var15) * 180.0D / Math.PI);
            //weather override!
            //yaw = weatherMan.wind.direction;
            int size = ConfigTornado.Storm_Tornado_aimAtPlayerAngleVariance;
            if (size > 0) {
            	yaw += rand.nextInt(size) - (size / 2);
            }
            
            angleIsOverridden = true;
			angleMovementTornadoOverride = yaw;
			
			//Weather.dbg("stormfront aimed at player " + CoroUtilEntity.getName(entP));
		}
	}
	
	//FYI rain doesnt count as storm
	public void setNoStorm() {
		Weather.dbg("storm ID: " + this.ID + " - ended storm event");
		levelCurIntensityStage = STATE_NORMAL;
		levelCurStagesIntensity = 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickClient() {

		if (isCloudlessStorm()) return;

		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(new Vec3(pos.x, pos.y, pos.z));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getInstance().isPaused()) {
				particleBehaviorFog.tickUpdateList();
			}
		}
        
		Player entP = Minecraft.getInstance().player;
		
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
		
		Vec3 playerAdjPos = new Vec3(entP.getX(), pos.y, entP.getZ());
		double maxSpawnDistFromPlayer = 512;
		


		//maintain clouds new system


		//spawn clouds

		if (!baby && this.manager.getWorld().getGameTime() % (delay + (isSpinning() ? ConfigStorm.Storm_ParticleSpawnDelay : ConfigMisc.Cloud_ParticleSpawnDelay)) == 0) {
			for (int i = 0; i < loopSize; i++) {
				/*if (listParticlesCloud.size() == 0) {
					double spawnRad = 1;
					Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layers.get(layer), pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					EntityRotFX particle;
					if (WeatherUtil.isAprilFoolsDay()) {
						particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0, ParticleRegistry.chicken);
					} else {
						particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0, ParticleRegistry.cloud256_test);
					}

					listParticlesCloud.add(particle);
				}*/
				if (listParticlesCloud.size() < (size + extraSpawning) / 1F) {
					double spawnRad = size;
					
					/*if (layer != 0) {
						spawnRad = size * 5;
					}*/
					
					//Weather.dbg("listParticlesCloud.size(): " + listParticlesCloud.size());
					
					//Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), layers.get(layer), pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), getPosTop().y + 30, pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						if (getAvoidAngleIfTerrainAtOrAheadOfPosition(getAdjustedAngle(), tryPos) == 0) {
							EntityRotFX particle;
							if (WeatherUtil.isAprilFoolsDay()) {
								particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0, ParticleRegistry.chicken);
							} else {

								particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0);
								if (isFirenado && isSpinning()) {
									//if (particle.getEntityId() % 20 < 5) {
										particle.setSprite(ParticleRegistry.cloud256_fire);
										particle.setColor(1F, 1F, 1F);

									//}
								}
							}

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
		}
		
		//ground effects
		if (levelCurIntensityStage >= STATE_HIGHWIND && !baby) {
			for (int i = 0; i < (stormType == TYPE_WATER ? 50 : 3)/*loopSize/2*/; i++) {
				if (listParticlesGround.size() < (stormType == TYPE_WATER ? 600 : 150)/*size + extraSpawning*/) {
					double spawnRad = size/4*3;
					
					if (stormType == TYPE_WATER) {
						spawnRad = size*3;
					}
					
					//Weather.dbg("listParticlesCloud.size(): " + listParticlesCloud.size());
					
					Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), posGround.y, pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.x, 0, (int)tryPos.z)).getY();
						EntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay()) {
							particle = spawnFogParticle(tryPos.x, groundY + 3, tryPos.z, 0, ParticleRegistry.potato);
						} else {
							particle = spawnFogParticle(tryPos.x, groundY + 3, tryPos.z, 0);
						}
						
						particle.setScale(200 * 0.15F);
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

		boolean tornadoV2 = true;

		//spawn funnel
		if (!tornadoV2) {
			if (isTornadoFormingOrGreater() || (attrib_waterSpout)) {
				if (this.manager.getWorld().getGameTime() % (delay + ConfigStorm.Storm_ParticleSpawnDelay) == 0) {
					for (int i = 0; i < loopSize; i++) {
						//temp comment out
						//if (attrib_tornado_severity > 0) {

						//Weather.dbg("spawn");

						//trim!
						if (listParticlesFunnel.size() >= sizeMaxFunnelParticles) {
							listParticlesFunnel.get(0).remove();
							listParticlesFunnel.remove(0);
						}

						if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {


							Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad), pos.y, pos.z + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));
							//int y = entP.world.getPrecipitationHeight((int)tryPos.x, (int)tryPos.z);

							if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
								EntityRotFX particle;
								if (!isFirenado/* && false*/) {
									if (WeatherUtil.isAprilFoolsDay()) {
										particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1, ParticleRegistry.potato);
									} else {
										particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1);
									}
								} else {
									particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1, ParticleRegistry.cloud256_fire);

								}


								//move these to a damn profile damnit!
								particle.setMaxAge(150 + ((levelCurIntensityStage - 1) * 100) + rand.nextInt(100));

								float baseBright = 0.3F;
								float randFloat = (rand.nextFloat() * 0.6F);

								particle.rotationYaw = rand.nextInt(360);

								float finalBright = Math.min(1F, baseBright + randFloat);

								//highwind aka spout in this current code location
								if (levelCurIntensityStage == STATE_HIGHWIND) {
									particle.setScale(150 * 0.15F);
									particle.setColor(finalBright - 0.2F, finalBright - 0.2F, finalBright);
								} else {
									particle.setScale(250 * 0.15F);
									particle.setColor(finalBright, finalBright, finalBright);
								}

								if (isFirenado) {
									particle.setColor(1F, 1F, 1F);
									particle.setScale(particle.getScale() * 0.7F);
								}


								listParticlesFunnel.add(particle);

								//System.out.println(listParticlesFunnel.size());
							}
						} else {
							//Weather.dbg("particles maxed");
						}
					}
				}
			}

			for (int i = 0; i < listParticlesFunnel.size(); i++) {
				EntityRotFX ent = listParticlesFunnel.get(i);
				//System.out.println(ent.getPosY());
				if (!ent.isAlive()) {
					listParticlesFunnel.remove(ent);
				} else if (ent.getPosY() > pos.y) {
					ent.remove();
					listParticlesFunnel.remove(ent);
					//System.out.println("asd");
				} else {
					double var16 = this.pos.x - ent.getPosX();
					double var18 = this.pos.z - ent.getPosZ();
					ent.rotationYaw = -(float) (Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					ent.rotationYaw -= ent.getEntityId() % 90;
					ent.rotationPitch = 30F;

					//fade spout blue to grey
					if (levelCurIntensityStage == STATE_HIGHWIND) {
						int fadingDistStart = 30;
						if (ent.getPosY() > posGround.y + fadingDistStart) {
							float maxVal = ent.bCol;
							float fadeRate = 0.002F;
							ent.setColor(Math.min(maxVal, ent.rCol + fadeRate), Math.min(maxVal, ent.gCol + fadeRate), maxVal);
						}
					}

					spinEntity(ent);
				}
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive()) {
				listParticlesCloud.remove(ent);
			} else {
				//ent.posX = pos.x + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.getEntityId());
				float posZ = (float) Math.cos(ent.getEntityId());
				ent.setPosition(pos.x + posX*radius, ent.posY, pos.z + posZ*radius);*/
		        
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
				
				double curDist = ent.getDistance(getPosTop().x, ent.getPosY(), getPosTop().z);

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
					
					
					double vecX = ent.getPosX() - getPosTop().x;
			        double vecZ = ent.getPosZ() - getPosTop().z;
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

			        	double diffX = this.getPosTop().x - ent.getPosX();
		                double diffZ = this.getPosTop().z - ent.getPosZ();
		                ent.rotationYaw = (float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		                ent.rotationYaw = -(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		                //ent.rotationYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) + 90F;
		                ent.rotationPitch = 20F - (ent.getEntityId() % 10);
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

					//TODO: prevent new particles spawning inside or near solid blocks

					if ((manager.getWorld().getGameTime()+this.ID) % 40 == 0) {
						ent.avoidTerrainAngle = getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, ent.getPos());
					}

					angle += ent.avoidTerrainAngle;

					if (ent.avoidTerrainAngle != 0) {
						/*float angleAdj = 90;
						if (this.ID % 2 == 0) {
							angleAdj = -90;
						}
						angle += angleAdj;*/

						speed *= 0.5D;
					}
					
					dropDownRange = 5;
			        if (/*curDist < 200 && */ent.getEntityId() % 20 < 5) {
			        	extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
			        }
					
					if (curSpeed < speed * 1D) {
			        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
				        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
			        }
				}
		        
				if (Math.abs(ent.getPosY() - (getPosTop().y - extraDropCalc)) > 2F) {
			        if (ent.getPosY() < getPosTop().y - extraDropCalc) {
		        		ent.setMotionY(ent.getMotionY() + 0.1D);
		        	} else {
		        		ent.setMotionY(ent.getMotionY() - 0.1D);
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
		        
		        //double distToGround = ent.world.getHeightValue((int)pos.x, (int)pos.z);
		        
		        //ent.setPosition(ent.posX, pos.y, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.remove();
				listParticles.remove(ent);
			}*/
		}
		
		for (int i = 0; i < listParticlesGround.size(); i++) {
			EntityRotFX ent = listParticlesGround.get(i);
			
			double curDist = ent.getDistance(pos.x, ent.getPosY(), pos.z);
			
			if (!ent.isAlive()) {
				listParticlesGround.remove(ent);
			} else {
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
			
				double speed = Math.max(0.2F, 5F * spinSpeed) + (rand.nextDouble() * 0.01D);
				double distt = size;//300D;
				
				
				double vecX = ent.getPosX() - pos.x;
		        double vecZ = ent.getPosZ() - pos.z;
		        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
		        
		        angle += 85;
		        
		        int maxParticleSize = 60;
		        
		        if (stormType == TYPE_WATER) {
		        	maxParticleSize = 150;
		        	speed /= 5D;
				}
		        
		        ent.setScale((float) Math.min(maxParticleSize * 0.15F, curDist * 2F * 0.15F));
		        
		        if (curDist < 20) {
		        	ent.remove();
		        }

	        	double var16 = this.pos.x - ent.getPosX();
                double var18 = this.pos.z - ent.getPosZ();
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
		return manager.wind.getWindSpeedForClouds();
	}
	
	public float getAdjustedAngle() {
		float angle = manager.wind.getWindAngleForClouds();
		
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
		
		float bestMove = Mth.wrapDegrees(targetYaw - angle);
		
		if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}
		
		//Weather.dbg("ID: " + ID + " - " + manager.wind.getWindAngleForClouds() + " - final angle: " + angle);
		
		return angle;
	}

	public float getAvoidAngleIfTerrainAtOrAheadOfPosition(float angle, Vec3 pos) {
		double scanDistMax = 120;
		for (int scanAngle = -20; scanAngle < 20; scanAngle += 10) {
			for (double scanDistRange = 20; scanDistRange < scanDistMax; scanDistRange += 10) {
				double scanX = pos.x + (-Math.sin(Math.toRadians(angle + scanAngle)) * scanDistRange);
				double scanZ = pos.z + (Math.cos(Math.toRadians(angle + scanAngle)) * scanDistRange);

				int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), new BlockPos(scanX, 0, scanZ)).getY();

				if (pos.y < height) {
					if (scanAngle <= 0) {
						return 90;
					} else {
						return -90;
					}
				}
			}
		}
		return 0;
	}

	public void spinEntityv2(LivingEntity entity) {
		Vec3 posCenter = getPosTop();
		for (Layer layer : tornadoFunnelSimple.listLayers) {
			if (entity.position().y - 1.5F < layer.getPos().y) {
				posCenter = layer.getPos();
				break;
			}
		}

		double vecx = posCenter.x - entity.position().x;
		double vecz = posCenter.z - entity.position().z;

		Vec3 vecXZ = new Vec3(posCenter.x, entity.getY(), posCenter.z);

		double distXZMax = tornadoFunnelSimple.getConfig().getEntityPullDistXZ();
		double distXZMaxForYGrab = tornadoFunnelSimple.getConfig().getEntityPullDistXZForY();
		double distXZ = Math.min(Math.sqrt(entity.distanceToSqr(vecXZ)), distXZMax);
		double distXZForYGrab = Math.min(Math.sqrt(entity.distanceToSqr(vecXZ)), distXZMaxForYGrab);

		float angle = (float)(Mth.atan2(vecz, vecx) * 180.0D / Math.PI + 180F);

		angle += 50;

		double entHeightFromBase = Math.max(0.1F, entity.getY() - posGround.y);
		double heightMathMax = 50;
		if (baby) heightMathMax = 15;
		//double heightMathMax = tornadoFunnelSimple.getConfig().getHeight();
		double heightAmp = (heightMathMax - entHeightFromBase) / heightMathMax;

		angle += (40 * heightAmp);

		angle = (float) Math.toRadians(angle);
		double pullY = 0.2 * (distXZMaxForYGrab - distXZForYGrab) / distXZMaxForYGrab;
		double pullXZ = 0.2 * (distXZMax - distXZ) / distXZMax;
		double xx = -Math.sin(angle) * pullXZ;
		double zz = Math.cos(angle) * pullXZ;
		double yy = pullY;

		if (!baby && entity.getDeltaMovement().y > 0.5F) {
			yy = 0;
		}

		entity.setDeltaMovement(entity.getDeltaMovement().x + xx, entity.getDeltaMovement().y + yy, entity.getDeltaMovement().z + zz);

		entity.fallDistance = 0;
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
    	
    	Level world = CoroUtilEntOrParticle.getWorld(entity1);
    	long worldTime = world.getGameTime();
    	
    	Entity ent = null;
    	if (entity1 instanceof Entity) {
    		ent = (Entity) entity1;
    	}
    	
        //ConfigTornado.Storm_Tornado_height;
        double radius = 10D;
        double scale = conf.tornadoWidthScale;
        double d1 = entity.pos.x - CoroUtilEntOrParticle.getPosX(entity1);
        double d2 = entity.pos.z - CoroUtilEntOrParticle.getPosZ(entity1);
        
        if (conf.type == conf.TYPE_SPOUT) {
        	float range = 30F * (float) Math.sin((Math.toRadians(((worldTime * 0.5F) + (ID * 50)) % 360)));
        	float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(entity1) - posGround.y) / (pos.y - posGround.y)));
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

        double distY = entity.pos.y - CoroUtilEntOrParticle.getPosY(entity1);
        double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

        if (CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.y < 0.0D)
        {
            distY = 1.0D;
        }
        else
        {
            distY = CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.y;
        }

        if (distY > maxHeight)
        {
            distY = maxHeight;
        }

        //float weight = WeatherUtilEntity.getWeight(entity1, forTornado);
        float weight = WeatherUtilEntity.getWeight(entity1);
        double grab = (10D / weight)/* / ((distY / maxHeight) * 1D)*/ * ((Math.abs((maxHeight - distY)) / maxHeight));
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

        pullY += (float)(conf.tornadoLiftRate / (weight / 2F)/* * (Math.abs(radius - distXZ) / radius)*/);
        
        
        if (entity1 instanceof Player)
        {
            double adjPull = 0.2D / ((weight * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            //double adjGrab = (10D * (((float)(((double)WeatherUtilEntity.playerInAirTime + 1D) / 400D))));
            double adjGrab = (10D * (((float)(((double)0 + 1D) / 400D))));

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
        else if (entity1 instanceof LivingEntity)
        {
            double adjPull = 0.005D / ((weight * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            int airTime = ent.getPersistentData().getInt("timeInAir");
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

            if (ent.getDeltaMovement().y > -1.5)
            {
                ent.fallDistance = 0F;
            }
            
            if (ent.getDeltaMovement().y > 0.3F) ent.setDeltaMovement(ent.getDeltaMovement().x, 0.3F, ent.getDeltaMovement().z);

            if (forTornado) ent.setOnGround(false);

            //its always raining during these, might as well extinguish them
            ent.clearFire();

            //System.out.println(adjPull);
        }
        
        
        grab += conf.relTornadoSize;
        
        double profileAngle = Math.max(1, (75D + grab - (10D * scale)));
        
        f1 = (float)((double)f1 + profileAngle);
        
        //debug - dont do this here, breaks server
        /*if (entity1 instanceof EntityIconFX) {
        	if (entity1.getEntityId() % 20 < 5) {
        		if (((EntityIconFX) entity1).renderOrder != -1) {
        			if (entity1.world.getGameTime() % 40 == 0) {
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

        if (entity1 instanceof LivingEntity)
        {
            //f5 /= (WeatherUtilEntity.getWeight(entity1, forTornado) * ((distXZ + 1D) / radius));
            f5 /= (WeatherUtilEntity.getWeight(entity1) * ((distXZ + 1D) / radius));
        }
        
        //if player and not spout
        if (entity1 instanceof Player && conf.type != 0) {
        	//System.out.println("grab: " + f5);
        	if (ent.isOnGround()) {
        		f5 *= 10.5F;
        	} else {
        		f5 *= 5F;
        	}
        	//if (entity1.world.rand.nextInt(2) == 0) entity1.onGround = false;
        } else if (entity1 instanceof LivingEntity && conf.type != 0) {
        	f5 *= 1.5F;
        }

        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof LivingEntity) {
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
        
        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof LivingEntity) {
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
	        long lastPullTime = ent.getPersistentData().getLong("lastPullTime");
	        if (lastPullTime == worldTime) {
	        	//System.out.println("preventing double pull");
	        	pullY = 0;
	        }
	        ent.getPersistentData().putLong("lastPullTime", worldTime);
        }
        
        setVel(entity1, -moveX, pullY, moveZ);
	}
	
	public void setVel(Object entity, float f, float f1, float f2)
    {
        CoroUtilEntOrParticle.setMotionX(entity, CoroUtilEntOrParticle.getMotionX(entity) + f);
		CoroUtilEntOrParticle.setMotionY(entity, CoroUtilEntOrParticle.getMotionY(entity) + f1);
		CoroUtilEntOrParticle.setMotionZ(entity, CoroUtilEntOrParticle.getMotionZ(entity) + f2);
    }

	@OnlyIn(Dist.CLIENT)
	public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder) {
		return spawnFogParticle(x, y, z, parRenderOrder, ParticleRegistry.cloud256);
	}
	
	@OnlyIn(Dist.CLIENT)
    public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex) {
    	double speed = 0D;
		Random rand = new Random();
    	EntityRotFX entityfx = particleBehaviorFog.spawnNewParticleIconFX(Minecraft.getInstance().level, tex, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, parRenderOrder);
		particleBehaviorFog.initParticle(entityfx);
		
		//potato
		//entityfx.setColor(1f, 1f, 1f);
		
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
			entityfx.renderOrder = 1;
			
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
		
		/*if (layer == 1) {
			baseBright = 0.1F;
		}*/
		
		float finalBright = Math.min(1F, baseBright+randFloat);

		/*if (isFirenado) {
			finalBright = 1F;
		}*/

		entityfx.setColor(finalBright, finalBright, finalBright);
		
		//entityfx.setColor(1, 1, 1);
		
		//DEBUG
		if (debug) {
			if (levelTemperature < 0) {
				entityfx.setColor(0, 0, finalBright);
			} else if (levelTemperature > 0) {
				entityfx.setColor(finalBright, 0, 0);
			}
		}

		//TODO: 1.14 rotEffRenderer
		entityfx.spawnAsWeatherEffect();
		//Minecraft.getInstance().particleEngine.add(entityfx);
		particleBehaviorFog.particles.add(entityfx);
		return entityfx;
    }

	@Override
	public void cleanup() {
		super.cleanup();
		if (tornadoHelper != null) {
			tornadoHelper.cleanup();
		}
		tornadoHelper = null;
		if (tornadoFunnelSimple != null) {
			tornadoFunnelSimple.cleanup();
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void cleanupClient() {
		super.cleanupClient();
		listParticlesCloud.clear();
		listParticlesFunnel.clear();
		if (particleBehaviorFog != null && particleBehaviorFog.particles != null) particleBehaviorFog.particles.clear();
		particleBehaviorFog = null;
		tornadoHelper = null;
		if (tornadoFunnelSimple != null) {
			tornadoFunnelSimple.cleanupClient();
		}
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
	
	public void addWeatherEffectLightning(LightningBoltWeather parEnt, boolean custom) {
		//manager.getWorld().addWeatherEffect(parEnt);
		/**
		 * TODO: 1.14 fix lightning
		 * manager.getWorld().weatherEffects.add(parEnt);
		 * 		((WeatherManagerServer)manager).syncLightningNew(parEnt, custom);
		 */
	}
	
	@Override
	public int getUpdateRateForNetwork() {
		if (levelCurIntensityStage >= StormObject.STATE_HIGHWIND) {
			return 2;
		} else {
			return super.getUpdateRateForNetwork();
		}
	}

	public Vec3 getPosTop() {
		if (isTornadoFormingOrGreater()) {
			return tornadoFunnelSimple.getPosTop();
		}
		return pos;
	}

	public void setupForcedTornado(Entity entity) {
		this.layer = 0;
		if (entity != null) {
			this.spawnerUUID = entity.getUUID().toString();
			this.naturallySpawned = false;
			this.levelTemperature = 0.1F;
			this.pos = entity.position();
			this.levelWater = this.levelWaterStartRaining * 2;
			this.attrib_precipitation = true;
			this.levelCurIntensityStage = this.STATE_STAGE1;
			this.alwaysProgresses = true;

			this.initFirstTime();

			//lock it to current stage or less
			this.levelStormIntensityMax = this.levelCurIntensityStage;
		}
	}

	public void setupPlayerControlledTornado(Entity entity) {
		this.playerControlled = true;
	}

	public Player getPlayer() {
		if (spawnerUUID.equals("")) return null;
		return manager.getWorld().getPlayerByUUID(UUID.fromString(spawnerUUID));
	}

	public boolean isPlayerControlled() {
		return playerControlled;
	}

	public void setPlayerControlled(boolean playerControlled) {
		this.playerControlled = playerControlled;
	}

	public int getPlayerControlledTimeLeft() {
		return playerControlledTimeLeft;
	}

	public void setPlayerControlledTimeLeft(int playerControlledTimeLeft) {
		this.playerControlledTimeLeft = playerControlledTimeLeft;
	}

	public boolean isBaby() {
		return baby;
	}

	public void setBaby(boolean baby) {
		this.baby = baby;
	}
}
