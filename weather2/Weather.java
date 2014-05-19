package weather2;

import CoroUtil.util.CoroUtilFile;

import modconfig.ConfigMod;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import weather2.config.ConfigMisc;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;

@NetworkMod(channels = { "WeatherData" }, clientSideRequired = true, serverSideRequired = true, packetHandler = WeatherPacketHandler.class)
@Mod(modid = "weather2", name="weather2", version="v2.0")
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
    }
    
    @Init
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    	MinecraftForge.EVENT_BUS.register(new Weather2EventHandler());
    	//TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
    	
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
    	
    	initProperNeededForWorld = true;
    }
    
    public static void initTry() {
    	if (initProperNeededForWorld) {
    		System.out.println("Weather2 being reinitialized");
    		initProperNeededForWorld = false;
	    	CoroUtilFile.getWorldFolderName();
	    	
	    	if (ServerTickHandler.lookupDimToWeatherMan.get(0) == null) {
	    		ServerTickHandler.addWorldToWeather(0);
	    	}
	    	
	    	ServerTickHandler.lookupDimToWeatherMan.get(0).readFromFile();
    	}
    }
    
    public static void writeOutData(boolean unloadInstances) {
    	//write out overworld only, because only dim with volcanos planned
    	ServerTickHandler.lookupDimToWeatherMan.get(0).writeToFile();
    }
	
	public static void dbg(Object obj) {
		//if (ZAConfig.debugConsole) {
			System.out.println(obj);
		//}
	}

}
