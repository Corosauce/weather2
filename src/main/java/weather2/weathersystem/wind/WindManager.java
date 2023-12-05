package weather2.weathersystem.wind;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilEntOrParticle;
import com.corosus.coroutil.util.CoroUtilMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.PerlinNoiseHelper;
import weather2.ServerWeatherProxy;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.config.ConfigWind;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Random;

public class WindManager {
	public WeatherManager manager;

	//global
	public float windAngleGlobal = 0;
	public float windSpeedGlobal = 0;
	public float windSpeedGlobalChangeRate = 0.05F;
	public int windSpeedGlobalRandChangeTimer = 0;
	public int windSpeedGlobalRandChangeDelay = 10;

	//generic?
	/*public float windSpeedMin = 0.00001F;
	public float windSpeedMax = 1F;*/

	//events - design derp, we're making this client side, so its set based on closest storm to the client side player
	public float windAngleEvent = 0;
	public BlockPos windOriginEvent = BlockPos.ZERO;
	public float windSpeedEvent = 0;
	//client side only
	//its assumed this will get set by whatever initializes an event, and this class counts it down from a couple seconds, helps wind system know what takes priority
	public int windTimeEvent = 0;

	//gusts
	public float windAngleGust = 0;
	public float windSpeedGust = 0;
	public int windTimeGust = 0;
	//public float directionGust = 0;
	//public float directionBeforeGust = 0;
	public int windGustEventTimeRand = 60;
	public float chanceOfWindGustEvent = 0.5F;

	//low wind event
	public int lowWindTimer = 0;

	//high wind event
	public int highWindTimer = 0;

	public static boolean FORCE_ON_DEBUG_TESTING = false;

	public HashMap<Long, WindInfoCache> lookupChunkToWindInfo = new HashMap<>();
	//this one specifically hashes with a y value, so different vertical heights within the same chunk can have different results still
	public HashMap<Long, WindInfoCache> lookupChunkWithHeightToWindInfo = new HashMap<>();
	public int cachedWindInfoUpdateFrequency = 100;
	public int cachedChunkHeightUpdateFrequency = 20*60*5;

	//used by client particles, and off thread work
	public float cachedWindSpeedClient = 0;

	public WindManager(WeatherManager parManager) {
		manager = parManager;
		
		Random rand = new Random();
		
		windAngleGlobal = rand.nextInt(360);
	}

	public float getWindSpeed() {
		return getWindSpeed(null, 1);
	}

	public float getWindSpeed(@Nullable BlockPos pos) {
		return getWindSpeed(pos, 1);
	}
	
	public float getWindSpeed(@Nullable BlockPos pos, float extraHeightAmpMax) {
		if (pos != null) {
			return getWindSpeedPositional(pos, extraHeightAmpMax);
		}
		if (windTimeEvent > 0 && (windSpeedEvent > windSpeedGust && windSpeedEvent > windSpeedGlobal)) {
			return windSpeedEvent;
		} else if (windTimeGust > 0) {
			return windSpeedGust;
		} else {
			return windSpeedGlobal;
		}
	}

	public float getWindSpeedPositional(BlockPos pos) {
		return getWindSpeedPositional(pos, 1);
	}

	public float getWindSpeedPositional(BlockPos pos, float extraHeightAmpMax) {
		return getWindSpeedPositional(pos, extraHeightAmpMax, true);
	}

