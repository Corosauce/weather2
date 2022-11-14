package weather2;

import net.minecraft.world.level.biome.Biome;
import weather2.datatypes.PrecipitationType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.client.SceneEnhancer;
import weather2.ltcompat.ClientWeatherIntegration;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import javax.annotation.Nullable;

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

	@Nullable
	public PrecipitationType getPrecipitationType(Biome biome) {
		if (Weather.isLoveTropicsInstalled()) {
			return ClientWeatherIntegration.get().getPrecipitationType();
		} else {
			if (biome == null) return null;
			if (biome.getPrecipitation() == Biome.Precipitation.RAIN) return PrecipitationType.NORMAL;
			if (biome.getPrecipitation() == Biome.Precipitation.SNOW) return PrecipitationType.SNOW;
			if (biome.getPrecipitation() == Biome.Precipitation.NONE) return null;
		}
		return null;
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
			return sandstorm != null && posPlayer.distanceTo(sandstorm.pos) < sandstorm.getSize();
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
