package weather2.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAnemometer extends BlockContainer
{
    public BlockAnemometer()
    {
        super(Material.circuits);
    	setBlockBounds(0.4F, 0, 0.4F, 0.6F, 0.3F, 0.6F);
    }
    
    /*@Override
    public IIcon getIcon(int par1, int par2) {
    	return Blocks.stone.getIcon(par1, par2);
    }*/

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}
    
    @Override
    public boolean isOpaqueCube() {
    	return false;
    }
    
    @Override
    public boolean isBlockSolid(IBlockAccess par1iBlockAccess, BlockPos pos, EnumFacing facing) {
    	return true;
    }
    
    @Override
    public boolean isFullCube()
    {
        return false;
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityAnemometer();
	}
}
