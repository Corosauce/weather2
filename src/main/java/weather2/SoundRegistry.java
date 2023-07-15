package weather2;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class SoundRegistry {
    @Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegisterEvent event) {
            SoundRegistry.init(event);
        }
    }

    public static void init(RegisterEvent event) {
        register(event, "env.waterfall");
        register(event, "env.wind_calm");
        register(event, "env.wind_calmfade");
        register(event, "streaming.destruction");
        register(event, "streaming.destruction_0_");
        register(event, "streaming.destruction_1_");
        register(event, "streaming.destruction_2_");
        register(event, "streaming.destruction_s");
        register(event, "streaming.destructionb");
        register(event, "streaming.siren");
        register(event, "streaming.wind_close");
        register(event, "streaming.wind_close_0_");
        register(event, "streaming.wind_close_1_");
        register(event, "streaming.wind_close_2_");
        register(event, "streaming.wind_far");
        register(event, "streaming.wind_far_0_");
        register(event, "streaming.wind_far_1_");
        register(event, "streaming.wind_far_2_");

        register(event, "streaming.sandstorm_high1");
        register(event, "streaming.sandstorm_med1");
        register(event, "streaming.sandstorm_med2");
        register(event, "streaming.sandstorm_low1");
        register(event, "streaming.sandstorm_low2");

        register(event, "streaming.siren_sandstorm_1");
        register(event, "streaming.siren_sandstorm_2");
        register(event, "streaming.siren_sandstorm_3");
        register(event, "streaming.siren_sandstorm_4");
        register(event, "streaming.siren_sandstorm_5_extra");
        register(event, "streaming.siren_sandstorm_6_extra");

    }

    public static void register(RegisterEvent regEvent, String soundPath) {
        ResourceLocation resLoc = new ResourceLocation(Weather.MODID, soundPath);
        SoundEvent event = new SoundEvent(resLoc);
        regEvent.register(ForgeRegistries.Keys.SOUND_EVENTS, resLoc, () -> event);
    }

	public static SoundEvent get(String soundPath) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Weather.MODID, soundPath));
	}

}
