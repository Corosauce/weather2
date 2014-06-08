package weather2.block;

import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilSound;
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

public class TileEntityTSiren extends TileEntity
{
    public long lastPlayTime = 0L;
    public long lastVolUpdate = 0L;
    public int soundID = -1;
    public int lineBeingEdited = -1;

    public void updateEntity()
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
    		StormObject so = ClientTickHandler.weatherManager.getClosestStorm(Vec3.createVectorHelper(xCoord, yCoord, zCoord), ConfigMisc.sirenActivateDistance, StormObject.ATTRIB_FORMINGTORNADO);

            if (so != null)
            {
            	//if (so.attrib_tornado_severity > 0) {
            		//Weather.dbg("soooooouuuunnnnddddddd");
	                this.lastPlayTime = System.currentTimeMillis() + 13000L;
	                this.soundID = WeatherUtilSound.playMovingSound(Weather.modID + ":tornado.siren", (float)mc.thePlayer.posX, (float)mc.thePlayer.posY, (float)mc.thePlayer.posZ, 1.0F, 1.0F);
            	//}
            }
        }

        if (this.lastVolUpdate < System.currentTimeMillis())
        {
            this.lastVolUpdate = System.currentTimeMillis() + 100L;
            Entity pl = mc.thePlayer;

            if (pl != null)
            {
                float var3 = (float)((120.0D - (double)MathHelper.sqrt_double(this.getDistanceFrom(pl.posX, pl.posY, pl.posZ))) / 120.0D);

                if (var3 < 0.0F)
                {
                    var3 = 0.0F;
                }

                String var2 = "sound_" + this.soundID;
                WeatherUtilSound.setVolume(var2, var3);
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
