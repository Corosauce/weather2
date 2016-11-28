package weather2.config;

import modconfig.IConfigCategory;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.storm.StormObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Corosus on 11/28/2016.
 */
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

    @Override
    public String getConfigFileName() {
        return "Weather2" + File.separator + "Wind";
    }

    @Override
    public String getCategory() {
        return "Weather2: Wind";
    }

    @Override
    public void hookUpdatedValues() {

    }
}
