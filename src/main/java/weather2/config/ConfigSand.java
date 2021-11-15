package weather2.config;

public class ConfigSand {

	//sandstorm settings
	public static boolean Sandstorm_UseGlobalServerRate = false;
	public static int Sandstorm_OddsTo1 = 30;
	public static int Sandstorm_TimeBetweenInTicks = 20*60*20*3;

	//tick delay
    public static int Sandstorm_Sand_Buildup_TickRate = 40;

    public static int Sandstorm_Sand_Buildup_LoopAmountBase = 800;

    public static boolean Sandstorm_Sand_Buildup_AllowOutsideDesert = true;

    public static double Sandstorm_Particle_Dust_effect_rate = 0.6D;
    public static double Precipitation_Particle_effect_rate = 0.7D;
    public static double Sandstorm_Particle_Debris_effect_rate = 0.6D;
}
