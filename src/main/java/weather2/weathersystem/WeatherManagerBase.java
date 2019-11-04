package weather2.weathersystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

import CoroUtil.util.CoroUtilPhysics;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.io.FileUtils;

import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.util.Vec3;

public class WeatherManagerBase {

	//shared stuff, stormfront list
	
	public int dim;
	
	//storms
	private List<WeatherObject> listStormObjects = new ArrayList<>();
	public HashMap<Long, WeatherObject> lookupStormObjectsByID = new HashMap<>();
	public HashMap<Integer, ArrayList<StormObject>> lookupStormObjectsByLayer = new HashMap<>();
	//private ArrayList<ArrayList<StormObject>> listStormObjectsByLayer = new ArrayList<ArrayList<StormObject>>();
	
	//volcanos
	/*private List<VolcanoObject> listVolcanoes = new ArrayList<>();
	public HashMap<Long, VolcanoObject> lookupVolcanoes = new HashMap<>();*/
	
	//wind
	public WindManager windMan;
	
	//for client only
	public boolean isVanillaRainActiveOnServer = false;
	public boolean isVanillaThunderActiveOnServer = false;
	public int vanillaRainTimeOnServer = 0;
	
	public long lastStormFormed = 0;

	public long lastSandstormFormed = 0;

	//0 = none, 1 = usual max overcast
	public float cloudIntensity = 1F;

	private HashSet<Long> listWeatherBlockDamageDeflector = new HashSet<>();

	public WeatherManagerBase(int parDim) {
		dim = parDim;
		windMan = new WindManager(this);
		lookupStormObjectsByLayer.put(0, new ArrayList<>());
		lookupStormObjectsByLayer.put(1, new ArrayList<>());
		lookupStormObjectsByLayer.put(2, new ArrayList<>());
	}
	
	public void reset() {
		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject so = getStormObjects().get(i);
			
			so.reset();
		}
		
		getStormObjects().clear();
		lookupStormObjectsByID.clear();
		try {
			for (int i = 0; i < lookupStormObjectsByLayer.size(); i++) {
				lookupStormObjectsByLayer.get(i).clear();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		/*for (int i = 0; i < getVolcanoObjects().size(); i++) {
			VolcanoObject vo = getVolcanoObjects().get(i);
			
			vo.reset();
		}
		
		getVolcanoObjects().clear();
		lookupVolcanoes.clear();*/
		
		windMan.reset();

		//do not reset this, its static (shared between client and server) and client side calls reset()
		//WeatherObject.lastUsedStormID = 0;
	}
	
	public World getWorld() {
		return null;
	}
	
	public void tick() {
		World world = getWorld();
		if (world != null) {
			//tick storms
			List<WeatherObject> list = getStormObjects();
			for (int i = 0; i < list.size(); i++) {
				WeatherObject so = list.get(i);
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
									so2.remove();
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
								Weather.dbg("WARNING!!! - detected isDead storm object still in client side list, had to remove storm object with ID " + so.ID + " from client side, wasnt properly isDead via main channels");
								removeStormObject(so.ID);
							}
							//Weather.dbg("client storm is dead and still in list, bug?");
						}
						
					//}
				}
			}
						
			//tick volcanos
			/*for (int i = 0; i < getVolcanoObjects().size(); i++) {
				getVolcanoObjects().get(i).tick();
			}*/

