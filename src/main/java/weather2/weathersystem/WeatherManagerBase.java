package weather2.weathersystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.io.FileUtils;

import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.util.Vec3;

public class WeatherManagerBase {

	//shared stuff, stormfront list
	
	public int dim;
	
	//storms
	private List<StormObject> listStormObjects = new ArrayList<StormObject>();
	public HashMap<Long, StormObject> lookupStormObjectsByID = new HashMap<Long, StormObject>();
	public HashMap<Integer, ArrayList<StormObject>> lookupStormObjectsByLayer = new HashMap<Integer, ArrayList<StormObject>>();
	//private ArrayList<ArrayList<StormObject>> listStormObjectsByLayer = new ArrayList<ArrayList<StormObject>>();
	
	//volcanos
	private List<VolcanoObject> listVolcanoes = new ArrayList<VolcanoObject>();
	public HashMap<Long, VolcanoObject> lookupVolcanoes = new HashMap<Long, VolcanoObject>();
	
	//wind
	public WindManager windMan = new WindManager(this);
	
	//for client only
	public boolean isVanillaRainActiveOnServer = false;
	
	public long lastStormFormed = 0;
	
	public WeatherManagerBase(int parDim) {
		dim = parDim;
		lookupStormObjectsByLayer.put(0, new ArrayList<StormObject>());
		lookupStormObjectsByLayer.put(1, new ArrayList<StormObject>());
		lookupStormObjectsByLayer.put(2, new ArrayList<StormObject>());
	}
	
	public void reset() {
		for (int i = 0; i < getStormObjects().size(); i++) {
			StormObject so = getStormObjects().get(i);
			
			so.reset();
		}
		
		getStormObjects().clear();
		lookupStormObjectsByID.clear();
		try {
			lookupStormObjectsByLayer.get(0).clear();
			lookupStormObjectsByLayer.get(1).clear();
			lookupStormObjectsByLayer.get(2).clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		for (int i = 0; i < getVolcanoObjects().size(); i++) {
			VolcanoObject vo = getVolcanoObjects().get(i);
			
			vo.reset();
		}
		
		getVolcanoObjects().clear();
		lookupVolcanoes.clear();
		
		windMan.reset();
		
		StormObject.lastUsedStormID = 0;
	}
	
	public World getWorld() {
		return null;
	}
	
	public void tick() {
		World world = getWorld();
		if (world != null) {
			//tick storms
			List<StormObject> list = getStormObjects();
			for (int i = 0; i < list.size(); i++) {
				StormObject so = list.get(i);
				if (this instanceof WeatherManagerServer && so.isDead) {
					removeStormObject(so.ID);
					((WeatherManagerServer)this).syncStormRemove(so);
				} else {
					
					/*if (getWorld().isRemote && so.ticksSinceLastPacketReceived > 20*60) {
						Weather.dbg("WARNING!!! - detected no packets received in last 60 seconds for storm ID: " + so.ID + " this is an ongoing bug, force removing storm on client side");
						removeStormObject(so.ID);
						
						//if it failed still
						if (!so.isDead) {
							for (int ii = 0; ii < listStormObjects.size(); ii++) {
								StormObject so2 = listStormObjects.get(ii);
								if (so2 == so) {
									Weather.dbg("second attempt removal via list iteration");
									so2.setDead();
									listStormObjects.remove(so2);
									lookupStormObjectsByID.remove(so2.ID);
									lookupStormObjectsByLayer.get(so2.layer).remove(so2);
								}	
							}
							
						}
					} else {*/
					
						if (!so.isDead) {
							so.tick();
						} else {
							if (getWorld().isRemote) {
								Weather.dbg("WARNING!!! - detected isDead storm object still in client side list, had to remove storm object with ID " + so.ID + " from client side, wasnt properly removed via main channels");
								removeStormObject(so.ID);
							}
							//Weather.dbg("client storm is dead and still in list, bug?");
						}
						
					//}
				}
			}
						
			//tick volcanos
			for (int i = 0; i < getVolcanoObjects().size(); i++) {
				getVolcanoObjects().get(i).tick();
			}

			//tick wind
			windMan.tick();
		}
	}
	
	public void tickRender(float partialTick) {
		World world = getWorld();
		if (world != null) {
			//tick storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				getStormObjects().get(i).tickRender(partialTick);
			}
		}
	}
	
