package weather2.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
    public void updateState(World worldIn, BlockPos pos, IBlockState state)
    {
        boolean flag = worldIn.isBlockPowered(pos);

        if (flag != ((Boolean)state.getValue(ENABLED)).booleanValue())
        {
            worldIn.setBlockState(pos, state.withProperty(ENABLED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(ENABLED, Boolean.valueOf(false));
    }
}
