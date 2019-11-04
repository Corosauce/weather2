package weather2;

import CoroUtil.util.CoroUtilFile;
import modconfig.IConfigCategory;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weather2.config.*;
import weather2.player.PlayerData;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerServer;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Weather.MODID)
public class Weather
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "weather2";

    public static List<IConfigCategory> listConfigs = new ArrayList<>();

    public static ConfigMisc configMisc = null;

    public static CommonProxy proxy;

    public static boolean initProperNeededForWorld = true;

    public Weather() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStarting);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStart);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStop);

        MinecraftForge.EVENT_BUS.register(this);

        // Register ourselves for server and other game events we are interested in
        /*MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());*/

        MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());

        configMisc = new ConfigMisc();
        //TODO: 1.14 uncomment
        /*ConfigMod.addConfigFile(addConfig(configMisc));
        ConfigMod.addConfigFile(addConfig(new ConfigWind()));
        ConfigMod.addConfigFile(addConfig(new ConfigSand()));
        ConfigMod.addConfigFile(addConfig(new ConfigSnow()));
        ConfigMod.addConfigFile(addConfig(new ConfigStorm()));
        ConfigMod.addConfigFile(addConfig(new ConfigTornado()));
        ConfigMod.addConfigFile(addConfig(new ConfigParticle()));
        ConfigMod.addConfigFile(addConfig(new ConfigFoliage()));*/
        //TODO: 1.14 uncomment
        /*WeatherUtilConfig.nbtLoadDataAll();*/


    }

    /**
     * To work around the need to force a configmod refresh on these when EZ GUI changes values
     *
     * @param config
     * @return
     */
    public static IConfigCategory addConfig(IConfigCategory config) {
        listConfigs.add(config);
        return config;
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code

        proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());
        proxy.init();

        //CapabilityManager.INSTANCE.register(IChunkData.class, new ChunkDataStorage(), DefaultChunkCapData::new);
        //WorldPersistenceHooks.addHook(new EDGWorldPeristenceHook());

        DeferredWorkQueue.runLater(WeatherNetworking::register);

        //moved from common proxy
        //SoundRegistry.init();
        WeatherUtilConfig.processLists();
        //TODO: 1.14 need for LT? addMapping(EntityLightningBolt.class, "weather2_lightning_bolt", 2, 512, 5, true);
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        CommandWeather2.register(event.getCommandDispatcher());
        //event.registerServerCommand(new CommandWeather2Old());
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartedEvent event) {

    }

    @SubscribeEvent
    public void serverStop(FMLServerStoppedEvent event) {
        writeOutData(true);
        resetStates();

        initProperNeededForWorld = true;
    }

    public static void initTry() {
        if (initProperNeededForWorld) {
            LOGGER.info("Weather2: being reinitialized");
            initProperNeededForWorld = false;
            CoroUtilFile.getWorldFolderName();

            ServerTickHandler.initialize();
        }
    }

    public static void resetStates() {
        ServerTickHandler.reset();
    }

    public static void writeOutData(boolean unloadInstances) {
        //write out overworld only, because only dim with volcanos planned
        try {
            WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
            if (wm != null) {
                wm.writeToFile();
            }
            PlayerData.writeAllPlayerNBT(unloadInstances);
            //doesnt cover all needs, client connected to server needs this called from gui close too
            //maybe dont call this from here so client connected to server doesnt override what a client wants his 'server' settings to be in his singleplayer world
            //factoring in we dont do per world settings for this
            //WeatherUtilConfig.nbtSaveDataAll();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doClientStuff(final FMLClientSetupEvent event) {

    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here

        }
    }

    public static void dbg(Object obj) {
    if (ConfigMisc.consoleDebug) {
        LOGGER.info(obj);
    }
}

    public static void dbgStackTrace() {
        if (ConfigMisc.consoleDebug) {
            StackTraceElement[] arr = Thread.currentThread().getStackTrace();
            for (StackTraceElement ele : arr) {
                LOGGER.error(ele.toString());
            }
        }
    }
}
