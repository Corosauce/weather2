package weather2.weathersystem;

import com.corosus.coroutil.util.CULog;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import weather2.IWorldData;
import weather2.Weather;
import weather2.WorldNBTData;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.*;
import weather2.weathersystem.wind.WindManager;

import java.util.*;

public abstract class WeatherManager implements IWorldData {
	public final ResourceKey<Level> dimension;
	private final WindManager wind = new WindManager(this);
	private List<WeatherObject> listStormObjects = new ArrayList<>();
	public HashMap<Long, WeatherObject> lookupStormObjectsByID = new HashMap<>();

	public long lastStormFormed = 0;

	public long lastSandstormFormed = 0;

	//0 = none, 1 = usual max overcast
	public float cloudIntensity = 1F;

	//for client only
	public boolean isVanillaRainActiveOnServer = false;
	public boolean isVanillaThunderActiveOnServer = false;
	public int vanillaRainTimeOnServer = 0;

	private HashMap<Long, BlockPos> listWeatherBlockDamageDeflector = new HashMap<>();

	public WeatherManager(ResourceKey<Level> dimension) {
		this.dimension = dimension;
	}

	public abstract Level getWorld();

	public void tick() {
		Level world = getWorld();
		if (world != null) {
			//tick storms
			List<WeatherObject> list = getStormObjects();
			for (int i = 0; i < list.size(); i++) {
				WeatherObject so = list.get(i);
				if (this instanceof WeatherManagerServer && so.isDead) {
					removeStormObject(so.ID);
					((WeatherManagerServer)this).syncStormRemove(so);
				} else {

					if (!so.isDead) {
						so.tick();
					} else {
						if (getWorld().isClientSide) {
							Weather.dbg("WARNING!!! - detected isDead storm object still in client side list, had to remove storm object with ID " + so.ID + " from client side, wasnt properly isDead via main channels");
							removeStormObject(so.ID);
						}
					}
				}
			}

			//tick wind
			wind.tick();
		}
	}

	public WeatherObjectParticleStorm getClosestParticleStormByIntensity(Vec3 parPos, WeatherObjectParticleStorm.StormType type) {
		return getClosestParticleStormByIntensity(parPos, type, false);
	}

