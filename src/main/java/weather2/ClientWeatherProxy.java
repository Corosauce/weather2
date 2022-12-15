package weather2;

import net.minecraft.world.level.biome.Biome;
import weather2.datatypes.PrecipitationType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.client.SceneEnhancer;
import weather2.ltcompat.ClientWeatherIntegration;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

import javax.annotation.Nullable;

public final class ClientWeatherProxy {
	private static ClientWeatherProxy instance;

	private static boolean cacheIsSnowstorm = false;
	private static boolean cacheIsSandstorm = false;
	private static boolean cacheIsHail = false;
	private static int cacheRate = 40;

	private ClientWeatherProxy() {
	}

	public static ClientWeatherProxy get() {
		if (instance == null) {
			instance = new ClientWeatherProxy();
		}
		return instance;
	}

	public void reset() {
		cacheIsSnowstorm = false;
		cacheIsSandstorm = false;
		cacheIsHail = false;
	}

	public float getRainAmount() {
		if (isWeatherEffectsServerSideControlled()) {
			return ClientWeatherIntegration.get().getRainAmount();
		} else {
			return ClientWeatherHelper.get().getPrecipitationStrength(Minecraft.getInstance().player);
		}
	}

	public float getVanillaRainAmount() {
		if (Weather.isLoveTropicsInstalled()) {
			return ClientWeatherIntegration.get().getVanillaRainAmount();
		} else {
			return ClientWeatherHelper.get().getPrecipitationStrength(Minecraft.getInstance().player);
		}
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
			if (player.level.getGameTime() % cacheRate == 0) {
				Vec3 posPlayer = new Vec3(client.player.getX(), 0, client.player.getZ());
				WeatherObjectParticleStorm storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(posPlayer, WeatherObjectParticleStorm.StormType.SANDSTORM);
				cacheIsSandstorm = storm != null && posPlayer.distanceTo(storm.pos) < storm.getSize();
			}
			return cacheIsSandstorm;
		}
	}

	public boolean isSnowstorm() {
		//return ClientWeatherIntegration.get().isSnowstorm();
		if (isWeatherEffectsServerSideControlled()) {
			return ClientWeatherIntegration.get().isSnowstorm();
		} else {
			Minecraft client = Minecraft.getInstance();
			Player player = client.player;
			if (player == null) return false;
			if (player.level.getGameTime() % cacheRate == 0) {
				Vec3 posPlayer = new Vec3(client.player.getX(), 0, client.player.getZ());
				WeatherObjectParticleStorm storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(posPlayer, WeatherObjectParticleStorm.StormType.SNOWSTORM);
				cacheIsSnowstorm = storm != null && posPlayer.distanceTo(storm.pos) < storm.getSize();
			}
			return cacheIsSnowstorm;
		}
	}

	public boolean isHail() {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null) return false;
		if (player.level.getGameTime() % cacheRate == 0) {
			Vec3 posPlayer = new Vec3(client.player.getX(), 0, client.player.getZ());
			double maxStormDist = 512 / 4 * 3;
			StormObject storm = ClientTickHandler.weatherManager.getClosestStorm(posPlayer, maxStormDist, StormObject.STATE_HAIL, StormObject.STATE_HAIL, false);
			cacheIsHail = storm != null && posPlayer.distanceTo(storm.posGround) < storm.getSize();
		}
		return cacheIsHail;
	}

	public boolean hasWeather() {
		if (SceneEnhancer.FORCE_ON_DEBUG_TESTING) return true;
		return ClientWeatherIntegration.get().hasWeather();
	}

	public boolean isWeatherEffectsServerSideControlled() {
		return Weather.isLoveTropicsInstalled();
	}
}
