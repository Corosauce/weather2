package weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigSand implements IConfigCategory {


    @ConfigComment("Takes the sand out of sandwiches")
    public static boolean Storm_NoSandstorms = false;

	//sandstorm settings
	public static boolean Sandstorm_UseGlobalServerRate = false;
	public static int Sandstorm_OddsTo1 = 30;
	@ConfigComment("Time between sandstorms for either each player or entire server depending on if global rate is on, default: 3 client days")
	public static int Sandstorm_TimeBetweenInTicks = 20*60*20*3;

    @ConfigComment("Amount of game ticks between sand buildup iterations, keep it high to prevent client side chunk tick spam that destroys FPS")
    public static int Sandstorm_Sand_Buildup_TickRate = 40;

    @ConfigComment("Base amount of loops done per iteration, scaled by the sandstorms intensity (value given here is the max possible)")
    public static int Sandstorm_Sand_Buildup_LoopAmountBase = 800;

    @ConfigComment("Allow layered sand blocks to buildup outside deserty biomes where sandstorm is")
    public static boolean Sandstorm_Sand_Buildup_AllowOutsideDesert = true;

    public static boolean Sandstorm_Siren_PleaseNoDarude = false;

    @Override
    public String getName() {
        return "Sand";
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
