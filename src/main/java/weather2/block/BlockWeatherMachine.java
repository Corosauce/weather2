package weather2.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilMisc;

public class BlockWeatherMachine extends BlockContainer
{
    public BlockWeatherMachine()
    {
        super(Material.CLAY);
        setHardness(0.6F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityWeatherMachine();
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    	
    	if (!par1World.isRemote && hand == EnumHand.MAIN_HAND) {
	    	TileEntity tEnt = par1World.getTileEntity(pos);
	    	
	    	if (tEnt instanceof TileEntityWeatherMachine) {
	    		((TileEntityWeatherMachine) tEnt).cycleWeatherType(par5EntityPlayer.isSneaking());
	    		String msg = "Off";
                if (((TileEntityWeatherMachine) tEnt).weatherType == 1) {
                    msg = "Rain";
                } else if (((TileEntityWeatherMachine) tEnt).weatherType == 2) {
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
	    		CoroUtilMisc.sendCommandSenderMsg(par5EntityPlayer, "Weather Machine set to " + msg);
	    		return true;
	    	}
    	}
    	
    	return true;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        System.out.println("clicked");
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
}
