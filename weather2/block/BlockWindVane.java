package weather2.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWindVane extends BlockContainer
{
    public BlockWindVane(int var1)
    {
        super(var1, Material.clay);
    	setBlockBounds(0.4F, 0, 0.4F, 0.6F, 0.3F, 0.6F);
    }
    
    @Override
    public Icon getIcon(int par1, int par2) {
    	return Block.stone.getIcon(par1, par2);
    }

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityWindVane();
    }
    
    @Override
    public boolean isOpaqueCube() {
    	return false;
    }
    
    @Override
    public boolean isBlockSolid(IBlockAccess par1iBlockAccess, int par2,
    		int par3, int par4, int par5) {
    	return true;//super.isBlockSolid(par1iBlockAccess, par2, par3, par4, par5);
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
}
