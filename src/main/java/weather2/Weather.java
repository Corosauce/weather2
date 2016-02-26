package weather2;

import java.util.ArrayList;
import java.util.List;

import modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import weather2.config.ConfigMisc;
import weather2.player.PlayerData;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerServer;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;

//@NetworkMod(channels = { "WeatherData", "EZGuiData" }, clientSideRequired = true, serverSideRequired = true, packetHandler = WeatherPacketHandler.class)
@Mod(modid = "weather2", name="weather2", version="v2.3.11")
public class Weather {
	
	@Mod.Instance( value = "weather2" )
	public static Weather instance;
	public static String modID = "weather2";
	
	public static long lastWorldTime;
    
    /** For use in preInit ONLY */
    public Configuration preInitConfig;

    @SidedProxy(clientSide = "weather2.ClientProxy", serverSide = "weather2.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForWorld = true;
    
    public static String eventChannelName = "weather2";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	eventChannel.register(new EventHandlerPacket());
    	MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
		
    	ConfigMod.addConfigFile(event, "weather2Misc", new ConfigMisc());
    	WeatherUtilConfig.nbtLoadDataAll();
    }
    
	@Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    	
    }
    
    @Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandWeather2());
    }
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	writeOutData(true);
    	resetStates();
    	
    	initProperNeededForWorld = true;
    }
    
    public static void initTry() {
    	if (initProperNeededForWorld) {
    		System.out.println("Weather2 being reinitialized");
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

	/**
	 * Triggered when communicating with other mods
	 * @param event
	 */
    @EventHandler
    public void handleIMCMessages(IMCMessage event) {

    	
    	
    }
	
	public static void dbg(Object obj) {
		if (ConfigMisc.consoleDebug) {
			System.out.println(obj);
		}
	}

}
