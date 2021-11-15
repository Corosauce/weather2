package weather2.util;

import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManager;

public class WindReader {
	public static float getWindAngle(World world) {
		WeatherManager weather = getWeatherManagerFor(world);
		return weather != null ? weather.wind.getWindAngle() : 0;
	}
	
	public static float getWindSpeed(World world) {
		WeatherManager weather = getWeatherManagerFor(world);
		return weather != null ? weather.wind.getWindSpeed() : 0;
	}

	private static WeatherManager getWeatherManagerFor(World world) {
		if (world.isRemote) {
			return getWeatherManagerClient();
		} else {
			return ServerTickHandler.getWeatherManagerFor((world.getDimensionKey()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static WeatherManager getWeatherManagerClient() {
		return ClientTickHandler.weatherManager;
	}
}
