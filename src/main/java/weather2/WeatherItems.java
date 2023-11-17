package weather2;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import weather2.item.WeatherItem;

public class WeatherItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Weather.MODID);

    public static final RegistryObject<Item> WEATHER_ITEM = ITEMS.register(WeatherBlocks.WEATHER_ITEM, () -> new WeatherItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<BlockItem> BLOCK_DEFLECTOR_ITEM = WeatherItems.ITEMS.register(WeatherBlocks.DEFLECTOR, () -> new BlockItem(WeatherBlocks.BLOCK_DEFLECTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_TORNADO_SIREN_ITEM = ITEMS.register(WeatherBlocks.TORNADO_SIREN, () -> new BlockItem(WeatherBlocks.BLOCK_TORNADO_SIREN.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_TORNADO_SENSOR_ITEM = ITEMS.register(WeatherBlocks.TORNADO_SENSOR, () -> new BlockItem(WeatherBlocks.BLOCK_TORNADO_SENSOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_SAND_LAYER_ITEM = ITEMS.register(WeatherBlocks.SAND_LAYER, () -> new BlockItem(WeatherBlocks.BLOCK_SAND_LAYER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_FORECAST_ITEM = ITEMS.register(WeatherBlocks.WEATHER_FORECAST, () -> new BlockItem(WeatherBlocks.BLOCK_FORECAST.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_ANEMOMETER_ITEM = ITEMS.register(WeatherBlocks.ANEMOMETER, () -> new BlockItem(WeatherBlocks.BLOCK_ANEMOMETER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_WIND_VANE_ITEM = ITEMS.register(WeatherBlocks.WIND_VANE, () -> new BlockItem(WeatherBlocks.BLOCK_WIND_VANE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLOCK_WIND_TURBINE_ITEM = ITEMS.register(WeatherBlocks.WIND_TURBINE, () -> new BlockItem(WeatherBlocks.BLOCK_WIND_TURBINE.get(), new Item.Properties()));

    public static void registerHandlers(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
