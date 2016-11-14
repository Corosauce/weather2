package weather2.weathersystem.wind;

import java.util.Random;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.Vec3;

public class WindManager {

	//2 wind layers:
	
	//1: event wind:
	//1a: storm event, pulling wind into tornado
	//1b: wind gusts
	//2: high level wind that clouds use
	
	//particles use in priority order: storm event, if no event, gust, if no gust, global wind
	
	//global wind wont have gusts, but slowly changes intensity and angle
	
	//weather effect wind will have gusts and overrides from weather events
	
	//small design exception:
	//- gusts are server side global, as planned
	//- events are client side player, required small adjustments
	
	//TODO: save wind state to disk
	
	public WeatherManagerBase manager;
	
	//global
	public float windAngleGlobal = 0;
	public float windSpeedGlobal = 0;
	public float windSpeedGlobalChangeRate = 0.05F;
	public int windSpeedGlobalRandChangeTimer = 0;
	public int windSpeedGlobalRandChangeDelay = 10;
	
	//generic?
	public float windSpeedMin = 0.01F;
	public float windSpeedMax = 1F;
	
	//events - design derp, we're making this client side, so its set based on closest storm to the client side player
	public float windAngleEvent = 0;
	public float windSpeedEvent = 0;
	public int windTimeEvent = 0; //its assumed this will get set by whatever initializes an event, and this class counts it down from a couple seconds, helps wind system know what takes priority
	
	//gusts
	public float windAngleGust = 0;
	public float windSpeedGust = 0;
	public int windTimeGust = 0;
	//public float directionGust = 0;
	//public float directionBeforeGust = 0;
	public int windGustEventTimeRand = 60;
	public float chanceOfWindGustEvent = 0.5F;

	//low wind event
	public int lowWindTimer = 0;
	public int lowWindTimerEnableAmountBase = 20*60*2;
	public int lowWindTimerEnableAmountRnd = 20*60*10;
	public int lowWindOddsTo1 = 20*200;
	
	//high wind event
	public int highWindTimer = 0;
	public int highWindTimerEnableAmountBase = 20*60*2;
	public int highWindTimerEnableAmountRnd = 20*60*10;
	public int highWindOddsTo1 = 20*400;
	
	public WindManager(WeatherManagerBase parManager) {
		manager = parManager;
		
		Random rand = new Random();
		
		windAngleGlobal = rand.nextInt(360);
	}
	
	//Speed getters\\
	
	//WIP
	public float getWindSpeedForPriority() {
		//gets event wind, or if none, global, etc
		if (windTimeEvent > 0) {
			return getWindSpeedForEvents();
		} else if (windTimeGust > 0) {
			return getWindSpeedForGusts();
		} else {
			return getWindSpeedForClouds();
		}
	}
	
	public float getWindSpeedForEvents() {
		if (windTimeEvent > 0) {
			return windSpeedEvent;
		} else {
			return 0;
		}
	}
	
	public float getWindSpeedForGusts() {
		return windSpeedGust;
	}
	
	public float getWindSpeedForClouds() {
		return windSpeedGlobal;
	}
	
	//Angle getters\\
	
	/**
	 * WIP, Returns angle in degrees, 0-360
	 * 
	 * @return
	 */
	public float getWindAngleForPriority() {
		//gets event wind, or if none, global, etc
		if (windTimeEvent > 0) {
			return getWindAngleForEvents();
		} else if (windTimeGust > 0) {
			return getWindAngleForGusts();
		} else {
			return getWindAngleForClouds();
		}
	}
	
	/**
	 * Returns angle in degrees, 0-360
	 * 
	 * @return
	 */
	public float getWindAngleForEvents() {
		return windAngleEvent;
	}
	
	/**
	 * Returns angle in degrees, 0-360
	 * 
	 * @return
	 */
	public float getWindAngleForGusts() {
		return windAngleGust;
	}
	
	/**
	 * Returns angle in degrees, 0-360
	 * 
	 * @return
	 */
	public float getWindAngleForClouds() {
		return windAngleGlobal;
	}
	