	/**
	 * Uses various caches to factor in event wind speed (server side only), height based wind speed amplifier (cached to 16x16x16 areas)
	 *
	 * @param pos
	 * @param extraHeightAmpMax
	 * @return
	 */
	//TODO: design flaw: use of extraHeightAmpMax gets cached into WindInfoCache, will mess with results if 2 sources use 2 different extraHeightAmpMax
	//workaround for now is that on server side, only turbine is using this cache, need to make the extraHeightAmpMax calculation outside the cache, by fixing the other hacks below
	public float getWindSpeedPositional(BlockPos pos, float extraHeightAmpMax, boolean useClientCache) {
		if (manager.getWorld().isClientSide() && useClientCache) {
			return cachedWindSpeedClient;
		}
		this.manager.getWorld().getProfiler().push("weather2_wind_calculation");
		boolean eventFastest = false;
		float lastWindSpeed;
		//dont waste cpu on client using the cache system when we only need the info around the player, especially given all the particles using it
		if (manager.getWorld().isClientSide()) {
			lastWindSpeed = windTimeEvent > 0 ? windSpeedEvent : 0;
		} else {
			lastWindSpeed = getCachedWindSpeedEventForChunkPos(pos);
		}
		if (lastWindSpeed > windSpeedGlobal && lastWindSpeed > windSpeedGlobal) eventFastest = true;
		lastWindSpeed = Math.max(lastWindSpeed, windSpeedGlobal);
		if (windTimeGust > 0) lastWindSpeed = Math.max(lastWindSpeed, windSpeedGust);
		int averageHeight = getCachedAverageChunkHeightAround(pos);
		float windSpeedHeightAmp = getWindSpeedAmplifierForHeight(pos.getY(), averageHeight, extraHeightAmpMax);
		lastWindSpeed *= windSpeedHeightAmp;
		//give a constant speed buff if high enough
		if (windSpeedHeightAmp > 1.3F) {
			lastWindSpeed += (windSpeedHeightAmp-1F) * 1F;
		}
		//TODO: remove the need for this hack, and eventFastest
		//ok so i wanted the cap for turbines to be at 3, and everything else either 1 or 1.5 as shown above, this hacky if statement and event check will have to do for now
		float cap = 1F;
		if (eventFastest) {
			cap = 2F;
		}
		if (extraHeightAmpMax >= 2) {
			cap = extraHeightAmpMax + 1;
		}
		this.manager.getWorld().getProfiler().pop();
		return Math.min(cap, lastWindSpeed);
	}

	public void startHighWindEvent() {
		highWindTimer = ConfigWind.highWindTimerEnableAmountBase + (new Random()).nextInt(ConfigWind.highWindTimerEnableAmountRnd);
	}

	public boolean isHighWindEventActive() {
		return highWindTimer > 0;
	}

	public void stopHighWindEvent() {
		highWindTimer = 0;
	}

	public void startLowWindEvent() {
		lowWindTimer = ConfigWind.lowWindTimerEnableAmountBase + (new Random()).nextInt(ConfigWind.lowWindTimerEnableAmountRnd);
	}

	public void stopLowWindEvent() {
		lowWindTimer = 0;
	}

	public float getWindSpeedForGusts() {
		return windSpeedGust;
	}

	public float getWindSpeedForClouds() {
		return windSpeedGlobal;
	}

	public float getWindAngle(Vec3 pos) {
		if (windTimeEvent > 0) {
			return getWindAngleForEvents(pos);
		} else if (windTimeGust > 0) {
			return windAngleGust;
		} else {
			return windAngleGlobal;
		}
	}

	/**
	 * Returns angle in degrees, 0-360
	 *
	 * @return
	 */
	public float getWindAngleForEvents() {
		return windAngleEvent;
	}

