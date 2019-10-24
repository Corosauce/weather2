package weather2.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerBase;

/**
 * Main Helper class for getting wind data via weather2 api
 *
 * position isn't actually used yet, but use it for any future proofing for wind grids, etc
 */
public class WindDataHelper {

	/**
	 * For specifying the type of wind angle or speed on client or server.
	 *
	 * PRIORITY: whatever wind mode has control of things at the moment for the area, the priorities in order is:
	 * - EVENT
	 * - GUST
	 * - CLOUD
	 *
	 * EVENT: extreme weather entities like tornado, hurricane, any future ones
	 *
	 * GUST: short lived wind gusts, primarily for particle effects, but is server side too
	 *
	 * CLOUD: global wind, what clouds in sky always use
	 */
	public enum WindType {
		PRIORITY,
		EVENT,
		GUST,
		CLOUD
	}

	/**
	 * Get the priroty wind angle at a location
	 *
	 * @param parWorld
	 * @param parLocation
	 * @return degree between 0 and 360
	 */
	public static float getWindAngle(World parWorld, BlockPos parLocation) {
		return getWindAngle(parWorld, parLocation, WindType.PRIORITY);
	}

	/**
	 * Get the wind angle at a location
	 *
	 * @param parWorld
	 * @param parLocation
	 * @param parWindType
	 * @return degree between 0 and 360
	 */
	public static float getWindAngle(World parWorld, BlockPos parLocation, WindType parWindType) {
		WeatherManagerBase wMan = null;
		if (parWorld.isRemote) {
			wMan = getWeatherManagerClient();
		} else {
			wMan = ServerTickHandler.lookupDimToWeatherMan.get(parWorld.getDimension().getType().getId());
		}
		
		if (wMan != null) {
			if (parWindType == WindType.PRIORITY) {
				return wMan.windMan.getWindAngleForPriority(null);
			} else if (parWindType == WindType.EVENT) {
				return wMan.windMan.getWindAngleForEvents();
			} else if (parWindType == WindType.GUST) {
				return wMan.windMan.getWindAngleForGusts();
			} else if (parWindType == WindType.CLOUD) {
				return wMan.windMan.getWindAngleForClouds();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Get the priority wind speed at a location
	 *
	 * @param parWorld
	 * @param parLocation
	 * @return usually ranging between 0 and 1, might be over 1 in some extreme weather cases
	 */
	public static float getWindSpeed(World parWorld, BlockPos parLocation) {
		return getWindSpeed(parWorld, parLocation, WindType.PRIORITY);
	}

	/**
	 * Get the wind speed at a location
	 *
	 * @param parWorld
	 * @param parLocation
	 * @param parWindType
	 * @return usually ranging between 0 and 1, might be over 1 in some extreme weather cases
	 */
	public static float getWindSpeed(World parWorld, BlockPos parLocation, WindType parWindType) {
		WeatherManagerBase wMan = null;
		if (parWorld.isRemote) {
			wMan = getWeatherManagerClient();
		} else {
			wMan = ServerTickHandler.lookupDimToWeatherMan.get(parWorld.getDimension().getType().getId());
		}
		
		if (wMan != null) {
			if (parWindType == WindType.PRIORITY) {
				return wMan.windMan.getWindSpeedForPriority();
			} else if (parWindType == WindType.EVENT) {
				return wMan.windMan.getWindSpeedForEvents();
			} else if (parWindType == WindType.GUST) {
				return wMan.windMan.getWindSpeedForGusts();
			} else if (parWindType == WindType.CLOUD) {
				return wMan.windMan.getWindSpeedForClouds();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private static WeatherManagerBase getWeatherManagerClient() {
		return ClientTickHandler.weatherManager;
	}
}
