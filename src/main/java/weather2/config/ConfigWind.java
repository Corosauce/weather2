package weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigWind implements IConfigCategory {

    public static boolean Misc_windOn = true;
    public static boolean Wind_LowWindEvents = true;
    public static boolean Wind_HighWindEvents = true;

    public static int lowWindTimerEnableAmountBase = 20*60*2;
    public static int lowWindTimerEnableAmountRnd = 20*60*10;
    public static int lowWindOddsTo1 = 20*200;

    public static int highWindTimerEnableAmountBase = 20*60*2;
    public static int highWindTimerEnableAmountRnd = 20*60*10;
    public static int highWindOddsTo1 = 20*400;

    public static double globalWindChangeAmountRate = 1F;

    public static double windSpeedMin = 0.00001D;
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
