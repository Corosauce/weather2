package weather2.weathersystem.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import weather2.CommandWeather2;

public class BiomeTypes {
	
	public static boolean isBiomeWeatherActive(World world, BlockPos blockpos){
		boolean biomeIsWarm = false;
		//Even though Ocean is cold we treat it like its hot because it is Humidissimo
		
		if (world.getBiome(blockpos) == Biomes.OCEAN || world.getBiome(blockpos) == Biomes.DEEP_OCEAN || world.getBiome(blockpos) == Biomes.FOREST || world.getBiome(blockpos) == Biomes.FOREST_HILLS || world.getBiome(blockpos) == Biomes.BIRCH_FOREST || world.getBiome(blockpos) == Biomes.BIRCH_FOREST_HILLS || world.getBiome(blockpos) == Biomes.MUTATED_FOREST || world.getBiome(blockpos) == Biomes.MUTATED_BIRCH_FOREST || world.getBiome(blockpos) == Biomes.MUTATED_BIRCH_FOREST_HILLS || world.getBiome(blockpos) == Biomes.MUTATED_ROOFED_FOREST || world.getBiome(blockpos) == Biomes.JUNGLE || world.getBiome(blockpos) == Biomes.JUNGLE_EDGE || world.getBiome(blockpos) == Biomes.JUNGLE_HILLS || world.getBiome(blockpos) == Biomes.MUTATED_JUNGLE || world.getBiome(blockpos) == Biomes.MUTATED_JUNGLE_EDGE || world.getBiome(blockpos) == Biomes.SWAMPLAND || world.getBiome(blockpos) == Biomes.MUTATED_SWAMPLAND || world.getBiome(blockpos) == Biomes.SAVANNA || world.getBiome(blockpos) == Biomes.SAVANNA_PLATEAU || world.getBiome(blockpos) == Biomes.MUTATED_SAVANNA || world.getBiome(blockpos) ==  Biomes.MUTATED_SAVANNA_ROCK || world.getBiome(blockpos) == Biomes.PLAINS){
			biomeIsWarm = true;
		}
		System.out.println(biomeIsWarm);
		if (biomeIsWarm){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isBiomeFavorableForSnowstorms(Biome biome){
		if (biome == Biomes.ICE_MOUNTAINS || biome == Biomes.ICE_PLAINS || biome == Biomes.MUTATED_ICE_FLATS || biome == Biomes.EXTREME_HILLS || biome == Biomes.EXTREME_HILLS_EDGE || biome == Biomes.EXTREME_HILLS_WITH_TREES || biome == Biomes.MUTATED_EXTREME_HILLS || biome == Biomes.MUTATED_EXTREME_HILLS_WITH_TREES || biome == Biomes.COLD_BEACH || biome == Biomes.COLD_TAIGA || biome == Biomes.COLD_TAIGA_HILLS || biome == Biomes.MUTATED_TAIGA_COLD || biome == Biomes.FOREST_HILLS){
			return true;
		} else {
			return false;
		}
	}
	
}