package weather2;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import weather2.item.WeatherItem;

public class WeatherItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Weather.MODID);

    //TODO: 1.20
    //public static final RegistryObject<Item> WEATHER_ITEM = ITEMS.register("weather_item", () -> new WeatherItem(new Item.Properties().stacksTo(64), Weather.CREATIVE_TAB));

    public static void registerHandlers(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
