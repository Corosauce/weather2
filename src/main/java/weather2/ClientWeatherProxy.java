package weather2;

import weather2.datatypes.PrecipitationType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.client.SceneEnhancer;
import weather2.ltcompat.ClientWeatherIntegration;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

public final class ClientWeatherProxy {
	private static ClientWeatherProxy instance = new ClientWeatherProxy();

	private ClientWeatherProxy() {
	}

	public static ClientWeatherProxy get() {
		return instance;
	}

	public static void reset() {
		instance = new ClientWeatherProxy();
	}

	public float getRainAmount() {
		if (isWeatherEffectsServerSideControlled()) {
			return ClientWeatherIntegration.get().getRainAmount();
		} else {
			return ClientWeatherHelper.get().getRainStrengthAndControlVisuals(Minecraft.getInstance().player);
		}
	}

	public float getVanillaRainAmount() {
		return ClientWeatherIntegration.get().getVanillaRainAmount();
	}

	public PrecipitationType getPrecipitationType() {
		return ClientWeatherIntegration.get().getPrecipitationType();
	}

	public float getWindSpeed() {
		return ClientWeatherIntegration.get().getWindSpeed();
	}

	public boolean isHeatwave() {
		return ClientWeatherIntegration.get().isHeatwave();
	}

	public boolean isSandstorm() {
		if (isWeatherEffectsServerSideControlled()) {
			return ClientWeatherIntegration.get().isSandstorm();
		} else {
			Minecraft client = Minecraft.getInstance();
			Player player = client.player;
			if (player == null) return false;
			Vec3 posPlayer = new Vec3(client.player.getX(), 0, client.player.getZ());
			WeatherObjectSandstorm sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(posPlayer);
			return sandstorm != null;
		}
	}

	public boolean isSnowstorm() {
		return ClientWeatherIntegration.get().isSnowstorm();
	}

	public boolean hasWeather() {
		if (SceneEnhancer.FORCE_ON_DEBUG_TESTING) return true;
		return ClientWeatherIntegration.get().hasWeather();
	}

	public boolean isWeatherEffectsServerSideControlled() {
		return Weather.isLoveTropicsInstalled();
	}
}
