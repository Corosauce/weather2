package weather2.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import weather2.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockSandLayer extends Block
{
    public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
    protected static final AxisAlignedBB[] SAND_AABB = new AxisAlignedBB[] {
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D), 
    	new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};

    public BlockSandLayer()
    {
        super(Material.SAND);
        //TODO: full block set before this is called
        this.setDefaultState(this.stateContainer.getBaseState().with(LAYERS, Integer.valueOf(8)));
        //this.setTickRandomly(true);
        this.setCreativeTab(ItemGroup.DECORATIONS);
        this.setSoundType(SoundType.SAND);
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
    {
        return SAND_AABB[((Integer)state.get(LAYERS)).intValue()];
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return ((Integer)worldIn.getBlockState(pos).get(LAYERS)).intValue() < 5;
    }

    /**
     * Checks if an IBlockState represents a block that is opaque and a full cube.
     */
    @Override
    public boolean isTopSolid(BlockState state)
    {
        //return ((Integer)state.get(LAYERS)).intValue() == 7;
    	return ((Integer)state.get(LAYERS)).intValue() == 8;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(BlockState stateContainer, IBlockAccess worldIn, BlockPos pos)
    {
        int i = ((Integer)stateContainer.get(LAYERS)).intValue();
        float f = 0.125F;
        AxisAlignedBB axisalignedbb = stateContainer.getBoundingBox(worldIn, pos);
        return new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.maxX, (double)((float)i * 0.125F), axisalignedbb.maxZ);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Override
    public boolean isOpaqueCube(BlockState state)
    {
        return ((Integer)state.get(LAYERS)).intValue() >= 8;
    }

    @Override
    public boolean isFullCube(BlockState state)
    {
        return ((Integer)state.get(LAYERS)).intValue() >= 8;
    }
    
    //TODO: for testing heightmap issue
    /*@Override
    public boolean isFullBlock(IBlockState state) {
    	return ((Integer)state.get(LAYERS)).intValue() >= 8;
    }*/

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        BlockState iblockstate = worldIn.getBlockState(pos.down());
        Block block = iblockstate.getBlock();
        return /*block != Blocks.ICE && block != Blocks.PACKED_ICE ? */(iblockstate.getBlock().isLeaves(iblockstate, worldIn, pos.down()) ? true : (block == this && ((Integer)iblockstate.get(LAYERS)).intValue() >= 7 ? true : iblockstate.isOpaqueCube() && iblockstate.getMaterial().blocksMovement()))/* : false*/;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
    	/*if (!worldIn.isRemote) {
    		WeatherUtilBlock.divideToNeighborCheck(state, worldIn, pos, blockIn);
    	}*/
        this.checkAndDropBlock(worldIn, pos, state);
    }

    
    private boolean checkAndDropBlock(World worldIn, BlockPos pos, BlockState state)
    {
        if (!this.canPlaceBlockAt(worldIn, pos))
        {
            worldIn.setBlockToAir(pos);
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, @Nullable ItemStack stack)
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Nullable
    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return CommonProxy.itemSandLayer;
    }


    /*public void tick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11)
        {
            worldIn.setBlockToAir(pos);
        }
    }*/

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState stateContainer, IBlockAccess blockAccess, BlockPos pos, Direction side)
    {
        if (side == Direction.UP)
        {
            return true;
        }
        else
        {
            BlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
            return iblockstate.getBlock() == this && ((Integer)iblockstate.get(LAYERS)).intValue() >= ((Integer)stateContainer.get(LAYERS)).intValue() ? true : super.shouldSideBeRendered(stateContainer, blockAccess, pos, side);
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public BlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().with(LAYERS, Integer.valueOf((meta & 7) + 1));
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return ((Integer)worldIn.getBlockState(pos).get(LAYERS)).intValue() == 1;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(BlockState state)
    {
        return ((Integer)state.get(LAYERS)).intValue() - 1;
    }

    @Override public int quantityDropped(BlockState state, int fortune, Random random){ return ((Integer)state.get(LAYERS)); }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {LAYERS});
    }
    
    //TODO: why did i add this
    /*@Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world,
    		BlockPos pos, EnumFacing side) {
    	IBlockState state = this.getActualState(base_state, world, pos);
        return ((Integer)state.get(LAYERS)) >= 8;
    }*/

    @Override
    public boolean causesSuffocation(BlockState state) {
        return false;
    }
}