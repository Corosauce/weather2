package weather2;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import weather2.block.*;
import weather2.blockentity.*;
import weather2.item.WeatherItem;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeatherBlocks {

    public static final String SAND_LAYER = "sand_layer";
    public static final String DEFLECTOR = "weather_deflector";
    public static final String TORNADO_SENSOR = "tornado_sensor";
    public static final String TORNADO_SIREN = "tornado_siren";
    public static final String WEATHER_MACHINE = "weather_machine";

    public static final String WEATHER_FORECAST = "weather_forecast";
    public static final String WIND_VANE = "wind_vane";
    public static final String ANEMOMETER = "anemometer";
    public static final String TORNADO_SIREN_MANUAL = "tornado_siren_manual";

    public static final String SAND_LAYER_PLACEABLE = "sand_layer_placeable";
    public static final String WEATHER_ITEM = "weather_item";
    public static final String POCKET_SAND = "pocket_sand";
    public static final String WIND_TURBINE = "wind_turbine";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Weather.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Weather.MODID);

    public static final RegistryObject<SandLayerBlock> BLOCK_SAND_LAYER = BLOCKS.register(SAND_LAYER, () -> new SandLayerBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.SAND).strength(0.1F).sound(SoundType.SAND)));
    public static final RegistryObject<DeflectorBlock> BLOCK_DEFLECTOR = BLOCKS.register(DEFLECTOR, () -> new DeflectorBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<ForecastBlock> BLOCK_FORECAST = BLOCKS.register(WEATHER_FORECAST, () -> new ForecastBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<SensorBlock> BLOCK_TORNADO_SENSOR = BLOCKS.register(TORNADO_SENSOR, () -> new SensorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<AnemometerBlock> BLOCK_ANEMOMETER = BLOCKS.register(ANEMOMETER, () -> new AnemometerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<WindVaneBlock> BLOCK_WIND_VANE = BLOCKS.register(WIND_VANE, () -> new WindVaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<SirenBlock> BLOCK_TORNADO_SIREN = BLOCKS.register(TORNADO_SIREN, () -> new SirenBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    public static final RegistryObject<WindTurbineBlock> BLOCK_WIND_TURBINE = BLOCKS.register(WIND_TURBINE, () -> new WindTurbineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));
    //public static final RegistryObject<WeatherMachineBlock> BLOCK_WEATHER_MACHINE = BLOCKS.register(WEATHER_MACHINE, () -> new WeatherMachineBlock(BlockBehaviour.Properties.of(Material.STONE).strength(0.5F, 6F).sound(SoundType.STONE)));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<DeflectorBlockEntity>> BLOCK_ENTITY_DEFLECTOR = BLOCK_ENTITIES.register(DEFLECTOR, () ->
            BlockEntityType.Builder.of(DeflectorBlockEntity::new, BLOCK_DEFLECTOR.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<SirenBlockEntity>> BLOCK_ENTITY_TORNADO_SIREN = BLOCK_ENTITIES.register(TORNADO_SIREN, () ->
            BlockEntityType.Builder.of(SirenBlockEntity::new, BLOCK_TORNADO_SIREN.get()).build(null));

    public static final RegistryObject<BlockEntityType<SensorBlockEntity>> BLOCK_ENTITY_TORNADO_SENSOR = BLOCK_ENTITIES.register(TORNADO_SENSOR, () ->
            BlockEntityType.Builder.of(SensorBlockEntity::new, BLOCK_TORNADO_SENSOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<AnemometerBlockEntity>> BLOCK_ENTITY_ANEMOMETER = BLOCK_ENTITIES.register(ANEMOMETER, () ->
            BlockEntityType.Builder.of(AnemometerBlockEntity::new, BLOCK_ANEMOMETER.get()).build(null));

    public static final RegistryObject<BlockEntityType<WindVaneBlockEntity>> BLOCK_ENTITY_WIND_VANE = BLOCK_ENTITIES.register(WIND_VANE, () ->
            BlockEntityType.Builder.of(WindVaneBlockEntity::new, BLOCK_WIND_VANE.get()).build(null));

    public static final RegistryObject<BlockEntityType<WindTurbineBlockEntity>> BLOCK_ENTITY_WIND_TURBINE = BLOCK_ENTITIES.register(WIND_TURBINE, () ->
            BlockEntityType.Builder.of(WindTurbineBlockEntity::new, BLOCK_WIND_TURBINE.get()).build(null));

    /*public static final RegistryObject<BlockEntityType<WeatherMachineBlockEntity>> BLOCK_ENTITY_WEATHER_MACHINE = BLOCK_ENTITIES.register(WEATHER_MACHINE, () ->
            BlockEntityType.Builder.of(WeatherMachineBlockEntity::new, BLOCK_WEATHER_MACHINE.get()).build(null));*/

    public static void registerHandlers(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

}
