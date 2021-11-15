package weather2.weathersystem;

import CoroUtil.util.CoroUtilPhysics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import weather2.Weather;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.wind.WindManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class WeatherManager {
	public final ResourceKey<Level> dimension;
	public final WindManager wind = new WindManager(this);
	private List<WeatherObject> listStormObjects = new ArrayList<>();
	public HashMap<Long, WeatherObject> lookupStormObjectsByID = new HashMap<>();

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
			wind.tick(getWorld());
		}
	}

	public WindManager getWindManager() {
		return this.wind;
	}

	public void addStormObject(WeatherObject so) {
		if (!lookupStormObjectsByID.containsKey(so.ID)) {
			listStormObjects.add(so);
			lookupStormObjectsByID.put(so.ID, so);
		} else {
			Weather.dbg("Weather2 WARNING!!! Received new storm create for an ID that is already active! design bug or edgecase with PlayerEvent.Clone, ID: " + so.ID);
			//Weather.dbgStackTrace();

		}
	}

	public List<WeatherObject> getStormObjects() {
		return listStormObjects;
	}

	public void removeStormObject(long ID) {
		WeatherObject so = lookupStormObjectsByID.get(ID);

		if (so != null) {
			so.remove();
			listStormObjects.remove(so);
			lookupStormObjectsByID.remove(ID);
		} else {
			Weather.dbg("error looking up storm ID on server for removal: " + ID + " - lookup count: " + lookupStormObjectsByID.size() + " - last used ID: " + WeatherObject.lastUsedStormID);
		}
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

				List<Vec3> nodes = sandstorm.getSandstormAsShape();

				double scale = sandstorm.getSandstormScale();
				boolean inStorm = CoroUtilPhysics.isInConvexShape(parPos, nodes);
				double dist = CoroUtilPhysics.getDistanceToShape(parPos, nodes);
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
}
