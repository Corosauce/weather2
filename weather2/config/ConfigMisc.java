package weather2.config;

import java.io.File;

import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;


public class ConfigMisc implements IConfigCategory {

	//cleanup once GUI plan takes form
	
	//misc
	public static boolean Misc_proxyRenderOverrideEnabled = true;
	//public static boolean Misc_takeControlOfGlobalRain = true;
	public static boolean Misc_windOn = true;
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
	public static boolean preventServerThunderstorms = true;
	
	//tornado
	public static boolean Storm_Tornado_grabPlayer = true;
	public static boolean Storm_Tornado_grabBlocks = true;
	public static boolean Storm_Tornado_GrabCond_StrengthGrabbing = true;
	public static boolean Storm_Tornado_GrabCond_List = false;
	public static boolean Storm_Tornado_GrabListBlacklistMode = false;
	public static String Storm_Tornado_GrabList = "";
	public static int Storm_Tornado_maxBlocksPerStorm = 200;
	public static int Storm_Tornado_maxBlocksGrabbedPerTick = 5;
	public static int Storm_Tornado_rarityOfDisintegrate = 15;
	public static int Storm_Tornado_rarityOfBreakOnFall = 5;
	public static int Storm_Tornado_rarityOfFirenado = -1;
	public static boolean Storm_Tornado_RefinedGrabRules = true;
	public static boolean Storm_Tornado_aimAtPlayerOnSpawn = true;
	public static int Storm_Tornado_aimAtPlayerAngleVariance = 5;
	
	//storm
	public static boolean Storm_FlyingBlocksHurt = true;
	public static int Storm_MaxPerPlayerPerLayer = 20;
	public static double Storm_IntensityAmplifier = 4;
	public static int Storm_Deadly_CollideDistance = 128;
	public static int Storm_LightningStrikeBaseValueOddsTo1 = 200;
	public static boolean Storm_NoRainVisual = false;
	public static int Storm_MaxRadius = 300;
	public static int Storm_AllTypes_TickRateDelay = 60;
	public static int Storm_Rain_WaterBuildUpRate = 10;
	public static int Storm_Rain_WaterSpendRate = 3;
	public static int Storm_Rain_WaterBuildUpOddsTo1FromSource = 15;
	public static int Storm_Rain_WaterBuildUpOddsTo1FromNothing = 100;
	public static int Storm_Rain_WaterBuildUp = 150;
	public static double Storm_TemperatureAdjustRate = 0.1D;
	public static double Storm_Deadly_MinIntensity = 5.3D;
	public static int Storm_HailPerTick = 10;
	public static int Storm_OddsTo1OfOceanBasedStorm = 300;
	public static int Storm_OddsTo1OfProgressionBase = 15;
	public static int Storm_OddsTo1OfProgressionStageMultiplier = 3;
	public static int Storm_ParticleSpawnDelay = 0;
	
	//per player storm settings
	public static int Player_Storm_Deadly_OddsTo1 = 30;
	public static int Player_Storm_Deadly_TimeBetweenInTicks = 20*60*20*3; //3 mc days
	public static int Player_Storm_Rain_OddsTo1 = 150;
	
	//clouds
	public static int Cloud_ParticleSpawnDelay = 0;
	public static int Cloud_Formation_MinDistBetweenSpawned = 256;
	public static boolean Cloud_Layer1_Enable = false;
	
	//lightning
	public static int Lightning_OddsTo1OfFire = 20;
	public static int Lightning_lifetimeOfFire = 3;
	
	//snow
	public static boolean Snow_PerformSnowfall = false;
	public static boolean Snow_ExtraPileUp = false;
	public static int Snow_RarityOfBuildup = 64;
	public static int Snow_MaxBlockBuildupHeight = 3;
	public static boolean Snow_SmoothOutPlacement = false;
	
	//particles
	public static boolean Wind_Particle_leafs = true;
	public static double Wind_Particle_effect_rate = 1D;
	public static boolean Wind_Particle_air = true;
	public static boolean Wind_Particle_sand = true;//not used since 1.3.2
	public static boolean Wind_Particle_waterfall = true;
	//public static boolean Wind_Particle_snow = false;
	public static boolean Wind_Particle_fire = true;
	public static boolean Wind_NoWindEvents = true;
	public static int Thread_Particle_Process_Delay = 400;
	public static boolean Particle_RainSnow = true;
	public static boolean Particle_VanillaAndWeatherOnly = false;
	
	//sound
	public static double volWindScale = 0.05D;
	public static double volWaterfallScale = 0.5D;
	public static double volWindTreesScale = 0.5D;
	
	//blocks
	public static double sirenActivateDistance = 256D;
	public static double sensorActivateDistance = 256D;
	public static int Block_sensorID = 1900;
	public static int Block_sirenID = 1901;
	public static int Block_windVaneID = 1902;
	public static int Block_weatherForecastID = 1903;
	public static int Block_weatherMachineID = 1904;
	
	//dimension settings
	public static String Dimension_List_Weather = "0";
	public static String Dimension_List_Clouds = "0";
	public static String Dimension_List_Storms = "0";
	public static String Dimension_List_WindEffects = "0";

	public ConfigMisc() {
		
	}
	
	@Override
	public String getConfigFileName() {
		return "Weather2" + File.separator + "Misc";
	}

	@Override
	public String getCategory() {
		return "Weather2: Misc";
	}

	@Override
	public void hookUpdatedValues() {
		WeatherUtil.doBlockList();
		WeatherUtilConfig.processLists();
	}

}
