package weather2.block;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import weather2.ServerTickHandler;
import weather2.config.ConfigMisc;
import weather2.config.ConfigTornado;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;

public class TileEntityWeatherMachine extends TileEntity implements ITickable
{
	
	//gui ideas
	
	/* Activity Mode: Locked on / Locked off / Time cycle
	 * Weather Type: Snow / Rain / Deadly Storm
	 * if activity mode on delay, otherwise just track last storm the tile entity made and wait for it to be dead or really far?:
	 * Weather Rate: 5 / 10 / 20 / 30 / 1 hr / 2 hr
	 * Size: ya
	 * 
	 * 
	 */
	
	//0 = snow (no, dont use anymore), 1 = rain, 2 = F1 tornado, 3 = stage 1 cyclone
	public int weatherType = 1;
	//0 = lightning, 1 = F1, 2 = F2, etc (snow would use this to increase snow rate maaaaaaaybbbeeeeee, needs more vars in StormObject)
	public int weatherIntensity = 0;
	//0 = uhh
	public int weatherRate = 0;
	//ya
	public int weatherSize = 50;
	//prevent storm moving via wind
	public boolean lockStormHere = true;
	
	public StormObject lastTickStormObject = null;

	public void cycleWeatherType() {
		weatherType++;
		int maxID = 6;
		if (ConfigTornado.Storm_NoTornadosOrCyclones || ConfigMisc.Block_WeatherMachineNoTornadosOrCyclones) {
			maxID = 4;
		}
		if (weatherType > maxID) {
			weatherType = 1; //skip snow
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		
		WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(worldObj.provider.getDimension());
		if (wm != null) {
    		//StormObject lastTickStormObject = wm.getClosestStorm(new Vec3(xCoord, StormObject.layers.get(0), zCoord), deflectorRadius, StormObject.STATE_NORMAL, true);
    		
    		if (lastTickStormObject != null) {
			
				wm.removeStormObject(lastTickStormObject.ID);
    			wm.syncStormRemove(lastTickStormObject);
			}
    	}
	}
	
	@Override
    public void update()
    {
    	if (!worldObj.isRemote) {
    		
    		//TEMP
    		weatherSize = 100;
    		
    		//weatherType = 3;
    		
    		if (worldObj.getTotalWorldTime() % 40 == 0) {
    			
    			if (lastTickStormObject != null && lastTickStormObject.isDead) {
    				lastTickStormObject = null;
    			}
    			
    			if (lastTickStormObject == null) {
    				WeatherManagerServer manager = ServerTickHandler.lookupDimToWeatherMan.get(worldObj.provider.getDimension());
    				
    				if (manager != null) {
    					StormObject so = new StormObject(manager);
    					so.initFirstTime();
    					so.pos = new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ());
    					so.layer = 0;
    					so.userSpawnedFor = "" + getPos().getX() + getPos().getY() + getPos().getZ();
    					//so.canSnowFromCloudTemperature = true;
    					so.naturallySpawned = false;
    					
    					
    					manager.addStormObject(so);
    					manager.syncStormNew(so);
    					lastTickStormObject = so;
    				}
    			}
    		}
    		
    		if (lastTickStormObject != null && !lastTickStormObject.isDead) {
    			
    			Random rand = new Random();
    			
    			if (lockStormHere) {
					//lastTickStormObject.pos = new Vec3(xCoord + rand.nextFloat() - rand.nextFloat(), StormObject.layers.get(0), zCoord + rand.nextFloat() - rand.nextFloat());
					lastTickStormObject.pos = new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ());
				}
				
				lastTickStormObject.size = weatherSize;
				

				lastTickStormObject.levelWater = 1000;
				lastTickStormObject.attrib_precipitation = true;
				lastTickStormObject.hasStormPeaked = false;
				lastTickStormObject.levelCurStagesIntensity = 0.9F;
				
				//defaults
				lastTickStormObject.levelCurIntensityStage = StormObject.STATE_NORMAL;
				lastTickStormObject.stormType = StormObject.TYPE_LAND;
				lastTickStormObject.levelTemperature = 40;
				
				if (weatherType == 0) {
					lastTickStormObject.levelTemperature = -40;
				} else if (weatherType == 1) {
				} else if (weatherType == 2) {
					lastTickStormObject.stormType = StormObject.TYPE_LAND;
					lastTickStormObject.levelCurIntensityStage = StormObject.STATE_THUNDER;
				} else if (weatherType == 3) {
					lastTickStormObject.stormType = StormObject.TYPE_LAND;
					lastTickStormObject.levelCurIntensityStage = StormObject.STATE_HIGHWIND;
				} else if (weatherType == 4) {
					lastTickStormObject.stormType = StormObject.TYPE_LAND;
					lastTickStormObject.levelCurIntensityStage = StormObject.STATE_HAIL;
				} else if (weatherType == 5) {
					lastTickStormObject.stormType = StormObject.TYPE_LAND;
					lastTickStormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
				} else if (weatherType == 6) {
					lastTickStormObject.stormType = StormObject.TYPE_WATER;
					lastTickStormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
				}
			}
    	}
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        var1.setInteger("weatherType", weatherType);
        return super.writeToNBT(var1);
    }

	@Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        weatherType = var1.getInteger("weatherType");

    }
}
