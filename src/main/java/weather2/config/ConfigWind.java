package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigWind implements IConfigCategory {

    @ConfigComment("No comment is given")
    public static boolean Misc_windOn = true;
    @ConfigComment("No comment is given")
    public static boolean Wind_LowWindEvents = true;
    @ConfigComment("No comment is given")
    public static boolean Wind_HighWindEvents = true;

    @ConfigComment("No comment is given")
    public static int lowWindTimerEnableAmountBase = 20*60*2;
    @ConfigComment("No comment is given")
    public static int lowWindTimerEnableAmountRnd = 20*60*10;
    @ConfigComment("No comment is given")
    public static int lowWindOddsTo1 = 20*200;

    @ConfigComment("No comment is given")
    public static int highWindTimerEnableAmountBase = 20*60*2;
    @ConfigComment("No comment is given")
    public static int highWindTimerEnableAmountRnd = 20*60*10;
    @ConfigComment("No comment is given")
    public static int highWindOddsTo1 = 20*400;

    @ConfigComment("No comment is given")
    public static double globalWindChangeAmountRate = 1F;

    @ConfigComment("No comment is given")
    public static double windSpeedMin = 0.00001D;
    @ConfigComment("No comment is given")
    public static double windSpeedMax = 1D;

    @ConfigComment("Min wind speed to maintain if its raining with global overcast mode on, overrides low wind events and windSpeedMin")
    public static double windSpeedMinGlobalOvercastRaining = 0.3D;

    @Override
    public String getName() {
        return "Wind";
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
