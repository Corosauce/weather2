package weather2.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WeatherUtilConfig {

	public static List<ResourceKey<Level>> listDimensionsWeather = new ArrayList<>();
	public static List<ResourceKey<Level>> listDimensionsClouds = new ArrayList<>();
	//used for deadly storms and sandstorms
	public static List<ResourceKey<Level>> listDimensionsStorms = new ArrayList<>();
	public static List<ResourceKey<Level>> listDimensionsWindEffects = new ArrayList<>();

	public static boolean shouldTickClouds(ResourceKey<Level> levelResourceKey) {
		return true;
		//return listDimensionsClouds.contains(levelResourceKey);
	}
	
}
