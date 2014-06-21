package weather2.block;

import java.util.Random;

import weather2.ServerTickHandler;
import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTSensor extends Block
{
    public BlockTSensor(int var1)
    {
        super(var1, Material.clay);
        this.setTickRandomly(true);
    }

    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5)
    {
        this.updateTick(var1, var2, var3, var4, var5);
    }

    @Override
    public void updateTick(World var1, int var2, int var3, int var4, Random var5)
    {
    	
    	if (var1.isRemote) return;
    	
        //var1.getBlockMetadata(var2, var3, var4);
        //List var7 = var1.getEntitiesWithinAABB(EntTornado.class, AxisAlignedBB.getBoundingBoxFromPool((double)var2, (double)var3, (double)var4, (double)var2 + 1.0D, (double)var3 + 1.0D, (double)var4 + 1.0D).expand(140.0D, 140.0D, 140.0D));
    	
    	boolean enable = false;
    	
    	WeatherManagerServer wms = ServerTickHandler.lookupDimToWeatherMan.get(var1.provider.dimensionId);
    	if (wms != null) {
    		StormObject so = wms.getClosestStorm(Vec3.createVectorHelper(var2, var3, var4), ConfigMisc.sensorActivateDistance, StormObject.STATE_FORMING);
    		if (so != null/* && so.attrib_tornado_severity > 0*/) {
    			enable = true;
    		}
    	}

        if (enable)
        {
            var1.setBlockMetadataWithNotify(var2, var3, var4, 15, 2);
        }
        else
        {
            var1.setBlockMetadataWithNotify(var2, var3, var4, 0, 2);
        }

        /*if(var7.size() > 0) {
           var1.setBlockMetadataWithNotify(var2, var3, var4, 15);
        } else {
           var1.setBlockMetadataWithNotify(var2, var3, var4, 0);
        }*/
        var1.notifyBlocksOfNeighborChange(var2, var3 - 1, var4, this.blockID);
        var1.notifyBlocksOfNeighborChange(var2, var3 + 1, var4, this.blockID);
        var1.notifyBlocksOfNeighborChange(var2, var3, var4, this.blockID);
        var1.markBlockRangeForRenderUpdate(var2, var3, var4, var2, var3, var4);
        var1.scheduleBlockUpdate(var2, var3, var4, this.blockID, this.tickRate(var1));
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess var1, int var2, int var3, int var4, int var5)
    {
        return var1.getBlockMetadata(var2, var3, var4) == 0 ? 0 : 15;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess var1, int var2, int var3, int var4, int var5)
    {
        return var1.getBlockMetadata(var2, var3, var4) == 0 ? 0 : 15;
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }
}
