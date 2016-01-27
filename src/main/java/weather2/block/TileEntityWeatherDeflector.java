package weather2.block;

import java.util.List;

import CoroUtil.util.Vec3;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

public class TileEntityWeatherDeflector extends TileEntity implements ITickable
{
	public int deflectorRadius = 150;

	@Override
    public void update()
    {
    	
    	if (!worldObj.isRemote) {
    		
    		if (worldObj.getTotalWorldTime() % 100 == 0) {
    			WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(worldObj.provider.getDimensionId());
    			if (wm != null) {
		    		//StormObject lastTickStormObject = wm.getClosestStorm(new Vec3(xCoord, StormObject.layers.get(0), zCoord), deflectorRadius, StormObject.STATE_NORMAL, true);
		    		
		    		List<StormObject> storms = wm.getStormsAround(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), deflectorRadius);
		    		
		    		for (int i = 0; i < storms.size(); i++) {
		    			StormObject storm = storms.get(i);
		    			
		    			if (storm != null) {
		    				wm.removeStormObject(storm.ID);
			    			wm.syncStormRemove(storm);
		    			}
		    		}
		    		
		    		
		    	}
    		}
    	}
    }

    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
}
