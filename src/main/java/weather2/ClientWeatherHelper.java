package weather2;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.StormObject;

/**
 * Moving client weather logic to here from scene enhancer.
 * Also when LT isnt in control of the weather, logic redirects to here for weather2s more special rules
 */
public final class ClientWeatherHelper {
	private static ClientWeatherHelper instance = new ClientWeatherHelper();

	private float curPrecipStr = 0F;
	private float curPrecipStrTarget = 0F;

	private float curOvercastStr = 0F;
	private float curOvercastStrTarget = 0F;

	private ClientWeatherHelper() {
	}

	public static ClientWeatherHelper get() {
		return instance;
	}

	public static void reset() {
		instance = new ClientWeatherHelper();
	}

	public void tick() {
		tickRainRates();
	}

	public float getRainStrengthAndControlVisuals(Player entP) {
		return getRainStrengthAndControlVisuals(entP, false);
	}
	public float getRainStrengthAndControlVisuals(Player entP, boolean forOvercast) {

		if (entP == null) return 0;
		double maxStormDist = 512 / 4 * 3;
		Vec3 plPos = new Vec3(entP.getX(), StormObject.static_YPos_layer0, entP.getZ());
		StormObject storm;

		ClientTickHandler.checkClientWeather();

		storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_FORMING, true);

		boolean closeEnough = false;
		double stormDist = 9999;
		float tempAdj = 1F;

		float sizeToUse = 0;

		float overcastModeMinPrecip = 0.23F;
		//overcastModeMinPrecip = 0.16F;
		overcastModeMinPrecip = (float) ConfigStorm.Storm_Rain_Overcast_Amount;

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
			double stormIntensity = (sizeToUse - stormDist) / sizeToUse;

			tempAdj = storm.levelTemperature/* > 0 ? 1F : -1F*/;

			//limit plain rain clouds to light intensity
			if (storm.levelCurIntensityStage == StormObject.STATE_NORMAL) {
				if (stormIntensity > 0.3) stormIntensity = 0.3;
			}

			if (ConfigStorm.Storm_NoRainVisual) {
				stormIntensity = 0;
			}

			if (stormIntensity < overcastModeMinPrecip) {
				stormIntensity = overcastModeMinPrecip;
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
			if (curOvercastStr < 0.001 && curOvercastStr > -0.001F) {
				return 0;
			} else {
				return curOvercastStr * tempAdj;
			}
		} else {
			if (curPrecipStr < 0.001 && curPrecipStr > -0.001F) {
				return 0;
			} else {
				return curPrecipStr * tempAdj;
			}
		}
	}

	public void controlVisuals(boolean precipitating) {
		Minecraft mc = Minecraft.getInstance();
		if (precipitating) {
			mc.level.getLevelData().setRaining(true);
			//TODO: will this do to replace setThundering?
			mc.level.setThunderLevel(1F);
		} else {
			if (!ClientTickHandler.clientConfigData.overcastMode) {
				mc.level.getLevelData().setRaining(false);
				mc.level.setThunderLevel(0F);
			} else {
				if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
					mc.level.getLevelData().setRaining(true);
					mc.level.setThunderLevel(1F);
				} else {

				}
			}
		}
	}

	public void tickRainRates() {

		float rateChange = 0.0005F;

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
