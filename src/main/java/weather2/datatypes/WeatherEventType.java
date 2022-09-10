package weather2.datatypes;

public enum WeatherEventType {
	HEAVY_RAIN("heavy_rain"),
	ACID_RAIN("acid_rain"),
	HAIL("hail"),
	HEATWAVE("heatwave"),
	SANDSTORM("sandstorm"),
	SNOWSTORM("snowstorm");

	private final String key;

	public static final WeatherEventType[] VALUES = values();

	WeatherEventType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
