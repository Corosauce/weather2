package weather2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.forge.CoroUtil;
import modconfig.ConfigMod;
import modconfig.IConfigCategory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.lang3.StringUtils;

import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.config.*;
import CoroUtil.util.CoroUtilFile;

public class WeatherUtilConfig {

	public static List<Integer> listDimensionsWeather = new ArrayList<Integer>();
	public static List<Integer> listDimensionsClouds = new ArrayList<Integer>();
	//used for deadly storms and sandstorms
	public static List<Integer> listDimensionsStorms = new ArrayList<Integer>();
	public static List<Integer> listDimensionsWindEffects = new ArrayList<Integer>();
	
	public static int CMD_BTN_PERF_STORM = 2;
	public static int CMD_BTN_PERF_NATURE = 3;
	public static int CMD_BTN_PERF_PRECIPRATE = 12;
	public static int CMD_BTN_PERF_SHADERS_PARTICLE = 18;
	public static int CMD_BTN_PERF_SHADERS_FOLIAGE = 19;
	
	public static int CMD_BTN_COMP_STORM = 4;
	public static int CMD_BTN_COMP_LOCK = 5;
	public static int CMD_BTN_COMP_PARTICLEPRECIP = 6;
	public static int CMD_BTN_COMP_SNOWFALLBLOCKS = 7;
	public static int CMD_BTN_COMP_LEAFFALLBLOCKS = 8;
	public static int CMD_BTN_COMP_PARTICLESNOMODS = 13;
	
	public static int CMD_BTN_PREF_RATEOFSTORM = 9;
	public static int CMD_BTN_PREF_CHANCEOFSTORM = 14;
	public static int CMD_BTN_PREF_CHANCEOFRAIN = 10;
	public static int CMD_BTN_PREF_BLOCKDESTRUCTION = 11;
	public static int CMD_BTN_PREF_TORNADOANDCYCLONES = 15;
	public static int CMD_BTN_PREF_SANDSTORMS = 16;
	public static int CMD_BTN_PREF_GLOBALRATE = 17;
	
	public static int CMD_BTN_HIGHEST_ID = 19;

	public static List<String> LIST_RATES = new ArrayList<String>(Arrays.asList("High", "Medium", "Low"));
	public static List<String> LIST_RATES2 = new ArrayList<String>(Arrays.asList("High", "Medium", "Low", "None"));
	public static List<String> LIST_TOGGLE = new ArrayList<String>(Arrays.asList("Off", "On"));
	public static List<String> LIST_CHANCE = new ArrayList<String>(Arrays.asList("1/2 Day", "1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days", "7 Days", "8 Days", "9 Days", "10 Days", "Never"));
	
	public static List<String> LIST_STORMSWHEN = new ArrayList<String>(Arrays.asList("Local Biomes", "Global Overcast"));
	public static List<String> LIST_LOCK = new ArrayList<String>(Arrays.asList("Always Off", "Always On", "Don't lock"));
	public static List<String> LIST_GLOBALRATE = new ArrayList<String>(Arrays.asList("Rand player", "Each player"));
	
	public static List<Integer> listSettingsClient = new ArrayList<Integer>();
	public static List<Integer> listSettingsServer = new ArrayList<Integer>();
	
	//for caching server data on client side (does not pertain to client only settings)
	public static NBTTagCompound nbtClientCache = new NBTTagCompound();
	
	//actual data that gets written out to disk
	public static NBTTagCompound nbtServerData = new NBTTagCompound();
	public static NBTTagCompound nbtClientData = new NBTTagCompound();
	
	static {
		listSettingsClient.add(CMD_BTN_PERF_STORM);
		listSettingsClient.add(CMD_BTN_PERF_NATURE);
		listSettingsClient.add(CMD_BTN_COMP_PARTICLEPRECIP);
		listSettingsClient.add(CMD_BTN_PERF_PRECIPRATE);
		listSettingsClient.add(CMD_BTN_COMP_PARTICLESNOMODS);
		listSettingsClient.add(CMD_BTN_PERF_SHADERS_PARTICLE);
		listSettingsClient.add(CMD_BTN_PERF_SHADERS_FOLIAGE);
		
		
		listSettingsServer.add(CMD_BTN_COMP_STORM);
		listSettingsServer.add(CMD_BTN_COMP_LOCK);
		listSettingsServer.add(CMD_BTN_COMP_SNOWFALLBLOCKS);
		listSettingsServer.add(CMD_BTN_COMP_LEAFFALLBLOCKS);
		listSettingsServer.add(CMD_BTN_PREF_RATEOFSTORM);
		listSettingsServer.add(CMD_BTN_PREF_CHANCEOFSTORM);
		listSettingsServer.add(CMD_BTN_PREF_CHANCEOFRAIN);
		listSettingsServer.add(CMD_BTN_PREF_BLOCKDESTRUCTION);
		listSettingsServer.add(CMD_BTN_PREF_TORNADOANDCYCLONES);
		listSettingsServer.add(CMD_BTN_PREF_SANDSTORMS);
		listSettingsServer.add(CMD_BTN_PREF_GLOBALRATE);
	}
	
