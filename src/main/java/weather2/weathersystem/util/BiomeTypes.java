package weather2.weathersystem.util;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class BiomeTypes {
	
	public static boolean isBiomeWarm(Biome biome){
		return biome.getTemperature() - 0.7 >= 0 || biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN;
	}
	
	public static boolean isBiomeHumid(Biome biome){
		return biome.isHighHumidity();
	}
	
	public static boolean isBiomeSnowy(Biome biome){
		return biome.isSnowyBiome();
	}
	
	public static boolean isBiomeArid(Biome biome){
		return biome.getBiomeName().contains("desert");
	}
}