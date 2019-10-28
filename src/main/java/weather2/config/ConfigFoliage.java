package weather2.config;

import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigFoliage implements IConfigCategory {

    public static int foliageShaderRange = 40;
    public static int Thread_Foliage_Process_Delay = 1000;
    public static boolean extraGrass = false;

    @Override
    public String getName() {
        return "Foliage";
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
