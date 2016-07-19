package weather2.block;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;

public class TileEntityTSiren extends TileEntity implements ITickable
{
    public long lastPlayTime = 0L;
    public long lastVolUpdate = 0L;
    //public int soundID = -1;
    public int lineBeingEdited = -1;

    @Override
    public void update()
    {
    	if (worldObj.isRemote) {
    		tickClient();
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickClient() {
    	
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
    	if (this.lastPlayTime < System.currentTimeMillis())
        {
    		StormObject so = ClientTickHandler.weatherManager.getClosestStorm(new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()), ConfigMisc.sirenActivateDistance, StormObject.STATE_FORMING);

            if (so != null)
            {
            	//if (so.attrib_tornado_severity > 0) {
            		//Weather.dbg("soooooouuuunnnnddddddd");
	                this.lastPlayTime = System.currentTimeMillis() + 13000L;
	                /*this.soundID = */WeatherUtilSound.playNonMovingSound(new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()), Weather.modID + ":streaming.siren", 1.0F, 1.0F, 120);
            	//}
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        return super.writeToNBT(var1);
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
    
}
