package weather2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTSiren extends ContainerBlock
{

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    public BlockTSiren()
    {
        this(Material.CLAY);
        this.setDefaultState(this.stateContainer.getBaseState().with(ENABLED, Boolean.valueOf(true)));
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
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {ENABLED});
    }
    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer)
    {
        return this.getDefaultState().with(ENABLED, Boolean.valueOf(true));
    }

    @Override
    public int getMetaFromState(BlockState state)
    {
        int i = 0;

        if (!((Boolean)state.get(ENABLED)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    @Override
    public BlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().with(ENABLED, Boolean.valueOf(isEnabled(meta)));
    }

    public static boolean isEnabled(int meta)
    {
        return (meta & 8) != 8;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.updateState(worldIn, pos, state);
    }

    public void updateState(World worldIn, BlockPos pos, BlockState state)
    {
        boolean flag = !worldIn.isBlockPowered(pos);

        if (flag != ((Boolean)state.get(ENABLED)).booleanValue())
        {
            worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)), 3);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, BlockState state)
    {
        this.updateState(worldIn, pos, state);
    }
}