	public void setWindTimeGust(int parVal) {
		windTimeGust = parVal;
		syncData();
		//Weather.dbg("Wind gust time set: " + parVal);
	}
	
	public void setWindTimeEvent(int parVal) {
		windTimeEvent = parVal;
		//syncData(); - might be too often
		//Weather.dbg("Wind event time set: " + parVal);
	}
	
	public void tick() {
		
		Random rand = new Random();
		
		//debug
		//Weather.dbg("wind angle: " + windAngleGlobal);
		//windAngleGlobal = 90;
		//windSpeedMin = 0.2F;
		//windAngleGlobal = 180;
		//lowWindOddsTo1 = 20*200;
		//lowWindTimer = 0;
		//windSpeedGlobalChangeRate = 0.05F;
		
		if (!ConfigMisc.Misc_windOn) {
			windSpeedGlobal = 0;
			windSpeedGust = 0;
			windTimeGust = 0;
			//windSpeedSmooth = 0;
		} else {
			
			if (!manager.getWorld().isRemote) {
				//WIND SPEED\\
				
				//global random wind speed change
				
				if (!ConfigMisc.Wind_NoWindEvents) {
					lowWindTimer = 0;
				}
				
				if (lowWindTimer <= 0) {
					if (windSpeedGlobalRandChangeTimer-- <= 0)
		            {
						//standard wind adjustment
						if (highWindTimer <= 0) {
							windSpeedGlobal += (rand.nextDouble() * windSpeedGlobalChangeRate) - (windSpeedGlobalChangeRate / 2);
						//only increase for high wind
						} else {
							windSpeedGlobal += (rand.nextDouble() * windSpeedGlobalChangeRate)/* - (windSpeedGlobalChangeRate / 2)*/;
						}
						windSpeedGlobalRandChangeTimer = windSpeedGlobalRandChangeDelay;
		            }
					
					//only allow for low wind if high wind not active
					if (highWindTimer <= 0) {
						if (ConfigMisc.Wind_NoWindEvents) {
							if (rand.nextInt(lowWindOddsTo1) == 0) {
								startLowWindEvent();
								Weather.dbg("low wind event, for ticks: " + lowWindTimer);
							}
						}
					}
					
					if (ConfigMisc.Wind_HighWindEvents) {
						if (rand.nextInt(highWindOddsTo1) == 0) {
							startHighWindEvent();
							Weather.dbg("high wind event, for ticks: " + highWindTimer);
						}
					}
				} else {
					lowWindTimer--;
					windSpeedGlobal -= 0.01F;
				}
				
				
				
				//enforce mins and maxs of wind speed
				if (windSpeedGlobal < windSpeedMin)
	            {
					windSpeedGlobal = windSpeedMin;
	            }
	
	            if (windSpeedGlobal > windSpeedMax)
	            {
	            	windSpeedGlobal = windSpeedMax;
	            }
	            
	            if (windTimeGust > 0) {
	            	windTimeGust--;
	            	
	            	if (windTimeGust == 0) {
	            		syncData();
	            	}
	            }
				
	            //smooth use
				/*if (windSpeed > windSpeedSmooth)
	            {
					windSpeedSmooth += 0.01F;
	            }
	            else if (windSpeed < windSpeedSmooth)
	            {
	            	windSpeedSmooth -= 0.01F;
	            }
	
	            if (windSpeedSmooth < 0)
	            {
	            	windSpeedSmooth = 0F;
	            }*/
	            
	            //WIND SPEED //
	            
	            //WIND ANGLE\\
	            
	            //windGustEventTimeRand = 100;
	            
	            float randGustWindFactor = 1F;
				
	            //gust data
	            if (this.windTimeGust == 0 && lowWindTimer <= 0 && highWindTimer <= 0)
	            {
	                if (chanceOfWindGustEvent > 0F)
	                {
	                    if (rand.nextInt((int)((100 - chanceOfWindGustEvent) * randGustWindFactor)) == 0)
	                    {
	                    	windSpeedGust = windSpeedGlobal + rand.nextFloat() * 0.6F;
	                        windAngleGust = rand.nextInt(360) - 180;
	                        setWindTimeGust(rand.nextInt(windGustEventTimeRand));
	                        //windEventTime += windTime;
	                        //unneeded since priority system determines wind to use
	                        //directionBeforeGust = windAngleGlobal;
	                    }
	                }
	            }
	            
				//global wind angle
	            windAngleGlobal += ((new Random()).nextInt(5) - 2) * 0.2F;
				
	            if (windAngleGlobal < -180)
	            {
	            	windAngleGlobal += 360;
	            }
	
	            if (windAngleGlobal > 180)
	            {
	            	windAngleGlobal -= 360;
	            }
	            
	            //WIND ANGLE //
			} else {
				
	            tickClient();
			}
		}
		
	}
	
