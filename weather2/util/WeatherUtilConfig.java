package weather2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import CoroUtil.util.CoroUtilFile;

import modconfig.ConfigMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import weather2.Weather;
import weather2.config.ConfigMisc;

public class WeatherUtilConfig {

	public static List<Integer> listDimensionsWeather = new ArrayList<Integer>();
	public static List<Integer> listDimensionsClouds = new ArrayList<Integer>();
	public static List<Integer> listDimensionsStorms = new ArrayList<Integer>();
	public static List<Integer> listDimensionsWindEffects = new ArrayList<Integer>();
	
	public static int CMD_BTN_PERF_STORM = 2;
	public static int CMD_BTN_PERF_NATURE = 3;
	public static int CMD_BTN_PERF_PRECIPRATE = 12;
	
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
	
	public static int CMD_BTN_HIGHEST_ID = 14;

	public static List<String> LIST_RATES = new ArrayList<String>(Arrays.asList("High", "Medium", "Low"));
	public static List<String> LIST_RATES2 = new ArrayList<String>(Arrays.asList("High", "Medium", "Low", "None"));
	public static List<String> LIST_TOGGLE = new ArrayList<String>(Arrays.asList("Off", "On"));
	public static List<String> LIST_CHANCE = new ArrayList<String>(Arrays.asList("1/2 Day", "1 Day", "2 Days", "3 Days", "4 Days", "5 Days", "6 Days", "7 Days", "8 Days", "9 Days", "10 Days", "Never"));
	
