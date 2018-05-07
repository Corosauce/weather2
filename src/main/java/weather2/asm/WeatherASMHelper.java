package weather2.asm;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import weather2.api.WeatherDataHelper;

public class WeatherASMHelper {

    public static boolean isRainingAt(World world, BlockPos pos) {
        //System.out.println("success!");
        return isRainingAtImpl(world, pos);
    }

    public static boolean isRainingAtImpl(World world, BlockPos position) {
        /*if (!world.isRaining())
        {
            return false;
        }
        else */if (!world.canSeeSky(position))
        {
            return false;
        }
        else if (world.getPrecipitationHeight(position).getY() > position.getY())
        {
            return false;
        }
        else
        {

            if (isRainingAtWeather2(world, position)) {

                //SS injects here
                Biome biome = world.getBiome(position);

                if (biome.getEnableSnow()) {
                    return false;
                } else {
                    return world.canSnowAt(position, false) ? false : biome.canRain();
                }

            }
        }

        return false;
    }

    /**
     * TODO: CACHE ME!!!!
     *
     * @param world
     * @param position
     * @return
     */
    public static boolean isRainingAtWeather2(World world, BlockPos position) {
        return WeatherDataHelper.isPrecipitatingAt(world, position);
    }

}
