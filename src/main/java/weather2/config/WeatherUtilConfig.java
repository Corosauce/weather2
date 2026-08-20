package weather2.config;

import java.util.*;

public class WeatherUtilConfig {

	public static Set<String> listDimensionsWeather = new HashSet<>();
	public static Set<String> listDimensionsClouds = new HashSet<>();
	//used for deadly storms and sandstorms
	public static Set<String> listDimensionsStorms = new HashSet<>();
	public static Set<String> listDimensionsWindEffects = new HashSet<>();

	public static boolean shouldTickClouds(String levelResourceKey) {
		return listDimensionsClouds.contains(levelResourceKey);
	}

	public static void processLists() {
		listDimensionsWeather = parseSet(ConfigMisc.Dimension_List_Weather);
		listDimensionsClouds = parseSet(ConfigMisc.Dimension_List_Clouds);
		listDimensionsStorms = parseSet(ConfigMisc.Dimension_List_Storms);
		listDimensionsWindEffects = parseSet(ConfigMisc.Dimension_List_WindEffects);
	}

	public static Set<String> parseSet(String parData) {
		Set<String> result = new HashSet<>();

		for (String entry : parData.replace(',', ' ').split("\\s+")) {
			if (!entry.isEmpty()) {
				result.add(entry);
			}
		}

		return result;
	}
	
}
