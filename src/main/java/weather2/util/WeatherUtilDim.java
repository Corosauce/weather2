package weather2.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WeatherUtilDim {

    public static boolean canBlockSeeSky(World world, BlockPos pos) {
        if (pos.getY() >= getSeaLevel(world)) {
            return world.canBlockSeeSky(pos);
        } else {
            BlockPos blockpos = new BlockPos(pos.getX(), getSeaLevel(world), pos.getZ());
            if (!world.canBlockSeeSky(blockpos)) {
                return false;
            } else {
                for(BlockPos blockpos1 = blockpos.down(); blockpos1.getY() > pos.getY(); blockpos1 = blockpos1.down()) {
                    BlockState blockstate = world.getBlockState(blockpos1);
                    if (blockstate.getOpacity(world, blockpos1) > 0 && !blockstate.getMaterial().isLiquid()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static int getSeaLevel(World world) {
        //TODO: sync customizable sea level to client, also use World.getSeaLevel if logical server
        return 63;
    }

}
