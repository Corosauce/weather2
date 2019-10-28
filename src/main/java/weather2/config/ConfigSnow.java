package weather2.config;

import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigSnow implements IConfigCategory {


	//snow
	public static boolean Snow_PerformSnowfall = false;
	//public static boolean Snow_ExtraPileUp = false;
	public static int Snow_RarityOfBuildup = 64;
	//public static int Snow_MaxBlockBuildupHeight = 3;
	//public static boolean Snow_SmoothOutPlacement = false;

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
