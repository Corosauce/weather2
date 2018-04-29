package weather2.client.foliage;

import CoroUtil.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class FoliageReplacerCrossGrass extends FoliageReplacerCross {

    public FoliageReplacerCrossGrass(IBlockState state) {
        super(state);
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        if (baseMaterial == null || world.getBlockState(pos).getMaterial() == baseMaterial) {
            if (stateSensitive) {
                IBlockState stateScan = world.getBlockState(pos.up());
                if (stateScan.getBlock() == state.getBlock()) {
                    boolean fail = false;
                    for (Map.Entry<IProperty, Comparable> entrySet : lookupPropertiesToComparable.entrySet()) {
                        if (stateScan.getValue(entrySet.getKey()) != entrySet.getValue()) {
                            fail = true;
                            break;
                        }
                    }
                    if (fail) {
                        return false;
                    }
                    return true;
                    /*IProperty asdasd = BlockCrops.AGE;
                    Comparable realValue = stateScan.getValue(BlockCrops.AGE);
                    Comparable needValue = EnumFacing.WEST;
                    needValue = 7;*/
                } else {
                    return false;
                }
                //return world.getBlockState(pos.up()) == state;
                //return world.getBlockState(pos.up()).getBlock() == state.getBlock();
            } else {
                return world.getBlockState(pos.up()).getBlock() == state.getBlock();
            }
        } else {
            return false;
        }

    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        //TODO: handle multi height cross detection here or make child class based off this one to do it
        int height = expectedHeight;
        if (height == -1) {
            //Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
            Block block = state.getBlock();

            //already verified up 1 == block needed
            height = 0;

            while (block == state.getBlock()) {
                height++;
                block = world.getBlockState(pos.up(height)).getBlock();
            }
        }


        /*Vec3 vec = new Vec3(0.1, 0, 0.1);
        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0.25, 0, 0));
        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(-0.25, 0, 0));
        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0, 0, 0.25));
        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0, 0, -0.25));*/

        Vec3 vec = new Vec3(0.2, 0, 0.2);
        //vec = new Vec3(0.0, 0, 0.0);
        FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0, 0, 0));
        //FoliageEnhancerShader.addForPos(this, height, pos, vec, biomeColorize, -1, new Vec3(0, 0, 0));
    }
}
