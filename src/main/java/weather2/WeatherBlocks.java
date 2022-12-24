package weather2;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import weather2.block.*;
import weather2.blockentity.DeflectorBlockEntity;
import weather2.blockentity.SirenBlockEntity;
import weather2.blockentity.WeatherMachineBlockEntity;

import java.util.Set;

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

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Weather.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Weather.MODID);

    public static final RegistryObject<SandLayerBlock> BLOCK_SAND_LAYER = BLOCKS.register(SAND_LAYER, () -> new SandLayerBlock(BlockBehaviour.Properties.of(Material.SAND).randomTicks().strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.SAND)));
    public static final RegistryObject<DeflectorBlock> BLOCK_DEFLECTOR = BLOCKS.register(DEFLECTOR, () -> new DeflectorBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().strength(1.5F, 6F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    //public static final RegistryObject<SensorBlock> BLOCK_TORNADO_SENSOR = BLOCKS.register(TORNADO_SENSOR, () -> new SensorBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().strength(1.5F, 6F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<SirenBlock> BLOCK_TORNADO_SIREN = BLOCKS.register(TORNADO_SIREN, () -> new SirenBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().strength(1.5F, 6F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    //public static final RegistryObject<WeatherMachineBlock> BLOCK_WEATHER_MACHINE = BLOCKS.register(WEATHER_MACHINE, () -> new WeatherMachineBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().strength(1.5F, 6F).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Weather.MODID);
    public static final RegistryObject<BlockItem> BLOCK_DEFLECTOR_ITEM = ITEMS.register(DEFLECTOR, () -> new BlockItem(BLOCK_DEFLECTOR.get(), new Item.Properties().tab(Weather.CREATIVE_TAB)));
    public static final RegistryObject<BlockItem> BLOCK_TORNADO_SIREN_ITEM = ITEMS.register(TORNADO_SIREN, () -> new BlockItem(BLOCK_TORNADO_SIREN.get(), new Item.Properties().tab(Weather.CREATIVE_TAB)));
    public static final RegistryObject<BlockItem> BLOCK_SAND_LAYER_ITEM = ITEMS.register(SAND_LAYER, () -> new BlockItem(BLOCK_SAND_LAYER.get(), new Item.Properties().tab(Weather.CREATIVE_TAB)));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<DeflectorBlockEntity>> BLOCK_ENTITY_DEFLECTOR = BLOCK_ENTITIES.register(DEFLECTOR, () ->
            BlockEntityType.Builder.of(DeflectorBlockEntity::new, BLOCK_DEFLECTOR.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<SirenBlockEntity>> BLOCK_ENTITY_TORNADO_SIREN = BLOCK_ENTITIES.register(TORNADO_SIREN, () ->
            BlockEntityType.Builder.of(SirenBlockEntity::new, BLOCK_TORNADO_SIREN.get()).build(null));

    /*public static final RegistryObject<BlockEntityType<DeflectorBlockEntity>> BLOCK_ENTITY_TORNADO_SENSOR = BLOCK_ENTITIES.register(TORNADO_SENSOR, () ->
            BlockEntityType.Builder.of(DeflectorBlockEntity::new, BLOCK_TORNADO_SENSOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<WeatherMachineBlockEntity>> BLOCK_ENTITY_WEATHER_MACHINE = BLOCK_ENTITIES.register(WEATHER_MACHINE, () ->
            BlockEntityType.Builder.of(WeatherMachineBlockEntity::new, BLOCK_WEATHER_MACHINE.get()).build(null));*/

    public static PoiType POI_DEFLECTOR;

    public static void registerHandlers(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ITEMS.register(modBus);
    }

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<PoiType> event) {
        String registryName = Weather.MODID + ":" + DEFLECTOR;
        POI_DEFLECTOR = (new PoiType(registryName, PoiType.getBlockStates(BLOCK_DEFLECTOR.get()), 0, 1)).setRegistryName(registryName);
        event.getRegistry().register(POI_DEFLECTOR);
    }

}
