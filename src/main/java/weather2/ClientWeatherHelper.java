package weather2;

import com.corosus.coroutil.util.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.config.ConfigMisc;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.StormObject;

/**
 * Moving client weather logic to here from scene enhancer.
 * Also when LT isnt in control of the weather, logic redirects to here for weather2s more special rules
 */
public final class ClientWeatherHelper {
	private static ClientWeatherHelper instance;

	private float curPrecipStr = 0F;
	private float curPrecipStrTarget = 0F;

	private float curOvercastStr = 0F;
	private float curOvercastStrTarget = 0F;

	private ClientWeatherHelper() {
	}

	public static ClientWeatherHelper get() {
		if (instance == null) {
			instance = new ClientWeatherHelper();
		}
		return instance;
	}

	public void reset() {
		instance.curPrecipStr = 0F;
		instance.curPrecipStrTarget = 0F;

		instance.curOvercastStr = 0F;
		instance.curOvercastStrTarget = 0F;
	}

	public void tick() {
		tickRainRates();
	}

	public float getPrecipitationStrength(Player entP) {
		return getPrecipitationStrength(entP, false);
	}

	/**
	 * returns 0 to 1 of storm strength
	 *
	 * @param entP
	 * @param forOvercast
	 * @return
	 */
	public float getPrecipitationStrength(Player entP, boolean forOvercast) {

		if (entP == null) return 0;
		double maxStormDist = 512 / 4 * 3;
		Vec3 plPos = new Vec3(entP.getX(), StormObject.static_YPos_layer0, entP.getZ());
		StormObject storm;

		ClientTickHandler.getClientWeather();

		storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_FORMING, -1, true);

		boolean closeEnough = false;
		double stormDist = 9999;
		float tempAdj = 1F;

		float sizeToUse = 0;

		float overcastModeMinPrecip = 0.23F;
		//overcastModeMinPrecip = 0.16F;
		//overcastModeMinPrecip = (float) ConfigStorm.Storm_Rain_Overcast_Amount;
		overcastModeMinPrecip = ClientTickHandler.weatherManager.vanillaRainAmountOnServer;

		//evaluate if storms size is big enough to be over player
		if (storm != null) {

			sizeToUse = storm.size;
			//extend overcast effect, using x2 for now since we cant cancel sound and ground particles, originally was 4x, then 3x, change to that for 1.7 if lex made change
			if (forOvercast) {
				sizeToUse *= 1F;
			}

			stormDist = storm.pos.distanceTo(plPos);
			//System.out.println("storm dist: " + stormDist);
			if (sizeToUse > stormDist) {
				closeEnough = true;
			}
		}

		if (closeEnough) {
			//max of 1 if at center of storm, subtract player xz distance out of the size to act like its a weaker storm
			double stormIntensity = (sizeToUse - stormDist) / sizeToUse;

			//why is this not a -1 or 1 anymore?!
			//tempAdj = storm.levelTemperature/* > 0 ? 1F : -1F*/;

			tempAdj = 1F;//storm.levelTemperature/* > 0 ? 1F : -1F*/;

			//limit plain rain clouds to light intensity
			if (storm.levelCurIntensityStage == StormObject.STATE_NORMAL) {
				if (stormIntensity > 0.3) stormIntensity = 0.3;
			}

			if (ConfigStorm.Storm_NoRainVisual) {
				stormIntensity = 0;
			}

			//TODO: verify this if statement was added correctly
			if (forOvercast) {
				if (stormIntensity < overcastModeMinPrecip) {
					stormIntensity = overcastModeMinPrecip;
				}
			}
			if (forOvercast) {
				curOvercastStrTarget = (float) stormIntensity;
			} else {
				curPrecipStrTarget = (float) stormIntensity;
			}
		} else {
			if (!ClientTickHandler.clientConfigData.overcastMode) {
				if (forOvercast) {
					curOvercastStrTarget = 0;
				} else {
					curPrecipStrTarget = 0;
				}
			} else {
				if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
					if (forOvercast) {
						curOvercastStrTarget = overcastModeMinPrecip;
					} else {
						curPrecipStrTarget = overcastModeMinPrecip;
					}
				} else {
					if (forOvercast) {
						curOvercastStrTarget = 0;
					} else {
						curPrecipStrTarget = 0;
					}
				}
			}
		}

		if (forOvercast) {
			if (curOvercastStr < 0.002 && curOvercastStr > -0.002F) {
				return 0;
			} else {
				return curOvercastStr * tempAdj;
			}
		} else {
			if (curPrecipStr < 0.002 && curPrecipStr > -0.002F) {
				return 0;
			} else {
				return curPrecipStr * tempAdj;
			}
		}
	}

	public void controlVisuals(boolean precipitating) {
		Minecraft mc = Minecraft.getInstance();
		ClientTickHandler.getClientWeather();
		ClientWeatherProxy weather = ClientWeatherProxy.get();
		float rainAmount = weather.getVanillaRainAmount();
		float visualDarknessAmplifier = 0.5F;
		//using 1F to make shaders happy
		visualDarknessAmplifier = 1F;
		//CULog.dbg("rainAmount: " + rainAmount);
		if (!ConfigMisc.Aesthetic_Only_Mode) {
			if (precipitating) {
				mc.level.getLevelData().setRaining(rainAmount > 0);
				mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
				mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);

			} else {
				//TODO: i think these glitch out and trigger on world load if it was already raining, will think its false for a sec and lock sky visual to off
				if (!ClientTickHandler.clientConfigData.overcastMode) {
					mc.level.getLevelData().setRaining(false);
					mc.level.setRainLevel(0);
					mc.level.setThunderLevel(0);
				} else {
					if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
						mc.level.getLevelData().setRaining(true);
						mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
						mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);
					} else {

					}
				}
			}
		}

		//TESTING
		/*mc.level.getLevelData().setRaining(true);
		mc.level.setRainLevel(1);
		mc.level.setThunderLevel(1);*/
	}

	public void tickRainRates() {

		float rateChange = 0.0015F;

		if (curOvercastStr > curOvercastStrTarget) {
			curOvercastStr -= rateChange;
		} else if (curOvercastStr < curOvercastStrTarget) {
			curOvercastStr += rateChange;
		}

		if (curPrecipStr > curPrecipStrTarget) {
			curPrecipStr -= rateChange;
		} else if (curPrecipStr < curPrecipStrTarget) {
			curPrecipStr += rateChange;
		}
	}
}
