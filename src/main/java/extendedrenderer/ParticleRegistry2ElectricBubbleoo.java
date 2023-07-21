package extendedrenderer;

import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import weather2.Weather;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry2ElectricBubbleoo {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.PARTICLE_TYPES, Weather.MODID);

    @ObjectHolder(registryName = Weather.MODID + ":acidrain_splash", value = Weather.MODID + ":acidrain_splash")
    public static RegistryObject<SimpleParticleType> ACIDRAIN_SPLASH = PARTICLE_TYPES.register("acidrain_splash", () -> new SimpleParticleType(false));

    public static void registerHandlers(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void registerParticleFactory(RegisterParticleProvidersEvent evt) {
        evt.register(ParticleRegistry2ElectricBubbleoo.ACIDRAIN_SPLASH.get(),
                WaterDropParticle.Provider::new);
    }
}
