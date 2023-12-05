package weather2.config;

import com.corosus.modconfig.ConfigParams;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigSound implements IConfigCategory {

    @ConfigParams(min = 0, max = 5)
    public static double leavesVolume = 0F;
    @ConfigParams(min = 0, max = 5)
    public static double tornadoWindVolume = 1F;
    @ConfigParams(min = 0, max = 5)
    public static double tornadoDamageVolume = 1F;
    @ConfigParams(min = 0, max = 5)
    public static double windyStormVolume = 1F;
    @ConfigParams(min = 0, max = 5)
    public static double sirenVolume = 1F;

    @Override
    public String getName() {
        return "Sound";
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
