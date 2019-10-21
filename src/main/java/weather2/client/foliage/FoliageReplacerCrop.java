package weather2.client.foliage;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 4 meshes square crop
 */
public class FoliageReplacerCrop extends FoliageReplacerBase {

    public FoliageReplacerCrop(BlockState state) {
        super(state);
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.ORGANIC && world.getBlockState(pos.up()).getOwner() == state.getOwner();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        FoliageEnhancerShader.addForPos(this, expectedHeight, pos);
    }
}
