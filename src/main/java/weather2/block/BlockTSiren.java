package weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTSiren extends BlockContainer
{

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    public BlockTSiren()
    {
        this(Material.CLAY);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ENABLED, Boolean.valueOf(true)));
    }

    public BlockTSiren(Material mat)
    {
        super(mat);
        setHardness(0.6F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityTSiren();
    }
    
    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {ENABLED});
    }
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(ENABLED, Boolean.valueOf(true));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

        if (!((Boolean)state.getValue(ENABLED)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(ENABLED, Boolean.valueOf(isEnabled(meta)));
    }

    public static boolean isEnabled(int meta)
    {
        return (meta & 8) != 8;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.updateState(worldIn, pos, state);
    }

    public void updateState(World worldIn, BlockPos pos, IBlockState state)
    {
        boolean flag = !worldIn.isBlockPowered(pos);

        if (flag != ((Boolean)state.getValue(ENABLED)).booleanValue())
        {
            worldIn.setBlockState(pos, state.withProperty(ENABLED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        this.updateState(worldIn, pos, state);
    }
}
