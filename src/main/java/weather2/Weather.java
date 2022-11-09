package weather2;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import com.corosus.modconfig.IConfigCategory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weather2.command.WeatherCommand;
import weather2.config.*;
import weather2.util.WeatherUtilSound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Weather.MODID)
public class Weather
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "weather2";

    public static boolean initProperNeededForWorld = true;

    public static List<IConfigCategory> listConfigs = new ArrayList<>();
    public static ConfigMisc configMisc = null;

    public Weather() {
        // Register the setup method for modloading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStop);
        modBus.addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
        modBus.register(WeatherBlocks.class);

        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        new File("./config/Weather2").mkdirs();
        configMisc = new ConfigMisc();
        ConfigMod.addConfigFile(MODID, addConfig(configMisc));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigWind()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigSand()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigSnow()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigStorm()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigTornado()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigParticle()));
        ConfigMod.addConfigFile(MODID, addConfig(new ConfigFoliage()));
        //WeatherUtilConfig.nbtLoadDataAll();
    }

    public static IConfigCategory addConfig(IConfigCategory config) {
        listConfigs.add(config);
        return config;
    }

    private void setup(final FMLCommonSetupEvent event) {
        //DeferredWorkQueue.runLater(WeatherNetworking::register);
        WeatherNetworking.register();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        WeatherUtilSound.init();

    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void serverStop(ServerStoppedEvent event) {
        initProperNeededForWorld = true;
    }

    /*private void addReloadListenersLate(AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) resourceManager -> CookingRegistry.initFoodRegistry(event.getDataPackRegistries().getRecipeManager()));
    }*/

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
}
