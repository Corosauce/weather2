package weather2.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPool;
import net.minecraft.client.audio.SoundPoolEntry;
import paulscode.sound.SoundSystem;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WeatherUtilSound {
	
	@SideOnly(Side.CLIENT)
    public static SoundSystem sndSystem;
    @SideOnly(Side.CLIENT)
    public static SoundPool soundPool;
    public static int lastSoundID;
	
	//sound stuff not initialized - mainly tornado sounds
	public static long lastSoundPositionUpdate;
    public static String snd_dmg_close[] = new String[3];
    public static String snd_wind_close[] = new String[3];
    public static String snd_wind_far[] = new String[3];
    public static Map soundToLength = new HashMap();
    public static int snd_rand[] = new int[3];
    public static long soundTimer[] = new long[3];
    public static int soundID[] = new int[3];
    
    public static void init() {
    	Random rand = new Random();
    	snd_dmg_close[0] = "destruction_0_";
        snd_dmg_close[1] = "destruction_1_";
        snd_dmg_close[2] = "destruction_2_";
        snd_wind_close[0] = "wind_close_0_";
        snd_wind_close[1] = "wind_close_1_";
        snd_wind_close[2] = "wind_close_2_";
        snd_wind_far[0] = "wind_far_0_";
        snd_wind_far[1] = "wind_far_1_";
        snd_wind_far[2] = "wind_far_2_";
        snd_rand[0] = rand.nextInt(3);
        snd_rand[1] = rand.nextInt(3);
        snd_rand[2] = rand.nextInt(3);
        soundID[0] = -1;
        soundID[1] = -1;
        soundID[2] = -1;
        soundToLength.put(snd_dmg_close[0], 2515);
        soundToLength.put(snd_dmg_close[1], 2580);
        soundToLength.put(snd_dmg_close[2], 2741);
        soundToLength.put(snd_wind_close[0], 4698);
        soundToLength.put(snd_wind_close[1], 7324);
        soundToLength.put(snd_wind_close[2], 6426);
        soundToLength.put(snd_wind_far[0], 12892);
        soundToLength.put(snd_wind_far[1], 9653);
        soundToLength.put(snd_wind_far[2], 12003);
    }
	
	@SideOnly(Side.CLIENT)
    public static void setVolume(String soundID, float vol)
    {
		try {
	        /*if (sndSystem == null)
	        {*/
	            getSoundSystem();
	        //}
	
	        if (sndSystem != null)
	        {
	            sndSystem.setVolume(new StringBuilder().append(soundID).toString(), vol * FMLClientHandler.instance().getClient().gameSettings.soundVolume);
	        }
		} catch (Exception ex) {
			//error from resource changes, sound system got reset
        	ex.printStackTrace();
        	
        	getSoundSystem();
        }
    }
	
    @SideOnly(Side.CLIENT)
    public static int getLastSoundID()
    {
        /*if (sndSystem == null)
        {*/
            getSoundSystem();
        //}

        if (sndSystem != null)
        {
            Field field = null;

            try
            {
                field = (SoundManager.class).getDeclaredField("field_77378_e");
                field.setAccessible(true);
                //int j = (int)(field.getFloat(item) * (petHealFactor * (float)((EntityCreature)entityliving1).enhanced));
                lastSoundID = field.getInt(sndSystem);
                return lastSoundID;//ModLoader.getMinecraftInstance().sndManager.latestSoundID;
            }
            catch (Exception ex)
            {
                try
                {
                    field = (SoundManager.class).getDeclaredField("latestSoundID");
                    field.setAccessible(true);
                    lastSoundID = field.getInt(sndSystem);
                    return lastSoundID;
                }
                catch (Exception ex2)
                {
                    return -1;
                }
            }
        }

        return -1;
    }

    @SideOnly(Side.CLIENT)
    public static void getSoundSystem()
    {
    	sndSystem = FMLClientHandler.instance().getClient().sndManager.sndSystem;
    	soundPool = FMLClientHandler.instance().getClient().sndManager.soundPoolStreaming;
    }
    
    @SideOnly(Side.CLIENT)
    public static int playMovingSound(String var1, float var2, float var3, float var4, float var5, float var6)
    {
    	try {
	        getSoundSystem();
	
	        if (sndSystem != null)
	        {
	            if (var1 != null)
	            {
	                SoundPoolEntry var7 = soundPool.getRandomSoundFromSoundPool(var1);
	
	                if (var7 != null && var5 > 0.0F)
	                {
	                    lastSoundID = (lastSoundID + 1) % 256;
	                    String snd = "sound_" + lastSoundID;
	                    float var9 = 16.0F;
	
	                    if (var5 > 1.0F)
	                    {
	                        var9 *= var5;
	                    }
	                    sndSystem.backgroundMusic(snd, var7.getSoundUrl(), var7.getSoundName(), false);
	                    sndSystem.setVolume(snd, var5 * FMLClientHandler.instance().getClient().gameSettings.soundVolume);
	                    sndSystem.play(snd);
	                }
	            }
	        }
    	} catch (Exception ex) {
        	//despite forcing a getSoundSystem(); call, some are still getting a crash from inside usage of sndSystem.backgroundMusic, this is an attempt to silently catch it
        	//iirc original issue was resource pack switching
    		
    		//second note: issue was still happening, moved try catch to encapsulate entire method, might return invalid sound id now on exception catch
    		
    		//third note: continued issue was where other methods call setVolume, since getSoundSystem is reflectionless, just force it to be called whenever something wants to setVolume or play special sound way
        }

        return lastSoundID;
    }
	
    
}
