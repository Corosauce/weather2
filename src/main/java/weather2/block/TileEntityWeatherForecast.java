package weather2.block;

import java.util.ArrayList;
import java.util.List;

import CoroUtil.util.Vec3;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import weather2.ClientTickHandler;
import weather2.weathersystem.storm.StormObject;

public class TileEntityWeatherForecast extends TileEntity implements ITickable
{
	
	//since client receives data every couple seconds, we need to smooth out everything for best visual
	
	public float smoothAngle = 0;
	public float smoothSpeed = 0;
	
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public StormObject lastTickStormObject = null;
	
	public List<StormObject> storms = new ArrayList<StormObject>();
	
	//public MapHandler mapHandler;
	
	@Override
    public void update()
    {
    	if (worldObj.isRemote) {
    		if (worldObj.getTotalWorldTime() % 200 == 0) {
    			lastTickStormObject = ClientTickHandler.weatherManager.getClosestStorm(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024, StormObject.STATE_THUNDER, true);
    			
    			storms = ClientTickHandler.weatherManager.getStormsAround(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024);
    		}
    	} else {
    		/*if (mapHandler == null) {
    			mapHandler = new MapHandler(this);
    		}
    		mapHandler.tick();*/
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
