package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigParticle implements IConfigCategory {



    //particles
	/*public static boolean Wind_Particle_leafs = true;
    @ConfigComment("Currently used for rates of leaf, waterfall, and fire particles")
	public static double Wind_Particle_effect_rate = 0.7D;
	public static boolean Wind_Particle_waterfall = true;
	//public static boolean Wind_Particle_snow = false;
	public static boolean Wind_Particle_fire = false;
	@ConfigComment("Enables or disables all precipitation particle types")
	public static boolean Particle_RainSnow = true;
    public static boolean Particle_Rain = false;
    public static boolean Particle_Rain_GroundSplash = true;
    public static boolean Particle_Rain_DownfallSheet = false;
	public static boolean Particle_VanillaAndWeatherOnly = false;*/

    @ConfigComment("Adjust amount of precipitation based particles, works as a multiplier")
	public static double Precipitation_Particle_effect_rate = 0.7D;
	//public static double Sandstorm_Particle_Debris_effect_rate = 0.6D;
	//public static double Sandstorm_Particle_Dust_effect_rate = 0.6D;

    @ConfigComment("Adjust amount of all weather2 based particles, works as a multiplier")
    public static double Particle_effect_rate = 1D;

    @ConfigComment("If true, uses vanilla rain/snow non particle precipitation")
    public static boolean Particle_vanilla_precipitation = false;

    @ConfigComment("If set to false, particles are spawned in using the vanilla particle renderer, may cause issues, performance seems worse")
    public static boolean Particle_engine_weather2 = true;

    @ConfigComment("Extra flying block particles to spawn when the tornado rips up a block")
    public static int Particle_Tornado_extraParticleCubes = 2;

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