			//tick wind
			windMan.tick();
		}
	}
	
	public void tickRender(float partialTick) {
		World world = getWorld();
		if (world != null) {
			//tick storms
			//There are scenarios where getStormObjects().get(i) returns a null storm, uncertain why, for now try to catch it and move on
			try {
				for (int i = 0; i < getStormObjects().size(); i++) {
					WeatherObject obj = getStormObjects().get(i);
					if (obj != null) {
						obj.tickRender(partialTick);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<WeatherObject> getStormObjects() {
		return listStormObjects;
	}
	
	public List<StormObject> getStormObjectsByLayer(int layer) {
		return lookupStormObjectsByLayer.get(layer);
	}

	public StormObject getStormObjectByID(long ID) {
		WeatherObject obj = lookupStormObjectsByID.get(ID);
		if (obj instanceof StormObject) {
			return (StormObject) obj;
		} else {
			return null;
		}
	}
	
	public void addStormObject(WeatherObject so) {
		if (!lookupStormObjectsByID.containsKey(so.ID)) {
			listStormObjects.add(so);
			lookupStormObjectsByID.put(so.ID, so);
			if (so instanceof StormObject) {
				StormObject so2 = (StormObject) so;
				lookupStormObjectsByLayer.get(so2.layer).add(so2);
			}
		} else {
			Weather.dbg("Weather2 WARNING!!! Received new storm create for an ID that is already active! design bug or edgecase with PlayerEvent.Clone, ID: " + so.ID);
			Weather.dbgStackTrace();

		}
	}
	
	public void removeStormObject(long ID) {
		WeatherObject so = lookupStormObjectsByID.get(ID);
		
		if (so != null) {
			so.remove();
			listStormObjects.remove(so);
			lookupStormObjectsByID.remove(ID);
			if (so instanceof StormObject) {
				StormObject so2 = (StormObject) so;
				lookupStormObjectsByLayer.get(so2.layer).remove(so2);
			}
		} else {
			Weather.dbg("error looking up storm ID on server for removal: " + ID + " - lookup count: " + lookupStormObjectsByID.size() + " - last used ID: " + WeatherObject.lastUsedStormID);
		}
	}
	
	/*public List<VolcanoObject> getVolcanoObjects() {
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
			vo.remove();
			listVolcanoes.remove(vo);
			lookupVolcanoes.remove(ID);
			
			Weather.dbg("removing volcano");
		}
	}*/
	
	public StormObject getClosestStormAny(Vec3 parPos, double maxDist) {
		return getClosestStorm(parPos, maxDist, -1, true);
	}
	
	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin) {
		return getClosestStorm(parPos, maxDist, severityFlagMin, false);
	}
	
	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin, boolean orRain) {
		
		/*StormObject closestStorm = null;
		double closestDist = Double.MAX_VALUE;
		
		List<WeatherObject> listStorms = getStormObjects();
		
		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof StormObject) {
				StormObject storm = (StormObject) wo;
				if (storm == null || storm.isDead) continue;
				double dist = storm.pos.distanceTo(parPos);
				*//*if (getWorld().isRemote) {
					System.out.println("close storm candidate: " + dist + " - " + storm.state + " - " + storm.attrib_rain);
				}*//*
				if (dist < closestDist && dist <= maxDist) {
					if ((storm.attrib_precipitation && orRain) || (severityFlagMin == -1 || storm.levelCurIntensityStage >= severityFlagMin)) {
						closestStorm = storm;
						closestDist = dist;
					}
				}
			}
			
		}
		
		return closestStorm;*/

		//not sure i can avoid a double use of distance calculation adding to iteration cost, this method might not be stream worthy
		return getStormObjects().stream()
				.map(wo -> (StormObject)wo)
				.filter(so -> !so.isDead)
				.filter(so -> (so.attrib_precipitation && orRain) || (severityFlagMin == -1 || so.levelCurIntensityStage >= severityFlagMin))
				.filter(so -> so.pos.distanceTo(parPos) < maxDist)
				.min(Comparator.comparing(so -> so.pos.distanceTo(parPos))).orElse(null);
	}

	public boolean isPrecipitatingAt(BlockPos pos) {
		return isPrecipitatingAt(new Vec3(pos));
	}

	/**
	 * TODO: Heavy on the processing, consider caching the result by location for 20 ticks
	 *
	 * @param parPos
	 * @return
	 */
	public boolean isPrecipitatingAt(Vec3 parPos) {
		/*List<WeatherObject> listStorms = getStormObjects();

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof StormObject) {
				StormObject storm = (StormObject) wo;
				if (storm == null || storm.isDead) continue;
				if (storm.attrib_precipitation) {
					double dist = storm.pos.distanceTo(parPos);
					if (dist < storm.size) {
						return true;
					}
				}
			}
		}

		return false;*/

		return getStormObjects().stream()
				.map(wo -> (StormObject)wo)
				.anyMatch(so -> !so.isDead && so.attrib_precipitation && so.pos.distanceTo(parPos) < so.size);
	}

	/**
	 * Simply compares stormfront distances, doesnt factor in tail
	 *
	 * @param parPos
	 * @param maxDist
	 * @return
	 */
	public WeatherObjectSandstorm getClosestSandstorm(Vec3 parPos, double maxDist) {
		
		WeatherObjectSandstorm closestStorm = null;
		double closestDist = 9999999;
		
		List<WeatherObject> listStorms = getStormObjects();
		
		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof WeatherObjectSandstorm) {
				WeatherObjectSandstorm storm = (WeatherObjectSandstorm) wo;
				if (storm == null || storm.isDead) continue;
				double dist = storm.pos.distanceTo(parPos);
				/*if (getWorld().isRemote) {
					System.out.println("close storm candidate: " + dist + " - " + storm.state + " - " + storm.attrib_rain);
				}*/
				if (dist < closestDist && dist <= maxDist) {
					//if ((storm.attrib_precipitation && orRain) || (severityFlagMin == -1 || storm.levelCurIntensityStage >= severityFlagMin)) {
						closestStorm = storm;
						closestDist = dist;
					//}
				}
			}
			
		}
		
		return closestStorm;
	}

	/**
	 * Gets the most intense sandstorm, used for effects and sounds
	 *
	 * @param parPos
	 * @return
	 */
	public WeatherObjectSandstorm getClosestSandstormByIntensity(Vec3 parPos/*, double maxDist*/) {

		WeatherObjectSandstorm bestStorm = null;
		double closestDist = 9999999;
		double mostIntense = 0;

		List<WeatherObject> listStorms = getStormObjects();

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof WeatherObjectSandstorm) {
				WeatherObjectSandstorm sandstorm = (WeatherObjectSandstorm) wo;
				if (sandstorm == null || sandstorm.isDead) continue;

				List<Vec3> field_75884_a = sandstorm.getSandstormAsShape();

				double scale = sandstorm.getSandstormScale();
				boolean inStorm = CoroUtilPhysics.isInConvexShape(parPos, field_75884_a);
				double dist = CoroUtilPhysics.getDistanceToShape(parPos, field_75884_a);
				//if best is within storm, compare intensity
				if (inStorm) {
					//System.out.println("in storm");
					closestDist = 0;
					if (scale > mostIntense) {
						mostIntense = scale;
						bestStorm = sandstorm;
					}
				//if best is not within storm, compare distance to shape
				} else if (closestDist > 0/* && dist < maxDist*/) {
					if (dist < closestDist) {
						closestDist = dist;
						bestStorm = sandstorm;
					}
				}
			}

		}

		return bestStorm;
	}

	public List<WeatherObject> getSandstormsAround(Vec3 parPos, double maxDist) {
		List<WeatherObject> storms = new ArrayList<>();

		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);
			if (wo instanceof WeatherObjectSandstorm) {
				WeatherObjectSandstorm storm = (WeatherObjectSandstorm) wo;
				if (storm.isDead) continue;

				if (storm.pos.distanceTo(parPos) < maxDist) {
					storms.add(storm);
				}
			}
		}

		return storms;
	}

    public List<WeatherObject> getStormsAroundForDeflector(Vec3 parPos, double maxDist) {
        List<WeatherObject> storms = new ArrayList<>();

        for (int i = 0; i < getStormObjects().size(); i++) {
            WeatherObject wo = getStormObjects().get(i);
            if (wo.isDead) continue;
            if (wo instanceof StormObject) {
                StormObject storm = (StormObject) wo;
                if (storm.pos.distanceTo(parPos) < maxDist && ((storm.attrib_precipitation && ConfigStorm.Storm_Deflector_RemoveRainstorms) || storm.levelCurIntensityStage >= ConfigStorm.Storm_Deflector_MinStageRemove)) {
                    storms.add(storm);
                }
            } else if (wo instanceof WeatherObjectSandstorm && ConfigStorm.Storm_Deflector_RemoveSandstorms) {
                WeatherObjectSandstorm sandstorm = (WeatherObjectSandstorm)wo;
                List<Vec3> field_75884_a = sandstorm.getSandstormAsShape();
                double distToStorm = CoroUtilPhysics.getDistanceToShape(parPos, field_75884_a);
                if (distToStorm < maxDist) {
                    storms.add(wo);
                }
            }
        }

        return storms;
    }

	public List<WeatherObject> getStormsAround(Vec3 parPos, double maxDist) {
		List<WeatherObject> storms = new ArrayList<>();
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);
			if (wo.isDead) continue;
			if (wo instanceof StormObject) {
				StormObject storm = (StormObject) wo;
				if (storm.pos.distanceTo(parPos) < maxDist && (storm.attrib_precipitation || storm.levelCurIntensityStage > StormObject.STATE_NORMAL)) {
					storms.add(storm);
				}
			} else if (wo instanceof WeatherObjectSandstorm) {
				WeatherObjectSandstorm sandstorm = (WeatherObjectSandstorm)wo;
				List<Vec3> field_75884_a = sandstorm.getSandstormAsShape();
				double distToStorm = CoroUtilPhysics.getDistanceToShape(parPos, field_75884_a);
				if (distToStorm < maxDist) {
					storms.add(wo);
				}
			}
		}
		
		return storms;
	}
	
	public void writeToFile() {
		CompoundNBT mainNBT = new CompoundNBT();
		/*CompoundNBT listVolcanoesNBT = new CompoundNBT();
		for (int i = 0; i < listVolcanoes.size(); i++) {
			VolcanoObject td = listVolcanoes.get(i);
			CompoundNBT teamNBT = new CompoundNBT();
			td.write(teamNBT);
			listVolcanoesNBT.put("volcano_" + td.ID, teamNBT);
		}
		mainNBT.put("volcanoData", listVolcanoesNBT);
		mainNBT.putLong("lastUsedIDVolcano", VolcanoObject.lastUsedID);*/
		
		CompoundNBT listStormsNBT = new CompoundNBT();
		for (int i = 0; i < listStormObjects.size(); i++) {
			WeatherObject obj = listStormObjects.get(i);
			obj.getNbtCache().setUpdateForced(true);
			obj.write();
			obj.getNbtCache().setUpdateForced(false);
			listStormsNBT.put("storm_" + obj.ID, obj.getNbtCache().getNewNBT());
		}
		mainNBT.put("stormData", listStormsNBT);
		mainNBT.putLong("lastUsedIDStorm", WeatherObject.lastUsedStormID);
		
		mainNBT.putLong("lastStormFormed", lastStormFormed);

		mainNBT.putLong("lastSandstormFormed", lastSandstormFormed);

		mainNBT.putFloat("cloudIntensity", this.cloudIntensity);

		mainNBT.put("windMan", windMan.write(new CompoundNBT()));
		
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
		
		CompoundNBT rtsNBT = new CompoundNBT();
		
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
		lastSandstormFormed = rtsNBT.getLong("lastSandstormFormed");

		//prevent setting to 0 for worlds updating to new weather version
		if (rtsNBT.contains("cloudIntensity")) {
			cloudIntensity = rtsNBT.getFloat("cloudIntensity");
		}
		
		/*VolcanoObject.lastUsedID = rtsNBT.getLong("lastUsedIDVolcano");*/
		WeatherObject.lastUsedStormID = rtsNBT.getLong("lastUsedIDStorm");

		windMan.read(rtsNBT.getCompound("windMan"));
		
		/*CompoundNBT nbtVolcanoes = rtsNBT.getCompound("volcanoData");
		
		Iterator it = nbtVolcanoes.keySet().iterator();
		
		while (it.hasNext()) {
			String tagName = (String) it.next();
			CompoundNBT teamData = nbtVolcanoes.getCompound(tagName);
			
			VolcanoObject to = new VolcanoObject(ServerTickHandler.lookupDimToWeatherMan.get(0));
			try {
				to.read(teamData);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//to.initAITree();
			//addVolcanoObject(to);
			
			//THIS LINE NEEDS REFINING FOR PLAYERS WHO JOIN AFTER THE FACT!!!
			//((WeatherManagerServer)(this)).syncVolcanoNew(to);
			
			//listVolcanoes.add(to);
			//lookupVolcanoes.put(to.ID, to);
			
			to.initPost();
		}*/
		
		CompoundNBT nbtStorms = rtsNBT.getCompound("stormData");
		
		Iterator it = nbtStorms.keySet().iterator();
		
		while (it.hasNext()) {
			String tagName = (String) it.next();
			CompoundNBT data = nbtStorms.getCompound(tagName);
			
			if (ServerTickHandler.lookupDimToWeatherMan.get(dim) != null) {
                WeatherObject wo = null;
                if (data.getInt("weatherObjectType") == EnumWeatherObjectType.CLOUD.ordinal()) {
                    wo = new StormObject(this/*-1, -1, null*/);
                } else if (data.getInt("weatherObjectType") == EnumWeatherObjectType.SAND.ordinal()) {
                    wo = new WeatherObjectSandstorm(this);
                    //initStormNew???
                }
				try {
					wo.getNbtCache().setNewNBT(data);
					wo.read();
					wo.getNbtCache().updateCacheFromNew();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				addStormObject(wo);

				//TODO: possibly unneeded/redundant/bug inducing, packets will be sent upon request from client
				((WeatherManagerServer)(this)).syncStormNew(wo);
			} else {
				System.out.println("WARNING: trying to load storm objects for missing dimension: " + dim);
			}
			
			//listVolcanoes.add(to);
			//lookupVolcanoes.put(to.ID, to);
			
			//to.initPost();
		}
		
		
	}
	
	public WindManager getWindManager() {
		return this.windMan;
	}

	public HashSet<Long> getListWeatherBlockDamageDeflector() {
		return listWeatherBlockDamageDeflector;
	}

	public void setListWeatherBlockDamageDeflector(HashSet<Long> listWeatherBlockDamageDeflector) {
		this.listWeatherBlockDamageDeflector = listWeatherBlockDamageDeflector;
	}
}
