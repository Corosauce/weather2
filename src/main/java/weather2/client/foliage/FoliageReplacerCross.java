package weather2.client.foliage;

import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * 2 meshes diagonal cross
 */
public class FoliageReplacerCross extends FoliageReplacerBase {

    public FoliageReplacerCross(BlockState state) {
        super(state);
    }

    public FoliageReplacerCross(BlockState state, int expectedHeight) {
        super(state);
        this.expectedHeight = expectedHeight;
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        if (baseMaterial == null || world.getBlockState(pos).getMaterial() == baseMaterial) {
            if (stateSensitive) {
                BlockState stateScan = world.getBlockState(pos.up());
                if (stateScan.getBlock() == state.getBlock()) {
                    boolean fail = false;
                    for (Map.Entry<IProperty, Comparable> entrySet : lookupPropertiesToComparable.entrySet()) {
                        if (stateScan.get(entrySet.getKey()) != entrySet.get()) {
                            fail = true;
                            break;
                        }
                    }
                    if (fail) {
                        return false;
                    }
                    return true;
                    /*IProperty asdasd = BlockCrops.AGE;
                    Comparable realValue = stateScan.get(BlockCrops.AGE);
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
            //Minecraft.getInstance().mouseHelper.ungrabMouseCursor();
            Block block = state.getBlock();

            //already verified up 1 == block needed
            height = 0;

            while (block == state.getBlock()) {
                height++;
                block = world.getBlockState(pos.up(height)).getBlock();
            }
        }
        FoliageEnhancerShader.addForPos(this, height, pos, randomizeCoord ? new Vec3(0.4, 0, 0.4) : null, biomeColorize);
    }
}
