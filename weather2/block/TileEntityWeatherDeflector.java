package weather2.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

public class TileEntityWeatherDeflector extends TileEntity
{
	public int deflectorRadius = 100;

    public void updateEntity()
    {
    	if (!worldObj.isRemote) {
    		
    		if (worldObj.getTotalWorldTime() % 100 == 0) {
    			WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(worldObj.provider.dimensionId);
    			if (wm != null) {
		    		StormObject lastTickStormObject = wm.getClosestStorm(Vec3.createVectorHelper(xCoord, StormObject.layers.get(0), zCoord), deflectorRadius, StormObject.STATE_NORMAL, true);
		    		
		    		if (lastTickStormObject != null) {
	    			
	    				wm.removeStormObject(lastTickStormObject.ID);
		    			wm.syncStormRemove(lastTickStormObject);
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
