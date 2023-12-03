package weather2.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeatherUtilConfig {

	public static List<String> listDimensionsWeather = new ArrayList<>();
	public static List<String> listDimensionsClouds = new ArrayList<>();
	//used for deadly storms and sandstorms
	public static List<String> listDimensionsStorms = new ArrayList<>();
	public static List<String> listDimensionsWindEffects = new ArrayList<>();

	public static boolean shouldTickClouds(String levelResourceKey) {
		return listDimensionsClouds.contains(levelResourceKey);
	}

	public static void processLists() {
		listDimensionsWeather = parseList(ConfigMisc.Dimension_List_Weather);
		listDimensionsClouds = parseList(ConfigMisc.Dimension_List_Clouds);
		listDimensionsStorms = parseList(ConfigMisc.Dimension_List_Storms);
		listDimensionsWindEffects = parseList(ConfigMisc.Dimension_List_WindEffects);
	}

	public static List<String> parseList(String parData) {
		String listStr = parData;
		listStr = listStr.replace(",", " ");
		String[] arrStr = listStr.split(" ");
		for (int i = 0; i < arrStr.length; i++) {
			try {
				arrStr[i] = arrStr[i];
			} catch (Exception ex) {
				arrStr[i] = "minecraft:none"; //set to -999999, hope no dimension id of this exists
			}
		}
		return new ArrayList(Arrays.asList(arrStr));
	}
	
}
