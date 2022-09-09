package weather2.ltcompat;

import com.lovetropics.minigames.common.core.game.weather.PrecipitationType;
import com.lovetropics.weather.ClientWeather;

public final class ClientWeatherIntegration {
	private static ClientWeatherIntegration instance = new ClientWeatherIntegration();

	private ClientWeatherIntegration() {
	}

	public static ClientWeatherIntegration get() {
		return instance;
	}

	public static void reset() {
		instance = new ClientWeatherIntegration();
	}

	public float getRainAmount() {
		return ClientWeather.get().getRainAmount();
	}

	public float getVanillaRainAmount() {
		return ClientWeather.get().getVanillaRainAmount();
	}

	public PrecipitationType getRainType() {
		return ClientWeather.get().getRainType();
	}

	public float getWindSpeed() {
		return ClientWeather.get().getWindSpeed();
	}

	public boolean isHeatwave() {
		return ClientWeather.get().isHeatwave();
	}

	public boolean isSandstorm() {
		return ClientWeather.get().isSandstorm();
	}

	public boolean isSnowstorm() {
		return ClientWeather.get().isSnowstorm();
	}

	public boolean hasWeather() {
		return ClientWeather.get().hasWeather();
	}
}
