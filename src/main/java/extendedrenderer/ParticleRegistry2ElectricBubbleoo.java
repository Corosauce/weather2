package extendedrenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import weather2.Weather;

@ObjectHolder(Weather.MODID)
@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry2ElectricBubbleoo {

    @ObjectHolder("acidrain_splash")
    public static SimpleParticleType ACIDRAIN_SPLASH;

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt){
        SimpleParticleType acidrain_splash = new SimpleParticleType(false);
        acidrain_splash.setRegistryName(Weather.MODID, "acidrain_splash");
        evt.getRegistry().register(acidrain_splash);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent evt){
        Minecraft.getInstance().particleEngine.register(ParticleRegistry2ElectricBubbleoo.ACIDRAIN_SPLASH,
                WaterDropParticle.Provider::new);
    }
}
