package weather2.block;

import java.util.Random;

import net.minecraft.block.ContainerBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWindVane extends ContainerBlock
{
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.4F, 0, 0.4F, 0.6F, 0.3F, 0.6F);
	
    public BlockWindVane()
    {
        super(Material.CIRCUITS);
        setHardness(0.6F);
        setResistance(10.0F);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source,
                                        BlockPos pos) {
    	return AABB;
    }

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityWindVane();
    }
    
    @Override
    public boolean isOpaqueCube(BlockState state) {
    	return false;
    }
    
    /*@Override
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos,
    		EnumFacing side) {
    	return true;
    }*/
    
    @Override
    public boolean isFullCube(BlockState state)
    {
        return false;
    }
}
