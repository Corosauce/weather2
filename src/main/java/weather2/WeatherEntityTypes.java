package weather2;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import weather2.weathersystem.storm.LightningBoltWeatherNew;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeatherEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, Weather.MODID);

    @ObjectHolder(registryName = Weather.MODID + ":lightning_bolt", value = Weather.MODID + ":lightning_bolt")
    public static RegistryObject<EntityType<LightningBoltWeatherNew>> LIGHTNING_BOLT = ENTITY_TYPES.register("lightning_bolt", () -> EntityType.Builder.of(LightningBoltWeatherNew::new, MobCategory.MISC)
            .noSave()
            .sized(0.0F, 0.0F)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
            .build("lightning_bolt"));

    public static void registerHandlers(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
