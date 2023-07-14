package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigParticle implements IConfigCategory {



    //particles
    @ConfigComment("No comment is given")
    public static boolean Wind_Particle_leafs = true;
    @ConfigComment("Currently used for rates of leaf, waterfall, and fire particles")
	public static double Wind_Particle_effect_rate = 0.7D;
    @ConfigComment("No comment is given")
    public static boolean Wind_Particle_waterfall = true;
    //@ConfigComment("No comment is given")
    //public static boolean Wind_Particle_snow = false;
    @ConfigComment("No comment is given")
    public static boolean Wind_Particle_fire = false;
	@ConfigComment("Enables or disables all precipitation particle types")
	public static boolean Particle_RainSnow = true;
    @ConfigComment("No comment is given")
    public static boolean Particle_Rain = false;
    @ConfigComment("No comment is given")
    public static boolean Particle_Rain_GroundSplash = true;
    @ConfigComment("No comment is given")
    public static boolean Particle_Rain_DownfallSheet = false;
    @ConfigComment("No comment is given")
    public static boolean Particle_VanillaAndWeatherOnly = false;
    @ConfigComment("No comment is given")
    public static double Precipitation_Particle_effect_rate = 0.7D;
    @ConfigComment("No comment is given")
    public static double Sandstorm_Particle_Debris_effect_rate = 0.6D;
    @ConfigComment("No comment is given")
    public static double Sandstorm_Particle_Dust_effect_rate = 0.6D;

    @Override
    public String getName() {
        return "Particle";
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
