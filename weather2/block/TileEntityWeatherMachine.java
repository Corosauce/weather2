package weather2.block;

import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.api.WindReader;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityWeatherMachine extends TileEntity
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
	
	//0 = snow, 1 = rain, 2 = deadly storm
	public int weatherType = 0;
	//0 = lightning, 1 = F1, 2 = F2, etc (snow would use this to increase snow rate maaaaaaaybbbeeeeee, needs more vars in StormObject)
	public int weatherIntensity = 0;
	//0 = uhh
	public int weatherRate = 0;
	//ya
	public int weatherSize = 50;
	//prevent storm moving via wind
	public boolean lockStormHere = true;
	
	public StormObject lastTickStormObject = null;

    public void updateEntity()
    {
    	if (!worldObj.isRemote) {
    		
    		//TEMP
    		weatherSize = 40;
    		
    		if (worldObj.getTotalWorldTime() % 40 == 0) {
    			
    			if (lastTickStormObject != null && lastTickStormObject.isDead) {
    				lastTickStormObject = null;
    			}
    			
    			if (lastTickStormObject == null) {
    				WeatherManagerServer manager = ServerTickHandler.lookupDimToWeatherMan.get(worldObj.provider.dimensionId);
    				
    				if (manager != null) {
    					StormObject so = new StormObject(manager);
    					so.initFirstTime();
    					so.pos = Vec3.createVectorHelper(xCoord, yCoord, zCoord);
    					so.layer = 0;
    					so.userSpawnedFor = "" + xCoord + yCoord + zCoord;
    					
    					if (weatherType == 0) {
    						so.levelTemperature = -40;
    						so.levelWater = 1000;
    						so.attrib_precipitation = true;
    					}
    					
    					manager.addStormObject(so);
    					manager.syncStormNew(so);
    					lastTickStormObject = so;
    				}
    			}
    		}
    		
    		if (lastTickStormObject != null && !lastTickStormObject.isDead) {
    			if (lockStormHere) {
					lastTickStormObject.pos = Vec3.createVectorHelper(xCoord, yCoord, zCoord);
				}
				
				lastTickStormObject.size = weatherSize;
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
