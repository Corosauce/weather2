package weather2;

import CoroUtil.forge.CULog;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerTickHandler
{   
	//Used for easy iteration, could be replaced
    public static ArrayList<WeatherManagerServer> listWeatherMans;
    
    //Main lookup method for dim to weather systems
    public static HashMap<Integer, WeatherManagerServer> lookupDimToWeatherMan;
    
	public static World lastWorld;
    
	public static CompoundNBT worldNBT = new CompoundNBT();
	
    static {
    	
    	listWeatherMans = new ArrayList();
    	lookupDimToWeatherMan = new HashMap<Integer, WeatherManagerServer>();
    	
    }
    
    public static void onTickInGame()
    {
    	
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            return;
        }

        World world = WeatherUtil.getWorld(0);
        
        if (world != null && lastWorld != world) {
        	lastWorld = world;
        	//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandWaveHeight());
        	//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandWeather());
        }
        
        //regularly save data
        if (world != null) {
        	if (world.getGameTime() % ConfigMisc.Misc_AutoDataSaveIntervalInTicks == 0) {
        		Weather.writeOutData(false);
        	}
        }
        
        /*World worlds[] = DimensionManager.getWorlds();
        
        //add use of CSV of supported dimensions here once feature is added, for now just overworld
        
        for (int i = 0; i < worlds.length; i++) {
			if (!lookupDimToWeatherMan.containsKey(worlds[i].getDimension().getType().getId())) {

				if (WeatherUtilConfig.listDimensionsWeather.contains(worlds[i].getDimension().getType().getId())) {
					addWorldToWeather(worlds[i].getDimension().getType().getId());
				}
			}

			//tick it
			WeatherManagerServer wms = lookupDimToWeatherMan.get(worlds[i].getDimension().getType().getId());
			if (wms != null) {
				lookupDimToWeatherMan.get(worlds[i].getDimension().getType().getId()).tick();
			}
        }*/

        Iterable<ServerWorld> worlds = WeatherUtil.getWorlds();

        for (ServerWorld worldEntry : worlds) {
			if (!lookupDimToWeatherMan.containsKey(worldEntry.getDimension().getType().getId())) {

				if (WeatherUtilConfig.listDimensionsWeather.contains(worldEntry.getDimension().getType().getId())) {
					addWorldToWeather(worldEntry.getDimension().getType().getId());
				}
			}

			//tick it
			WeatherManagerServer wms = lookupDimToWeatherMan.get(worldEntry.getDimension().getType().getId());
			if (wms != null) {
				lookupDimToWeatherMan.get(worldEntry.getDimension().getType().getId()).tick();
			}
		}

        if (ConfigMisc.Aesthetic_Only_Mode) {
        	if (!ConfigMisc.overcastMode) {
        		ConfigMisc.overcastMode = true;
				CULog.dbg("detected Aesthetic_Only_Mode on, setting overcast mode on");
				WeatherUtilConfig.setOvercastModeServerSide(ConfigMisc.overcastMode);
				//TODO: 1.14 uncomment
				//ConfigMod.forceSaveAllFilesFromRuntimeSettings();
				syncServerConfigToClient();
			}
		}

        //TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
		if (world.getGameTime() % 200 == 0) {
			syncServerConfigToClient();
		}
        
        /*boolean testCustomLightning = false;
        if (testCustomLightning) {
        	if (world.getGameTime() % 20 == 0) {
	        	PlayerEntity player = world.getClosestPlayer(0, 0, 0, -1, false);
	        	if (player != null) {
	        		EntityLightningBoltCustom lightning = new EntityLightningBoltCustom(world, player.posX, player.posY, player.posZ);
	        		world.addWeatherEffect(lightning);
	        		lookupDimToWeatherMan.get(0).syncLightningNew(lightning, true);
	        	}
        	}
        }*/
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
    	Weather.dbg("Weather2: Unregistering manager for dim: " + dim);
    	WeatherManagerServer wm = lookupDimToWeatherMan.get(dim);
    	
    	if (wm != null) {
	    	listWeatherMans.remove(wm);
	    	lookupDimToWeatherMan.remove(dim);
    	}
    	
    	
    	//wm.readFromFile();
    	wm.writeToFile();
    }

    public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
		WeatherManagerServer wm = lookupDimToWeatherMan.get(entP.world.getDimension().getType().getId());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
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
		Weather.dbg("Weather2: ServerTickHandler resetting");
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
    		Weather.dbg("Weather2: reset state failed to manually clear lists, listWeatherMans.size(): " + listWeatherMans.size() + " - lookupDimToWeatherMan.size(): " + lookupDimToWeatherMan.size() + " - forcing a full clear of lists");
    		listWeatherMans.clear();
    		lookupDimToWeatherMan.clear();
    	}
    }
    
    public static WeatherManagerServer getWeatherSystemForDim(int dimID) {
    	return lookupDimToWeatherMan.get(dimID);
    }

	public static void syncServerConfigToClient() {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "ClientConfigData");
		data.putString("command", "syncUpdate");
		//data.put("data", parManager.nbtSyncForClient());

		ClientConfigData.write(data);

		//Weather.eventChannel.sendToAll(PacketHelper.getNBTPacket(data, Weather.eventChannelName));
		WeatherNetworking.HANDLER.send(PacketDistributor.ALL.noArg(), new PacketNBTFromServer(data));
	}

	public static void syncServerConfigToClientPlayer(ServerPlayerEntity player) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "ClientConfigData");
		data.putString("command", "syncUpdate");
		//data.put("data", parManager.nbtSyncForClient());

		ClientConfigData.write(data);

		//Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), player);
		WeatherNetworking.HANDLER.send(PacketDistributor.ALL.noArg(), new PacketNBTFromServer(data));
	}
}
