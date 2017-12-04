package weather2.client.foliage;

import extendedrenderer.foliage.FoliageReplacerBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FoliageReplacerMultiTallPlant extends FoliageReplacerBase {

    //TODO: variants on the plant type
    public IBlockState state;

    public FoliageReplacerMultiTallPlant(IBlockState state) {
        this.state = state;
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.GRASS && world.getBlockState(pos.up()).getBlock() == state.getBlock();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {

    }
}
