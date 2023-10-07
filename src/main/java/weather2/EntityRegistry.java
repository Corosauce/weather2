package weather2;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import weather2.weathersystem.storm.LightningBoltWeatherNew;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegistry {

    /*@ObjectHolder(Weather.MODID + ":lightning_bolt")
    public static EntityType<LightningBoltWeatherNew> lightning_bolt;*/

    public static final RegistryObject<EntityType<LightningBoltWeatherNew>> LIGHTNING_BOLT = Weather.R.entity("lightning_bolt", () -> EntityType.Builder
            .<LightningBoltWeatherNew>of(LightningBoltWeatherNew::new, MobCategory.MISC)
            .noSave()
            .sized(0.0F, 0.0F)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
            .build("gateway"));
    /*
    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityType<?>> e) {
        IForgeRegistry<EntityType<?>> r = e.getRegistry();
        r.register(
                EntityType.Builder.of(LightningBoltWeatherNew::new, MobCategory.MISC)
                        .noSave()
                        .sized(0.0F, 0.0F)
                        .clientTrackingRange(16)
                        .updateInterval(Integer.MAX_VALUE)
                        .build("lightning_bolt")
                        .setRegistryName("lightning_bolt"));
    }*/

}
