package weather2;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundRegistry {

	private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<String, SoundEvent>();

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
			SoundRegistry.init();
		}
	}

	public static void init() {
		register("env.waterfall");
		register("env.wind_calm");
		register("env.wind_calmfade");
		register("streaming.destruction");
		register("streaming.destruction_0_");
		register("streaming.destruction_1_");
		register("streaming.destruction_2_");
		register("streaming.destruction_s");
		register("streaming.destructionb");
		register("streaming.siren");
		register("streaming.wind_close");
		register("streaming.wind_close_0_");
		register("streaming.wind_close_1_");
		register("streaming.wind_close_2_");
		register("streaming.wind_far");
		register("streaming.wind_far_0_");
		register("streaming.wind_far_1_");
		register("streaming.wind_far_2_");

		register("streaming.sandstorm_high1");
		register("streaming.sandstorm_med1");
		register("streaming.sandstorm_med2");
		register("streaming.sandstorm_low1");
		register("streaming.sandstorm_low2");

		register("streaming.siren_sandstorm_1");
		register("streaming.siren_sandstorm_2");
		register("streaming.siren_sandstorm_3");
		register("streaming.siren_sandstorm_4");
		register("streaming.siren_sandstorm_5_extra");
		register("streaming.siren_sandstorm_6_extra");
		
	}

	public static void register(String soundPath) {
		ResourceLocation resLoc = new ResourceLocation(Weather.MODID, soundPath);
		SoundEvent event = new SoundEvent(resLoc).setRegistryName(resLoc);
		ForgeRegistries.SOUND_EVENTS.register(event);
		if (lookupStringToEvent.containsKey(soundPath)) {
			System.out.println("WEATHER SOUNDS WARNING: duplicate sound registration for " + soundPath);
		}
		lookupStringToEvent.put(soundPath, event);
	}

	public static SoundEvent get(String soundPath) {
		return lookupStringToEvent.get(soundPath);
	}

}