	public float getWindAngleForEvents(Vec3 pos) {
		if (pos != null && !windOriginEvent.equals(BlockPos.ZERO)) {
			double var11 = windOriginEvent.getX() + 0.5D - pos.x;
			double var15 = windOriginEvent.getZ() + 0.5D - pos.z;
			return (-((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI) - 45;
		} else {
			return windAngleEvent;
		}
	}

	/**
	 * Returns angle in degrees, 0-360
	 *
	 * @return
	 */
	public float getWindAngleForGusts() {
		return windAngleGust;
	}

	/**
	 * Returns angle in degrees, 0-360
	 *
	 * @return
	 */
	public float getWindAngleForClouds() {
		return windAngleGlobal;
	}

	public void setWindTimeGust(int time) {
		windTimeGust = time;
	}

	public void setWindTimeEvent(int parVal) {
		windTimeEvent = parVal;
		//syncData(); - might be too often
		//Weather.dbg("Wind event time set: " + parVal);
	}

	public void tick() {

		Random rand = CoroUtilMisc.random();

		//windSpeedGust = 0;

		if (!ConfigWind.Misc_windOn) {
			windSpeedGlobal = 0;
			windSpeedGust = 0;
			windTimeGust = 0;
			//windSpeedSmooth = 0;
		} else {

			if (!manager.getWorld().isClientSide()) {
				//WIND SPEED\\

				//global random wind speed change

				if (!ConfigWind.Wind_LowWindEvents) {
					lowWindTimer = 0;
				}

				if (lowWindTimer <= 0) {
					if (windSpeedGlobalRandChangeTimer-- <= 0)
					{
						//standard wind adjustment
						if (highWindTimer <= 0) {
							windSpeedGlobal += (rand.nextDouble() * windSpeedGlobalChangeRate) - (windSpeedGlobalChangeRate / 2);
							//only increase for high wind
						} else {
							windSpeedGlobal += (rand.nextDouble() * windSpeedGlobalChangeRate)/* - (windSpeedGlobalChangeRate / 2)*/;
						}
						windSpeedGlobalRandChangeTimer = windSpeedGlobalRandChangeDelay;
					}

					//only allow for low wind if high wind not active
					if (highWindTimer <= 0) {
						if (ConfigWind.Wind_LowWindEvents) {
							if (rand.nextInt(ConfigWind.lowWindOddsTo1) == 0) {
								startLowWindEvent();
								Weather.dbg("low wind event started, for ticks: " + lowWindTimer);
							}
						}
					} else {
						//fix edge case where if a high wind event is manually started, low wind could still be trying to take control
						stopLowWindEvent();
					}

					if (ConfigWind.Wind_HighWindEvents && highWindTimer <= 0) {
						if (rand.nextInt(ConfigWind.highWindOddsTo1) == 0) {
							startHighWindEvent();
							Weather.dbg("high wind event started, for ticks: " + highWindTimer);
						}
					}
				} else {
					lowWindTimer--;
					if (lowWindTimer <= 0) {
						Weather.dbg("low wind event ended");
					}
					windSpeedGlobal -= 0.01F;
				}

				if (highWindTimer > 0) {
					highWindTimer--;
					if (highWindTimer <= 0) {
						Weather.dbg("high wind event ended");
					}
				}

				//enforce mins and maxs of wind speed
				if (windSpeedGlobal < ConfigWind.windSpeedMin)
				{
					windSpeedGlobal = (float)ConfigWind.windSpeedMin;
				}

				if (windSpeedGlobal > ConfigWind.windSpeedMax)
				{
					windSpeedGlobal = (float)ConfigWind.windSpeedMax;
				}

				if (windTimeGust > 0) {
					windTimeGust--;

					if (windTimeGust == 0) {
						windSpeedGust = 0;
						syncData();
					}
				}

				if (ConfigMisc.overcastMode && manager.getWorld().isRaining()) {
					if (windSpeedGlobal < ConfigWind.windSpeedMinGlobalOvercastRaining) {
						windSpeedGlobal = (float) ConfigWind.windSpeedMinGlobalOvercastRaining;
					}
				}

				float speedOverride = ServerWeatherProxy.getWindSpeed((ServerLevel) manager.getWorld());
				if (speedOverride != -1) {
					windSpeedGlobal = speedOverride;
				}

				//smooth use
				/*if (windSpeed > windSpeedSmooth)
	            {
					windSpeedSmooth += 0.01F;
	            }
	            else if (windSpeed < windSpeedSmooth)
	            {
	            	windSpeedSmooth -= 0.01F;
	            }

	            if (windSpeedSmooth < 0)
	            {
	            	windSpeedSmooth = 0F;
	            }*/

				//WIND SPEED //

				//WIND ANGLE\\

				//windGustEventTimeRand = 100;

				float randGustWindFactor = 1F;

				//gust data
				if (this.windTimeGust == 0 && lowWindTimer <= 0/* && highWindTimer <= 0*/)
				{
					if (chanceOfWindGustEvent > 0F)
					{
						if (rand.nextInt((int)((100 - chanceOfWindGustEvent) * randGustWindFactor)) == 0)
						{
							windSpeedGust = windSpeedGlobal + rand.nextFloat() * 0.6F;
							boolean randomDirectionGust = false;
							if (randomDirectionGust) {
								windAngleGust = rand.nextInt(360) - 180;
							} else {
								windAngleGust = windAngleGlobal + rand.nextInt(120) - 60;
							}

							setWindTimeGust(rand.nextInt(windGustEventTimeRand * 3));
							//windEventTime += windTime;
							//unneeded since priority system determines wind to use
							//directionBeforeGust = windAngleGlobal;
						}
					}
				}

				//global wind angle
				//windAngleGlobal += ((new Random()).nextInt(5) - 2) * 0.2F;
				windAngleGlobal += (rand.nextFloat() * ConfigWind.globalWindAngleChangeAmountRate) - (rand.nextFloat() * ConfigWind.globalWindAngleChangeAmountRate);

				//windAngleGlobal += 0.1;

				//windAngleGlobal = 0;

				if (windAngleGlobal < -180)
				{
					windAngleGlobal += 360;
				}

				if (windAngleGlobal > 180)
				{
					windAngleGlobal -= 360;
				}

				//WIND ANGLE //
			} else {

				tickClient();
			}
		}

		/*windSpeedGlobal = 0.9F;
		windAngleGlobal = 270;*/

	}

	@OnlyIn(Dist.CLIENT)
	public void tickClient() {
		Player entP = Minecraft.getInstance().player;

		if (windTimeEvent > 0) {
			windTimeEvent--;
			if (windTimeEvent == 0) {
				windTimeGust = 0;
			}
		}

		//event data
		if (entP != null) {

			if (entP != null && entP.level().getGameTime() % 5 == 0) {
				cachedWindSpeedClient = getWindSpeedPositional(entP.blockPosition(), 1, false);
			}

			if (manager.getWorld().getGameTime() % 20 == 0) {
				float maxDist = 512;
				StormObject so = manager.getClosestStorm(new Vec3(entP.getX(), StormObject.layers.get(0), entP.getZ()), maxDist, StormObject.STATE_HIGHWIND);

				if (so != null) {

					windOriginEvent = CoroUtilBlock.blockPos(so.posGround.x, so.posGround.y, so.posGround.z);

					setWindTimeEvent(80);

					//player pos aiming at storm
					double var11 = so.posGround.x - entP.getX();
					double var15 = so.posGround.z - entP.getZ();
					float yaw = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;

					windAngleEvent = yaw;
					double dist = entP.position().distanceTo(so.posGround);
					windSpeedEvent = getEventSpeedFactor(dist, maxDist);
				}
			}
		}
	}

	public float getEventSpeedFactor(double dist, double maxDist) {
		return (float) (1F - (dist / maxDist)) * 2F;
	}

	public WindInfoCache getWindInfoCacheForChunk(BlockPos blockPos, boolean forHeightChunks) {
		//note: the y value hashing here is used when we want data at that height level
		BlockPos chunkPos = new BlockPos(blockPos.getX() >> 4, forHeightChunks ? blockPos.getY() >> 4 : 0, blockPos.getZ() >> 4);

		HashMap<Long, WindInfoCache> lookup = forHeightChunks ? lookupChunkWithHeightToWindInfo : lookupChunkToWindInfo;
		long hash = chunkPos.asLong();
		if (lookup.containsKey(hash)) {
			return lookup.get(hash);
		} else {
			WindInfoCache cache = new WindInfoCache();
			lookup.put(hash, cache);
			return cache;
		}
	}

	public float getCachedWindSpeedForHeight(BlockPos blockPos, float extraHeightAmpMax) {
		WindInfoCache cache = getWindInfoCacheForChunk(blockPos, true);
		if (cache.cacheTimeWindSpeedAtChunkHeight == 0 || cache.cacheTimeWindSpeedAtChunkHeight + cachedWindInfoUpdateFrequency <= manager.getWorld().getGameTime()) {
			cache.cacheTimeWindSpeedAtChunkHeight = manager.getWorld().getGameTime();
			cache.windSpeedAtChunkHeight = getWindSpeed(blockPos, extraHeightAmpMax);
		}
		return cache.windSpeedAtChunkHeight;
	}

	public float getCachedWindSpeedEventForChunkPos(BlockPos blockPos) {
		WindInfoCache cache = getWindInfoCacheForChunk(blockPos, false);
		if (cache.cacheTimeWindSpeedEvent == 0 || cache.cacheTimeWindSpeedEvent + cachedWindInfoUpdateFrequency <= manager.getWorld().getGameTime()) {
			cache.cacheTimeWindSpeedEvent = manager.getWorld().getGameTime();
			cache.windSpeedEvent = calculateWindSpeedEventForPos(blockPos);
		}
		return cache.windSpeedEvent;
	}

	public float calculateWindSpeedEventForPos(BlockPos pos) {
		float maxDist = 512;
		Vec3 posVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
		StormObject so = manager.getClosestStorm(posVec, maxDist, StormObject.STATE_HIGHWIND);
		if (so != null) {
			double dist = posVec.distanceTo(so.posGround);
			return getEventSpeedFactor(dist, maxDist);
		}
		return 0;
	}

	public int getCachedAverageChunkHeightAround(BlockPos blockPos) {
		WindInfoCache cache = getWindInfoCacheForChunk(blockPos, false);
		if (cache.cacheTimeChunkHeight == 0 || cache.cacheTimeChunkHeight + cachedChunkHeightUpdateFrequency <= manager.getWorld().getGameTime()) {
			cache.cacheTimeChunkHeight = manager.getWorld().getGameTime();
			BlockPos chunkPos = new BlockPos(blockPos.getX() >> 4, 0, blockPos.getZ() >> 4);
			cache.averageChunkHeightAround = calculateAverageChunkHeightAround(chunkPos);
		}
		return cache.averageChunkHeightAround;
	}

	/**
	 * Gets heights around the chunk in middle of each chunk checked, stepping out 'squareRadius' times, each check spaced out by 'scanSpacing' chunks
	 *
	 * @param chunkPos
	 * @return
	 */
	public int calculateAverageChunkHeightAround(BlockPos chunkPos) {
		int squareRadius = 2;
		int scanSpacing = 3;
		int count = 0;
		int totalHeight = 0;
		for (int x = -squareRadius; x <= squareRadius; x++) {
			for (int z = -squareRadius; z <= squareRadius; z++) {
				BlockPos pos = new BlockPos((chunkPos.getX() + (x * scanSpacing)) * 16 + 8, 0, (chunkPos.getZ() + (z * scanSpacing)) * 16 + 8);
				int height = manager.getWorld().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
				if (height > -64) {
					totalHeight += height;
					count++;
				}
			}
		}
		if (count == 0) return -64;
		int avgHeight = totalHeight / count;
		//System.out.println("avgHeight: " + avgHeight);
		return avgHeight;
	}

	/**
	 *	Get an amp between average height found in area and max build height
	 *
	 *  averageHeight is the bottom
	 *  getMaxBuildHeight is top
	 *  height is somewhere in between
	 *
	 *  dimensions with smaller range between base height and max build height are easier to benefit from, less height needed
	 *  this is the best option imo considering the alternatives
	 *
	 * @param height
	 * @param averageHeight
	 * @return
	 */
	public float getWindSpeedAmplifierForHeight(int height, int averageHeight, float extraHeightAmpMax) {
		//prevent weird math using negative numbers
		int maxSpeedHeight = manager.getWorld().getHeight();
		if (manager.getWorld().getMinBuildHeight() < 0) {
			int heightAdj = Math.abs(manager.getWorld().getMinBuildHeight());
			height += heightAdj;
			averageHeight += heightAdj;
		}
		int range = maxSpeedHeight - averageHeight;
		height -= averageHeight;
		return 1F + Math.max(0, ((float)height / (float)range) * extraHeightAmpMax);
	}

	public void applyWindForceNew(Object ent, float multiplier, float maxSpeed) {
		applyWindForceNew(ent, multiplier, maxSpeed, true);
	}

	/**
	 * 
	 * To solve the problem of speed going overkill due to bad formulas
	 * 
	 * end goal: make object move at speed of wind
	 * - object has a weight that slows that adjustment
	 * - conservation of momentum
	 * 
	 * calculate force based on wind speed vs objects speed
	 * - use that force to apply to weight of object
	 * - profit
	 */
	public void applyWindForceNew(Object ent, float multiplier, float maxSpeed, boolean dynamicWind) {

		Vec3 pos = new Vec3(CoroUtilEntOrParticle.getPosX(ent), CoroUtilEntOrParticle.getPosY(ent), CoroUtilEntOrParticle.getPosZ(ent));

		Vec3 motion = applyWindForceImpl(pos, new Vec3(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)),
				WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed, dynamicWind);
		
		CoroUtilEntOrParticle.setMotionX(ent, motion.x);
    	CoroUtilEntOrParticle.setMotionZ(ent, motion.z);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 */
	public Vec3 applyWindForceImpl(Vec3 pos, Vec3 motion, float weight, float multiplier, float maxSpeed, boolean dynamicWind) {
		float windSpeed = 0;
		if (pos != null && ConfigWind.Wind_UsePerlinNoise) {
			/*if (windTimeGust > 0) {
				windSpeed = getWindSpeedPerlinNoise(pos);
			} else */{
				windSpeed = (getWindSpeed(dynamicWind ? CoroUtilBlock.blockPos(pos) : null) * 0.5F) + (getWindSpeedPerlinNoise(pos) * 0.5F);
			}
		} else {
			windSpeed = getWindSpeed(dynamicWind ? CoroUtilBlock.blockPos(pos) : null);
		}
    	float windAngle = getWindAngle(pos);

    	float windX = (float) -Math.sin(Math.toRadians(windAngle)) * windSpeed;
    	float windZ = (float) Math.cos(Math.toRadians(windAngle)) * windSpeed;
    	
    	float objX = (float) motion.x;
    	float objZ = (float) motion.z;
		
    	float windWeight = 1F;
    	float objWeight = weight;
    	
    	//divide by zero protection
    	if (objWeight <= 0) {
    		objWeight = 0.001F;
    	}

    	float weightDiff = windWeight / objWeight;
    	
    	float vecX = (objX - windX) * weightDiff;
    	float vecZ = (objZ - windZ) * weightDiff;
    	
    	vecX *= multiplier;
    	vecZ *= multiplier;
    	
    	//copy over existing motion data
    	Vec3 newMotion = motion;
    	
    	double speedCheck = (Math.abs(vecX) + Math.abs(vecZ)) / 2D;
        if (speedCheck < maxSpeed) {
        	newMotion = new Vec3(objX - vecX, motion.y, objZ - vecZ);
        } else {
        	float speedDampen = (float)(maxSpeed / speedCheck);
			newMotion = new Vec3(objX - vecX*speedDampen, motion.y, objZ - vecZ*speedDampen);
		}
        
        return newMotion;
	}

	public CompoundTag nbtSyncForClient() {
		CompoundTag data = new CompoundTag();

		//idea: only sync the wind data client cares about (the active priority wind)

		data.putFloat("windSpeedGlobal", windSpeedGlobal);
		data.putFloat("windAngleGlobal", windAngleGlobal);
		data.putFloat("windSpeedGust", windSpeedGust);
		data.putFloat("windAngleGust", windAngleGust);

		/*data.putFloat("windSpeedEvent", windSpeedEvent);
		data.putFloat("windAngleEvent", windAngleEvent);
		data.putInt("windTimeEvent", windTimeEvent);*/

		data.putInt("windTimeGust", windTimeGust);

		return data;
	}

	public void nbtSyncFromServer(CompoundTag parNBT) {

		windSpeedGlobal = parNBT.getFloat("windSpeedGlobal");
		windAngleGlobal = parNBT.getFloat("windAngleGlobal");
		windSpeedGust = parNBT.getFloat("windSpeedGust");
		windAngleGust = parNBT.getFloat("windAngleGust");

		/*windSpeedEvent = parNBT.getFloat("windSpeedEvent");
		windAngleEvent = parNBT.getFloat("windAngleEvent");
		windTimeEvent = parNBT.getInt("windTimeEvent");*/

		windTimeGust = parNBT.getInt("windTimeGust");
	}

	public Vec3 getWindForce(@Nullable BlockPos pos) {
		float windSpeed = this.getWindSpeed(pos);
		float windAngle = this.getWindAngle(null);
		float windX = (float) -Math.sin(Math.toRadians(windAngle)) * windSpeed;
		float windZ = (float) Math.cos(Math.toRadians(windAngle)) * windSpeed;
		return new Vec3(windX, 0, windZ);
	}

	public void syncData() {
		if (manager instanceof WeatherManagerServer) {
			((WeatherManagerServer) manager).syncWindUpdate(this);
		}
	}

	public void reset() {
		manager = null;
	}

	public void read(CompoundTag data) {
		windSpeedGlobal = data.getFloat("windSpeedGlobal");
		windAngleGlobal = data.getFloat("windAngleGlobal");

		windSpeedGust = data.getFloat("windSpeedGust");
		windAngleGust = data.getFloat("windAngleGust");
		windTimeGust = data.getInt("windTimeGust");

		windSpeedEvent = data.getFloat("windSpeedEvent");
		windAngleEvent = data.getFloat("windAngleEvent");
		windTimeEvent = data.getInt("windTimeEvent");

		lowWindTimer = data.getInt("lowWindTimer");
		highWindTimer = data.getInt("highWindTimer");
	}

	public CompoundTag write(CompoundTag data) {
		data.putFloat("windSpeedGlobal", windSpeedGlobal);
		data.putFloat("windAngleGlobal", windAngleGlobal);

		data.putFloat("windSpeedGust", windSpeedGust);
		data.putFloat("windAngleGust", windAngleGust);
		data.putInt("windTimeGust", windTimeGust);

		data.putFloat("windSpeedEvent", windSpeedEvent);
		data.putFloat("windAngleEvent", windAngleEvent);
		data.putInt("windTimeEvent", windTimeEvent);

		data.putInt("lowWindTimer", lowWindTimer);
		data.putInt("highWindTimer", highWindTimer);

		return data;
	}

	public float getWindSpeedPerlinNoise(Vec3 pos) {
		PerlinNoise perlinNoise = PerlinNoiseHelper.get().getPerlinNoise();
		/*int indexX = index % xWide;
		int indexZ = index / xWide;*/
		int indexX = (int) Math.floor(pos.x);
		int indexZ = (int) Math.floor(pos.z);
		double scale = 10;
		long time = Minecraft.getInstance().level.getGameTime() * 2;
		double posYAdj = 0;
		double noiseVal = perlinNoise.getValue(((indexX) * scale) + time, ((indexZ) * scale) + time, posYAdj)/* + 0.2F*/;
		return (float) Math.max(-1.5F, Math.min(1.5F, noiseVal * 4F));
	}

}
