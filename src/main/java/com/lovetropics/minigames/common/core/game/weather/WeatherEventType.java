package com.lovetropics.minigames.common.core.game.weather;

public enum WeatherEventType {
	HEAVY_RAIN("heavy_rain"),
	ACID_RAIN("acid_rain"),
	HAIL("hail"),
	HEATWAVE("heatwave"),
	SANDSTORM("sandstorm"),
	SNOWSTORM("snowstorm");

	//public static final Codec<WeatherEventType> CODEC = MoreCodecs.stringVariants(values(), WeatherEventType::getKey);

	private final String key;

	WeatherEventType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
