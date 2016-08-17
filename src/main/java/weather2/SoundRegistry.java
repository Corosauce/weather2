package weather2;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SoundRegistry {

	private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<String, SoundEvent>();

	public static void init() {
		register("env.waterfall");
		register("env.wind_calm");
		register("env.wind_calmfade");
	}

	public static void register(String soundPath) {
		ResourceLocation resLoc = new ResourceLocation(Weather.modID, soundPath);
		SoundEvent event = new SoundEvent(resLoc);
		GameRegistry.register(event, resLoc);
		if (lookupStringToEvent.containsKey(soundPath)) {
			System.out.println("WEATHER SOUNDS WARNING: duplicate sound registration for " + soundPath);
		}
		lookupStringToEvent.put(soundPath, event);
	}

	public static SoundEvent get(String soundPath) {
		return lookupStringToEvent.get(soundPath);
	}

}
