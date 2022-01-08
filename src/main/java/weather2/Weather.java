package weather2;

import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlserverevents.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weather2.util.WeatherUtilSound;

import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Weather.MODID)
public class Weather
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "weather2";

    public static boolean initProperNeededForWorld = true;

    public Weather() {
        // Register the setup method for modloading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStop);
        modBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modBus.register(WeatherBlocks.class);

        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
    }

    private void setup(final FMLCommonSetupEvent event) {
        //DeferredWorkQueue.runLater(WeatherNetworking::register);
        WeatherNetworking.register();

        WeatherControllerManager.setFactory(ServerWeatherController::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        WeatherUtilSound.init();

    }

    @SubscribeEvent
    public void serverStop(FMLServerStoppedEvent event) {
        initProperNeededForWorld = true;
    }

    /*private void addReloadListenersLate(AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) resourceManager -> CookingRegistry.initFoodRegistry(event.getDataPackRegistries().getRecipeManager()));
    }*/

    public static void dbg(Object obj) {
        System.out.println(obj);
    }
}
