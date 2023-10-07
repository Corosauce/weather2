package extendedrenderer;

import weather2.DeferredHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import weather2.Weather;

//@ObjectHolder(Weather.MODID)
@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry2ElectricBubbleoo {

    //@ObjectHolder("acidrain_splash")
    //public static SimpleParticleType ACIDRAIN_SPLASH;
    public static final RegistryObject<SimpleParticleType> ACIDRAIN_SPLASH = Weather.R.particle("acidrain_splash", () -> new SimpleParticleType(false));

    /*@SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> evt){
        SimpleParticleType acidrain_splash = new SimpleParticleType(false);
        acidrain_splash.setRegistryName(Weather.MODID, "acidrain_splash");
        evt.getRegistry().register(acidrain_splash);
    }*/

    /*@OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerParticleFactory(ParticleFactoryRegisterEvent evt){
        Minecraft.getInstance().particleEngine.register(ParticleRegistry2ElectricBubbleoo.ACIDRAIN_SPLASH.get(),
                WaterDropParticle.Provider::new);
    }*/

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void factories(RegisterParticleProvidersEvent event) {
        //event.registerSprite(ACIDRAIN_SPLASH.get(), WaterDropParticleImpl::new);
        Minecraft.getInstance().particleEngine.register(new SimpleParticleType(false), WaterDropParticle.Provider::new);
        //event.registerSpecial(new SimpleParticleType(false), WaterDropParticle.Provider::new);
    }

    public static void bootstrap() {}
}
