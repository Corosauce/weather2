package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigStorm implements IConfigCategory {


    @ConfigComment("No comment is given")
    public static int Storm_OddsTo1OfHighWindWaterSpout = 150;
    @ConfigComment("No comment is given")
    public static boolean Storm_FlyingBlocksHurt = true;
    @ConfigComment("No comment is given")
    public static int Storm_MaxPerPlayerPerLayer = 20;
    @ConfigComment("No comment is given")
    public static int Storm_Deadly_CollideDistance = 128;
    @ConfigComment("No comment is given")
    public static int Storm_LightningStrikeBaseValueOddsTo1 = 200;
    @ConfigComment("No comment is given")
    public static boolean Storm_NoRainVisual = false;
    @ConfigComment("No comment is given")
    public static int Storm_MaxRadius = 300;
    @ConfigComment("No comment is given")
    public static int Storm_AllTypes_TickRateDelay = 60;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_WaterBuildUpRate = 10;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_WaterSpendRate = 3;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_WaterBuildUpOddsTo1FromSource = 15;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_WaterBuildUpOddsTo1FromNothing = 100;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_WaterBuildUpOddsTo1FromOvercastRaining = 30;
    //@ConfigComment("No comment is given")
    //public static int Storm_Rain_WaterBuildUp = 150;
    @ConfigComment("No comment is given")
    public static double Storm_TemperatureAdjustRate = 0.1D;
    //@ConfigComment("No comment is given")
    //public static double Storm_Deadly_MinIntensity = 5.3D;
    @ConfigComment("No comment is given")
    public static int Storm_HailPerTick = 10;
    @ConfigComment("No comment is given")
    public static int Storm_OddsTo1OfOceanBasedStorm = 300;
    //@ConfigComment("No comment is given")
    //public static int Storm_OddsTo1OfLandBasedStorm = -1;
    //@ConfigComment("No comment is given")
    //public static int Storm_OddsTo1OfProgressionBase = 15;
    //@ConfigComment("No comment is given")
    //public static int Storm_OddsTo1OfProgressionStageMultiplier = 3;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_HighWind = 90;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_Hail = 80;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F0_Tornado = 70;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_C0_Cyclone = 70;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F1_Tornado = 50;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_C1_Cyclone = 50;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F2_Tornado = 40;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_C2_Cyclone = 40;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F3_Tornado = 30;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_C3_Cyclone = 30;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F4_Tornado = 20;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_C4_Cyclone = 20;
    @ConfigComment("No comment is given")
    public static int Storm_PercentChanceOf_F5_Tornado = 10;
	@ConfigComment("Also known as full blown hurricane")
	public static int Storm_PercentChanceOf_C5_Cyclone = 10;
    @ConfigComment("No comment is given")
    public static int Storm_ParticleSpawnDelay = 3;
	
	//per player storm settings
    @ConfigComment("No comment is given")
    public static int Player_Storm_Deadly_OddsTo1 = 30;
    @ConfigComment("No comment is given")
    public static int Player_Storm_Deadly_TimeBetweenInTicks = 20*60*20*3; //3 mc days
	
	//per server storm settings
    @ConfigComment("No comment is given")
    public static boolean Server_Storm_Deadly_UseGlobalRate = false;
	@ConfigComment("Used if Server_Storm_Deadly_UseGlobalRate is on, replaces use of Player_Storm_Deadly_OddsTo1")
	public static int Server_Storm_Deadly_OddsTo1 = 30;
	@ConfigComment("Used if Server_Storm_Deadly_UseGlobalRate is on, replaces use of Player_Storm_Deadly_TimeBetweenInTicks")
	public static int Server_Storm_Deadly_TimeBetweenInTicks = 20*60*20*3;

	@ConfigComment("For areas without the right mix of hot and cold biomes")
	public static int Player_Storm_Deadly_OddsTo1_Land_Based = 300;
	@ConfigComment("For areas without the right mix of hot and cold biomes")
	public static int Player_Storm_Deadly_TimeBetweenInTicks_Land_Based = 20*60*20*10; //10 mc days
	@ConfigComment("For areas without the right mix of hot and cold biomes")
	public static int Server_Storm_Deadly_OddsTo1_Land_Based = 300;
	@ConfigComment("For areas without the right mix of hot and cold biomes")
	public static int Server_Storm_Deadly_TimeBetweenInTicks_Land_Based = 20*60*20*10; //10 mc days

    @ConfigComment("No comment is given")
    public static boolean preventServerThunderstorms = true;
	//lightning
    @ConfigComment("No comment is given")
    public static int Lightning_OddsTo1OfFire = 20;
    @ConfigComment("No comment is given")
    public static int Lightning_lifetimeOfFire = 3;
    @ConfigComment("No comment is given")
    public static int Lightning_DistanceToPlayerForEffects = 256;

    @ConfigComment("No comment is given")
    public static boolean Lightning_StartsFires = false;

    @ConfigComment("No comment is given")
    public static int Storm_Deflector_RadiusOfStormRemoval = 150;

    @ConfigComment("The minimum stage a storm has to be at to be removed, stages are: 0 = anything, 1 = thunder, 2 = high wind, 3 = hail, 4 = F0/C0, 5 = F1/C1, 6 = F2/C2, 7 = F3/C3, 8 = F4/C4, 9 = F5/C5")
    public static int Storm_Deflector_MinStageRemove = 1;
    @ConfigComment("No comment is given")
    public static boolean Storm_Deflector_RemoveRainstorms = false;
    @ConfigComment("No comment is given")
    public static boolean Storm_Deflector_RemoveSandstorms = true;

	@ConfigComment("Minimum amount of visual rain shown when its raining globally during overcast mode")
    public static double Storm_Rain_Overcast_Amount = 0.01D;
    @ConfigComment("No comment is given")
    public static int Storm_Rain_Overcast_OddsTo1 = 50;

    @ConfigComment("No comment is given")
    public static int Storm_Rain_OddsTo1 = 150;

	@ConfigComment("How often in ticks, a rainstorm updates its list of entities under the rainstorm to extinguish. Extinguishes entities under rainclouds when globalOvercast is off. Set to 0 or less to disable")
	public static int Storm_Rain_TrackAndExtinguishEntitiesRate = 200;

    @Override
    public String getName() {
        return "Storm";
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

    }
}