	public void startHighWindEvent() {
		highWindTimer = highWindTimerEnableAmountBase + (new Random()).nextInt(highWindTimerEnableAmountRnd);
	}
	
	public void stopHighWindEvent() {
		highWindTimer = 0;
	}
	
	public void startLowWindEvent() {
		lowWindTimer = lowWindTimerEnableAmountBase + (new Random()).nextInt(lowWindTimerEnableAmountRnd);
	}
	
	public void stopLowWindEvent() {
		lowWindTimer = 0;
	}

	@SideOnly(Side.CLIENT)
	public void tickClient() {
		EntityPlayer entP = FMLClientHandler.instance().getClient().thePlayer;

        if (windTimeEvent > 0) {
        	windTimeEvent--;
        }
		
		//event data
		if (entP != null) {
	        if (manager.getWorld().getTotalWorldTime() % 10 == 0) {
	        	StormObject so = manager.getClosestStorm(new Vec3(entP.posX, StormObject.layers.get(0), entP.posZ), 256, StormObject.STATE_HIGHWIND);
	        	
	        	//FIX SO IT DOESNT COUNT RAINSTORMS! - i did?
	        	if (so != null) {
	        		
	        		setWindTimeEvent(80);
	        		
	        		double stormDist = entP.getDistance(so.posGround.xCoord, so.posGround.yCoord, so.posGround.zCoord);
	        		
	        		//player pos aiming at storm
	        		double var11 = so.posGround.xCoord - entP.posX;
		            double var15 = so.posGround.zCoord - entP.posZ;
		            float yaw = -((float)Math.atan2(var11, var15)) * 180.0F / (float)Math.PI;
		            
		            windAngleEvent = yaw;
		            windSpeedEvent = 2F; //make dynamic?
		            
		            //Weather.dbg("!!!!!!!!!!!!!!!!!!!storm event near: " + stormDist);
	        	}
	        }
		}
	}
	
	public NBTTagCompound nbtSyncForClient() {
		NBTTagCompound data = new NBTTagCompound();
		
		//idea: only sync the wind data client cares about (the active priority wind)
		
		data.setFloat("windSpeedGlobal", windSpeedGlobal);
		data.setFloat("windAngleGlobal", windAngleGlobal);
		data.setFloat("windSpeedGust", windSpeedGust);
		data.setFloat("windAngleGust", windAngleGust);
		
		/*data.setFloat("windSpeedEvent", windSpeedEvent);
		data.setFloat("windAngleEvent", windAngleEvent);
		data.setInteger("windTimeEvent", windTimeEvent);*/
		
		data.setInteger("windTimeGust", windTimeGust);
		
		return data;
	}
	
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		
		windSpeedGlobal = parNBT.getFloat("windSpeedGlobal");
		windAngleGlobal = parNBT.getFloat("windAngleGlobal");
		windSpeedGust = parNBT.getFloat("windSpeedGust");
		windAngleGust = parNBT.getFloat("windAngleGust");
		
		/*windSpeedEvent = parNBT.getFloat("windSpeedEvent");
		windAngleEvent = parNBT.getFloat("windAngleEvent");
		windTimeEvent = parNBT.getInteger("windTimeEvent");*/
		
