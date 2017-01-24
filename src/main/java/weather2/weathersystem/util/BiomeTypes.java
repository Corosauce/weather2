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
		
		if (world.getBiomeGenForCoords(blockpos) == Biomes.OCEAN || world.getBiomeGenForCoords(blockpos) == Biomes.DEEP_OCEAN || world.getBiomeGenForCoords(blockpos) == Biomes.FOREST || world.getBiomeGenForCoords(blockpos) == Biomes.FOREST_HILLS || world.getBiomeGenForCoords(blockpos) == Biomes.BIRCH_FOREST || world.getBiomeGenForCoords(blockpos) == Biomes.BIRCH_FOREST_HILLS || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_FOREST || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_BIRCH_FOREST || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_BIRCH_FOREST_HILLS || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_ROOFED_FOREST || world.getBiomeGenForCoords(blockpos) == Biomes.JUNGLE || world.getBiomeGenForCoords(blockpos) == Biomes.JUNGLE_EDGE || world.getBiomeGenForCoords(blockpos) == Biomes.JUNGLE_HILLS || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_JUNGLE || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_JUNGLE_EDGE || world.getBiomeGenForCoords(blockpos) == Biomes.SWAMPLAND || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_SWAMPLAND || world.getBiomeGenForCoords(blockpos) == Biomes.SAVANNA || world.getBiomeGenForCoords(blockpos) == Biomes.SAVANNA_PLATEAU || world.getBiomeGenForCoords(blockpos) == Biomes.MUTATED_SAVANNA || world.getBiomeGenForCoords(blockpos) ==  Biomes.MUTATED_SAVANNA_ROCK || world.getBiomeGenForCoords(blockpos) == Biomes.PLAINS){
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