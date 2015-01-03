package weather2.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtil;

public class BlockWeatherMachine extends BlockContainer
{
    public BlockWeatherMachine(int var1)
    {
        super(Material.clay);
    }

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityWeatherMachine();
    }
    
    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3,
    		int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
    		float par8, float par9) {
    	
    	if (!par1World.isRemote) {
	    	TileEntity tEnt = par1World.getTileEntity(par2, par3, par4);
	    	
	    	if (tEnt instanceof TileEntityWeatherMachine) {
	    		((TileEntityWeatherMachine) tEnt).cycleWeatherType();
	    		String msg = "rain";
	    		if (((TileEntityWeatherMachine) tEnt).weatherType == 2) {
	    			msg = "Lightning";
	    		} else if (((TileEntityWeatherMachine) tEnt).weatherType == 3) {
	    			msg = "High wind";
	    		} else if (((TileEntityWeatherMachine) tEnt).weatherType == 4) {
	    			msg = "Hail";
	    		} else if (((TileEntityWeatherMachine) tEnt).weatherType == 5) {
	    			msg = "F1 tornado";
	    		} else if (((TileEntityWeatherMachine) tEnt).weatherType == 6) {
	    			msg = "Stage 1 Tropical Cyclone";
	    		}
	    		CoroUtil.sendPlayerMsg((EntityPlayerMP) par5EntityPlayer, "Weather Machine set to " + msg);
	    		return true;
	    	}
    	}
    	
    	return super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
    }
}
