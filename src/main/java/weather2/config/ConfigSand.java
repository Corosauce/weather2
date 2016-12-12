package weather2.config;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigSand implements IConfigCategory {


	//sandstorm settings
	public static boolean Sandstorm_UseGlobalServerRate = false;
	public static int Sandstorm_OddsTo1 = 30;
	@ConfigComment("Time between sandstorms for either each player or entire server depending on if global rate is on, default: 3 mc days")
	public static int Sandstorm_TimeBetweenInTicks = 20*60*20*3;

    @Override
    public String getName() {
        return "Sand";
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