	//client should call this on detecting a close/save of GUI
	public static void processNBTToModConfigClient() {
		nbtSaveDataClient();
		
		Weather.dbg("processNBTToModConfigClient");
		
		Weather.dbg("nbtClientData: " + nbtClientData);
		
		String modIDWeather = Weather.configMisc.getRegistryName();
		String modIDCoroUtil = CoroUtil.configCoroUtil.getRegistryName();
		
		try {
			if (nbtClientData.hasKey("btn_" + CMD_BTN_COMP_PARTICLEPRECIP)) {
				ConfigParticle.Particle_RainSnow = LIST_TOGGLE.get(nbtClientData.getInteger("btn_" + CMD_BTN_COMP_PARTICLEPRECIP)).equalsIgnoreCase("on");
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_STORM)) {
				if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("high")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 0;
					ConfigStorm.Storm_ParticleSpawnDelay = 1;
					ConfigParticle.Sandstorm_Particle_Debris_effect_rate = 1;
					ConfigParticle.Sandstorm_Particle_Dust_effect_rate = 1;
				} else if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("medium")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 2;
					ConfigStorm.Storm_ParticleSpawnDelay = 3;
					ConfigParticle.Sandstorm_Particle_Debris_effect_rate = 0.6D;
					ConfigParticle.Sandstorm_Particle_Dust_effect_rate = 0.6D;
				} else if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("low")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 5;
					ConfigStorm.Storm_ParticleSpawnDelay = 5;
					ConfigParticle.Sandstorm_Particle_Debris_effect_rate = 0.3D;
					ConfigParticle.Sandstorm_Particle_Dust_effect_rate = 0.3D;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_NATURE)) {
				if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("high")) {
					ConfigParticle.Wind_Particle_effect_rate = 1F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("medium")) {
					ConfigParticle.Wind_Particle_effect_rate = 0.7F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("low")) {
					ConfigParticle.Wind_Particle_effect_rate = 0.3F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("none")) {
					ConfigParticle.Wind_Particle_effect_rate = 0.0F;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_PRECIPRATE)) {
				//ConfigMisc.Particle_RainSnow = true;
				if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("high")) {
					ConfigParticle.Precipitation_Particle_effect_rate = 1D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("medium")) {
					ConfigParticle.Precipitation_Particle_effect_rate = 0.7D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("low")) {
					ConfigParticle.Precipitation_Particle_effect_rate = 0.3D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("none")) {
					ConfigParticle.Precipitation_Particle_effect_rate = 0D;
					//ConfigMisc.Particle_RainSnow = false;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_COMP_PARTICLESNOMODS)) {
				ConfigParticle.Particle_VanillaAndWeatherOnly = LIST_TOGGLE.get(nbtClientData.getInteger("btn_" + CMD_BTN_COMP_PARTICLESNOMODS)).equalsIgnoreCase("on");
			}

			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_SHADERS_PARTICLE)) {
				int val = nbtClientData.getInteger("btn_" + CMD_BTN_PERF_SHADERS_PARTICLE);
				if (val == 0) {
					ConfigCoroUtil.particleShaders = false;
				} else if (val == 1) {
					ConfigCoroUtil.particleShaders = true;
				}
			}

			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_SHADERS_FOLIAGE)) {
				int val = nbtClientData.getInteger("btn_" + CMD_BTN_PERF_SHADERS_FOLIAGE);
				if (val == 0) {
					ConfigCoroUtil.foliageShaders = false;
				} else if (val == 1) {
					ConfigCoroUtil.foliageShaders = true;
				}
			}
			
			NBTTagCompound nbtDims = nbtClientData.getCompoundTag("dimData");
			//Iterator it = nbtDims.getTags().iterator();
			
			Weather.dbg("before cl: " + listDimensionsWindEffects);
			
			Iterator it = nbtDims.getKeySet().iterator();
			while (it.hasNext()) {
			 	String tagName = (String) it.next();
			 	NBTTagInt entry = (NBTTagInt) nbtDims.getTag(tagName);
				String[] vals = tagName.split("_");
				
				if (vals[2].equals("3")) {
					int dimID = Integer.parseInt(vals[1]);
					if (entry.getInt() == 0) {
						//if off			
						if (listDimensionsWindEffects.contains(dimID)) {
							listDimensionsWindEffects.remove((Object)dimID);
						}
					} else {
						//if on
						if (!listDimensionsWindEffects.contains(dimID)) {
							listDimensionsWindEffects.add(dimID);
						}
					}					
				}
			}
			
			Weather.dbg("after cl: " + listDimensionsWindEffects);
			
			processListsReverse();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}


		/*ConfigMod.configLookup.get(modIDWeather).writeConfigFile(true);
		ConfigMod.configLookup.get(modIDCoroUtil).writeConfigFile(true);*/
		ConfigMod.forceSaveAllFilesFromRuntimeSettings();
		
		//work lists here
		
		//client nbt to client mod config setttings, and server nbt to server mod config settings
		//invoke whatever method modconfig uses to write out its data, for both its client and server side
	}

	//server should call this on detecting of a save request (close of GUI packet send)
	public static void processNBTToModConfigServer() {
		nbtSaveDataServer();
		
		Weather.dbg("processNBTToModConfigServer");
		
		Weather.dbg("nbtServerData: " + nbtServerData);
		
		//String modID = "weather2Misc";
		
		try {
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_STORM)) {
				ConfigMisc.overcastMode = LIST_STORMSWHEN.get(nbtServerData.getInteger("btn_" + CMD_BTN_COMP_STORM)).equalsIgnoreCase("Global Overcast");
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_LOCK)) {
				int val = nbtServerData.getInteger("btn_" + CMD_BTN_COMP_LOCK);
				if (val == 1) {
					ConfigMisc.lockServerWeatherMode = 1;
				} else if (val == 0) {
					ConfigMisc.lockServerWeatherMode = 0;
				} else {
					ConfigMisc.lockServerWeatherMode = -1;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_SNOWFALLBLOCKS)) {
				boolean val = nbtServerData.getInteger("btn_" + CMD_BTN_COMP_SNOWFALLBLOCKS) == 1;
				ConfigSnow.Snow_PerformSnowfall = val;
				//ConfigSnow.Snow_ExtraPileUp = val;
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_RATEOFSTORM)) {
				int numDays = nbtServerData.getInteger("btn_" + CMD_BTN_PREF_RATEOFSTORM);
				if (numDays == 0) {
					ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks = 12000;
					ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks = 12000;
				} else if (numDays == 11) {
					//potentially remove the 'never' clause from here in favor of the dimension specific disabling of 'storms' which is already used in code
					//for now consider this a second layer of rules to the storm creation process, probably not a user friendly idea
					ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks = -1;
					ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks = -1;
				} else {
					ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks = 24000*numDays;
					ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks = 24000*numDays;
				}
				
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)) {
				if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("high")) {
					ConfigStorm.Player_Storm_Deadly_OddsTo1 = 30;
					ConfigStorm.Server_Storm_Deadly_OddsTo1 = 30;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("medium")) {
					ConfigStorm.Player_Storm_Deadly_OddsTo1 = 45;
					ConfigStorm.Server_Storm_Deadly_OddsTo1 = 45;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("low")) {
					ConfigStorm.Player_Storm_Deadly_OddsTo1 = 60;
					ConfigStorm.Server_Storm_Deadly_OddsTo1 = 60;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)) {
				if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("high")) {
					ConfigStorm.Storm_Rain_OddsTo1 = 150;
					ConfigStorm.Storm_Rain_Overcast_OddsTo1 = ConfigStorm.Storm_Rain_OddsTo1 / 3;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("medium")) {
					ConfigStorm.Storm_Rain_OddsTo1 = 300;
					ConfigStorm.Storm_Rain_Overcast_OddsTo1 = ConfigStorm.Storm_Rain_OddsTo1 / 3;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("low")) {
					ConfigStorm.Storm_Rain_OddsTo1 = 450;
					ConfigStorm.Storm_Rain_Overcast_OddsTo1 = ConfigStorm.Storm_Rain_OddsTo1 / 3;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("none")) {
					ConfigStorm.Storm_Rain_OddsTo1 = -1;
					ConfigStorm.Storm_Rain_Overcast_OddsTo1 = -1;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_BLOCKDESTRUCTION)) {
				ConfigTornado.Storm_Tornado_grabBlocks = LIST_TOGGLE.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_BLOCKDESTRUCTION)).equalsIgnoreCase("on");
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_TORNADOANDCYCLONES)) {
				ConfigTornado.Storm_NoTornadosOrCyclones = LIST_TOGGLE.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_TORNADOANDCYCLONES)).equalsIgnoreCase("off");
			}

			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_SANDSTORMS)) {
				ConfigSand.Storm_NoSandstorms = LIST_TOGGLE.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_SANDSTORMS)).equalsIgnoreCase("off");
			}

			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_GLOBALRATE)) {
				ConfigStorm.Server_Storm_Deadly_UseGlobalRate = nbtServerData.getInteger("btn_" + CMD_BTN_PREF_GLOBALRATE) == 0;
				ConfigSand.Sandstorm_UseGlobalServerRate = nbtServerData.getInteger("btn_" + CMD_BTN_PREF_GLOBALRATE) == 0;

				//System.out.println("ConfigStorm.Server_Storm_Deadly_UseGlobalRate: " + ConfigStorm.Server_Storm_Deadly_UseGlobalRate);
			}
			
			NBTTagCompound nbtDims = nbtServerData.getCompoundTag("dimData");
			//Iterator it = nbtDims.getTags().iterator();
			
			Weather.dbg("before: " + listDimensionsWeather);
			
			Iterator it = nbtDims.getKeySet().iterator();
			while (it.hasNext()) {
			 	String tagName = (String) it.next();
			 	NBTTagInt entry = (NBTTagInt) nbtDims.getTag(tagName);
				String[] vals = tagName.split("_");
				//if weather
				if (vals[2].equals("0")) {
					int dimID = Integer.parseInt(vals[1]);
					if (entry.getInt() == 0) {
						//if off			
						if (listDimensionsWeather.contains(dimID)) {
							listDimensionsWeather.remove(dimID);
						}
					} else {
						//if on
						if (!listDimensionsWeather.contains(dimID)) {
							listDimensionsWeather.add(dimID);
						}
					}					
				} else if (vals[2].equals("1")) {
					int dimID = Integer.parseInt(vals[1]);
					if (entry.getInt() == 0) {
						//if off			
						if (listDimensionsClouds.contains(dimID)) {
							listDimensionsClouds.remove(dimID);
						}
					} else {
						//if on
						if (!listDimensionsClouds.contains(dimID)) {
							listDimensionsClouds.add(dimID);
						}
					}					
				} else if (vals[2].equals("2")) {
					int dimID = Integer.parseInt(vals[1]);
					if (entry.getInt() == 0) {
						//if off			
						if (listDimensionsStorms.contains(dimID)) {
							listDimensionsStorms.remove(dimID);
						}
					} else {
						//if on
						if (!listDimensionsStorms.contains(dimID)) {
							listDimensionsStorms.add(dimID);
						}
					}					
				}/* else if (vals[2].equals("3")) {
					int dimID = Integer.parseInt(vals[1]);
					if (tag.data == 0) {
						//if off			
						if (listDimensionsWindEffects.contains(dimID)) {
							listDimensionsWindEffects.remove(dimID);
						}
					} else {
						//if on
						if (!listDimensionsWindEffects.contains(dimID)) {
							listDimensionsWindEffects.add(dimID);
						}
					}					
				}*/
				Weather.dbg("dim: " + vals[1] + " - setting ID: " + vals[2] + " - data: " + entry.getInt());
			}
			
			Weather.dbg("after: " + listDimensionsWeather);
			
			processListsReverse();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		for (IConfigCategory config : Weather.listConfigs) {
			//refresh configmods caches and data
			ConfigMod.configLookup.get(config.getRegistryName()).writeConfigFile(true);
			//not needed
			//ConfigMod.populateData(config.getRegistryName());
		}

		ServerTickHandler.syncServerConfigToClient();

		//ConfigMod.configLookup.get(modID).writeConfigFile(true);
		//ConfigMod.populateData(modID);
		
		//work lists here
		
		//client nbt to client mod config setttings, and server nbt to server mod config settings
		//invoke whatever method modconfig uses to write out its data, for both its client and server side
	}
	
	/*public static void nbtSaveDataAll() {
		nbtSaveDataClient();
		nbtSaveDataServer();
	}*/
	
	public static void nbtReceiveClientData(NBTTagCompound parNBT) {
		for (int i = 0; i <= CMD_BTN_HIGHEST_ID; i++) {
			if (parNBT.hasKey("btn_" + i)) {
				nbtServerData.setInteger("btn_" + i, parNBT.getInteger("btn_" + i));
			}
		}
		
		//also add dimension feature config, its iterated over
		nbtServerData.setTag("dimData", parNBT.getCompoundTag("dimData"));
		
		processNBTToModConfigServer();
	}
	
	public static void nbtReceiveServerDataForCache(NBTTagCompound parNBT) {
		nbtClientCache = parNBT;
		
		Weather.dbg("nbtClientCache: " + nbtServerData);
	}
	
	public static void nbtSaveDataClient() {
		nbtWriteNBTToDisk(nbtClientData, true);
	}
	
	public static void nbtSaveDataServer() {
		nbtWriteNBTToDisk(nbtServerData, false);
	}
	
	public static void nbtLoadDataAll() {
		nbtLoadDataClient();
		nbtLoadDataServer();
	}

	public static void nbtLoadDataClient() {
		nbtClientData = nbtReadNBTFromDisk(true);
	}
	
	public static void nbtLoadDataServer() {
		nbtServerData = nbtReadNBTFromDisk(false);
	}
	
	public static NBTTagCompound createNBTDimensionListing() {
		NBTTagCompound data = new NBTTagCompound();
		
		World[] worlds = DimensionManager.getWorlds();
		
		for (int i = 0; i < worlds.length; i++) {
			NBTTagCompound nbtDim = new NBTTagCompound();
			int dimID = worlds[i].provider.getDimension();
			nbtDim.setInteger("ID", dimID); //maybe redundant if we name tag as dimID too
			nbtDim.setString("name", worlds[i].provider.getDimensionType().getName());
			nbtDim.setBoolean("weather", listDimensionsWeather.contains(dimID));
			nbtDim.setBoolean("clouds", listDimensionsClouds.contains(dimID));
			nbtDim.setBoolean("storms", listDimensionsStorms.contains(dimID));
			
			//PROCESS ME ELSEWHERE!!! - must be done in EZGUI post receiving of this data because client still needs this server created dimension listing first
			//nbtDim.setBoolean("effects", listDimensionsWindEffects.contains(dimID));
			data.setTag("" + dimID, nbtDim);
			///data.setString("" + worlds[i].provider.dimensionId, worlds[i].provider.getDimensionName());
		}
		
		return data;
	}
	
	public static void processLists() {
		listDimensionsWeather = parseList(ConfigMisc.Dimension_List_Weather);
		listDimensionsClouds = parseList(ConfigMisc.Dimension_List_Clouds);
		listDimensionsStorms = parseList(ConfigMisc.Dimension_List_Storms);
		listDimensionsWindEffects = parseList(ConfigMisc.Dimension_List_WindEffects);
	}
	
	public static void processListsReverse() {
		ConfigMisc.Dimension_List_Weather = StringUtils.join(listDimensionsWeather, " ");
		ConfigMisc.Dimension_List_Clouds = StringUtils.join(listDimensionsClouds, " ");
		ConfigMisc.Dimension_List_Storms = StringUtils.join(listDimensionsStorms, " ");
		ConfigMisc.Dimension_List_WindEffects = StringUtils.join(listDimensionsWindEffects, " ");
	}
	
	public static List<Integer> parseList(String parData) {
		String listStr = parData;
		listStr = listStr.replace(",", " ");
		String[] arrStr = listStr.split(" ");
		Integer[] arrInt = new Integer[arrStr.length];
		for (int i = 0; i < arrStr.length; i++) {
			try {
				arrInt[i] = Integer.parseInt(arrStr[i]);
			} catch (Exception ex) {
				arrInt[i] = -999999; //set to -999999, hope no dimension id of this exists
			}
		}
		return new ArrayList(Arrays.asList(arrInt));
	}
	
	public static void nbtWriteNBTToDisk(NBTTagCompound parData, boolean saveForClient) {
		String fileURL = null;
		if (saveForClient) {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigClientData.dat";
		} else {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigServerData.dat";
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(parData, fos);
	    	fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Weather.dbg("Error writing Weather2 EZ GUI data");
		}
	}
	
	public static NBTTagCompound nbtReadNBTFromDisk(boolean loadForClient) {
		NBTTagCompound data = new NBTTagCompound();
		String fileURL = null;
		if (loadForClient) {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigClientData.dat";
		} else {
			fileURL = CoroUtilFile.getMinecraftSaveFolderPath() + File.separator + "Weather2" + File.separator + "EZGUIConfigServerData.dat";
		}
		
		try {
			if ((new File(fileURL)).exists()) {
				data = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Weather.dbg("Error reading Weather2 EZ GUI data");
		}
		return data;
	}

	public static void setOvercastModeServerSide(boolean val) {
		nbtServerData.setInteger("btn_" + CMD_BTN_COMP_STORM, val ? 1 : 0);
		nbtSaveDataServer();
	}
	
}
