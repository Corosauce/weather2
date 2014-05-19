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
	private List<StormObject> listStormObject = new ArrayList<StormObject>();
	public HashMap<Long, StormObject> lookupStormObjects = new HashMap<Long, StormObject>();
	
	//volcanos
	private List<VolcanoObject> listVolcanoes = new ArrayList<VolcanoObject>();
	public HashMap<Long, VolcanoObject> lookupVolcanoes = new HashMap<Long, VolcanoObject>();
	
	//wind
	public WindManager windMan = new WindManager(this);
	
	public WeatherManagerBase(int parDim) {
		dim = parDim;
	}
	
	public World getWorld() {
		return null;
	}
	
	public void tick() {
		
	}
	
	public List<StormObject> getStormObjects() {
		return listStormObject;
	}
	
	public void addStormObject(StormObject so) {
		listStormObject.add(so);
		lookupStormObjects.put(so.ID, so);
	}
	
	public void removeStormObject(long ID) {
		StormObject so = lookupStormObjects.get(ID);
		
		if (so != null) {
			so.setDead();
			listStormObject.remove(so);
			lookupStormObjects.remove(ID);
		}
	}
	
	public List<VolcanoObject> getVolcanoObjects() {
		return listVolcanoes;
	}
	
	public void addStormObject(VolcanoObject so) {
		listVolcanoes.add(so);
		lookupVolcanoes.put(so.ID, so);
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
	
	public StormObject getClosestStorm(Vec3 parPos, double maxDist) {
		
		StormObject closestStorm = null;
		double closestDist = 9999999;
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			StormObject storm = getStormObjects().get(i);
			double dist = storm.pos.distanceTo(parPos);
			if (dist < closestDist && dist <= maxDist && storm.state >= StormObject.STATE_RAIN) {
				closestStorm = storm;
				closestDist = dist;
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
			listVolcanoes.add(to);
			lookupVolcanoes.put(to.ID, to);
			to.initPost();
		}
	}
}
