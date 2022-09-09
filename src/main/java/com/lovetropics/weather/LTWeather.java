package com.lovetropics.weather;

import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LTWeather.MODID)
public class LTWeather
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "ltweather";

    public static boolean initProperNeededForWorld = true;

    public LTWeather() {
        // Register the setup method for modloading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStop);
        modBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        //DeferredWorkQueue.runLater(WeatherNetworking::register);
        LTWeatherNetworking.register();

        WeatherControllerManager.setFactory(ServerWeatherController::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public void serverStop(ServerStoppedEvent event) {
        initProperNeededForWorld = true;
    }

    public static void dbg(Object obj) {
        System.out.println(obj);
    }
}
