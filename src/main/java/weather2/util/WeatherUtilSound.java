package weather2.util;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.SoundRegistry;
import weather2.client.sound.MovingSoundStreamingSource;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;

/**
 * TODO: rewrite this to use a class that contains array of sounds, amount of them, length of them, and the last played time and next random index
 * would help cleanup the weird array use this class does
 */
public class WeatherUtilSound {

    public static String snd_tornado_dmg_close[] = new String[3];
    public static String snd_wind_close[] = new String[3];
    public static String snd_wind_far[] = new String[3];
    public static String snd_sandstorm_low[] = new String[2];
    public static String snd_sandstorm_med[] = new String[2];
    public static String snd_sandstorm_high[] = new String[1];
    public static HashMap<String, Integer> soundToLength = new HashMap<>();

    /**
     * These need to match the amount of array'd strings we use for sounds, was 3, now 6 for sandstorm addition
     */
    public static int snd_rand[] = new int[6];
    public static long soundTimer[] = new long[6];
    
    public static void init() {
    	Random rand = new Random();
    	snd_tornado_dmg_close[0] = "destruction_0_";
        snd_tornado_dmg_close[1] = "destruction_1_";
        snd_tornado_dmg_close[2] = "destruction_2_";
        snd_wind_close[0] = "wind_close_0_";
        snd_wind_close[1] = "wind_close_1_";
        snd_wind_close[2] = "wind_close_2_";
        snd_wind_far[0] = "wind_far_0_";
        snd_wind_far[1] = "wind_far_1_";
        snd_wind_far[2] = "wind_far_2_";
        snd_sandstorm_low[0] = "sandstorm_low1";
        snd_sandstorm_low[1] = "sandstorm_low2";
        snd_sandstorm_med[0] = "sandstorm_med1";
        snd_sandstorm_med[1] = "sandstorm_med2";
        snd_sandstorm_high[0] = "sandstorm_high1";
        snd_rand[0] = rand.nextInt(snd_tornado_dmg_close.length);
        snd_rand[1] = rand.nextInt(snd_wind_close.length);
        snd_rand[2] = rand.nextInt(snd_wind_far.length);
        snd_rand[3] = rand.nextInt(snd_sandstorm_high.length);
        snd_rand[4] = rand.nextInt(snd_sandstorm_med.length);
        snd_rand[5] = rand.nextInt(snd_sandstorm_low.length);
        /*soundID[0] = -1;
        soundID[1] = -1;
        soundID[2] = -1;*/
        soundToLength.put(snd_tornado_dmg_close[0], 2515);
        soundToLength.put(snd_tornado_dmg_close[1], 2580);
        soundToLength.put(snd_tornado_dmg_close[2], 2741);
        soundToLength.put(snd_wind_close[0], 4698);
        soundToLength.put(snd_wind_close[1], 7324);
        soundToLength.put(snd_wind_close[2], 6426);
        soundToLength.put(snd_wind_far[0], 12892);
        soundToLength.put(snd_wind_far[1], 9653);
        soundToLength.put(snd_wind_far[2], 12003);
        soundToLength.put(snd_sandstorm_low[0], 8004);
        soundToLength.put(snd_sandstorm_low[1], 7119);
        soundToLength.put(snd_sandstorm_med[0], 16325);
        soundToLength.put(snd_sandstorm_med[1], 12776);
        soundToLength.put(snd_sandstorm_high[0], 23974);

        soundToLength.put("siren_sandstorm_1", 11923);
        soundToLength.put("siren_sandstorm_2", 20122);
        soundToLength.put("siren_sandstorm_3", 10366);
        soundToLength.put("siren_sandstorm_4", 44274);
        soundToLength.put("siren_sandstorm_5_extra", 1282);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void playNonMovingSound(Vec3 parPos, String var1, float var5, float var6, float parCutOffRange)
    {
    	//String prefix = "streaming.";
    	String affix = ".ogg";
    	//ResourceLocation res = new ResourceLocation(var1);
    	SoundEvent event = SoundRegistry.get(var1);
    	MovingSoundStreamingSource sound = new MovingSoundStreamingSource(parPos, event, SoundCategory.WEATHER, var5, var6, parCutOffRange);
    	Minecraft.getInstance().getSoundHandler().play(sound);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void playMovingSound(StormObject parStorm, String var1, float var5, float var6, float parCutOffRange)
    {
    	//String prefix = "streaming.";
    	String affix = ".ogg";
    	
    	//ResourceLocation res = new ResourceLocation(var1);
    	SoundEvent event = SoundRegistry.get(var1);
    	
    	MovingSoundStreamingSource sound = new MovingSoundStreamingSource(parStorm, event, SoundCategory.WEATHER, var5, var6, parCutOffRange);
    	
    	Minecraft.getInstance().getSoundHandler().play(sound);

    }

    @OnlyIn(Dist.CLIENT)
    public static void playPlayerLockedSound(Vec3 parPos, String var1, float var5, float var6)
    {
        SoundEvent event = SoundRegistry.get(var1);
        MovingSoundStreamingSource sound = new MovingSoundStreamingSource(parPos, event, SoundCategory.WEATHER, var5, var6, true);
        Minecraft.getInstance().getSoundHandler().play(sound);
    }
	
    
}
