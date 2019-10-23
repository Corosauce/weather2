package weather2.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import weather2.ClientTickHandler;
import weather2.config.ConfigMisc;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import weather2.weathersystem.storm.WeatherObject;

public class TileEntityWeatherForecast extends TileEntity implements ITickableTileEntity
{
	
	//since client receives data every couple seconds, we need to smooth out everything for best visual
	
	public float smoothAngle = 0;
	public float smoothSpeed = 0;
	
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public StormObject lastTickStormObject = null;
	
	public List<WeatherObject> storms = new ArrayList<>();
	
	//public MapHandler mapHandler;
	
	@Override
    public void tick()
    {
    	if (world.isRemote) {
    		if (world.getGameTime() % 200 == 0 || storms.size() == 0) {
    			lastTickStormObject = ClientTickHandler.weatherManager.getClosestStorm(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024, StormObject.STATE_FORMING, true);

    			if (ConfigMisc.radarCloudDebug) {
    				//storms.clear();
					List<WeatherObject> listAdd = new ArrayList<>();
    				for (WeatherObject wo : ClientTickHandler.weatherManager.getStormObjects()) {
    					//if (wo instanceof StormObject && !((StormObject) wo).isCloudlessStorm()) {
							listAdd.add(wo);
						//}
					}
					storms = listAdd;
				} else {
					storms = ClientTickHandler.weatherManager.getStormsAround(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), 1024);
				}
    		}
    	} else {
    		/*if (mapHandler == null) {
    			mapHandler = new MapHandler(this);
    		}
    		mapHandler.tick();*/
    	}
    }

    public CompoundNBT write(CompoundNBT var1)
    {
        return super.write(var1);
    }

    public void read(CompoundNBT var1)
    {
        super.read(var1);

    }
}
