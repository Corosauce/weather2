package weather2;

import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class SoundLoader {
	@ForgeSubscribe
    public void onSound(SoundLoadEvent event) {
		
		//registerSound(event.manager, WeatherMod.modID + ":tornado/destruction.ogg");
		registerSound(event.manager, Weather.modID + ":waterfall.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/destruction_0_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/destruction_1_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/destruction_2_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/destruction_s.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/destructionb.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/siren.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_close.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_close_0_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_close_1_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_close_2_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_far.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_far_0_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_far_1_.ogg");
		registerStreaming(event.manager, Weather.modID + ":tornado/wind_far_2_.ogg");
    }
    
	private void registerSound(SoundManager manager, String path) {
        try {
        	manager.addSound(path);
        } catch (Exception ex) {
            System.out.println(String.format("Warning: unable to load sound file %s", path));
        }
    }
    
    private void registerStreaming(SoundManager manager, String path) {
        try {
            manager.soundPoolStreaming.addSound(path);
        } catch (Exception ex) {
            System.out.println(String.format("Warning: unable to load streaming file %s", path));
        }
    }
    
    private void registerMusic(SoundManager manager, String path) {
        try {
            manager.soundPoolMusic.addSound(path);
        } catch (Exception ex) {
            System.out.println(String.format("Warning: unable to load music file %s", path));
        }
    }

}
