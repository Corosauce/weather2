package weather2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import combat.RPGMod;

import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;

public class ServerTickHandler implements ITickHandler
{   
	//Used for easy iteration, could be replaced
    public static ArrayList<WeatherManagerServer> listWeatherMans;
    
    //Main lookup method for dim to weather systems
    public static HashMap<Integer, WeatherManagerServer> lookupDimToWeatherMan;
    
	public static World lastWorld;
    
	public static NBTTagCompound worldNBT = new NBTTagCompound(); 
	
    public ServerTickHandler() {
    	
    	listWeatherMans = new ArrayList();
    	lookupDimToWeatherMan = new HashMap<Integer, WeatherManagerServer>();
    	
    }

    @Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.WORLDLOAD))) {
        	World world = (World)tickData[0];
        	if (world.provider.dimensionId == 0) {
        		Weather.initTry();
        	}
        }
	}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
            onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD);
    }

    @Override
    public String getLabel()
    {
        return "Weather2 server ticker";
    }
    

    public void onTickInGame()
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
        	if (!lookupDimToWeatherMan.containsKey(worlds[i].provider.dimensionId)) {
        		
        		//temp overworld only! - not so temp anymore?
        		if (worlds[i].provider.dimensionId  == 0) {
        			addWorldToWeather(worlds[i].provider.dimensionId);
        		}
        	}
        	
        	//tick it
        	WeatherManagerServer wms = lookupDimToWeatherMan.get(worlds[i].provider.dimensionId);
        	if (wms != null) {
        		lookupDimToWeatherMan.get(worlds[i].provider.dimensionId).tick();
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
    	
    	wm.readFromFile();
    }
    
    public static void playerJoinedServerSyncFull(EntityPlayer entP) {
		WeatherManagerServer wm = lookupDimToWeatherMan.get(entP.worldObj.provider.dimensionId);
		if (wm != null) {
			wm.playerJoinedServerSyncFull(entP);
		}
	}
    
    public static void initialize() {
    	if (ServerTickHandler.lookupDimToWeatherMan.get(0) == null) {
    		ServerTickHandler.addWorldToWeather(0);
    	}
    	
    	ServerTickHandler.lookupDimToWeatherMan.get(0).readFromFile();
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