	public List<StormObject> getStormObjects() {
		return listStormObjects;
	}
	
	public List<StormObject> getStormObjectsByLayer(int layer) {
		return lookupStormObjectsByLayer.get(layer);
	}
	
	public void addStormObject(StormObject so) {
		if (!lookupStormObjectsByID.containsKey(so.ID)) {
			listStormObjects.add(so);
			lookupStormObjectsByID.put(so.ID, so);
			lookupStormObjectsByLayer.get(so.layer).add(so);
		} else {
			Weather.dbg("Weather2 WARNING!!! Client received new storm create for an ID that is already active! design bug");
		}
	}
	
	public void removeStormObject(long ID) {
		StormObject so = lookupStormObjectsByID.get(ID);
		
		if (so != null) {
			so.setDead();
			listStormObjects.remove(so);
			lookupStormObjectsByID.remove(ID);
			lookupStormObjectsByLayer.get(so.layer).remove(so);
		} else {
			Weather.dbg("error looking up storm ID on server for removal: " + ID + " - lookup count: " + lookupStormObjectsByID.size() + " - last used ID: " + StormObject.lastUsedStormID);
		}
	}
	
	public List<VolcanoObject> getVolcanoObjects() {
		return listVolcanoes;
	}
	
	public void addVolcanoObject(VolcanoObject so) {
		if (!lookupVolcanoes.containsKey(so.ID)) {
			listVolcanoes.add(so);
			lookupVolcanoes.put(so.ID, so);
		} else {
			Weather.dbg("Weather2 WARNING!!! Client received new volcano create for an ID that is already active! design bug");
		}
	}
	
	public void removeVolcanoObject(long ID) {
		VolcanoObject vo = lookupVolcanoes.get(ID);
		
		if (vo != null) {
			vo.setDead();
			listVolcanoes.remove(vo);
			lookupVolcanoes.remove(ID);
			
			Weather.dbg("removing volcano");
		}
	}
	
