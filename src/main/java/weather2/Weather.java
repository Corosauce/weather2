package weather2;

import modconfig.ConfigMod;
import modconfig.IConfigCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import weather2.config.*;
import weather2.item.ItemSandLayer;
import weather2.item.ItemWeatherRecipe;
import weather2.player.PlayerData;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerServer;
import CoroUtil.util.CoroUtilFile;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = "weather2", name="weather2", version=Weather.version, dependencies="required-after:coroutil@[1.12.1-1.2.12,)")
public class Weather {
	
	@Mod.Instance( value = Weather.modID )
	public static Weather instance;

	public static final String modID = "weather2";
	public static final String version = "${version}";

    @SidedProxy(clientSide = "weather2.ClientProxy", serverSide = "weather2.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForWorld = true;
    
    public static String eventChannelName = "weather2";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);

	public static List<IConfigCategory> listConfigs = new ArrayList<>();

	public static ConfigMisc configMisc = null;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	eventChannel.register(new EventHandlerPacket());
    	
    	MinecraftForge.EVENT_BUS.register(new EventHandlerFML());
		MinecraftForge.EVENT_BUS.register(new EventHandlerForge());

		configMisc = new ConfigMisc();
    	ConfigMod.addConfigFile(event, addConfig(configMisc));
		ConfigMod.addConfigFile(event, addConfig(new ConfigWind()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigSand()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigSnow()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigStorm()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigTornado()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigParticle()));
		ConfigMod.addConfigFile(event, addConfig(new ConfigFoliage()));
    	WeatherUtilConfig.nbtLoadDataAll();

		proxy.preInit();
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
    
	@Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init();
    }

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();

		//setting last state to track after configs load, but before ticking that uses it
		EventHandlerFML.extraGrassLast = ConfigFoliage.extraGrass;
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
    		System.out.println("Weather2: being reinitialized");
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
    @Mod.EventHandler
    public void handleIMCMessages(FMLInterModComms.IMCEvent event) {

    	
    	
    }
	
	public static void dbg(Object obj) {
		if (ConfigMisc.consoleDebug) {
			System.out.println(obj);
		}
	}

	public static void dbgStackTrace() {
		if (ConfigMisc.consoleDebug) {
			StackTraceElement[] arr = Thread.currentThread().getStackTrace();
			for (StackTraceElement ele : arr) {
				System.out.println(ele.toString());
			}
		}
	}

}
