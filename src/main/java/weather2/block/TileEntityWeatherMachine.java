package weather2.block;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import weather2.ServerTickHandler;
import weather2.Weather;
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
	
	//0 = off, 1 = rain, 2 = thunder, etc
	public int weatherType = 1;
	//ya
	public int weatherSize = 50;
	//prevent storm moving via wind
	public boolean lockStormHere = true;

	//TODO: replace with ID and just lookup each time, for better serialization
	public StormObject lastTickStormObject = null;

	//for tracking between world reloads
	public long lastTickStormObjectID = -1;

	public void cycleWeatherType(boolean reverse) {

		int maxID = 6;
		if (ConfigTornado.Storm_NoTornadosOrCyclones || ConfigMisc.Block_WeatherMachineNoTornadosOrCyclones) {
			maxID = 4;
		}

		int minID = 0;

		if (!reverse) {

			weatherType++;

			if (weatherType > maxID) {
				weatherType = minID;
			}
		} else {
			weatherType--;

			if (weatherType < minID) {
				weatherType = maxID;
			}
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();

		killStorm();
	}

	public void killStorm() {
		WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
		if (wm != null) {
			//StormObject lastTickStormObject = wm.getClosestStorm(new Vec3(xCoord, StormObject.layers.get(0), zCoord), deflectorRadius, StormObject.STATE_NORMAL, true);

			if (lastTickStormObject != null) {

				wm.removeStormObject(lastTickStormObject.ID);
				wm.syncStormRemove(lastTickStormObject);
				lastTickStormObject = null;
			}
		}
	}
	
	@Override
    public void update()
    {
    	if (!world.isRemote) {

    		if (weatherType == 0) {
    			if (lastTickStormObject != null) {
					killStorm();
				}
				return;
			}
    		
    		//TEMP...?
    		weatherSize = 100;
    		
    		//weatherType = 3;
    		
    		if (world.getTotalWorldTime() % 40 == 0) {
    			
    			if (lastTickStormObject != null && lastTickStormObject.isDead) {
    				lastTickStormObject = null;
    			}

    			//for when world is reloaded, regrab instance so a duplicate isnt greated (and so old one doesnt get loose)
    			if (lastTickStormObject == null && lastTickStormObjectID != -1) {
					WeatherManagerServer manager = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());

					if (manager != null) {
						StormObject obj = manager.getStormObjectByID(lastTickStormObjectID);
						if (obj != null) {
							lastTickStormObject = obj;
							Weather.dbg("regrabbed old storm instance by ID " + obj.ID + " for weather machine");
						}
					}
				}
    			
    			if (lastTickStormObject == null && !ConfigMisc.Aesthetic_Only_Mode) {
    				WeatherManagerServer manager = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
    				
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
    					lastTickStormObjectID = so.ID;
    				}
    			}
    		}
    		
    		if (lastTickStormObject != null && !lastTickStormObject.isDead) {
    			
    			Random rand = new Random();
    			
    			if (lockStormHere) {
					//lastTickStormObject.pos = new Vec3(xCoord + rand.nextFloat() - rand.nextFloat(), StormObject.layers.get(0), zCoord + rand.nextFloat() - rand.nextFloat());
					lastTickStormObject.pos = new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ());
					lastTickStormObject.motion = new Vec3(0, 0, 0);
				}

				lastTickStormObject.weatherMachineControlled = true;

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
        var1.setLong("lastTickStormObjectID", lastTickStormObjectID);
        return super.writeToNBT(var1);
    }

	@Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        weatherType = var1.getInteger("weatherType");
        if (var1.hasKey("lastTickStormObjectID")) {
			lastTickStormObjectID = var1.getLong("lastTickStormObjectID");
		}

    }
}
