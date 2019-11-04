package weather2.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import weather2.Weather;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.storm.StormObject;


public class ConfigMisc implements IConfigCategory {
	
	//misc
	public static boolean Misc_proxyRenderOverrideEnabled = false;
	//public static boolean Misc_takeControlOfGlobalRain = true;

	//cutoff a bit extra, noticed lots of storms being insta killed on creation
	public static int Misc_simBoxRadiusCutoff = 1024+100;
	public static int Misc_simBoxRadiusSpawn = 1024;
	public static boolean Misc_ForceVanillaCloudsOff = false;
	public static int Misc_AutoDataSaveIntervalInTicks = 20*60*30;
	public static boolean consoleDebug = false;

	public static boolean radarCloudDebug = false;
	
	//Weather
	@ConfigComment("If true, lets server side do vanilla weather rules, weather2 will only make storms when server side says 'rain' is on")
	public static boolean overcastMode = true;
	@ConfigComment("Used if overcastMode is off, 1 = lock weather on, 0 = lock weather off, -1 = dont lock anything, let server do whatever")
	public static int lockServerWeatherMode = 0; //is only used if overcastMode is off
	//cloudOption
	@ConfigComment("How many ticks between cloud particle spawning")
	public static int Cloud_ParticleSpawnDelay = 2;
	@ConfigComment("Distance between cloud formations, not particles, this includes invisible cloudless formations used during partial cloud coverage")
	public static int Cloud_Formation_MinDistBetweenSpawned = 300;
	@ConfigComment("For a second layer of passive non storm progressing cloudOption")
	public static boolean Cloud_Layer1_Enable = false;
	public static int Cloud_Layer0_Height = 200;
	public static int Cloud_Layer1_Height = 350;
	@ConfigComment("Not used at the moment")
	public static int Cloud_Layer2_Height = 500;

	@ConfigComment("How much to randomly change cloud coverage % amount, performed every 10 seconds")
	public static double Cloud_Coverage_Random_Change_Amount = 0.05D;

	@ConfigComment("Minimum percent of cloud coverage, supports negative for extended cloudless sky coverage")
	public static double Cloud_Coverage_Min_Percent = 0D;

	@ConfigComment("Maximum percent of cloud coverage, supports over 100% for extended full cloud sky coverage")
	public static double Cloud_Coverage_Max_Percent = 100D;
	
	public static int Thread_Particle_Process_Delay = 400;
	//sound
	public static double volWindScale = 0.05D;
	public static double volWaterfallScale = 0.5D;
	public static double volWindTreesScale = 0.5D;
	public static double volWindLightningScale = 1D;
	
	//blocks
	public static double sirenActivateDistance = 256D;
	public static double sensorActivateDistance = 256D;
	public static boolean Block_WeatherMachineNoTornadosOrCyclones = false;

	public static boolean Block_WeatherMachineNoRecipe = false;
	public static boolean Block_SensorNoRecipe = false;
	public static boolean Block_SirenNoRecipe = false;
	public static boolean Block_SirenManualNoRecipe = false;
	public static boolean Block_WindVaneNoRecipe = false;
	public static boolean Block_AnemometerNoRecipe = false;
	public static boolean Block_WeatherForecastNoRecipe = false;
	public static boolean Block_WeatherDeflectorNoRecipe = false;
	public static boolean Block_SandLayerNoRecipe = false;
	public static boolean Block_SandNoRecipe = false;
	public static boolean Item_PocketSandNoRecipe = false;
	@ConfigComment("Disabling this recipe will keep them from using other recipes since it depends on this item")
	public static boolean Item_WeatherItemNoRecipe = false;

	
	//dimension settings
	public static String Dimension_List_Weather = "0,-127";
	public static String Dimension_List_Clouds = "0,-127";
	public static String Dimension_List_Storms = "0,-127";
	public static String Dimension_List_WindEffects = "0,-127";

	public static boolean Villager_MoveInsideForStorms = true;
	public static int Villager_MoveInsideForStorms_Dist = 256;

	public static double shaderParticleRateAmplifier = 3D;

	public static boolean blockBreakingInvokesCancellableEvent = false;

	@ConfigComment("If true, will cancel vanilla behavior of setting clear weather when the player sleeps, for global overcast mode")
	public static boolean Global_Overcast_Prevent_Rain_Reset_On_Sleep = false;

	@ConfigComment("Use if you are on a server with weather but want it ALL off client side for performance reasons, overrides basically every client based setting")
	public static boolean Client_PotatoPC_Mode = false;

	@ConfigComment("Server and client side, Locks down the mod to only do wind, leaves, foliage shader if on, etc. No weather systems, turns overcast mode on")
	public static boolean Aesthetic_Only_Mode = true;

	public ConfigMisc() {
		
	}

	@Override
	public String getName() {
		return "Misc";
	}

	@Override
	public String getRegistryName() {
		return Weather.MODID + getName();
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
		//TODO: 1.14 uncomment
		//WeatherUtil.doBlockList();
		WeatherUtilConfig.processLists();
		
		StormObject.static_YPos_layer0 = Cloud_Layer0_Height;
		StormObject.static_YPos_layer1 = Cloud_Layer1_Height;
		StormObject.static_YPos_layer2 = Cloud_Layer2_Height;
		StormObject.layers = new ArrayList<>(Arrays.asList(StormObject.static_YPos_layer0, StormObject.static_YPos_layer1, StormObject.static_YPos_layer2));
	}

}
