package weather2.weathersystem.wind;

import CoroUtil.util.CoroUtilEntOrParticle;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import weather2.ClientWeather;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManager;

import java.util.Random;

public class WindManager {
	public WeatherManager manager;
	
	//global
	public float windAngleGlobal = 0;
	public float windSpeedGlobal = 0;

	//gusts
	public float windAngleGust = 0;
	public float windSpeedGust = 0;
	public int windTimeGust = 0;
	public int windGustEventTimeRand = 60;
	public float chanceOfWindGustEvent = 0.5F;

	public static boolean FORCE_ON_DEBUG_TESTING = false;
	
	public WindManager(WeatherManager parManager) {
		manager = parManager;
		
		Random rand = new Random();
		
		windAngleGlobal = rand.nextInt(360);
	}
	
	public float getWindSpeed() {
		if (windTimeGust > 0) {
			return windSpeedGust;
		} else {
			return windSpeedGlobal;
		}
	}

	public float getWindSpeedForGusts() {
		return windSpeedGust;
	}

	public float getWindSpeedForClouds() {
		return windSpeedGlobal;
	}

	public float getWindAngle() {
		if (windTimeGust > 0) {
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

	public void tick(World world) {
		Random rand = new Random();

		// TODO: better merge this logic
		if (world.isRemote) {
			windSpeedGlobal = ClientWeather.get().getWindSpeed();
			if (windSpeedGlobal == 0) {
				chanceOfWindGustEvent = 0;
			} else {
				chanceOfWindGustEvent = 0.5F;
			}
		} else {
			WeatherController weatherController = WeatherControllerManager.forWorld((ServerWorld) world);
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
		if (!world.isRemote) {
			//windAngleGlobal += 0.25F;
			//chanceOfWindGustEvent = 0;
		}

		if (windAngleGlobal < -180) {
			windAngleGlobal += 360;
		}

		if (windAngleGlobal > 180) {
			windAngleGlobal -= 360;
		}
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
	public void applyWindForceNew(Object ent, float multiplier, float maxSpeed) {
		Vector3d motion = applyWindForceImpl(new Vector3d(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)),
				WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);
		
		CoroUtilEntOrParticle.setMotionX(ent, motion.x);
    	CoroUtilEntOrParticle.setMotionZ(ent, motion.z);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 */
	public Vector3d applyWindForceImpl(Vector3d motion, float weight, float multiplier, float maxSpeed) {
		float windSpeed = getWindSpeed();
    	float windAngle = getWindAngle();

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
    	Vector3d newMotion = motion;
    	
    	double speedCheck = (Math.abs(vecX) + Math.abs(vecZ)) / 2D;
        if (speedCheck < maxSpeed) {
        	newMotion = new Vector3d(objX - vecX, motion.y, objZ - vecZ);
        } else {
        	float speedDampen = (float)(maxSpeed / speedCheck);
			newMotion = new Vector3d(objX - vecX*speedDampen, motion.y, objZ - vecZ*speedDampen);
		}
        
        return newMotion;
	}

	public CompoundNBT nbtSyncForClient() {
		CompoundNBT data = new CompoundNBT();

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

	public void nbtSyncFromServer(CompoundNBT parNBT) {

		windSpeedGlobal = parNBT.getFloat("windSpeedGlobal");
		windAngleGlobal = parNBT.getFloat("windAngleGlobal");
		windSpeedGust = parNBT.getFloat("windSpeedGust");
		windAngleGust = parNBT.getFloat("windAngleGust");

		/*windSpeedEvent = parNBT.getFloat("windSpeedEvent");
		windAngleEvent = parNBT.getFloat("windAngleEvent");
		windTimeEvent = parNBT.getInt("windTimeEvent");*/

		windTimeGust = parNBT.getInt("windTimeGust");
	}

	public Vector3d getWindForce() {
		float windSpeed = this.getWindSpeed();
		float windAngle = this.getWindAngle();
		float windX = (float) -Math.sin(Math.toRadians(windAngle)) * windSpeed;
		float windZ = (float) Math.cos(Math.toRadians(windAngle)) * windSpeed;
		return new Vector3d(windX, 0, windZ);
	}
}