	/**
	 * Gets the most intense sandstorm, used for effects and sounds
	 *
	 * @param parPos
	 * @return
	 */
	public WeatherObjectParticleStorm getClosestParticleStormByIntensity(Vec3 parPos, WeatherObjectParticleStorm.StormType type, boolean forced) {

		WeatherObjectParticleStorm bestStorm = null;
		double closestDist = 9999999;

		List<WeatherObject> listStorms = getStormObjects();

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof WeatherObjectParticleStorm) {
				WeatherObjectParticleStorm sandstorm = (WeatherObjectParticleStorm) wo;
				if (sandstorm == null || sandstorm.isDead || sandstorm.getType() != type) continue;

				double dist = parPos.distanceTo(sandstorm.pos);

				if (closestDist > 0/* && dist < maxDist*/) {
					if (dist < closestDist) {
						closestDist = dist;
						bestStorm = sandstorm;
					}
				}
			}

		}

		return bestStorm;
	}

	public void reset() {
		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject so = getStormObjects().get(i);

			so.reset();
		}

		getStormObjects().clear();
		lookupStormObjectsByID.clear();

		wind.reset();

		//do not reset this, its static (shared between client and server) and client side calls reset()
		//WeatherObject.lastUsedStormID = 0;
	}

	public void tickRender(float partialTick) {
		Level world = getWorld();
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
			}
		} else {
			Weather.dbg("Weather2 WARNING!!! Received new storm create for an ID that is already active! design bug or edgecase with PlayerEvent.Clone, ID: " + so.ID);
			//Weather.dbgStackTrace();

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
			}
		} else {
			Weather.dbg("error looking up storm ID on server for removal: " + ID + " - lookup count: " + lookupStormObjectsByID.size() + " - last used ID: " + WeatherObject.lastUsedStormID);
		}
	}

	public StormObject getClosestStormAny(Vec3 parPos, double maxDist) {
		return getClosestStorm(parPos, maxDist, -1, -1, true);
	}

	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin) {
		return getClosestStorm(parPos, maxDist, severityFlagMin, -1, false);
	}

	public StormObject getClosestStorm(Vec3 parPos, double maxDist, int severityFlagMin, int severityFlagMax, boolean orRain) {

		StormObject closestStorm = null;
		double closestDist = Double.MAX_VALUE;

		List<WeatherObject> listStorms = getStormObjects();

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof StormObject) {
				StormObject storm = (StormObject) wo;
				if (storm == null || storm.isDead) continue;
				double dist = storm.pos.distanceTo(parPos);
				if (dist < closestDist && dist <= maxDist && !storm.isFirenado) {
					if ((storm.attrib_precipitation && orRain) ||
							((severityFlagMin == -1 || storm.levelCurIntensityStage >= severityFlagMin) &&
									(severityFlagMax == -1 || storm.levelCurIntensityStage <= severityFlagMax))) {
						closestStorm = storm;
						closestDist = dist;
					}
				}
			}
		}

		return closestStorm;

		//not sure i can avoid a double use of distance calculation adding to iteration cost, this method might not be stream worthy
		/*return getStormObjects().stream()
				.map(wo -> (StormObject)wo)
				.filter(so -> !so.isDead)
				.filter(so -> (so.attrib_precipitation && orRain) || (severityFlagMin == -1 || so.levelCurIntensityStage >= severityFlagMin))
				.filter(so -> so.pos.distanceTo(parPos) < maxDist)
				.min(Comparator.comparing(so -> so.pos.distanceTo(parPos))).orElse(null);*/
	}

	public boolean isPrecipitatingAt(BlockPos pos) {
		return isPrecipitatingAt(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
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
	public WeatherObjectSandstormOld getClosestSandstorm(Vec3 parPos, double maxDist) {

		WeatherObjectSandstormOld closestStorm = null;
		double closestDist = 9999999;

		List<WeatherObject> listStorms = getStormObjects();

		for (int i = 0; i < listStorms.size(); i++) {
			WeatherObject wo = listStorms.get(i);
			if (wo instanceof WeatherObjectSandstormOld) {
				WeatherObjectSandstormOld storm = (WeatherObjectSandstormOld) wo;
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

	public List<WeatherObject> getSandstormsAround(Vec3 parPos, double maxDist) {
		List<WeatherObject> storms = new ArrayList<>();

		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);
			if (wo instanceof WeatherObjectSandstormOld) {
				WeatherObjectSandstormOld storm = (WeatherObjectSandstormOld) wo;
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
			} else if (wo instanceof WeatherObjectSandstormOld && ConfigStorm.Storm_Deflector_RemoveSandstorms) {
				WeatherObjectSandstormOld sandstorm = (WeatherObjectSandstormOld)wo;
				double distToStorm = parPos.distanceTo(sandstorm.pos);
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
			} else if (wo instanceof WeatherObjectSandstormOld) {
				WeatherObjectSandstormOld sandstorm = (WeatherObjectSandstormOld)wo;
				double distToStorm = parPos.distanceTo(sandstorm.pos);
				if (distToStorm < maxDist) {
					storms.add(wo);
				}
			}
		}

		return storms;
	}

	@Override
	public CompoundTag save(CompoundTag data) {

		CULog.dbg("WeatherManager save");

		CompoundTag listStormsNBT = new CompoundTag();
		for (int i = 0; i < listStormObjects.size(); i++) {
			WeatherObject obj = listStormObjects.get(i);
			obj.getNbtCache().setUpdateForced(true);
			obj.write();
			obj.getNbtCache().setUpdateForced(false);
			listStormsNBT.put("storm_" + obj.ID, obj.getNbtCache().getNewNBT());
		}
		data.put("stormData", listStormsNBT);
		data.putLong("lastUsedIDStorm", WeatherObject.lastUsedStormID);

		data.putLong("lastStormFormed", lastStormFormed);

		data.putLong("lastSandstormFormed", lastSandstormFormed);

		data.putFloat("cloudIntensity", this.cloudIntensity);

		data.put("windMan", wind.write(new CompoundTag()));
		return data;
	}

	public void read() {

		WorldNBTData worldNBTData = ((ServerLevel)getWorld()).getDataStorage().computeIfAbsent(WorldNBTData::load, WorldNBTData::new, Weather.MODID + "-" + "weather_data");
		worldNBTData.setDataHandler(this);

		CULog.dbg("weather data: " + worldNBTData.getData());

		CompoundTag data = worldNBTData.getData();

		lastStormFormed = data.getLong("lastStormFormed");
		lastSandstormFormed = data.getLong("lastSandstormFormed");

		//prevent setting to 0 for worlds updating to new weather version
		if (data.contains("cloudIntensity")) {
			cloudIntensity = data.getFloat("cloudIntensity");
		}

		WeatherObject.lastUsedStormID = data.getLong("lastUsedIDStorm");

		wind.read(data.getCompound("windMan"));

		CompoundTag nbtStorms = data.getCompound("stormData");

		Iterator it = nbtStorms.getAllKeys().iterator();

		while (it.hasNext()) {
			String tagName = (String) it.next();
			CompoundTag stormData = nbtStorms.getCompound(tagName);

			//if (ServerTickHandler.getWeatherManagerFor(dimension) != null) {
				WeatherObject wo = null;
				if (stormData.getInt("weatherObjectType") == EnumWeatherObjectType.CLOUD.ordinal()) {
					wo = new StormObject(this);
				} else if (stormData.getInt("weatherObjectType") == EnumWeatherObjectType.SAND.ordinal()) {
					wo = new WeatherObjectParticleStorm(this);
					((WeatherObjectParticleStorm)wo).setType(WeatherObjectParticleStorm.StormType.SANDSTORM);
					//initStormNew???
				} else if (stormData.getInt("weatherObjectType") == EnumWeatherObjectType.SNOW.ordinal()) {
					wo = new WeatherObjectParticleStorm(this);
					((WeatherObjectParticleStorm)wo).setType(WeatherObjectParticleStorm.StormType.SNOWSTORM);
					//initStormNew???
				}
				try {
					wo.getNbtCache().setNewNBT(stormData);
					wo.read();
					wo.getNbtCache().updateCacheFromNew();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				addStormObject(wo);

				//TODO: possibly unneeded/redundant/bug inducing, packets will be sent upon request from client
				((WeatherManagerServer)(this)).syncStormNew(wo);
			/*} else {
				System.out.println("WARNING: trying to load storm objects for missing dimension: " + dimension);
			}*/
		}

		CULog.dbg("reloaded weather objects: " + listStormObjects.size());
	}

	public WindManager getWindManager() {
		return this.wind;
	}

	public HashMap<Long, BlockPos> getListWeatherBlockDamageDeflector() {
		return listWeatherBlockDamageDeflector;
	}

	public void registerDeflector(BlockPos pos) {
		long hash = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
		if (this.listWeatherBlockDamageDeflector.containsKey(hash)) {
			this.listWeatherBlockDamageDeflector.put(hash, pos);
		}
	}

}
