package weather2.weathersystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.io.FileUtils;

import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.util.CoroUtilFile;

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
					((WeatherManagerServer)this).syncStormRemove(so);
					removeStormObject(so.ID);
				} else {
					//not sure why i need null manager check, it should be marked dead before thats null..... ugh
					if (!so.isDead/* && so.manager != null*/) {
						so.tick();
					}
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
			Weather.dbg("error looking up storm ID on server: " + ID + " - lookup count: " + lookupStormObjectsByID.size() + " - last used ID: " + StormObject.lastUsedStormID);
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
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			StormObject storm = getStormObjects().get(i);
			if (storm.isDead) continue;
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
	
	public void writeToFile() {
		NBTTagCompound mainNBT = new NBTTagCompound();
		NBTTagCompound listVolcanoesNBT = new NBTTagCompound();
		for (int i = 0; i < listVolcanoes.size(); i++) {
			VolcanoObject td = listVolcanoes.get(i);
			NBTTagCompound teamNBT = new NBTTagCompound();
			td.writeToNBT(teamNBT);
			listVolcanoesNBT.setCompoundTag("volcano_" + td.ID, teamNBT);
		}
		mainNBT.setCompoundTag("volcanoData", listVolcanoesNBT);
		mainNBT.setLong("lastUsedID", VolcanoObject.lastUsedID);
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;
		
		try {
			//Write out to file
			if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
			FileOutputStream fos = new FileOutputStream(saveFolder + "VolcanoData.dat");
	    	CompressedStreamTools.writeCompressed(mainNBT, fos);
	    	fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void readFromFile() {
		
		NBTTagCompound rtsNBT = new NBTTagCompound();
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "weather2" + File.separator;
		
		boolean readFail = false;
		
		try {
			if ((new File(saveFolder + "VolcanoData.dat")).exists()) {
				rtsNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "VolcanoData.dat"));
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
				File tmp = (new File(saveFolder + "VolcanoData_BACKUP0.dat"));
				if (tmp.exists()) FileUtils.copyFile(tmp, (new File(saveFolder + "VolcanoData_BACKUP1.dat")));
				if ((new File(saveFolder + "VolcanoData.dat").exists())) FileUtils.copyFile((new File(saveFolder + "VolcanoData.dat")), (new File(saveFolder + "VolcanoData_BACKUP0.dat")));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		} else {
			System.out.println("WARNING! Weather2 File: VolcanoData.dat failed to load, automatically restoring to backup from previous game run");
			try {
				//auto restore from most recent backup
				if ((new File(saveFolder + "VolcanoData_BACKUP0.dat")).exists()) {
					rtsNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "VolcanoData_BACKUP0.dat"));
				} else {
					System.out.println("WARNING! Failed to find backup file VolcanoData_BACKUP0.dat, nothing loaded");
				}
			} catch (Exception ex) { 
				ex.printStackTrace();
				System.out.println("WARNING! Error loading backup file VolcanoData_BACKUP0.dat, nothing loaded");
			}
		}
		
		VolcanoObject.lastUsedID = rtsNBT.getLong("lastUsedID");
		
		NBTTagCompound teamDataList = rtsNBT.getCompoundTag("volcanoData");
		Collection teamDataListCl = teamDataList.getTags();
		Iterator it = teamDataListCl.iterator();
		
		while (it.hasNext()) {
			NBTTagCompound teamData = (NBTTagCompound)it.next();
			
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
	}
}
