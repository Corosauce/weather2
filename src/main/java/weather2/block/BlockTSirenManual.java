package weather2.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTSirenManual extends BlockTSiren {

    public BlockTSirenManual()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta) {
        return new TileEntityTSirenManual();
    }

    @Override
    public void updateState(World worldIn, BlockPos pos, BlockState state)
    {
        boolean flag = worldIn.isBlockPowered(pos);

        if (flag != ((Boolean)state.get(ENABLED)).booleanValue())
        {
            worldIn.setBlockState(pos, state.withProperty(ENABLED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer)
    {
        return this.getDefaultState().withProperty(ENABLED, Boolean.valueOf(false));
    }
}

