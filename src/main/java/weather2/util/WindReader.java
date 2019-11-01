package weather2.util;

import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerBase;
import CoroUtil.util.Vec3;

public class WindReader {
	
	/*
	 * 
	 * not exactly a proper api class as it depends on weather2 imports, IMC method to come in future
	 * 
	 * 2 wind layers (in order of DOMINANT priority):
	 * 
	 * 1: event wind:
	 * 1a: storm event, pulling wind into tornado
	 * 1b: wind gusts
	 * 2: high level wind that clouds use
	 * 
	 * EnumTypes explained:
	 * DOMINANT: The priority taking wind data for the location
	 * CLOUD: Wind data used for clouds / high level things
	 * EVENT: Wind data used for storm events
	 * GUST: Wind data used for wind gusts
	 * 
	 * WindType.EVENT is client side only, due to wind technically being a global thing on server side, it was required to make events easily location based for player 
	 */
	
	public enum WindType {
		PRIORITY,
		EVENT,
		GUST,
		CLOUD
	}

	public static float getWindAngle(World parWorld, Vec3 parLocation) {
		return getWindAngle(parWorld, parLocation, WindType.PRIORITY);
	}
	
	public static float getWindAngle(World parWorld, Vec3 parLocation, WindType parWindType) {
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
	
	public static float getWindSpeed(World parWorld, Vec3 parLocation) {
		return getWindSpeed(parWorld, parLocation, WindType.PRIORITY);
	}
	
	public static float getWindSpeed(World parWorld, Vec3 parLocation, WindType parWindType) {
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