	public StormObject getClosestStormAny(Vec3 parPos, double maxDist) {
		return getClosestStorm(parPos, maxDist, -1, true);
	}
	
	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin) {
		return getClosestStorm(parPos, maxDist, severityFlagMin, false);
	}
	
	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin, boolean orRain) {
		
		StormObject closestStorm = null;
		double closestDist = 9999999;
		
		List<StormObject> listStorms = getStormObjects();
		
		for (int i = 0; i < listStorms.size(); i++) {
			StormObject storm = listStorms.get(i);
			if (storm == null || storm.isDead) continue;
			double dist = storm.pos.distanceTo(parPos);
			/*if (getWorld().isRemote) {
				System.out.println("close storm candidate: " + dist + " - " + storm.state + " - " + storm.attrib_rain);
			}*/
			if (dist < closestDist && dist <= maxDist) {
				if ((storm.attrib_precipitation && orRain) || (severityFlagMin == -1 || storm.levelCurIntensityStage >= severityFlagMin)) {
					closestStorm = storm;
					closestDist = dist;
				}
			}
		}
		
		return closestStorm;
	}
	
	public List<StormObject> getStormsAround(Vec3 parPos, double maxDist) {
		List<StormObject> storms = new ArrayList<StormObject>();
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			StormObject storm = getStormObjects().get(i);
			if (storm.isDead) continue;
			
			if (storm.pos.distanceTo(parPos) < maxDist && (storm.attrib_precipitation || storm.levelCurIntensityStage > StormObject.STATE_NORMAL)) {
				storms.add(storm);
			}
		}
		
		return storms;
	}
	
	public void writeToFile() {
		NBTTagCompound mainNBT = new NBTTagCompound();
		NBTTagCompound listVolcanoesNBT = new NBTTagCompound();
		for (int i = 0; i < listVolcanoes.size(); i++) {
			VolcanoObject td = listVolcanoes.get(i);
			NBTTagCompound teamNBT = new NBTTagCompound();
			td.writeToNBT(teamNBT);
			listVolcanoesNBT.setTag("volcano_" + td.ID, teamNBT);
		}
		mainNBT.setTag("volcanoData", listVolcanoesNBT);
		mainNBT.setLong("lastUsedIDVolcano", VolcanoObject.lastUsedID);
		
		NBTTagCompound listStormsNBT = new NBTTagCompound();
		for (int i = 0; i < listStormObjects.size(); i++) {
			StormObject obj = listStormObjects.get(i);
			NBTTagCompound objNBT = obj.writeToNBT();
			listStormsNBT.setTag("storm_" + obj.ID, objNBT);
		}
		mainNBT.setTag("stormData", listStormsNBT);
		mainNBT.setLong("lastUsedIDStorm", StormObject.lastUsedStormID);
		
		mainNBT.setLong("lastStormFormed", lastStormFormed);
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;
		
		try {
			//Write out to file
			if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
			FileOutputStream fos = new FileOutputStream(saveFolder + "WeatherData_" + dim + ".dat");
	    	CompressedStreamTools.writeCompressed(mainNBT, fos);
	    	fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void readFromFile() {
		
		NBTTagCompound rtsNBT = new NBTTagCompound();
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;
		
		boolean readFail = false;
		
		try {
			if ((new File(saveFolder + "WeatherData_" + dim + ".dat")).exists()) {
				rtsNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim + ".dat"));
			} else {
				//readFail = true; - first run, no point
			}
		} catch (Exception ex) { 
			ex.printStackTrace();
			readFail = true;
		}
		
		//If reading file was ok, make a backup and shift names for second backup
		if (!readFail) {
			try {
				File tmp = (new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat"));
				if (tmp.exists()) FileUtils.copyFile(tmp, (new File(saveFolder + "WeatherData_" + dim + "_BACKUP1.dat")));
				if ((new File(saveFolder + "WeatherData_" + dim + ".dat").exists())) FileUtils.copyFile((new File(saveFolder + "WeatherData_" + dim + ".dat")), (new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat")));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		} else {
			System.out.println("WARNING! Weather2 File: WeatherData.dat failed to load, automatically restoring to backup from previous game run");
			try {
				//auto restore from most recent backup
				if ((new File(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat")).exists()) {
					rtsNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "WeatherData_" + dim + "_BACKUP0.dat"));
				} else {
					System.out.println("WARNING! Failed to find backup file WeatherData_BACKUP0.dat, nothing loaded");
				}
			} catch (Exception ex) { 
				ex.printStackTrace();
				System.out.println("WARNING! Error loading backup file WeatherData_BACKUP0.dat, nothing loaded");
			}
		}
		
		lastStormFormed = rtsNBT.getLong("lastStormFormed");
		
		VolcanoObject.lastUsedID = rtsNBT.getLong("lastUsedIDVolcano");
		StormObject.lastUsedStormID = rtsNBT.getLong("lastUsedIDStorm");
		
		NBTTagCompound nbtVolcanoes = rtsNBT.getCompoundTag("volcanoData");
		
		Iterator it = nbtVolcanoes.getKeySet().iterator();
		
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound teamData = (NBTTagCompound)nbtVolcanoes.getCompoundTag(tagName);
			
			VolcanoObject to = new VolcanoObject(ServerTickHandler.lookupDimToWeatherMan.get(0)/*-1, -1, null*/);
			try {
				to.readFromNBT(teamData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//to.initAITree();
			addVolcanoObject(to);
			
			//THIS LINE NEEDS REFINING FOR PLAYERS WHO JOIN AFTER THE FACT!!!
			((WeatherManagerServer)(this)).syncVolcanoNew(to);
			
			//listVolcanoes.add(to);
			//lookupVolcanoes.put(to.ID, to);
			
			to.initPost();
		}
		
		NBTTagCompound nbtStorms = rtsNBT.getCompoundTag("stormData");
		
		it = nbtStorms.getKeySet().iterator();
		
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound teamData = (NBTTagCompound)nbtStorms.getCompoundTag(tagName);
			
			if (ServerTickHandler.lookupDimToWeatherMan.get(dim) != null) {
				StormObject to = new StormObject(ServerTickHandler.lookupDimToWeatherMan.get(dim)/*-1, -1, null*/);
				try {
					to.readFromNBT(teamData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				addStormObject(to);
				
				//THIS LINE NEEDS REFINING FOR PLAYERS WHO JOIN AFTER THE FACT!!!
				((WeatherManagerServer)(this)).syncStormNew(to);
			} else {
				System.out.println("WARNING: trying to load storm objects for missing dimension: " + dim);
			}
			
			//listVolcanoes.add(to);
			//lookupVolcanoes.put(to.ID, to);
			
			//to.initPost();
		}
		
		
	}
}
