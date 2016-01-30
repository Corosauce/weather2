package weather2;

import modconfig.ConfigMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import weather2.config.ConfigMisc;
import weather2.player.PlayerData;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerServer;
import CoroUtil.util.CoroUtilFile;

//@NetworkMod(channels = { "WeatherData", "EZGuiData" }, clientSideRequired = true, serverSideRequired = true, packetHandler = WeatherPacketHandler.class)
@Mod(modid = "weather2", name="weather2", version="v2.3.10")
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
    	
    	MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
		
    	ConfigMod.addConfigFile(event, "weather2Misc", new ConfigMisc());
    	WeatherUtilConfig.nbtLoadDataAll();
    }
    
	@Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    	MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
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
