package weather2.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import weather2.SoundRegistry;
import weather2.client.sound.MovingSoundStreamingSource;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;

public class WeatherUtilSound {

    public static String snd_dmg_close[] = new String[3];
    public static String snd_wind_close[] = new String[3];
    public static String snd_wind_far[] = new String[3];
    public static HashMap<String, Integer> soundToLength = new HashMap<>();
    public static int snd_rand[] = new int[3];
    public static long soundTimer[] = new long[3];
    
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
        /*soundID[0] = -1;
        soundID[1] = -1;
        soundID[2] = -1;*/
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
	
	/*@SideOnly(Side.CLIENT)
    public static void setVolume(String soundID, float vol)
    {
		try {
	        if (sndSystem == null)
	        {
	            getSoundSystem();
	        //}
	
	        if (sndSystem != null)
	        {
	            sndSystem.setVolume(new StringBuilder().append(soundID).toString(), vol * FMLClientHandler.instance().getClient().gameSettings.getSoundLevel(SoundCategory.WEATHER));
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
        if (sndSystem == null)
        {
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
    }*/

    /*@SideOnly(Side.CLIENT)
    public static void getSoundSystem()
    {
    	SoundManager temp = (SoundManager) OldUtil.getPrivateValueBoth(SoundHandler.class, FMLClientHandler.instance().getClient().getSoundHandler(), "field_147694_f", "sndManager");
    	sndSystem = (SoundSystem) OldUtil.getPrivateValueBoth(SoundManager.class, temp, "field_148620_e", "sndSystem");
    	//soundPool = FMLClientHandler.instance().getClient().sndManager.soundPoolStreaming;
    }*/
    
    @SideOnly(Side.CLIENT)
    public static void playNonMovingSound(Vec3 parPos, String var1, float var5, float var6, float parCutOffRange)
    {
    	//String prefix = "streaming.";
    	String affix = ".ogg";
    	//ResourceLocation res = new ResourceLocation(var1);
    	SoundEvent event = SoundRegistry.get(var1);
    	MovingSoundStreamingSource sound = new MovingSoundStreamingSource(parPos, event, SoundCategory.WEATHER, var5, var6, parCutOffRange);
    	FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    }
    
    @SideOnly(Side.CLIENT)
    public static void playMovingSound(StormObject parStorm, String var1, float var5, float var6, float parCutOffRange)
    {
    	//String prefix = "streaming.";
    	String affix = ".ogg";
    	
    	//ResourceLocation res = new ResourceLocation(var1);
    	SoundEvent event = SoundRegistry.get(var1);
    	
    	MovingSoundStreamingSource sound = new MovingSoundStreamingSource(parStorm, event, SoundCategory.WEATHER, var5, var6, parCutOffRange);
    	
    	FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    	
    	/*try {
	        //getSoundSystem();
	
	        if (sndSystem != null)
	        {
	            if (var1 != null)
	            {
	                if (var5 > 0.0F)
	                {
	                    lastSoundID = (lastSoundID + 1) % 256;
	                    String snd = "sound_" + lastSoundID;
	                    float var9 = 16.0F;
	
	                    if (var5 > 1.0F)
	                    {
	                        var9 *= var5;
	                    }
	                    //sndSystem.backgroundMusic(snd, new URL(prefix + var1 + affix), var1 + affix, false);
	                    sndSystem.newStreamingSource(true, var1, getURLForSoundResource(res), snd, false, var2, var3, var4, ISound.AttenuationType.NONE.getTypeInt(), 1F);
	                    sndSystem.setVolume(snd, var5 * FMLClientHandler.instance().getClient().gameSettings.getSoundLevel(SoundCategory.WEATHER));
	                    sndSystem.play(snd);
	                    System.out.println("testing play sound: " + var1);
	                }
	            }
	        }
    	} catch (Exception ex) {
    		ex.printStackTrace();
        	//despite forcing a getSoundSystem(); call, some are still getting a crash from inside usage of sndSystem.backgroundMusic, this is an attempt to silently catch it
        	//iirc original issue was resource pack switching
    		
    		//second note: issue was still happening, moved try catch to encapsulate entire method, might return invalid sound id now on exception catch
    		
    		//third note: continued issue was where other methods call setVolume, since getSoundSystem is reflectionless, just force it to be called whenever something wants to setVolume or play special sound way
        }*/
    }
    
    private static URL getURLForSoundResource(final ResourceLocation p_148612_0_)
    {
        String s = String.format("%s:%s:%s", new Object[] {"mcsounddomain", p_148612_0_.getResourceDomain(), p_148612_0_.getResourcePath()});
        URLStreamHandler urlstreamhandler = new URLStreamHandler()
        {
            private static final String __OBFID = "CL_00001143";
            protected URLConnection openConnection(final URL par1URL)
            {
                return new URLConnection(par1URL)
                {
                    private static final String __OBFID = "CL_00001144";
                    public void connect() {}
                    public InputStream getInputStream() throws IOException
                    {
                        return Minecraft.getMinecraft().getResourceManager().getResource(p_148612_0_).getInputStream();
                    }
                };
            }
        };

        try
        {
            return new URL((URL)null, s, urlstreamhandler);
        }
        catch (MalformedURLException malformedurlexception)
        {
            throw new Error("TODO: Sanely handle url exception! :D");
        }
    }
	
    
}
