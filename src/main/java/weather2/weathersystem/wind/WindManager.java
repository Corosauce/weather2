package weather2.weathersystem.wind;

import com.corosus.coroutil.util.CoroUtilEntOrParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerWeatherProxy;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.config.ConfigWind;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

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
	public int windTimeEvent = 0; //its assumed this will get set by whatever initializes an event, and this class counts it down from a couple seconds, helps wind system know what takes priority

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
	
	public WindManager(WeatherManager parManager) {
		manager = parManager;

		Random rand = new Random();
		
		windAngleGlobal = rand.nextInt(360);
	}
	
	public float getWindSpeed() {
		if (windTimeEvent > 0) {
			return windSpeedEvent;
		} else if (windTimeGust > 0) {
			return windSpeedGust;
		} else {
			return windSpeedGlobal;
		}
	}

	public void startHighWindEvent() {
		highWindTimer = ConfigWind.highWindTimerEnableAmountBase + RandomSource.create().nextInt(ConfigWind.highWindTimerEnableAmountRnd);
	}

	public boolean isHighWindEventActive() {
		return highWindTimer > 0;
	}

	public void stopHighWindEvent() {
		highWindTimer = 0;
	}

	public void startLowWindEvent() {
		lowWindTimer = ConfigWind.lowWindTimerEnableAmountBase + RandomSource.create().nextInt(ConfigWind.lowWindTimerEnableAmountRnd);
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

		Random rand = new Random();

		//debug
		//Weather.dbg("wind angle: " + windAngleGlobal);
		//windAngleGlobal = 90;
		//TODO: TEMP!!!
		//indSpeedGlobal = 0.71F;
		//windAngleGlobal = 180;
		//lowWindOddsTo1 = 20*200;
		//lowWindTimer = 0;
		//windSpeedGlobalChangeRate = 0.05F;

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

							setWindTimeGust(rand.nextInt(windGustEventTimeRand));
							//windEventTime += windTime;
							//unneeded since priority system determines wind to use
							//directionBeforeGust = windAngleGlobal;
						}
					}
				}

				//global wind angle
				//windAngleGlobal += (RandomSource.create().nextInt(5) - 2) * 0.2F;
				windAngleGlobal += (rand.nextFloat() * ConfigWind.globalWindChangeAmountRate) - (rand.nextFloat() * ConfigWind.globalWindChangeAmountRate);

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
		}

		//event data
		if (entP != null) {
			if (manager.getWorld().getGameTime() % 10 == 0) {
				StormObject so = manager.getClosestStorm(new Vec3(entP.getX(), StormObject.layers.get(0), entP.getZ()), 256, StormObject.STATE_HIGHWIND);

				if (so != null) {

					windOriginEvent = new BlockPos(so.posGround.x, so.posGround.y, so.posGround.z);

					setWindTimeEvent(80);

					//double stormDist = entP.getDistanceSq(so.posGround.x, so.posGround.y, so.posGround.z);

					//player pos aiming at storm
					double var11 = so.posGround.x - entP.getX();
					double var15 = so.posGround.z - entP.getZ();
					float yaw = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;

					windAngleEvent = yaw;
					windSpeedEvent = 2F; //make dynamic?

					//Weather.dbg("!!!!!!!!!!!!!!!!!!!storm event near: " + stormDist);
				}
			}
		}
	}

	/*public void tick(Level world) {
		Random rand = new Random();

		FORCE_ON_DEBUG_TESTING = true;

		// TODO: better merge this logic
		if (world.isClientSide) {
			windSpeedGlobal = ClientWeather.get().getWindSpeed();
			if (windSpeedGlobal == 0) {
				chanceOfWindGustEvent = 0;
			} else {
				chanceOfWindGustEvent = 0.5F;
			}
		} else {
			WeatherController weatherController = WeatherControllerManager.forWorld((ServerLevel) world);
			windSpeedGlobal = weatherController.getWindSpeed();
			if (windSpeedGlobal == 0) {
				chanceOfWindGustEvent = 0;
			} else {
				chanceOfWindGustEvent = 0.5F;
			}
		}

		if (windTimeGust > 0) {
			windTimeGust--;
		}

		float randGustWindFactor = 1F;

		//gust data
		if (this.windTimeGust == 0)
		{
			if (chanceOfWindGustEvent > 0F)
			{
				if (rand.nextInt((int)((100 - chanceOfWindGustEvent) * randGustWindFactor)) == 0)
				{
					windSpeedGust = windSpeedGlobal + rand.nextFloat() * 0.6F;
					windAngleGust = windAngleGlobal + rand.nextInt(120) - 60;

					setWindTimeGust(rand.nextInt(windGustEventTimeRand));
				}
			}
		}

		windAngleGlobal += rand.nextFloat() - rand.nextFloat();

		if (FORCE_ON_DEBUG_TESTING) {
			//windAngleGlobal += 1;
			windAngleGlobal = 0;
			windSpeedGlobal = 0.8F;
			chanceOfWindGustEvent = 0;
			chanceOfWindGustEvent = 0.5F;
		}

		//MORE TEST
		if (!world.isClientSide) {
			//windAngleGlobal += 0.25F;
			//chanceOfWindGustEvent = 0;
		}

		if (windAngleGlobal < -180) {
			windAngleGlobal += 360;
		}

		if (windAngleGlobal > 180) {
			windAngleGlobal -= 360;
		}
	}*/

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
	public void applyWindForceNew(Object ent, float multiplier, float maxSpeed) {

		Vec3 pos = new Vec3(CoroUtilEntOrParticle.getPosX(ent), CoroUtilEntOrParticle.getPosY(ent), CoroUtilEntOrParticle.getPosZ(ent));

		Vec3 motion = applyWindForceImpl(pos, new Vec3(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)),
				WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);
		
		CoroUtilEntOrParticle.setMotionX(ent, motion.x);
    	CoroUtilEntOrParticle.setMotionZ(ent, motion.z);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 */
	public Vec3 applyWindForceImpl(Vec3 pos, Vec3 motion, float weight, float multiplier, float maxSpeed) {
		float windSpeed = 0;
		if (pos != null && false) {
			windSpeed = getWindSpeedPerlinNoise(pos);
		} else {
			windSpeed = getWindSpeed();
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

	public Vec3 getWindForce() {
		float windSpeed = this.getWindSpeed();
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
		PerlinNoise perlinNoise = ClientTickHandler.weatherManager.cloudManager.getPerlinNoise();
		/*int indexX = index % xWide;
		int indexZ = index / xWide;*/
		int indexX = (int)pos.x;
		int indexZ = (int)pos.z;
		double scale = 5;
		long time = Minecraft.getInstance().level.getGameTime() * 1;
		double posYAdj = 0;
		double noiseVal = perlinNoise.getValue(((indexX) * scale) + time, ((indexZ) * scale) + time, posYAdj) + 0.2F;
		return (float) noiseVal * 2F;
	}
}
