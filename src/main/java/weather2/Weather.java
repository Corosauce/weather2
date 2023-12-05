package weather2;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.IConfigCategory;
import com.mojang.brigadier.CommandDispatcher;
import extendedrenderer.ParticleRegistry2ElectricBubbleoo;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weather2.command.WeatherCommand;
import weather2.config.*;
import weather2.data.BlockAndItemProvider;
import weather2.data.BlockLootTables;
import weather2.data.WeatherRecipeProvider;
import weather2.util.WeatherUtilSound;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Weather.MODID)
public class Weather
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredHelper R = DeferredHelper.create(Weather.MODID);

    public static final String MODID = "weather2";

    public static boolean initProperNeededForWorld = true;

    public static List<IConfigCategory> listConfigs = new ArrayList<>();
    public static ConfigMisc configMisc = null;

    //public static final CreativeModeTab CREATIVE_TAB = new WeatherTab();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> WEATHER_TAB = CREATIVE_MODE_TABS.register("weather_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.weather2"))
            .icon(() -> WeatherItems.WEATHER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WeatherItems.WEATHER_ITEM.get());
                output.accept(WeatherItems.BLOCK_TORNADO_SIREN_ITEM.get());
                output.accept(WeatherItems.BLOCK_TORNADO_SENSOR_ITEM.get());
                output.accept(WeatherItems.BLOCK_DEFLECTOR_ITEM.get());
                output.accept(WeatherItems.BLOCK_FORECAST_ITEM.get());
                output.accept(WeatherItems.BLOCK_SAND_LAYER_ITEM.get());
                output.accept(WeatherItems.BLOCK_ANEMOMETER_ITEM.get());
                output.accept(WeatherItems.BLOCK_WIND_VANE_ITEM.get());
                output.accept(WeatherItems.BLOCK_WIND_TURBINE_ITEM.get());
            }).build());

    public Weather() {

        ParticleRegistry2ElectricBubbleoo.bootstrap();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStop);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::gatherData);
        modBus.addListener(this::processIMC);
        modBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
        WeatherBlocks.registerHandlers(modBus);
        WeatherItems.registerHandlers(modBus);

        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.register(new WeatherBlocks());

        new File("./config/Weather2").mkdirs();
        configMisc = new ConfigMisc();
        ConfigMod.addConfigFile(MODID, addConfig(configMisc));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigWind()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigSand()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigSnow()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigStorm()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigTornado()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigParticle()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigDebug()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigSound()));
        //ConfigMod.addConfigFile(MODID, addConfig(new ConfigFoliage()));
        //WeatherUtilConfig.nbtLoadDataAll();

        SoundRegistry.init();

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ParticleRegistry::getRegisteredParticles);
            modBus.addListener(ClientRegistry::registerLayerDefinitions);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(WeatherItems.WEATHER_ITEM);
    }

    public static IConfigCategory addConfig(IConfigCategory config) {
        listConfigs.add(config);
        return config;
    }

    private void setup(final FMLCommonSetupEvent event) {
        WeatherNetworking.register();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        WeatherUtilSound.init();

    }

    private void processIMC(final InterModProcessEvent event)
    {
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void serverStart(ServerStartedEvent event) {
        //initProperNeededForWorld = true;
        //WeatherUtil.testAllBlocks();
    }

    @SubscribeEvent
    public void serverStop(ServerStoppedEvent event) {
        initProperNeededForWorld = true;
    }

    public static void dbg(Object obj) {
        CULog.dbg("" + obj);
    }

    public static boolean isLoveTropicsInstalled() {
        return ModList.get().isLoaded("ltminigames");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        WeatherCommand.register(dispatcher);
    }


    /**
     *
     * run runData for me
     *
     * @param event
     */
    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeServer()) {
            gen.addProvider(event.includeServer(), new WeatherRecipeProvider(gen.getPackOutput()));
            gen.addProvider(event.includeServer(), new LootTableProvider(gen.getPackOutput(), Collections.emptySet(),
                    List.of(new LootTableProvider.SubProviderEntry(BlockLootTables::new, LootContextParamSets.BLOCK))));
        }
        if (event.includeClient()) {
            gatherClientData(event);
        }
    }

    /**
     *
     * run runData for me
     *
     * @param event
     */
    @OnlyIn(Dist.CLIENT)
    private void gatherClientData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        gen.addProvider(event.includeClient(), new ParticleRegistry(packOutput, existingFileHelper));
        gen.addProvider(event.includeClient(), new BlockAndItemProvider(packOutput, existingFileHelper));
    }
}
