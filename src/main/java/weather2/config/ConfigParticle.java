package weather2.config;

import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigParticle implements IConfigCategory {



    //particles
	public static boolean Wind_Particle_leafs = true;
	public static double Wind_Particle_effect_rate = 1D;
	public static boolean Wind_Particle_air = true;
	public static boolean Wind_Particle_sand = true;//not used since 1.3.2
	public static boolean Wind_Particle_waterfall = true;
	//public static boolean Wind_Particle_snow = false;
	public static boolean Wind_Particle_fire = true;
	public static boolean Particle_RainSnow = true;
	public static boolean Particle_VanillaAndWeatherOnly = false;
	public static double Precipitation_Particle_effect_rate = 1D;
	public static double Sandstorm_Particle_Debris_effect_rate = 0.6D;
	public static double Sandstorm_Particle_Dust_effect_rate = 0.6D;

    @Override
    public String getName() {
        return "Particle";
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
