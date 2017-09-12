package weather2.block;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import weather2.ServerTickHandler;
import weather2.config.ConfigStorm;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import weather2.weathersystem.storm.WeatherObject;

public class TileEntityWeatherDeflector extends TileEntity implements ITickable
{

	@Override
    public void update()
    {
    	
    	if (!world.isRemote) {
    		
    		if (world.getTotalWorldTime() % 100 == 0) {
    			WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
    			if (wm != null) {
		    		//StormObject lastTickStormObject = wm.getClosestStorm(new Vec3(xCoord, StormObject.layers.get(0), zCoord), deflectorRadius, StormObject.STATE_NORMAL, true);
		    		
		    		List<WeatherObject> storms = wm.getStormsAroundForDeflector(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), ConfigStorm.Storm_Deflector_RadiusOfStormRemoval);
		    		
		    		for (int i = 0; i < storms.size(); i++) {
						WeatherObject storm = storms.get(i);
		    			
		    			if (storm != null) {
		    				wm.removeStormObject(storm.ID);
			    			wm.syncStormRemove(storm);
		    			}
		    		}
		    		
		    		
		    	}
    		}
    	}
    }

    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        return super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
}
