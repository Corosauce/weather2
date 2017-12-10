package weather2.client.foliage;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FoliageReplacerCross extends FoliageReplacerBase {

    public int expectedHeight = 1;

    public FoliageReplacerCross(IBlockState state) {
        super(state);
    }

    public FoliageReplacerCross(IBlockState state, int expectedHeight) {
        super(state);
        this.expectedHeight = expectedHeight;
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.GRASS && world.getBlockState(pos.up()).getBlock() == state.getBlock();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        //TODO: handle multi height cross detection here or make child class based off this one to do it
        FoliageEnhancerShader.addForPos(this, pos);
    }
}
