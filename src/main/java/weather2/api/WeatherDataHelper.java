package weather2.api;

import CoroUtil.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerBase;

public class WeatherDataHelper {

    /**
     * Check if precipitation occurring at position.
     * Use is somewhat expensive on cpu, consider caching result for frequent use
     *
     * @param world
     * @param position
     * @return
     */
	public static boolean isPrecipitatingAt(World world, BlockPos position) {
	    WeatherManagerBase weatherManager;
	    if (world.isRemote) {
	        weatherManager = getWeatherManagerForClient();
        } else {
	        weatherManager = ServerTickHandler.getWeatherSystemForDim(world.getDimension().getType().getId());
        }
        if (weatherManager != null) {
	        return weatherManager.isPrecipitatingAt(position);
        }
	    return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static WeatherManagerBase getWeatherManagerForClient() {
	    return ClientTickHandler.weatherManager;
    }

}
