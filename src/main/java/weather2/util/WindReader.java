package weather2.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManager;

public class WindReader {
	public static float getWindAngle(Level world) {
		return getWindAngle(world,null);
	}

	public static float getWindAngle(Level world, Vec3 pos) {
		WeatherManager weather = getWeatherManagerFor(world);
		return weather != null ? weather.getWindManager().getWindAngle(pos) : 0;
	}
	
	public static float getWindSpeed(Level world) {
		WeatherManager weather = getWeatherManagerFor(world);
		return weather != null ? weather.getWindManager().getWindSpeed() : 0;
	}

	private static WeatherManager getWeatherManagerFor(Level world) {
		if (world.isClientSide) {
			return getWeatherManagerClient();
		} else {
			return ServerTickHandler.getWeatherManagerFor((world.dimension()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static WeatherManager getWeatherManagerClient() {
		return ClientTickHandler.weatherManager;
	}
}
