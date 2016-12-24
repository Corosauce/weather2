package weather2.config;

import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigStorm implements IConfigCategory {



    public static int Storm_OddsTo1OfHighWindWaterSpout = 150;
	public static boolean Storm_FlyingBlocksHurt = true;
	public static int Storm_MaxPerPlayerPerLayer = 20;
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
	//public static double Storm_Deadly_MinIntensity = 5.3D;
	public static int Storm_HailPerTick = 10;
	public static int Storm_OddsTo1OfOceanBasedStorm = 300;
	public static int Storm_OddsTo1OfLandBasedStorm = -1;
	public static int Storm_OddsTo1OfProgressionBase = 15;
	public static int Storm_OddsTo1OfProgressionStageMultiplier = 3;
	public static int Storm_ParticleSpawnDelay = 0;
	
	//per player storm settings
	public static int Player_Storm_Deadly_OddsTo1 = 30;
	public static int Player_Storm_Deadly_TimeBetweenInTicks = 20*60*20*3; //3 mc days
	public static int Player_Storm_Rain_OddsTo1 = 150;
	
	//per server storm settings
	public static boolean Server_Storm_Deadly_UseGlobalRate = false;
	public static int Server_Storm_Deadly_OddsTo1 = 30;
	public static int Server_Storm_Deadly_TimeBetweenInTicks = 20*60*20*3;
	public static boolean preventServerThunderstorms = true;
	//lightning
	public static int Lightning_OddsTo1OfFire = 20;
	public static int Lightning_lifetimeOfFire = 3;
	public static int Lightning_DistanceToPlayerForEffects = 256;

	public static boolean Lightning_StartsFires = false;

    @Override
    public String getName() {
        return "Storm";
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

    }
}
