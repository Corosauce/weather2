package weather2.ltcompat;

import weather2.datatypes.PrecipitationType;

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
		return 0;
	}

	public float getVanillaRainAmount() {
		return 0;
	}

	public PrecipitationType getPrecipitationType() {
		return PrecipitationType.VALUES[0];
	}

	public float getWindSpeed() {
		return 0;
	}

	public boolean isHeatwave() {
		return false;
	}

	public boolean isSandstorm() {
		return false;
	}

	public boolean isSnowstorm() {
		return false;
	}

	public boolean hasWeather() {
		return false;
	}

	/**
	 * TODO: for LT, turn back on when LT is needed, activates dependency on LTWeather
	 */
	/*public float getRainAmount() {
		return ClientWeather.get().getRainAmount();
	}

	public float getVanillaRainAmount() {
		return ClientWeather.get().getVanillaRainAmount();
	}

	public PrecipitationType getPrecipitationType() {
		return PrecipitationType.VALUES[TypeBridge.getPrecipitationTypeOrdinal(ClientWeather.get())];
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
	}*/
}
