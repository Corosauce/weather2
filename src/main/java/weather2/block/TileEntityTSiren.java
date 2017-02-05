package weather2.block;

import CoroUtil.util.CoroUtilPhysics;
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
import weather2.config.ConfigSand;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import java.util.List;

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
            Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());

    		StormObject so = ClientTickHandler.weatherManager.getClosestStorm(pos, ConfigMisc.sirenActivateDistance, StormObject.STATE_FORMING);

            if (so != null)
            {
            	//if (so.attrib_tornado_severity > 0) {
            		//Weather.dbg("soooooouuuunnnnddddddd");
	                this.lastPlayTime = System.currentTimeMillis() + 13000L;
	                /*this.soundID = */WeatherUtilSound.playNonMovingSound(pos, "streaming.siren", 1.0F, 1.0F, 120);
            	//}
            } else {
                if (!ConfigSand.Sandstorm_Siren_PleaseNoDarude) {
                    WeatherObjectSandstorm sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(pos);

                    if (sandstorm != null) {
                        List<Vec3> points = sandstorm.getSandstormAsShape();

                        float distMax = 75F;

                        //double scale = sandstorm.getSandstormScale();
                        boolean inStorm = CoroUtilPhysics.isInConvexShape(pos, points);
                        double dist = Math.min(distMax, CoroUtilPhysics.getDistanceToShape(pos, points));

                        if (inStorm || dist < distMax) {
                            String soundToPlay = "siren_sandstorm_5_extra";
                            if (getWorld().rand.nextBoolean()) {
                                soundToPlay = "siren_sandstorm_6_extra";
                            }

                            float distScale = Math.max(0.1F, 1F - (float) ((dist) / distMax));
                            if (inStorm) distScale = 1F;

                            this.lastPlayTime = System.currentTimeMillis() + 15000L;//WeatherUtilSound.soundToLength.get(soundToPlay) - 500L;
                            WeatherUtilSound.playNonMovingSound(pos, "streaming." + soundToPlay, 1F, distScale, distMax);
                        }

                        /*if (inStorm) {
                            dist = 0;
                        }

                        System.out.println("dist?: " + dist);

                        float distScale = 1F - (float) ((dist) / distMax);
                        distScale *= 2F * scale;

                        String soundToPlay = "";

                        System.out.println("siren scale: " + distScale);

                        if (*//*inStorm || *//*distScale >= 0.75F) {
                            soundToPlay = "siren_sandstorm_4";
                        } else if (distScale >= 0.5F) {
                            soundToPlay = "siren_sandstorm_3";
                        } else if (distScale >= 0.25F) {
                            soundToPlay = "siren_sandstorm_2";
                        } else {
                            soundToPlay = "siren_sandstorm_1";
                        }

                        this.lastPlayTime = System.currentTimeMillis() + WeatherUtilSound.soundToLength.get(soundToPlay) - 500L;
                        WeatherUtilSound.playNonMovingSound(pos, "streaming." + soundToPlay, 1.0F, 1.0F, distMax);*/
                    }
                }
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
