package weather2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;

public class ServerTickHandler
{   
	//Used for easy iteration, could be replaced
    public static ArrayList<WeatherManagerServer> listWeatherMans;
    
    //Main lookup method for dim to weather systems
    public static HashMap<Integer, WeatherManagerServer> lookupDimToWeatherMan;
    
	public static World lastWorld;
    
	public static NBTTagCompound worldNBT = new NBTTagCompound(); 
	
    static {
    	
    	listWeatherMans = new ArrayList();
    	lookupDimToWeatherMan = new HashMap<Integer, WeatherManagerServer>();
    	
    }
    
    public static void onTickInGame()
    {
    	
        if (FMLCommonHandler.instance() == null || FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            return;
        }

        World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
        
        if (world != null && lastWorld != world) {
        	lastWorld = world;
        	//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandWaveHeight());
        	//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandWeather());
        }
        
        //regularly save data
        if (world != null) {
        	if (world.getTotalWorldTime() % ConfigMisc.Misc_AutoDataSaveIntervalInTicks == 0) {
        		Weather.writeOutData(false);
        	}
        }
        
        World worlds[] = DimensionManager.getWorlds();
        
        //add use of CSV of supported dimensions here once feature is added, for now just overworld
        
        for (int i = 0; i < worlds.length; i++) {
        	if (!lookupDimToWeatherMan.containsKey(worlds[i].provider.getDimensionId())) {
        		
        		if (WeatherUtilConfig.listDimensionsWeather.contains(worlds[i].provider.getDimensionId())) {
        			addWorldToWeather(worlds[i].provider.getDimensionId());
        		}
        	}
        	
        	//tick it
        	WeatherManagerServer wms = lookupDimToWeatherMan.get(worlds[i].provider.getDimensionId());
        	if (wms != null) {
        		lookupDimToWeatherMan.get(worlds[i].provider.getDimensionId()).tick();
        	}
        }
        
        boolean testRainRequest = false;
        if (testRainRequest) {
        	
        	List<IMCMessage> listMsgs = new ArrayList<IMCMessage>();
	    	listMsgs = FMLInterModComms.fetchRuntimeMessages(Weather.modID);
	    	for (int i = 0; i < listMsgs.size(); i++) {
	    		
	    		//System.out.println("Weather2 side: " + listMsgs.get(i).key + " - modID: " + listMsgs.get(i).getSender() + " - source: " + listMsgs.get(i).toString() + " - " + listMsgs.get(i).getNBTValue());
	    		
	    		if (listMsgs.get(i).key.equals("weather.raining")) {

	    			NBTTagCompound nbt = listMsgs.get(i).getNBTValue();
	    			
	    			String replyMod = nbt.getString("replymod");
					nbt.setBoolean("isRaining", true);
					
					FMLInterModComms.sendRuntimeMessage(replyMod, replyMod, "weather.raining", nbt);
	    			
	    		}
	    	}
        	
        }
        
        
        boolean debugIMC = false;
        if (debugIMC) {
	        try {
		    	List<IMCMessage> listMsgs = new ArrayList<IMCMessage>();
		    	listMsgs = FMLInterModComms.fetchRuntimeMessages(Weather.modID);
		    	for (int i = 0; i < listMsgs.size(); i++) {
		    		
		    		//System.out.println(listMsgs.get(i).key + " - modID: " + listMsgs.get(i).getSender() + " - source: " + listMsgs.get(i).toString() + " - " + listMsgs.get(i).getNBTValue());
		    	}
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
        }
    }
    
    //must only be used when world is active, soonest allowed is TickType.WORLDLOAD
    public static void addWorldToWeather(int dim) {
    	Weather.dbg("Registering Weather2 manager for dim: " + dim);
    	WeatherManagerServer wm = new WeatherManagerServer(dim);
    	
    	listWeatherMans.add(wm);
    	lookupDimToWeatherMan.put(dim, wm);
    	
    	wm.readFromFile();
    }
    
    public static void removeWorldFromWeather(int dim) {
    	Weather.dbg("Deregistering Weather2 manager for dim: " + dim);
    	WeatherManagerServer wm = lookupDimToWeatherMan.get(dim);
    	
    	if (wm != null) {
	    	listWeatherMans.remove(wm);
	    	lookupDimToWeatherMan.remove(dim);
    	}
    	
    	
    	//wm.readFromFile();
    	wm.writeToFile();
    }
    
    public static void playerJoinedServerSyncFull(EntityPlayerMP entP) {
		WeatherManagerServer wm = lookupDimToWeatherMan.get(entP.worldObj.provider.getDimensionId());
		if (wm != null) {
			wm.playerJoinedServerSyncFull(entP);
		}
	}
    
    public static void initialize() {
    	if (ServerTickHandler.lookupDimToWeatherMan.get(0) == null) {
    		ServerTickHandler.addWorldToWeather(0);
    	}
    	
    	//redundant
    	//ServerTickHandler.lookupDimToWeatherMan.get(0).readFromFile();
    }
    
    public static void reset() {
    	//World worlds[] = DimensionManager.getWorlds();
    	//for (int i = 0; i < worlds.length; i++) {
    	for (int i = 0; i < listWeatherMans.size(); i++) {
    		WeatherManagerBase wm = listWeatherMans.get(i);
    		int dim = wm.dim;
    		if (lookupDimToWeatherMan.containsKey(dim)) {
    			removeWorldFromWeather(dim);
    		}
    	}

    	//should never happen
    	if (listWeatherMans.size() > 0 || lookupDimToWeatherMan.size() > 0) {
    		Weather.dbg("Weather2 reset state failed to manually clear lists, listWeatherMans.size(): " + listWeatherMans.size() + " - lookupDimToWeatherMan.size(): " + lookupDimToWeatherMan.size() + " - forcing a full clear of lists");
    		listWeatherMans.clear();
    		lookupDimToWeatherMan.clear();
    	}
    }
}
