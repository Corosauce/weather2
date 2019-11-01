package weather2.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

/**
 * All stackable block code in this class considers "height" as a meta val height, not actual pixel height of AABB, basically 1 meta height = 2 pixel height, this is also used for all amount values
 * 
 * @author Corosus
 *
 */
public class WeatherUtilBlock {

	//TODO: 1.14 restore removed methods from previous git commits

	/**
	 * Safe version of World.getPrecipitationHeight that wont invoke chunkgen/chunkload if its requesting height in unloaded chunk
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static BlockPos getPrecipitationHeightSafe(World world, BlockPos pos) {
		if (world.isBlockLoaded(pos)) {
			return world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
		} else {
			return new BlockPos(pos.getX(), 0, pos.getZ());
		}
	}
}
