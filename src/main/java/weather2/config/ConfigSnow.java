package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigSnow implements IConfigCategory {

    public static boolean Storm_NoSnowstorms = false;

    public static boolean Snowstorm_UseGlobalServerRate = false;
    public static int Snowstorm_OddsTo1 = 30;
    @ConfigComment("Time between snowstorms for either each player or entire server depending on if global rate is on, default: 3 client days")
    public static int Snowstorm_TimeBetweenInTicks = 20*60*20*3;

    @ConfigComment("Amount of game ticks between snow buildup iterations, keep it high to prevent client side chunk tick spam that destroys FPS")
    public static int Snowstorm_Snow_Buildup_TickRate = 40;

    @ConfigComment("Base amount of loops done per iteration, scaled by the snowstorms intensity (value given here is the max possible), eg: at max storm intensity, every 40th tick, itll try to build up snow in 800 places around the storm")
    public static int Snowstorm_Snow_Buildup_LoopAmountBase = 800;

    @ConfigComment("Max height of snow allowed to buildup against something, higher = things get more buried over time")
    public static int Snowstorm_Snow_Block_Max_Height = 5;

    @ConfigComment("Allow layered snow blocks to buildup outside cold biomes where snowstorm is")
    public static boolean Snowstorm_Snow_Buildup_AllowOutsideColdBiomes = true;

    @Override
    public String getName() {
        return "Snow";
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