		windTimeGust = parNBT.getInteger("windTimeGust");
	}
	
	public void syncData() {
		if (manager instanceof WeatherManagerServer) {
			((WeatherManagerServer) manager).syncWindUpdate(this);
		}
	}
	
	public void reset() {
		manager = null;
	}
	

	
	public void applyWindForceNew(Object ent) {
		applyWindForceNew(ent, 1F/20F, 0.5F);
	}
	
	/**
	 * 
	 * To solve the problem of speed going overkill due to bad formulas
	 * 
	 * end goal: make object move at speed of wind
	 * - object has a weight that slows that adjustment
	 * - conservation of momentum
	 * 
	 * calculate force based on wind speed vs objects speed
	 * - use that force to apply to weight of object
	 * - profit
	 * 
	 * 
	 * @param ent
	 */
	public void applyWindForceNew(Object ent, float multiplier, float maxSpeed) {
		
		Vec3 motion = applyWindForceImpl(new Vec3(CoroUtilEntOrParticle.getMotionX(ent), CoroUtilEntOrParticle.getMotionY(ent), CoroUtilEntOrParticle.getMotionZ(ent)), 
				WeatherUtilEntity.getWeight(ent), multiplier, maxSpeed);
		
		CoroUtilEntOrParticle.setMotionX(ent, motion.xCoord);
    	CoroUtilEntOrParticle.setMotionZ(ent, motion.zCoord);
	}
	
	public Vec3 applyWindForceImpl(Vec3 motion, float weight) {
		return applyWindForceImpl(motion, weight, 1F/20F, 0.5F);
	}
	
	/**
	 * Handle generic uses of wind force, for stuff like weather objects that arent entities or paticles
	 * 
	 * @param motion
	 * @param weight
	 * @param multiplier
	 * @param maxSpeed
	 * @return
	 */
	public Vec3 applyWindForceImpl(Vec3 motion, float weight, float multiplier, float maxSpeed) {
		boolean debugParticle = false;
		/*if (ent instanceof EntityRotFX) {
			EntityRotFX part = (EntityRotFX) ent;
			if (part.debugID == 1) {
				debugParticle = true;
			}
		}*/
		
		WindManager windMan = this;//ClientTickHandler.weatherManager.windMan;
		
		float windSpeed = windMan.getWindSpeedForPriority();
    	float windAngle = windMan.getWindAngleForPriority();
    	
    	//Random rand = new Random();
    	
    	//temp
    	//windSpeed = 1F;
    	//windAngle = -90;//rand.nextInt(360);
    	
    	float windX = (float) -Math.sin(Math.toRadians(windAngle)) * windSpeed;
    	float windZ = (float) Math.cos(Math.toRadians(windAngle)) * windSpeed;
    	
    	float objX = (float) motion.xCoord;//CoroUtilEntOrParticle.getMotionX(ent);
    	float objZ = (float) motion.zCoord;//CoroUtilEntOrParticle.getMotionZ(ent);
		
    	float windWeight = 1F;
    	float objWeight = weight;
    	
    	//divide by zero protection
    	if (objWeight <= 0) {
    		objWeight = 0.001F;
    	}
    	
    	//TEMP
    	//objWeight = 1F;
    	
    	float weightDiff = windWeight / objWeight;
    	
    	float vecX = (objX - windX) * weightDiff;
    	float vecZ = (objZ - windZ) * weightDiff;
    	
    	vecX *= multiplier;
    	vecZ *= multiplier;
    	
    	if (debugParticle) {
    		System.out.println(windX + " vs " + objX);
    		System.out.println("diff: " + String.format("%.5g%n", vecX));
    	}
    	
    	//copy over existing motion data
    	Vec3 newMotion = new Vec3(motion);
    	
    	double speedCheck = (Math.abs(vecX) + Math.abs(vecZ)) / 2D;
        if (speedCheck < maxSpeed) {
        	newMotion.xCoord = objX - vecX;
        	newMotion.zCoord = objZ - vecZ;
	    	/*CoroUtilEntOrParticle.setMotionX(ent, objX - vecX);
	    	CoroUtilEntOrParticle.setMotionZ(ent, objZ - vecZ);*/
        }
        
        return newMotion;
	}
}
