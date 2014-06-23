package weather2;

import modconfig.ConfigMod;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import weather2.config.ConfigMisc;
import weather2.player.PlayerData;
import weather2.util.WeatherUtilConfig;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;

@NetworkMod(channels = { "WeatherData", "EZGuiData" }, clientSideRequired = true, serverSideRequired = true, packetHandler = WeatherPacketHandler.class)
@Mod(modid = "weather2", name="weather2", version="v2.2.1")
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
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, "weather2Misc", new ConfigMisc());
    	WeatherUtilConfig.nbtLoadDataAll();
    }
    
    @Init
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    	MinecraftForge.EVENT_BUS.register(new Weather2EventHandler());
    	GameRegistry.registerPlayerTracker(new WeatherPlayerTracker());
    	
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
    		ServerTickHandler.lookupDimToWeatherMan.get(0).writeToFile();
    		PlayerData.writeAllPlayerNBT(unloadInstances);
    		//doesnt cover all needs, client connected to server needs this called from gui close too
    		//maybe dont call this from here so client connected to server doesnt override what a client wants his 'server' settings to be in his singleplayer world
    		//factoring in we dont do per world settings for this
    		//WeatherUtilConfig.nbtSaveDataAll();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
	
	public static void dbg(Object obj) {
		if (ConfigMisc.consoleDebug) {
			System.out.println(obj);
		}
	}

}
