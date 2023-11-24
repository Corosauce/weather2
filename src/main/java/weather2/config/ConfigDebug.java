package weather2.config;

import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigDebug implements IConfigCategory {

    //public static int Particle_Reset_Frequency = 20*60*20;
    public static int Particle_Reset_Frequency = 0;
    public static boolean Particle_engine_render = true;
    public static boolean Particle_engine_tick = true;

    @Override
    public String getName() {
        return "Debug";
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
