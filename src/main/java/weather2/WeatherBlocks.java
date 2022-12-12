package weather2;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import weather2.block.DeflectorBlock;
import weather2.block.SandLayerBlock;
import weather2.blockentity.DeflectorBlockEntity;

public class WeatherBlocks {

    public static final String SAND_LAYER = "sand_layer";
    public static final String DEFLECTOR = "weather_deflector";

    public static Block blockSandLayer;

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Weather.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Weather.MODID);

    public static final RegistryObject<SandLayerBlock> BLOCK_SAND_LAYER = BLOCKS.register(SAND_LAYER, () -> new SandLayerBlock(BlockBehaviour.Properties.of(Material.SAND).randomTicks().strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.SAND)));
    public static final RegistryObject<DeflectorBlock> BLOCK_DEFLECTOR = BLOCKS.register(DEFLECTOR, () -> new DeflectorBlock(BlockBehaviour.Properties.of(Material.STONE).randomTicks().strength(1.5F, 6F).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    @SuppressWarnings("ConstantConditions") //no datafixer type needed
    public static final RegistryObject<BlockEntityType<DeflectorBlockEntity>> BLOCK_ENTITY_DEFLECTOR = BLOCK_ENTITIES.register(DEFLECTOR, () ->
            BlockEntityType.Builder.of(DeflectorBlockEntity::new, BLOCK_DEFLECTOR.get()).build(null));

    public static void registerHandlers(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    /*@SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        addBlock(event, blockSandLayer = (new SandLayerBlock(BlockBehaviour.Properties.of(Material.SAND).randomTicks().strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.SAND))), sand_layer, false);
    }

    public static void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab) {
        parBlock.setRegistryName(Weather.MODID, unlocalizedName);

        if (event != null) {
            event.getRegistry().register(parBlock);
        }
    }*/

}