	public static List<String> LIST_STORMSWHEN = new ArrayList<String>(Arrays.asList("Local Biomes", "Global Overcast"));
	public static List<String> LIST_LOCK = new ArrayList<String>(Arrays.asList("Off", "On", "Don't lock"));
	
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
		
		
		listSettingsServer.add(CMD_BTN_COMP_STORM);
		listSettingsServer.add(CMD_BTN_COMP_LOCK);
		listSettingsServer.add(CMD_BTN_COMP_SNOWFALLBLOCKS);
		listSettingsServer.add(CMD_BTN_COMP_LEAFFALLBLOCKS);
		listSettingsServer.add(CMD_BTN_PREF_RATEOFSTORM);
		listSettingsServer.add(CMD_BTN_PREF_CHANCEOFSTORM);
		listSettingsServer.add(CMD_BTN_PREF_CHANCEOFRAIN);
		listSettingsServer.add(CMD_BTN_PREF_BLOCKDESTRUCTION);
	}
	
	//client should call this on detecting a close/save of GUI
	public static void processNBTToModConfigClient() {
		nbtSaveDataClient();
		
		Weather.dbg("processNBTToModConfigClient");
		
		Weather.dbg("nbtClientData: " + nbtClientData);
		
		String modID = "weather2Misc";
		
		try {
			if (nbtClientData.hasKey("btn_" + CMD_BTN_COMP_PARTICLEPRECIP)) {
				ConfigMisc.Particle_RainSnow = LIST_TOGGLE.get(nbtClientData.getInteger("btn_" + CMD_BTN_COMP_PARTICLEPRECIP)).equalsIgnoreCase("on");
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_STORM)) {
				if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("high")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 0;
				} else if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("medium")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 2;
				} else if (LIST_RATES.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_STORM)).equalsIgnoreCase("low")) {
					ConfigMisc.Cloud_ParticleSpawnDelay = 5;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_NATURE)) {
				if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("high")) {
					ConfigMisc.Wind_Particle_effect_rate = 1F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("medium")) {
					ConfigMisc.Wind_Particle_effect_rate = 0.7F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("low")) {
					ConfigMisc.Wind_Particle_effect_rate = 0.3F;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_NATURE)).equalsIgnoreCase("none")) {
					ConfigMisc.Wind_Particle_effect_rate = 0.0F;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_PERF_PRECIPRATE)) {
				//ConfigMisc.Particle_RainSnow = true;
				if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("high")) {
					ConfigMisc.Particle_Precipitation_effect_rate = 1D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("medium")) {
					ConfigMisc.Particle_Precipitation_effect_rate = 0.7D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("low")) {
					ConfigMisc.Particle_Precipitation_effect_rate = 0.3D;
				} else if (LIST_RATES2.get(nbtClientData.getInteger("btn_" + CMD_BTN_PERF_PRECIPRATE)).equalsIgnoreCase("none")) {
					ConfigMisc.Particle_Precipitation_effect_rate = 0D;
					//ConfigMisc.Particle_RainSnow = false;
				}
			}
			
			if (nbtClientData.hasKey("btn_" + CMD_BTN_COMP_PARTICLESNOMODS)) {
				ConfigMisc.Particle_VanillaAndWeatherOnly = LIST_TOGGLE.get(nbtClientData.getInteger("btn_" + CMD_BTN_COMP_PARTICLESNOMODS)).equalsIgnoreCase("on");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		ConfigMod.configLookup.get(modID).writeConfigFile(true);
		
		//work lists here
		
		//client nbt to client mod config setttings, and server nbt to server mod config settings
		//invoke whatever method modconfig uses to write out its data, for both its client and server side
	}

	//server should call this on detecting of a save request (close of GUI packet send)
	public static void processNBTToModConfigServer() {
		nbtSaveDataServer();
		
		Weather.dbg("processNBTToModConfigServer");
		
		Weather.dbg("nbtServerData: " + nbtServerData);
		
		String modID = "weather2Misc";
		
		try {
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_STORM)) {
				ConfigMisc.overcastMode = LIST_STORMSWHEN.get(nbtServerData.getInteger("btn_" + CMD_BTN_COMP_STORM)).equalsIgnoreCase("Global Overcast");
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_LOCK)) {
				if (LIST_LOCK.get(nbtServerData.getInteger("btn_" + CMD_BTN_COMP_LOCK)).equalsIgnoreCase("on")) {
					ConfigMisc.lockServerWeatherMode = 1;
				} else if (LIST_LOCK.get(nbtServerData.getInteger("btn_" + CMD_BTN_COMP_LOCK)).equalsIgnoreCase("off")) {
					ConfigMisc.lockServerWeatherMode = 0;
				} else {
					ConfigMisc.lockServerWeatherMode = -1;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_COMP_SNOWFALLBLOCKS)) {
				boolean val = LIST_TOGGLE.get(nbtServerData.getInteger("btn_" + CMD_BTN_COMP_SNOWFALLBLOCKS)).equalsIgnoreCase("on");
				ConfigMisc.Snow_PerformSnowfall = val;
				ConfigMisc.Snow_ExtraPileUp = val;
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_RATEOFSTORM)) {
				int numDays = nbtServerData.getInteger("btn_" + CMD_BTN_PREF_RATEOFSTORM);
				if (numDays == 0) {
					ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks = 12000;
				} else if (numDays == 11) {
					//potentially remove the 'never' clause from here in favor of the dimension specific disabling of 'storms' which is already used in code
					//for now consider this a second layer of rules to the storm creation process, probably not a user friendly idea
					ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks = -1;
				} else {
					ConfigMisc.Player_Storm_Deadly_TimeBetweenInTicks = 24000*numDays;
				}
				
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)) {
				if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("high")) {
					ConfigMisc.Player_Storm_Deadly_OddsTo1 = 30;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("medium")) {
					ConfigMisc.Player_Storm_Deadly_OddsTo1 = 45;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFSTORM)).equalsIgnoreCase("low")) {
					ConfigMisc.Player_Storm_Deadly_OddsTo1 = 60;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)) {
				if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("high")) {
					ConfigMisc.Player_Storm_Rain_OddsTo1 = 150;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("medium")) {
					ConfigMisc.Player_Storm_Rain_OddsTo1 = 300;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("low")) {
					ConfigMisc.Player_Storm_Rain_OddsTo1 = 450;
				} else if (LIST_RATES2.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_CHANCEOFRAIN)).equalsIgnoreCase("none")) {
					ConfigMisc.Player_Storm_Rain_OddsTo1 = -1;
				}
			}
			
			if (nbtServerData.hasKey("btn_" + CMD_BTN_PREF_BLOCKDESTRUCTION)) {
				ConfigMisc.Storm_Tornado_grabBlocks = LIST_TOGGLE.get(nbtServerData.getInteger("btn_" + CMD_BTN_PREF_BLOCKDESTRUCTION)).equalsIgnoreCase("on");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		ConfigMod.configLookup.get(modID).writeConfigFile(true);
		
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
	
	public static void processLists() {
		listDimensionsWeather = parseList(ConfigMisc.Dimension_List_Weather);
		listDimensionsClouds = parseList(ConfigMisc.Dimension_List_Clouds);
		listDimensionsStorms = parseList(ConfigMisc.Dimension_List_Storms);
		listDimensionsWindEffects = parseList(ConfigMisc.Dimension_List_WindEffects);
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
		return Arrays.asList(arrInt);
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
	
}
