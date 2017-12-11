package weather2.client.foliage;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 4 meshes square crop
 */
public class FoliageReplacerCrop extends FoliageReplacerBase {

    public FoliageReplacerCrop(IBlockState state) {
        super(state);
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.GRASS && world.getBlockState(pos.up()).getBlock() == state.getBlock();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        FoliageEnhancerShader.addForPos(this, expectedHeight, pos);
    }
}
