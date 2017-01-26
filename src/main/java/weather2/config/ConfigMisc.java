package weather2.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import weather2.Weather;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.storm.StormObject;


public class ConfigMisc implements IConfigCategory {
	
	//misc
	public static boolean Misc_proxyRenderOverrideEnabled = true;
	//public static boolean Misc_takeControlOfGlobalRain = true;

	public static int Misc_simBoxRadiusCutoff = 1024;
	public static int Misc_simBoxRadiusSpawn = 1024;
	public static boolean Misc_ForceVanillaCloudsOff = true;
	public static int Misc_AutoDataSaveIntervalInTicks = 20*60*30;
	public static boolean consoleDebug = false;
	
	//Weather
	@ConfigComment("If true, lets server side do vanilla weather rules, weather2 will only make storms when server side says 'rain' is on")
	public static boolean overcastMode = false;
	@ConfigComment("Used if overcastMode is off, 1 = lock weather on, 0 = lock weather off, -1 = dont lock anything, let server do whatever")
	public static int lockServerWeatherMode = 0; //is only used if overcastMode is off
	//clouds
	public static int Cloud_ParticleSpawnDelay = 0;
	public static int Cloud_Formation_MinDistBetweenSpawned = 256;
	@ConfigComment("For a second layer of passive non storm progressing clouds")
	public static boolean Cloud_Layer1_Enable = false;
	public static int Cloud_Layer0_Height = 200;
	public static int Cloud_Layer1_Height = 350;
	@ConfigComment("Not used at the moment")
	public static int Cloud_Layer2_Height = 500;
	
	public static int Thread_Particle_Process_Delay = 400;
	//sound
	public static double volWindScale = 0.05D;
	public static double volWaterfallScale = 0.5D;
	public static double volWindTreesScale = 0.5D;
	
	//blocks
	public static double sirenActivateDistance = 256D;
	public static double sensorActivateDistance = 256D;
	public static boolean Block_WeatherMachineNoTornadosOrCyclones = false;

	public static boolean Block_WeatherMachineNoRecipe = false;
	
	//dimension settings
	public static String Dimension_List_Weather = "0,-127";
	public static String Dimension_List_Clouds = "0,-127";
	public static String Dimension_List_Storms = "0,-127";
	public static String Dimension_List_WindEffects = "0,-127";

	public static boolean Villager_MoveInsideForStorms = true;
	public static int Villager_MoveInsideForStorms_Dist = 256;

	public ConfigMisc() {
		
	}

	@Override
	public String getName() {
		return "Misc";
	}

	@Override
	public String getRegistryName() {
		return Weather.modID + getName();
	}

	@Override
	public String getConfigFileName() {
		return "Weather2" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Weather2: " + getName();
	}

	@Override
	public void hookUpdatedValues() {
		//Weather.dbg("block list processing disabled");
		WeatherUtil.doBlockList();
		WeatherUtilConfig.processLists();
		
		StormObject.static_YPos_layer0 = Cloud_Layer0_Height;
		StormObject.static_YPos_layer1 = Cloud_Layer1_Height;
		StormObject.static_YPos_layer2 = Cloud_Layer2_Height;
		StormObject.layers = new ArrayList<>(Arrays.asList(StormObject.static_YPos_layer0, StormObject.static_YPos_layer1, StormObject.static_YPos_layer2));
	}

}
