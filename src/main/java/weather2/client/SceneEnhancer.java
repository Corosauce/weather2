package weather2.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.SoundRegistry;
import weather2.api.WindReader;
import weather2.client.entity.particle.EntityWaterfallFX;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilParticle;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.api.weather.IWindHandler;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPhysics;
import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFogGround;
import extendedrenderer.particle.behavior.ParticleBehaviorMiniTornado;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.particle.entity.ParticleTexLeafColor;

@SideOnly(Side.CLIENT)
public class SceneEnhancer implements Runnable {

	//potential ideas:
    //TROPICRAFT FLOWER SPAWN POLLEN!
	
	//this is for the thread we make
	public World lastWorldDetected = null;

	//used for acting on fire/smoke
	public static ParticleBehaviors pm;
	
	public static List<Particle> spawnQueueNormal = new ArrayList();
    public static List<Particle> spawnQueue = new ArrayList();
    
    public static long threadLastWorldTickTime;
    public static int lastTickFoundBlocks;
    public static long lastTickAmbient;
    public static long lastTickAmbientThreaded;
    
    //consider caching somehow without desyncing or overflowing
    //WE USE 0 TO MARK WATER, 1 TO MARK LEAVES
    public static ArrayList<ChunkCoordinatesBlock> soundLocations = new ArrayList();
    public static HashMap<ChunkCoordinatesBlock, Long> soundTimeLocations = new HashMap();
    
    public static Block SOUNDMARKER_WATER = Blocks.WATER;
    public static Block SOUNDMARKER_LEAVES = Blocks.LEAVES;
    
    public static float curPrecipStr = 0F;
    public static float curPrecipStrTarget = 0F;
    
    public static float curOvercastStr = 0F;
    public static float curOvercastStrTarget = 0F;
    
    //testing
    public static ParticleBehaviorMiniTornado miniTornado;
    
    public static ParticleBehaviorFogGround particleBehaviorFog;
    
    public static Vec3d vecWOP = null;
    
    //sandstorm fog state
    public static double distToStormThreshold = 100;
    public static double distToStorm = distToStormThreshold + 50;
    public static float stormFogRed = 0;
    public static float stormFogGreen = 0;
    public static float stormFogBlue = 0;
    public static float stormFogRedOrig = 0;
    public static float stormFogGreenOrig = 0;
    public static float stormFogBlueOrig = 0;
    public static float stormFogDensity = 0;
    public static float stormFogDensityOrig = 0;

    public static float stormFogStart = 0;
    public static float stormFogEnd = 0;
    public static float stormFogStartOrig = 0;
    public static float stormFogEndOrig = 0;
    
    public static float stormFogStartClouds = 0;
    public static float stormFogEndClouds = 0;
    public static float stormFogStartCloudsOrig = 0;
    public static float stormFogEndCloudsOrig = 0;
    
    public static boolean needFogState = true;
    
    public static float scaleIntensityTarget = 0F;
    public static float scaleIntensitySmooth = 0F;
    
    public static float adjustAmountTarget = 0F;
    public static float adjustAmountSmooth = 0F;
    
    public static boolean isPlayerOutside = true;
	
	public SceneEnhancer() {
		pm = new ParticleBehaviors(null);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				tickClientThreaded();
				Thread.sleep(ConfigMisc.Thread_Particle_Process_Delay);
			} catch (Throwable throwable) {
                throwable.printStackTrace();
            }
		}
	}

	//run from client side _mc_ thread
	public void tickClient() {
		if (!WeatherUtil.isPaused()) {
			tryParticleSpawning();
			tickParticlePrecipitation();
			trySoundPlaying();
			
			Minecraft mc = FMLClientHandler.instance().getClient();
			tryWind(mc.theWorld);
			
			//tickTest();
			//tickTestFog();
			tickSandstorm();
			//tickTestSandstormParticles();


		}
	}
	
	//run from our newly created thread
	public void tickClientThreaded() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		if (mc.theWorld != null && lastWorldDetected != mc.theWorld) {
			lastWorldDetected = mc.theWorld;
			reset();
		}
		
		if (mc.theWorld != null && mc.thePlayer != null && WeatherUtilConfig.listDimensionsWindEffects.contains(mc.theWorld.provider.getDimension())) {
			profileSurroundings();
			tryAmbientSounds();
		}
	}
	
	public synchronized void trySoundPlaying()
    {
		try {
			if (lastTickAmbient < System.currentTimeMillis()) {
	    		lastTickAmbient = System.currentTimeMillis() + 500;
	    		
	    		Minecraft mc = FMLClientHandler.instance().getClient();
	        	
	        	World worldRef = mc.theWorld;
	        	EntityPlayer player = mc.thePlayer;
	        	
	        	int size = 32;
	            int hsize = size / 2;
	            int curX = (int)player.posX;
	            int curY = (int)player.posY;
	            int curZ = (int)player.posZ;
	            
	            Random rand = new Random();
	            
	            //trim out distant sound locations, also update last time played
	            for (int i = 0; i < soundLocations.size(); i++) {
	            	
	            	ChunkCoordinatesBlock cCor = soundLocations.get(i);
	            	
	            	if (Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)) > size) {
	            		soundLocations.remove(i--);
	            		soundTimeLocations.remove(cCor);
	            		//System.out.println("trim out soundlocation");
	            	} else {
	
	                    Block block = getBlock(worldRef, cCor.posX, cCor.posY, cCor.posZ);//Block.blocksList[id];
	                    
	                    if (block == null || (block.getMaterial(block.getDefaultState()) != Material.WATER && block.getMaterial(block.getDefaultState()) != Material.LEAVES)) {
	                    	soundLocations.remove(i);
	                		soundTimeLocations.remove(cCor);
	                    } else {
	                    	
		            		long lastPlayTime = 0;
		            		
		            		
		            		
		            		if (soundTimeLocations.containsKey(cCor)) {
		            			lastPlayTime = soundTimeLocations.get(cCor);
		            		}
		            		
		            		//System.out.println(Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)));
							if (lastPlayTime < System.currentTimeMillis()) {
								if (cCor.block == SOUNDMARKER_WATER) {
									soundTimeLocations.put(cCor, System.currentTimeMillis() + 2500 + rand.nextInt(50));
									//mc.getSoundHandler().playSound(Weather.modID + ":waterfall", cCor.posX, cCor.posY, cCor.posZ, (float)ConfigMisc.volWaterfallScale, 0.75F + (rand.nextFloat() * 0.05F));
									//mc.theWorld.playSound(cCor.posX, cCor.posY, cCor.posZ, Weather.modID + ":env.waterfall", (float)ConfigMisc.volWaterfallScale, 0.75F + (rand.nextFloat() * 0.05F), false);
									mc.theWorld.playSound(cCor.toBlockPos(), SoundRegistry.get("env.waterfall"), SoundCategory.AMBIENT, (float)ConfigMisc.volWaterfallScale, 0.75F + (rand.nextFloat() * 0.05F), false);
									//System.out.println("play waterfall at: " + cCor.posX + " - " + cCor.posY + " - " + cCor.posZ);
								} else if (cCor.block == SOUNDMARKER_LEAVES) {
									
										
									float windSpeed = WindReader.getWindSpeed(mc.theWorld, new Vec3(cCor.posX, cCor.posY, cCor.posZ), WindReader.WindType.EVENT);
									if (windSpeed > 0.2F) {
										soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
										//mc.getSoundHandler().playSound(Weather.modID + ":wind_calmfade", cCor.posX, cCor.posY, cCor.posZ, (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
										//mc.theWorld.playSound(cCor.posX, cCor.posY, cCor.posZ, Weather.modID + ":env.wind_calmfade", (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
										mc.theWorld.playSound(cCor.toBlockPos(), SoundRegistry.get("env.wind_calmfade"), SoundCategory.AMBIENT, (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
										//System.out.println("play leaves sound at: " + cCor.posX + " - " + cCor.posY + " - " + cCor.posZ + " - windSpeed: " + windSpeed);
									} else {
										windSpeed = WindReader.getWindSpeed(mc.theWorld, new Vec3(cCor.posX, cCor.posY, cCor.posZ));
										//if (windSpeed > 0.3F) {
										if (mc.theWorld.rand.nextInt(15) == 0) {
											soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
											//mc.getSoundHandler().playSound(Weather.modID + ":wind_calmfade", cCor.posX, cCor.posY, cCor.posZ, (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
											//mc.theWorld.playSound(cCor.posX, cCor.posY, cCor.posZ, Weather.modID + ":env.wind_calmfade", (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
											mc.theWorld.playSound(cCor.toBlockPos(), SoundRegistry.get("env.wind_calmfade"), SoundCategory.AMBIENT, (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
										}
											//System.out.println("play leaves sound at: " + cCor.posX + " - " + cCor.posY + " - " + cCor.posZ + " - windSpeed: " + windSpeed);
										//}
									}
										
									
								}
								
							} else {
								//System.out.println("still waiting, diff: " + (lastPlayTime - System.currentTimeMillis()));
							}
	                    }
	            	}
	            }
			}
		} catch (Exception ex) {
    		System.out.println("Weather2: Error handling sound play queue: ");
    		ex.printStackTrace();
    	}
    }
	
	//Threaded function
    @SideOnly(Side.CLIENT)
    public static void tryAmbientSounds()
    {
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
    	World worldRef = mc.theWorld;
    	EntityPlayer player = mc.thePlayer;
    	
    	Random rand = new Random();
    	
    	if (lastTickAmbientThreaded < System.currentTimeMillis()) {
    		lastTickAmbientThreaded = System.currentTimeMillis() + 500;
    		
    		int size = 32;
            int hsize = size / 2;
            int curX = (int)player.posX;
            int curY = (int)player.posY;
            int curZ = (int)player.posZ;
            
            //soundLocations.clear();
            
            
    		
    		for (int xx = curX - hsize; xx < curX + hsize; xx++)
            {
                for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
                {
                    for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
                    {
                        Block block = getBlock(worldRef, xx, yy, zz);
                        
                        if (block != null) {
                        	
                        	//Waterfall
                        	if (ConfigMisc.Wind_Particle_waterfall && ((block.getMaterial(block.getDefaultState()) == Material.WATER))) {
                            	
                            	int meta = getBlockMetadata(worldRef, xx, yy, zz);
                            	if ((meta & 8) != 0) {
                            		
                            		int bottomY = yy;
                            		int index = 0;
                            		
                            		//this scans to bottom till not water, kinda overkill? owell lets keep it, and also add rule if index > 4 (waterfall height of 4)
                            		while (yy-index > 0) {
                            			Block id2 = getBlock(worldRef, xx, yy-index, zz);
                            			if (id2 != null && !(id2.getMaterial(id2.getDefaultState()) == Material.WATER)) {
                            				break;
                            			}
                            			index++;
                            		}
                            		
                            		bottomY = yy-index+1;
                            		
                            		//check if +10 from here is water with right meta too
                            		int meta2 = getBlockMetadata(worldRef, xx, bottomY+10, zz);
                            		Block block2 = getBlock(worldRef, xx, bottomY+10, zz);;
                            		
                        			if (index >= 4 && (block2 != null && block2.getMaterial(block2.getDefaultState()) == Material.WATER && (meta2 & 8) != 0)) {
                        				boolean proxFail = false;
                        				for (int j = 0; j < soundLocations.size(); j++) {
                                			if (Math.sqrt(soundLocations.get(j).getDistanceSquared(xx, bottomY, zz)) < 5) {
                                				proxFail = true;
                                				break;
                                			}
                                		}
                        				
                        				if (!proxFail) {
                        					soundLocations.add(new ChunkCoordinatesBlock(xx, bottomY, zz, SOUNDMARKER_WATER, 0));
                        					//System.out.println("add waterfall");
                        				}
                        			}
                            	}
                            } else if (ConfigMisc.volWindTreesScale > 0 && ((block.getMaterial(block.getDefaultState()) == Material.LEAVES))) {
                            	boolean proxFail = false;
                				for (int j = 0; j < soundLocations.size(); j++) {
                        			if (Math.sqrt(soundLocations.get(j).getDistanceSquared(xx, yy, zz)) < 15) {
                        				proxFail = true;
                        				break;
                        			}
                        		}
                				
                				if (!proxFail) {
                					soundLocations.add(new ChunkCoordinatesBlock(xx, yy, zz, SOUNDMARKER_LEAVES, 0));
                					//System.out.println("add leaves sound location");
                				}
                            }
                        }
                    }
                }
            }
    	}
    }
	
	//called from our new thread, decided not to let client mc thread reset it, should be ok?
	public void reset() {
		//reset particle data, discard dead ones as that was a bug from weather1
		
		if (ExtendedRenderer.rotEffRenderer != null) {
        	for (int i = 0; i < ExtendedRenderer.rotEffRenderer.fxLayers.length; i++)                            
            {
        		for (int j = 0; j < ExtendedRenderer.rotEffRenderer.fxLayers[i].length; j++) {
        			if (ExtendedRenderer.rotEffRenderer.fxLayers[i][j] != null)
                    {
                        ExtendedRenderer.rotEffRenderer.fxLayers[i][j].clear();
                    }
        		}
                
            }
        }
		
		lastWorldDetected.weatherEffects.clear();
		
		if (WeatherUtilParticle.fxLayers == null) {
			WeatherUtilParticle.getFXLayers();
		}
		//WeatherUtilSound.getSoundSystem();
	}
	
	public void tickParticlePrecipitation() {
		
		if (ConfigMisc.Particle_RainSnow) {
			EntityPlayer entP = FMLClientHandler.instance().getClient().thePlayer;
			
			if (entP.posY >= StormObject.static_YPos_layer0) return;
			
			float curPrecipVal = getRainStrengthAndControlVisuals(entP);
			
			float maxPrecip = 0.5F;
			
			/*if (entP.worldObj.getTotalWorldTime() % 20 == 0) {
				Weather.dbg("curRainStr: " + curRainStr);
			}*/
			
			//Weather.dbg("curPrecipVal: " + curPrecipVal * 100F);
			
			
			int precipitationHeight = entP.worldObj.getPrecipitationHeight(new BlockPos(MathHelper.floor_double(entP.posX), 0, MathHelper.floor_double(entP.posZ))).getY();
			
			Biome biomegenbase = entP.worldObj.getBiomeGenForCoords(new BlockPos(MathHelper.floor_double(entP.posX), 0, MathHelper.floor_double(entP.posZ)));

            if (/*true*/biomegenbase != null/* || biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()*/)
            {
			
				float temperature = biomegenbase.getFloatTemperature(new BlockPos(MathHelper.floor_double(entP.posX), MathHelper.floor_double(entP.posY), MathHelper.floor_double(entP.posZ)));
	            double d3;
	            float f10;
	
	            if (/*curPrecipVal > 0*/entP.worldObj.getBiomeProvider().getTemperatureAtHeight(temperature, precipitationHeight) >= 0.15F) {
	            	
	            	//now absolute it for ez math
	            	curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
	            	
	            	//Weather.dbg("precip: " + curPrecipVal);
	            	
	            	//rain
					if (curPrecipVal > 0 && entP.worldObj.isRainingAt(new BlockPos(MathHelper.floor_double(entP.posX), MathHelper.floor_double(entP.getEntityBoundingBox().minY), MathHelper.floor_double(entP.posZ)))) {
						
						//Weather.dbg("rate: " + curPrecipVal * 20F * ConfigMisc.Particle_Precipitation_effect_rate);
						
						for (int i = 0; i < curPrecipVal * 20F * ConfigMisc.Particle_Precipitation_effect_rate; i++) {
							int spawnAreaSize = 15;
							/*EntityFallingRainFX ent = new EntityFallingRainFX(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + 15, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, 1.5D, 3);
							
							ent.severityOfRainRate = (int)(curPrecipVal * 5F);
					        //ent.renderDistanceWeight = 1.0D;
					        ent.setSize(1.2F, 1.2F);
					        ent.rotationYaw = ent.getWorld().rand.nextInt(360) - 180F;
					        ent.setGravity(0.00001F);
					        ent.spawnAsWeatherEffect();*/
					        
					        ParticleTexExtraRender rain = new ParticleTexExtraRender(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + 15, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, ParticleRegistry.rain);
					        rain.setCanCollide(true);
					        rain.setKillOnCollide(true);
					        //1.10.2: had to adjust weight from 1 to 10 to make it not super pulled for some reason
					        rain.windWeight = 1F;
					        rain.setFacePlayer(false);
					        rain.rotationYaw = rain.getWorld().rand.nextInt(360) - 180F;
					        rain.spawnAsWeatherEffect();
					        ClientTickHandler.weatherManager.addWeatheredParticle(rain);
						}
					}
					
	            } else {
	            	
	            	//now absolute it for ez math
	            	curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
	            	
	            	//snow
	            	if (curPrecipVal > 0) {
	            		
	            		//Weather.dbg("rate: " + curPrecipVal * 5F * ConfigMisc.Particle_Precipitation_effect_rate);
	            		
						for (int i = 0; i < curPrecipVal * 5F * ConfigMisc.Particle_Precipitation_effect_rate; i++) {
							int spawnAreaSize = 50;
							int spawnAbove = 10;
							/*EntityFallingSnowFX ent = new EntityFallingSnowFX(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + spawnAbove, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, 5.5D, 6);
							ent.severityOfRainRate = (int)(curPrecipVal * 5F);
					        //ent.renderDistanceWeight = 1.0D;
					        ent.setSize(1.2F, 1.2F);
					        ent.rotationYaw = ent.getWorld().rand.nextInt(360) - 180F;
					        ent.setGravity(0.00001F);
					        ent.spawnAsWeatherEffect();*/
					        
					        ParticleTexExtraRender snow = new ParticleTexExtraRender(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + 15, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, ParticleRegistry.snow);
					        snow.setScale(1.3F);
					        snow.setGravity(0.1F);
					        //more buggy weight issues
					        snow.windWeight = 0.1F/* * 400F*/;
					        snow.setMaxAge(200);
					        snow.setFacePlayer(false);
					        snow.setCanCollide(true);
					        snow.setKillOnCollide(true);
					        snow.rotationYaw = snow.getWorld().rand.nextInt(360) - 180F;
					        snow.spawnAsWeatherEffect();
					        ClientTickHandler.weatherManager.addWeatheredParticle(snow);
						}
					}
	            }
            }

		}
	}
	
	public static float getRainStrengthAndControlVisuals(EntityPlayer entP) {
		return getRainStrengthAndControlVisuals(entP, false);
	}
	
	//returns in negatives for snow now! closer to 0 = less of that effect
	public static float getRainStrengthAndControlVisuals(EntityPlayer entP, boolean forOvercast) {
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		double maxStormDist = 512 / 4 * 3;
		Vec3 plPos = new Vec3(entP.posX, StormObject.static_YPos_layer0, entP.posZ);
		StormObject storm = null;
		
		ClientTickHandler.checkClientWeather();
		
		storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_FORMING, true);
		
		if (forOvercast) {
			//storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_THUNDER, true);
		} else {
			//storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_FORMING, true);
			
			/*if (storm != null) {
				System.out.println("storm found? " + storm);
				System.out.println("storm water: " + storm.levelWater);
			}*/
		}
		
	    
	    
	    boolean closeEnough = false;
	    double stormDist = 9999;
	    float tempAdj = 1F;

    	float sizeToUse = 0;
	    
	    float overcastModeMinPrecip = 0.2F;
	    
	    //evaluate if storms size is big enough to over over player
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
		    
		    tempAdj = storm.levelTemperature > 0 ? 1F : -1F;
		    
		    //limit plain rain clouds to light intensity
		    if (storm.levelCurIntensityStage == StormObject.STATE_NORMAL) {
		    	if (stormIntensity > 0.3) stormIntensity = 0.3;
		    }
		    
		    if (ConfigMisc.Storm_NoRainVisual) {
		    	stormIntensity = 0;
		    }
		    
		    //System.out.println("intensity: " + stormIntensity);
	    	mc.theWorld.getWorldInfo().setRaining(true);
	    	mc.theWorld.getWorldInfo().setThundering(true);
	    	if (forOvercast) {
	    		curOvercastStrTarget = (float) stormIntensity;
	    	} else {
	    		curPrecipStrTarget = (float) stormIntensity;
	    	}
	    	//mc.theWorld.thunderingStrength = (float) stormIntensity;
	    } else {
	    	if (!ConfigMisc.overcastMode) {
		    	mc.theWorld.getWorldInfo().setRaining(false);
		    	mc.theWorld.getWorldInfo().setThundering(false);
		    	
		    	if (forOvercast) {
		    		curOvercastStrTarget = 0;
		    	} else {
		    		curPrecipStrTarget = 0;
		    	}
	    	} else {
	    		if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
	    			mc.theWorld.getWorldInfo().setRaining(true);
			    	mc.theWorld.getWorldInfo().setThundering(true);
			    	
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
	    	
	    	//mc.theWorld.setRainStrength(0);
	    	//mc.theWorld.thunderingStrength = 0;
	    }
	    
	    if (forOvercast) {
	    	
	    	/*if (ConfigMisc.overcastMode) {
	    		if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
	    			if (curOvercastStrTarget < overcastModeMinPrecip) {
	    				curOvercastStrTarget = overcastModeMinPrecip;
	    			}
	    		}
	    	}*/
	    	
	    	//mc.theWorld.setRainStrength(curOvercastStr);
	    	
	    	if (curOvercastStr > curOvercastStrTarget) {
	    		curOvercastStr -= 0.001F;
		    } else if (curOvercastStr < curOvercastStrTarget) {
		    	curOvercastStr += 0.001F;
		    }
	    	
	    	if (curOvercastStr < 0.0001 && curOvercastStr > -0.0001F) {
	    		curOvercastStr = 0;
	    	}
	    	
	    	return curOvercastStr * tempAdj;
	    } else {
	    	
	    	/*if (ConfigMisc.overcastMode) {
	    		if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
	    			if (curPrecipStrTarget < overcastModeMinPrecip) {
	    				curPrecipStrTarget = overcastModeMinPrecip;
	    			}
	    		}
	    	}*/
	    	
	    	//mc.theWorld.setRainStrength(curPrecipStr);
	    	
	    	if (curPrecipStr > curPrecipStrTarget) {
		    	curPrecipStr -= 0.001F;
		    } else if (curPrecipStr < curPrecipStrTarget) {
		    	curPrecipStr += 0.001F;
		    }
	    	
	    	if (curPrecipStr < 0.0001 && curPrecipStr > -0.0001F) {
	    		curPrecipStr = 0;
	    	}
	    	
	    	//Weather.dbg("curPrecipStr: " + curPrecipStr);
	    	
	    	return curPrecipStr * tempAdj;
	    }
	    
	    
	    
	}
	
	public synchronized void tryParticleSpawning()
    {
    	if (spawnQueue.size() > 0) {
    		//System.out.println("spawnQueue.size(): " + spawnQueue.size());
    	}
    	
    	try {
	        for (int i = 0; i < spawnQueue.size(); i++)
	        {
	            Particle ent = spawnQueue.get(i);
	
	            if (ent != null/* && ent.worldObj != null*/) {
	            
		            if (ent instanceof EntityRotFX)
		            {
		                ((EntityRotFX) ent).spawnAsWeatherEffect();
		            }/*
		            else
		            {
		                ent.worldObj.addWeatherEffect(ent);
		            }*/
		            ClientTickHandler.weatherManager.addWeatheredParticle(ent);
	            }
	        }
	        for (int i = 0; i < spawnQueueNormal.size(); i++)
	        {
	        	Particle ent = spawnQueueNormal.get(i);
	
	            if (ent != null/* && ent.worldObj != null*/) {
	            
	            	Minecraft.getMinecraft().effectRenderer.addEffect(ent);
	            }
	        }
    	} catch (Exception ex) {
    		System.out.println("Weather2: Error handling particle spawn queue: ");
    		ex.printStackTrace();
    	}

        spawnQueue.clear();
        spawnQueueNormal.clear();
    }
	
	public void profileSurroundings()
    {
        //tryClouds();
        
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	World worldRef = lastWorldDetected;
    	EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        WeatherManagerClient manager = ClientTickHandler.weatherManager;
    	
        if (worldRef == null || player == null || manager == null || manager.windMan == null)
        {
        	try {
        		Thread.sleep(1000L);
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
            return;
        }

        if (threadLastWorldTickTime == worldRef.getWorldTime())
        {
            return;
        }

        threadLastWorldTickTime = worldRef.getWorldTime();
        
        Random rand = new Random();
        
        //mining a tree causes leaves to fall
        int size = 40;
        int hsize = size / 2;
        int curX = (int)player.posX;
        int curY = (int)player.posY;
        int curZ = (int)player.posZ;
        //if (true) return;
        
        float windStr = manager.windMan.getWindSpeedForPriority();//(weatherMan.wind.strength <= 1F ? weatherMan.wind.strength : 1F);

        /*if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null) {
        	Block id = mc.theWorld.getBlockState(new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ())).getBlock();
        	//System.out.println(mc.theWorld.getBlockStateId(mc.objectMouseOver.blockX,mc.objectMouseOver.blockY,mc.objectMouseOver.blockZ));
        	if (CoroUtilBlock.isAir(id) && id.getMaterial() == Material.wood) {
        		float var5 = 0;

        		var5 = (Float)OldUtil.getPrivateValueSRGMCP(PlayerControllerMP.class, (PlayerControllerMP)mc.playerController, OldUtil.refl_curBlockDamageMP_obf, OldUtil.refl_curBlockDamageMP_mcp);

                if (var5 > 0) {
                	//weather2 disabled for now
                	//shakeTrees(8);
                }
        	}
        }*/

        if ((!ConfigMisc.Wind_Particle_leafs && !ConfigMisc.Wind_Particle_air && !ConfigMisc.Wind_Particle_sand && !ConfigMisc.Wind_Particle_waterfall)/* || weatherMan.wind.strength < 0.10*/)
        {
            return;
        }

        //Wind requiring code goes below
        int spawnRate = (int)(30 / (windStr + 0.001));
        
        

        float lastBlockCount = lastTickFoundBlocks;
        
        float particleCreationRate = (float) ConfigMisc.Wind_Particle_effect_rate;
        
        //TEST OVERRIDE
        //uh = (lastBlockCount / 30) + 1;
        float maxScaleSample = 15000;
        if (lastBlockCount > maxScaleSample) lastBlockCount = maxScaleSample-1;
        float scaleRate = (maxScaleSample - lastBlockCount) / maxScaleSample;
        
        spawnRate = (int) ((spawnRate / (scaleRate + 0.001F)) / (particleCreationRate + 0.001F));
        
        int BlockCountRate = (int)(((300 / scaleRate + 0.001F)) / (particleCreationRate + 0.001F)); 
        
        spawnRate *= (mc.gameSettings.particleSetting+1);
        BlockCountRate *= (mc.gameSettings.particleSetting+1);
        
        //since reducing threaded ticking to 200ms sleep, 1/4 rate, must decrease rand size
        spawnRate /= 2;
        
        //performance fix
        if (spawnRate < 40)
        {
            spawnRate = 40;
        }
        
        //performance fix
        if (BlockCountRate < 80) BlockCountRate = 80;
        //patch for block counts over 15000
        if (BlockCountRate > 5000) BlockCountRate = 5000;
        
        //TEMP!!!
        //uh = 10;
        
        //System.out.println("lastTickFoundBlocks: " + lastTickFoundBlocks + " - rand size: " + uh + " - " + BlockCountRate);
        
        lastTickFoundBlocks = 0;
        
        //Wind_Particle_waterfall = true;
        //Wind_Particle_leafs = true;
        //debug = true;
        //if (true) return;
        
        //if (debug) System.out.println("windStr: " + windStr + " chance: " + uh);
        //Semi intensive area scanning code
        for (int xx = curX - hsize; xx < curX + hsize; xx++)
        {
            for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
            {
                for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
                {
                        //for (int i = 0; i < p_blocks_leaf.size(); i++)
                        //{
                            Block block = getBlock(worldRef, xx, yy, zz);
                            
                            //if (block != null && block.getMaterial() == Material.leaves)
                            
                            if (/*id == ((Block)p_blocks_leaf.get(i)).blockID*/block != null && (block.getMaterial(block.getDefaultState()) == Material.LEAVES || block.getMaterial(block.getDefaultState()) == Material.VINE))
                            {
                            	
                            	lastTickFoundBlocks++;
                            	
                            	if (/*true || *//*worldRef.rand.nextInt(5) == 0 || */worldRef.rand.nextInt(spawnRate) == 0)
                                {
                            		//bottom of tree check || air beside vine check
	                                if (ConfigMisc.Wind_Particle_leafs && (CoroUtilBlock.isAir(getBlock(worldRef, xx, yy - 1, zz)) || CoroUtilBlock.isAir(getBlock(worldRef, xx - 1, yy, zz))))
	                                {
	                                	
	                                    //EntityRotFX var31 = new EntityTexBiomeColorFX(worldRef, (double)xx, (double)yy - 0.5, (double)zz, 0D, 0D, 0D, 10D, 0, WeatherUtilParticle.effLeafID, getBlockMetadata(worldRef, xx, yy, zz), xx, yy, zz);
	                                    EntityRotFX var31 = new ParticleTexLeafColor(worldRef, (double)xx, (double)yy/* - 0.5*/, (double)zz, 0D, 0D, 0D, ParticleRegistry.leaf);
	                                    //ParticleBreakingTemp test = new ParticleBreakingTemp(worldRef, (double)xx, (double)yy - 0.5, (double)zz, ParticleRegistry.leaf);
	                                    var31.setGravity(0.1F);
	                                    var31.setCanCollide(true);
	                                    //System.out.println("add particle");
	                                    //Minecraft.getMinecraft().effectRenderer.addEffect(var31);
	                                    //ExtendedRenderer.rotEffRenderer.addEffect(test);
	                                    //ExtendedRenderer.rotEffRenderer.addEffect(var31);
	                                    //WeatherUtil.setParticleGravity((EntityFX)var31, 0.1F);
	
	                                    //worldRef.spawnParticle(EnumParticleTypes.FALLING_DUST, (double)xx, (double)yy, (double)zz, 0.0D, 0.0D, 0.0D, 0);
	                                    
	                                    /*for (int ii = 0; ii < 10; ii++)
	                                    {
	                                        applyWindForce(var31);
	                                    }*/
	
	                                    var31.rotationYaw = rand.nextInt(360);
	                                    var31.rotationPitch = rand.nextInt(360);
	                                    //var31.spawnAsWeatherEffect();
	                                    
	                                    
	                                    
	                                    spawnQueue.add(var31);
	                                    
	                                }
	                                else
	                                {
	                                    /*if (Wind_Particle_leafs)
	                                    {
	                                        //This is non leaves, as in wildgrass or wahtever is in the p_blocks_leaf list (no special rules)
	                                        EntityRotFX var31 = new EntityTexFX(worldRef, (double)xx, (double)yy + 0.5, (double)zz, 0D, 0D, 0D, 10D, 0, effLeafID);
	                                        c_CoroWeatherUtil.setParticleGravity((EntityFX)var31, 0.1F);
	                                        var31.rotationYaw = rand.nextInt(360);
	                                        //var31.spawnAsWeatherEffect();
	                                        spawnQueue.add(var31);
	                                        //mc.effectRenderer.addEffect(var31);
	                                        
	                                        //System.out.println("leaf spawn!");
	                                    }*/
	                                }
                                }
                            }
                            else if (ConfigMisc.Wind_Particle_waterfall && player.getDistance(xx,  yy, zz) < 16 && (block != null && block.getMaterial(block.getDefaultState()) == Material.WATER)) {
                            	
                            	int meta = getBlockMetadata(worldRef, xx, yy, zz);
                            	if ((meta & 8) != 0) {
                            		lastTickFoundBlocks += 70; //adding more to adjust for the rate 1 waterfall block spits out particles
                            		int chance = (int)(1+(((float)BlockCountRate)/120F));
                            		
                            		Block block2 = getBlock(worldRef, xx, yy-1, zz);
                            		int meta2 = getBlockMetadata(worldRef, xx, yy-1, zz);
                            		Block block3 = getBlock(worldRef, xx, yy+10, zz);
                            		//Block block2 = Block.blocksList[id2];
                            		//Block block3 = Block.blocksList[id3];
                            		
                            		//if ((block2 == null || block2.getMaterial() != Material.water) && (block3 != null && block3.getMaterial() == Material.water)) {
                            			//chance /= 3;
                            			
                            		//}
                            		//System.out.println("woot! " + chance);
                                	if ((((block2 == null || block2.getMaterial(block2.getDefaultState()) != Material.WATER) || (meta2 & 8) == 0) && (block3 != null && block3.getMaterial(block3.getDefaultState()) == Material.WATER)) || worldRef.rand.nextInt(chance) == 0) {
                            		
	                            		float range = 0.5F;
	                            		
	                            		EntityRotFX waterP;
	                            		//if (rand.nextInt(10) == 0) {
	                            			//waterP = new EntityBubbleFX(worldRef, (double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D);
	                            		//} else {
	                            		waterP = new EntityWaterfallFX(worldRef, (double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
	                            		//}
                                	
	                            		
	                            		
                            			if (((block2 == null || block2.getMaterial(block2.getDefaultState()) != Material.WATER) || (meta2 & 8) == 0) && (block3 != null && block3.getMaterial(block3.getDefaultState()) == Material.WATER)) {
                            				
                            				range = 2F;
                            				float speed = 0.2F;
                            				
                            				for (int i = 0; i < 10; i++) {
                            					if (worldRef.rand.nextInt(chance / 2) == 0) {
                            						waterP = new EntityWaterfallFX(worldRef, 
    	                            						(double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
    	                            						(double)yy + 0.7F + ((rand.nextFloat() * range) - (range/2)), 
    	                            						(double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)),
    	                            						((rand.nextFloat() * speed) - (speed/2)),
    	                            						((rand.nextFloat() * speed) - (speed/2)),
    	                            						((rand.nextFloat() * speed) - (speed/2)),
    	                            						2D, 3);
    	                            				//waterP.motionX = -1.5F;
    	                            				waterP.setMotionY(4.5F);
    	                            				//System.out.println("woot! " + chance);
    	                            				spawnQueueNormal.add(waterP);
                            					}
	                            				
                            				}
                            			} else {
                            				waterP = new EntityWaterfallFX(worldRef, 
                            						(double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
                            						(double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
                            						(double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
                            				
                            				waterP.setMotionY(0.5F);
                            				
                            				spawnQueueNormal.add(waterP);
                            			}
	                            			
	                            		
	                            		//waterP.rotationYaw = rand.nextInt(360);
	                                	
                                	}
                            	}
                            	
                            } else if (ConfigMisc.Wind_Particle_fire && (block != null && block == Blocks.FIRE/*block.getMaterial() == Material.fire*/)) {
                            	lastTickFoundBlocks++;
                            	
                            	//
                            	if (worldRef.rand.nextInt(Math.max(1, (spawnRate / 100))) == 0) {
                            		double speed = 0.15D;
                            		//System.out.println("xx:" + xx);
                                	EntityRotFX entityfx = pm.spawnNewParticleIconFX(worldRef, ParticleRegistry.smoke, xx + rand.nextDouble(), yy + 0.2D + rand.nextDouble() * 0.2D, zz + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D, (rand.nextDouble() - rand.nextDouble()) * speed);//pm.spawnNewParticleWindFX(worldRef, ParticleRegistry.smoke, xx + rand.nextDouble(), yy + 0.2D + rand.nextDouble() * 0.2D, zz + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D, (rand.nextDouble() - rand.nextDouble()) * speed);
                                	ParticleBehaviors.setParticleRandoms(entityfx, true, true);
                                	ParticleBehaviors.setParticleFire(entityfx);
                                	entityfx.setMaxAge(100+rand.nextInt(300));
                                	spawnQueueNormal.add(entityfx);
                        			//entityfx.spawnAsWeatherEffect();
                        			//pm.particles.add(entityfx);
                            	}
                            }
                            else if (false && CoroUtilBlock.isAir(block))
                            {
                            	
                            	//null check biome in future if used
                            	float temp = worldRef.getBiomeGenForCoords(new BlockPos(xx, 0, zz)).getFloatTemperature(new BlockPos(xx, yy, zz));
                            	
                            	//System.out.println(temp);
                            	
                            	//Snow!
                            	/*if (false && ConfigMisc.Wind_Particle_snow && player.getDistance(xx, yy, zz) < 20 && (worldRef.rand.nextInt(100) == 0) && yy == ((int)player.posY+8) && temp <= 0.15F) {
	                            	EntityRotFX snow = new EntitySnowFX(worldRef, (double)xx, (double)yy + 0.5, (double)zz, 0D, 0D, 0D, 1F);
	                            	
	                            	snow.particleGravity = 0.0F;
	                            	//WeatherUtil.setParticleGravity((EntityFX)snow, 0.0F);
	                            	snow.noClip = true;
	                            	snow.particleScale = 0.2F;
	                                //WeatherUtil.setParticleScale((EntityFX)snow, 0.2F);
	                                snow.rotationYaw = rand.nextInt(360);
	                                snow.motionY = -0.1F;
	                                //var31.spawnAsWeatherEffect();
	                                spawnQueue.add(snow);
                            	}
                            	
                                if (ConfigMisc.Wind_Particle_air && windStr > 0.05 && worldRef.canBlockSeeTheSky(curX, curY, curZ))
                                {
                                	
                                	
                                	
                                	int chance = 200 - (int)(windStr * 100);
                                	if (chance <= 0) chance = 1;
                                    if ((worldRef.rand.nextInt(uh + 0) == 0) && worldRef.rand.nextInt(chance) == 0)
                                    {
                                        //EntityFX var31 = new EntitySmokeFX(worldRef, (double)xx, (double)yy+0.5, (double)zz, 0D, 0D, 0D);
                                        EntityRotFX var31 = new EntityTexFX(worldRef, (double)xx, (double)yy + 0.5, (double)zz, 0D, 0D, 0D, 10D, 0, effWind2ID);
                                        //var31.particleGravity = 0.3F;
                                        //mod_ExtendedRenderer.rotEffRenderer.addEffect(var31);

                                        for (int ii = 0; ii < 20; ii++)
                                        {
                                            applyWindForce(var31);
                                        }

                                        var31.particleGravity = 0.0F;
                                        //WeatherUtil.setParticleGravity((EntityFX)var31, 0.0F);
                                        var31.noClip = true;
                                        var31.particleScale = 0.2F;
                                        //WeatherUtil.setParticleScale((EntityFX)var31, 0.2F);
                                        var31.rotationYaw = rand.nextInt(360);
                                        //var31.spawnAsWeatherEffect();
                                        spawnQueue.add(var31);
                                    }
                                }*/
                            }
                        //}

                        /*if (Wind_Particle_sand) {
                        	int id = getBlockId(xx, yy, zz);
                        	if (id == ((Block)p_blocks_sand.get(0)).blockID) {
                        		if (id == Block.sand.blockID) {
                        			if (getBlockId(xx, yy+1, zz) == 0) {
                        				c_w_EntityTexFX var31 = new c_w_EntityTexFX(worldRef, (double)xx, (double)yy+0.5, (double)zz, 0D, 0D, 0D, 10D, 0, effSandID);
                        				//var31 = new EntityWindFX(worldRef, (double)xx, (double)yy+1.2, (double)zz, 0D, 0.0D, 0D, 9.5D, 1);
                        				var31.rotationYaw = rand.nextInt(360)-180F;
                        				var31.type = 1;
                        				c_CoroWeatherUtil.setParticleGravity((EntityFX)var31, 0.6F);
                        				c_CoroWeatherUtil.setParticleScale((EntityFX)var31, 0.3F);
                                        //var31.spawnAsWeatherEffect();
                        				spawnQueue.add(var31);
                        			}
                        		}
                        	}
                        }*/
                    
                }
            }
        }
    }
	
	@SideOnly(Side.CLIENT)
    public static void tryWind(World world)
    {
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;

        if (player == null)
        {
            return;
        }

        int dist = 60;
        
        List list = world.loadedEntityList;
        
        WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
        if (weatherMan == null) return;
        WindManager windMan = weatherMan.getWindManager();
        if (windMan == null) return;

        //Chunk Entities
        //we're not moving chunk entities with wind this way ....... i think, only weather events like spinning etc
        /*if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Entity entity1 = (Entity)list.get(i);

                if (canPushEntity(entity1) && !(entity1 instanceof EntityPlayer))
                {
                    applyWindForce(entity1, 1F);
                }
            }
        }*/
        
        Random rand = new Random();
        
        int handleCount = 0;
        
        //Weather Effects
        if (ClientTickHandler.weatherManager.windMan.getWindSpeedForPriority() >= 0.10)
        {
        	
        	for (int i = 0; i < ClientTickHandler.weatherManager.listWeatherEffectedParticles.size(); i++)
            //for (int i = 0; i < world.weatherEffects.size(); i++)
            {
            	
            	handleCount++;
            	
                Particle particle = ClientTickHandler.weatherManager.listWeatherEffectedParticles.get(i);
                
                if (!particle.isAlive()) {
                	ClientTickHandler.weatherManager.listWeatherEffectedParticles.remove(i--);
                	continue;
                }
                
                /*if (!(entity1 instanceof EntityLightningBolt))
                {*/
                	
                	
                	
                    //applyWindForce(entity1);
                    if (particle instanceof EntityRotFX)
                    {
                    	
                    	EntityRotFX entity1 = (EntityRotFX) particle;
                    	
                        if (entity1 == null)
                        {
                            continue;
                        }

                        
                        
                        if ((world.getHeight(new BlockPos(MathHelper.floor_double(entity1.getPosX()), 0, MathHelper.floor_double(entity1.getPosZ()))).getY() - 1 < (int)entity1.getPosY() + 1) || (entity1 instanceof ParticleTexFX))
                        {
                            /*if ((entity1 instanceof ParticleFlame))
                            {
                            	WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + 2);
                            }
                            else */if (entity1 instanceof IWindHandler) {
                            	if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge((Particle)entity1) % 2 == 0)
                                {
                            		WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + ((IWindHandler)entity1).getParticleDecayExtra());
                                }
                            }
                            else if (WeatherUtilParticle.getParticleAge((Particle)entity1) % 2 == 0)
                            {
                            	WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + 1);
                            }

                            //((Particle)entity1).particleAge=1;
                            
                            if ((entity1 instanceof ParticleTexFX) && ((ParticleTexFX)entity1).getParticleTexture() == ParticleRegistry.leaf/*((ParticleTexFX)entity1).getParticleTextureIndex() == WeatherUtilParticle.effLeafID*/)
                            {
                                if (entity1.getMotionX() < 0.01F && entity1.getMotionZ() < 0.01F)
                                {
                                    entity1.setMotionY(entity1.getMotionY() + rand.nextDouble() * 0.02);
                                }

                                //entity1.motionX += rand.nextDouble() * 0.03;
                                //entity1.motionZ += rand.nextDouble() * 0.03;
                                entity1.setMotionY(entity1.getMotionY() - 0.01F);
                                //do it twice!
                                
                            }
                        }

                        //if (canPushEntity(entity1)) {
                        /*if (!(entity1 instanceof EntTornado))
                        {
                            
                        }*/
                        
                        //we apply it twice apparently, k
                        //applyWindForce(entity1, 2D, 0.5D);
                        windMan.applyWindForceNew(entity1, 1F/20F, 0.5F);
                        //applyWindForce(entity1);
                    }
                //}
            }
        }
        
        //System.out.println("particles moved: " + handleCount);

        //WindManager windMan = ClientTickHandler.weatherManager.windMan;
        
        //Particles
        if (WeatherUtilParticle.fxLayers != null && windMan.getWindSpeedForPriority() >= 0.10)
        {
        	//Built in particles
            for (int layer = 0; layer < WeatherUtilParticle.fxLayers.length; layer++)
            {
                for (int i = 0; i < WeatherUtilParticle.fxLayers[layer].length; i++)
                {
                	//for (int j = 0; j < WeatherUtilParticle.fxLayers[layer][i].size(); j++)
                	for (Particle entity1 : WeatherUtilParticle.fxLayers[layer][i])
                    {
                	
	                    //Particle entity1 = (Particle)WeatherUtilParticle.fxLayers[layer][i].get(j);
	                    
	                    if (ConfigMisc.Particle_VanillaAndWeatherOnly) {
	                    	String className = entity1.getClass().getName();
	                    	if (className.contains("net.minecraft.") || className.contains("weather2.")) {
	                    		
	                    	} else {
	                    		continue;
	                    	}
	                    	
	                    	//Weather.dbg("process: " + className);
	                    }
	
	                    if ((world.getHeight(new BlockPos(MathHelper.floor_double(CoroUtilEntOrParticle.getPosX(entity1)), 0, MathHelper.floor_double(CoroUtilEntOrParticle.getPosZ(entity1)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(entity1) + 1) || (entity1 instanceof ParticleTexFX))
	                    {
	                        if ((entity1 instanceof ParticleFlame))
	                        {
	                        	if (windMan.getWindSpeedForPriority() >= 0.50) WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + 2);
	                        }
	                        else if (entity1 instanceof IWindHandler) {
	                        	if (((IWindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge((Particle)entity1) % 2 == 0)
	                            {
	                        		WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + ((IWindHandler)entity1).getParticleDecayExtra());
	                            }
	                        }
	                        else if (WeatherUtilParticle.getParticleAge((Particle)entity1) % 2 == 0)
	                        {
	                        	WeatherUtilParticle.setParticleAge((Particle)entity1, WeatherUtilParticle.getParticleAge((Particle)entity1) + 1);
	                        }
	
	                        //rustle!
	                        if (!(entity1 instanceof EntityWaterfallFX)) {
	                        	//EntityWaterfallFX ent = (EntityWaterfallFX) entity1;
		                        /*if (entity1.onGround)
		                        {
		                            //entity1.onGround = false;
		                            entity1.motionY += rand.nextDouble() * entity1.getMotionX();
		                        }*/
		
		                        if (CoroUtilEntOrParticle.getMotionX(entity1) < 0.01F && CoroUtilEntOrParticle.getMotionZ(entity1) < 0.01F)
		                        {
		                            //ent.setMotionY(ent.getMotionY() + rand.nextDouble() * 0.02);
		                            CoroUtilEntOrParticle.setMotionY(entity1, CoroUtilEntOrParticle.getMotionY(entity1) + rand.nextDouble() * 0.02);
		                        }
	                        }
	
	                        //entity1.motionX += rand.nextDouble() * 0.03;
	                        //entity1.motionZ += rand.nextDouble() * 0.03;
	                        //entity1.motionY += -0.04 + rand.nextDouble() * 0.04;
	                        //if (canPushEntity(entity1)) {
	                        //if (!(entity1 instanceof EntityFlameFX)) {
	                        //applyWindForce(entity1);
	                        windMan.applyWindForceNew(entity1, 1F/20F, 0.5F);
	                    }
                    }
                }
            }

            //My particle renderer - actually, instead add ones you need to weatherEffects (add blank renderer file)
            /*for (int layer = 0; layer < ExtendedRenderer.rotEffRenderer.layers; layer++)
            {
                for (int i = 0; i < ExtendedRenderer.rotEffRenderer.fxLayers[layer].size(); i++)
                {
                    Entity entity1 = (Entity)ExtendedRenderer.rotEffRenderer.fxLayers[layer].get(i);
                }
            }*/
        }

        //this was code to push play around if really windy, lets not do this anymore, who slides around in wind IRL?
        //maybe maybe if a highwind/hurricane state is active, adjust their ACTIVE movement to adhere to wind vector
        /*if (windMan.getWindSpeedForPriority() >= 0.70)
        {
            if (WeatherUtilEntity.canPushEntity(player))
            {
                applyWindForce(player, 0.2F);
            }
        }*/

        //NEEEEEEEED TO STOP WIND WHEN UNDERGROUND!
        float volScaleFar = windMan.getWindSpeedForPriority() * 1F;

        if (windMan.getWindSpeedForPriority() <= 0.07F)
        {
            volScaleFar = 0F;
        }
        
        volScaleFar *= ConfigMisc.volWindScale;

        //Sound whistling noise
        //First, use volume to represent intensity, maybe get different sound samples for higher level winds as they sound different
        //Second, when facing towards wind, you're ears hear it tearing by you more, when turned 90 degrees you do not, simulate this
        
        //weather2: commented out to test before sound code goes in!!!!!!!!!!!!!!!!!!!!!
        /*tryPlaySound(WeatherUtil.snd_wind_far, 2, mc.thePlayer, volScaleFar);

        if (lastSoundPositionUpdate < System.currentTimeMillis())
        {
            lastSoundPositionUpdate = System.currentTimeMillis() + 100;

            if (soundID[2] > -1 && soundTimer[2] < System.currentTimeMillis())
            {
                setVolume(new StringBuilder().append("sound_" + soundID[2]).toString(), volScaleFar);
            }
        }*/
    }
	
	public static void applyWindForce(Object ent)
    {
        applyWindForce(ent, 1D, 1D);
    }
	
    public static void applyWindForce(Object entOrParticle, double multiplier, double maxSpeed)
    {
    	WindManager windMan = ClientTickHandler.weatherManager.windMan;
    	
    	float windSpeed = windMan.getWindSpeedForPriority();
    	float windAngle = windMan.getWindAngleForPriority();
    	
        double speed = windSpeed * 0.1D / WeatherUtilEntity.getWeight(entOrParticle);
        speed *= multiplier;

        if (entOrParticle instanceof Entity) {
        	Entity ent = (Entity) entOrParticle;
        	if ((ent.onGround && windSpeed < 0.7) && speed < 0.3)
            {
                speed = 0D;
            }
        }
        
        double vecX = CoroUtilEntOrParticle.getMotionX(entOrParticle);
        double vecY = CoroUtilEntOrParticle.getMotionY(entOrParticle);
        double vecZ = CoroUtilEntOrParticle.getMotionZ(entOrParticle);
        
        double speedCheck = (Math.abs(vecX)/* + Math.abs(vecY)*/ + Math.abs(vecZ)) / 2D;
        if (speedCheck < maxSpeed) {
        	CoroUtilEntOrParticle.setMotionX(entOrParticle, CoroUtilEntOrParticle.getMotionX(entOrParticle) + speed * (double)(-MathHelper.sin(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));
            CoroUtilEntOrParticle.setMotionZ(entOrParticle, CoroUtilEntOrParticle.getMotionZ(entOrParticle) + speed * (double)(MathHelper.cos(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));
        }
        
        
        /*if (ent instanceof EntityKoaManly) {
        	System.out.println("wind move speed: " + speed + " | " + ent.worldObj.isRemote);
        }*/

        
        /*entOrParticle.motionX += speed * (double)(-MathHelper.sin(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0FweatherMan.wind.yDirection / 180.0F * (float)Math.PI));
        entOrParticle.motionZ += speed * (double)(MathHelper.cos(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0FweatherMan.wind.yDirection / 180.0F * (float)Math.PI));*/
        
        //commented out for weather2, yStrength was 0
        //ent.motionY += weatherMan.wind.yStrength * 0.1D * (double)(-MathHelper.sin((weatherMan.wind.yDirection) / 180.0F * (float)Math.PI));
    }
	
	//Thread safe functions
    @SideOnly(Side.CLIENT)
    private static Block getBlock(World parWorld, int x, int y, int z)
    {
        try
        {
            if (!parWorld.isBlockLoaded(new BlockPos(x, 0, z)))
            {
                return null;
            }

            return parWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
        }
        catch (Exception ex)
        {
            return null;
        }
    }
    
    @SideOnly(Side.CLIENT)
    private static int getBlockMetadata(World parWorld, int x, int y, int z)
    {
        if (!parWorld.isBlockLoaded(new BlockPos(x, 0, z)))
        {
            return 0;
        }

        IBlockState state = parWorld.getBlockState(new BlockPos(x, y, z));
        return state.getBlock().getMetaFromState(state);
    }
    
    public static void tickTest() {
    	Minecraft mc = Minecraft.getMinecraft();
    	if (miniTornado == null) {
    		miniTornado = new ParticleBehaviorMiniTornado(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
    	}
    	
    	//temp
    	//miniTornado.coordSource = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ - 4);
    	
    	
    	
    	if (true || miniTornado.particles.size() == 0) {
	    	for (int i = 0; i < 1; i++) {
	    		ParticleTexFX part = new ParticleTexFX(mc.theWorld, miniTornado.coordSource.xCoord, miniTornado.coordSource.yCoord, miniTornado.coordSource.zCoord, 0, 0, 0, ParticleRegistry.squareGrey);
	    		miniTornado.initParticle(part);
	    		miniTornado.particles.add(part);
	    		part.spawnAsWeatherEffect();
	    	}
    	}
		
    	miniTornado.tickUpdateList();
    	
    	double x = 5;
    	double z = 5;
    	double x2 = 5;
    	double z2 = 0;
    	
    	double vecX = x - x2;
    	double vecZ = z - z2;
    	
    	double what = Math.atan2(vecZ, vecX);
    	
    	//System.out.println(Math.toDegrees(what));
    }
    
    public static void tickTestFog() {
    	Minecraft mc = Minecraft.getMinecraft();
    	if (particleBehaviorFog == null) {
    		
    		particleBehaviorFog = new ParticleBehaviorFogGround(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
    	} else {
    		particleBehaviorFog.coordSource = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 0.5D, mc.thePlayer.posZ);
    	}
    	
    	if (mc.theWorld.getTotalWorldTime() % 300 == 0) {
	    	if (/*true || */particleBehaviorFog.particles.size() <= 10000) {
		    	for (int i = 0; i < 1; i++) {
		    		ParticleTexFX part = new ParticleTexFX(mc.theWorld, particleBehaviorFog.coordSource.xCoord, particleBehaviorFog.coordSource.yCoord, particleBehaviorFog.coordSource.zCoord
		    				, 0, 0, 0, ParticleRegistry.cloud256);
		    		part.setMotionX(-1);
		    		part.setMotionY(0.1);
		    		particleBehaviorFog.initParticle(part);
		    		//particleBehaviorFog.particles.add(part);
		    		part.spawnAsWeatherEffect();
		    		part.windWeight = 5F;
		    		part.debugID = 1;
		    		part.setMaxAge(280);
		    		part.setVanillaMotionDampen(false);
		    		ClientTickHandler.weatherManager.addWeatheredParticle(part);
		    	}
	    	}
    	}
    	
    	particleBehaviorFog.tickUpdateList();
    }
    
    public static void tickTestSandstormParticles() {
    	Minecraft mc = Minecraft.getMinecraft();
    	
    	//vecWOP = null;
    	
    	if (vecWOP == null) {
    		particleBehaviorFog = new ParticleBehaviorFogGround(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
    		vecWOP = new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    	}
    	
    	
    	
    	
    	
    	//need circumference math to keep constant distance between particles to spawn based on size of storm
    	//this also needs adjusting based on the chosed particle scale (that is based on players distance to storm)
    	
    	//if (!isInside) {
    		for (int i = 0; i < 0; i++) {
	    		ParticleTexFX part = new ParticleTexFX(mc.theWorld, vecWOP.xCoord, vecWOP.yCoord, vecWOP.zCoord
	    				, 0, 0, 0, ParticleRegistry.cloud256);
	    		particleBehaviorFog.initParticle(part);
	    		part.setFacePlayer(false);
	    		
	    		//particleBehaviorFog.particles.add(part);
	    		part.spawnAsWeatherEffect();
    		}
    	//}
    		
    	//particleBehaviorFog.tickUpdateList();
    	
		boolean derp = false;
        if (derp) {
        	IBlockState state = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY-1, mc.thePlayer.posZ));
	    	int id = Block.getStateId(state);
	    	id = 12520;
	    	double speed = 0.2D;
	    	Random rand = mc.theWorld.rand;
	    	mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_DUST, mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 
	    			(rand.nextDouble() - rand.nextDouble()) * speed, (rand.nextDouble()) * speed * 2D, (rand.nextDouble() - rand.nextDouble()) * speed, id);
    	}
    }
    
    /**
     * Manages transitioning fog densities and color from current vanilla settings to our desired settings, and vice versa
     */
    public static void tickSandstorm() {
    	
    	//debug code start
    	/*distToStorm--;
    	if (distToStorm <= 0) {
    		distToStorm = distToStormThreshold + 100;
    	}*/
    	
    	
    	//debug code end
    	
    	//TODO: make transition code actually tie to distance, not just trigger a static transition
    	Minecraft mc = Minecraft.getMinecraft();
    	Vec3 posPlayer = new Vec3(mc.thePlayer.posX, 0/*mc.thePlayer.posY*/, mc.thePlayer.posZ);
    	WeatherObjectSandstorm sandstorm = ClientTickHandler.weatherManager.getClosestSandstorm(posPlayer, 9999/*distToStormThreshold + 10*/);
    	float scaleIntensityTarget = 0F;
    	if (sandstorm != null) {

			if (mc.theWorld.getTotalWorldTime() % 40 == 0) {
				isPlayerOutside = WeatherUtilEntity.isEntityOutside(mc.thePlayer);
				//System.out.println("isPlayerOutside: " + isPlayerOutside);
			}


    		scaleIntensityTarget = sandstorm.getSandstormScale();
    		/*Vec3 posNoY = new Vec3(sandstorm.pos);
    		posNoY.yCoord = mc.thePlayer.posY;
    		distToStorm = posPlayer.distanceTo(posNoY);*/
    		
    		List<Vec3> points = sandstorm.getSandstormAsShape();
    		
    		/*for (Vec3 point : points) {
    			System.out.println("point: " + point.toString());
    		}*/
    		
    		boolean inStorm = CoroUtilPhysics.isInConvexShape(posPlayer, points);
        	if (inStorm) {
        		//System.out.println("in storm");
        		distToStorm = 0;
        	} else {
        		
        		distToStorm = CoroUtilPhysics.getDistanceToShape(posPlayer, points);
        		//System.out.println("near storm: " + distToStorm);
        	}
    	} else {
    		distToStorm = distToStormThreshold + 10;
    	}
    	
    	scaleIntensitySmooth = adjVal(scaleIntensitySmooth, scaleIntensityTarget, 0.01F);
    	
    	//temp off
    	//distToStorm = distToStormThreshold;
    	
    	//distToStorm = 0;
    	
    	/**
    	 * new way to detect in sandstorm
    	 * 1. use in convex shape method
    	 * 2. if not, get closest point of shape to player, use that for distance
    	 * -- note, add extra points to compare against so its hard to enter between points and have it think player is 50 blocks away still
    	 * 
    	 * - better idea, do 1., then if not, do point vs "minimum distance from a point to a line segment."
    	 */
    	
    	
    	
    	//square shape test
    	/*points.add(new Vec3(-100, 0, -100));
    	points.add(new Vec3(-100, 0, 100));
    	points.add(new Vec3(100, 0, 100));
    	points.add(new Vec3(100, 0, -100));*/
    	
    	//triangle test
    	/*points.add(new Vec3(-100, 0, -100));
    	points.add(new Vec3(-100, 0, 100));
    	points.add(new Vec3(100, 0, 0));*/
    	//points.add(new Vec3(100, 0, -100));
    	
    	//tested works well
    	
    	
    	
    	float fogColorChangeRate = 0.01F;
    	float fogDistChangeRate = 2F;
    	float fogDensityChangeRate = 0.01F;
    	
    	//make it be full intensity once storm is halfway there
    	adjustAmountTarget = 1F - (float) ((distToStorm) / distToStormThreshold);
    	adjustAmountTarget *= 2F * scaleIntensitySmooth * (isPlayerOutside ? 1F : 0.5F);
    	
    	if (adjustAmountTarget < 0F) adjustAmountTarget = 0F;
    	if (adjustAmountTarget > 1F) adjustAmountTarget = 1F;
    	
    	//since size var adjusts by 10 every x seconds, transition is rough, try to make it smooth but keeps up
    	if (adjustAmountSmooth < adjustAmountTarget) {
    		adjustAmountSmooth = adjVal(adjustAmountSmooth, adjustAmountTarget, 0.003F);
    	} else {
    		adjustAmountSmooth = adjVal(adjustAmountSmooth, adjustAmountTarget, 0.002F);
    	}
    	
    	if (adjustAmountSmooth > 0 && /*sandstorm != null && */mc.theWorld.getTotalWorldTime() % 20 == 0) {
    		//System.out.println(adjustAmount + " - " + distToStorm);
    		System.out.println("adjustAmountTarget: " + adjustAmountTarget);
    		System.out.println("adjustAmountSmooth: " + adjustAmountSmooth);
    	}
    	
    	if (adjustAmountSmooth > 0/*distToStorm < distToStormThreshold*/) {
    		if (needFogState) {
    			System.out.println("getting fog state");
    			stormFogRed = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175080_Q");
    			stormFogGreen = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175082_R");
    			stormFogBlue = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175081_S");
    			
    			//account for player being in fog as game loads, all values are 0 in this scenario
    			//values arent perfect, are from noon daytime, but better than black
    			if (stormFogRed == 0 && stormFogGreen == 0 && stormFogBlue == 0) {
    				stormFogRed = 0.7225088F;
    				stormFogGreen = 0.8253213F;
    				stormFogBlue = 1F;
    			}
    			
    			try {
    				Object fogState = ObfuscationReflectionHelper.getPrivateValue(GlStateManager.class, null, "field_179155_g");
    				Class<?> innerClass = Class.forName("net.minecraft.client.renderer.GlStateManager$FogState");
    				Field fieldDensity = null;
    				Field fieldStart = null;
    				Field fieldEnd = null;
    				try {
    					fieldDensity = innerClass.getField("field_179048_c");
    					fieldDensity.setAccessible(true);
    					fieldStart = innerClass.getField("field_179045_d");
    					fieldStart.setAccessible(true);
    					fieldEnd = innerClass.getField("field_179046_e");
    					fieldEnd.setAccessible(true);
					} catch (Exception e) {
						//dev env mode
						fieldDensity = innerClass.getField("density");
						fieldDensity.setAccessible(true);
						fieldStart = innerClass.getField("start");
    					fieldStart.setAccessible(true);
    					fieldEnd = innerClass.getField("end");
    					fieldEnd.setAccessible(true);
					}
    				stormFogDensity = fieldDensity.getFloat(fogState);
    				
    				stormFogStart = fieldStart.getFloat(fogState);
    				stormFogEnd = fieldEnd.getFloat(fogState);
    				
    				stormFogStartClouds = 0;
    				stormFogEndClouds = 192;
    				
    				
    				stormFogStartOrig = stormFogStart;
    				stormFogEndOrig = stormFogEnd;
    				stormFogStartCloudsOrig = stormFogStartClouds;
    				stormFogEndCloudsOrig = stormFogEndClouds;
    				
    				stormFogDensityOrig = stormFogDensity;
    				
				} catch (Exception e) {
					e.printStackTrace();
				}
    			
    			stormFogRedOrig = stormFogRed;
    			stormFogGreenOrig = stormFogGreen;
    			stormFogBlueOrig = stormFogBlue;
    			needFogState = false;
    		}
    		
    		
    		
    		/*stormFogRed = adjVal(stormFogRed, 0.7F, fogColorChangeRate);
    		stormFogGreen = adjVal(stormFogGreen, 0.6F, fogColorChangeRate);
    		stormFogBlue = adjVal(stormFogBlue, 0.3F, fogColorChangeRate);
    		
    		stormFogDensity = adjVal(stormFogDensity, 0.5F, fogDensityChangeRate);
    		
    		stormFogStart = adjVal(stormFogStart, 0F, fogDistChangeRate);
    		stormFogEnd = adjVal(stormFogEnd, 20F, fogDistChangeRate);
    		stormFogStartClouds = adjVal(stormFogStartClouds, 0F, fogDistChangeRate);
    		stormFogEndClouds = adjVal(stormFogEndClouds, 20F, fogDistChangeRate);*/
    		
    		//new dynamic adjusting
    		stormFogRed = stormFogRedOrig + (-(stormFogRedOrig - 0.7F) * adjustAmountSmooth);
    		stormFogGreen = stormFogGreenOrig + (-(stormFogGreenOrig - 0.6F) * adjustAmountSmooth);
    		stormFogBlue = stormFogBlueOrig + (-(stormFogBlueOrig - 0.3F) * adjustAmountSmooth);
    		
    		stormFogDensity = stormFogDensityOrig + (-(stormFogDensityOrig - 0.5F) * adjustAmountSmooth);
    		
    		stormFogStart = stormFogStartOrig + (-(stormFogStartOrig - 0F) * adjustAmountSmooth);
    		stormFogEnd = stormFogEndOrig + (-(stormFogEndOrig - 20F) * adjustAmountSmooth);
    		stormFogStartClouds = stormFogStartCloudsOrig + (-(stormFogStartCloudsOrig - 0F) * adjustAmountSmooth);
    		stormFogEndClouds = stormFogEndCloudsOrig + (-(stormFogEndCloudsOrig - 20F) * adjustAmountSmooth);
    		
    		//System.out.println("ON");
    	} else {
    		if (!needFogState) {
    			System.out.println("resetting need for fog state");
    		}
    		needFogState = true;
    		
    		//if these values are already equal it shouldnt actually do anything
    		/*stormFogRed = adjVal(stormFogRed, stormFogRedOrig, fogColorChangeRate);
    		stormFogGreen = adjVal(stormFogGreen, stormFogGreenOrig, fogColorChangeRate);
    		stormFogBlue = adjVal(stormFogBlue, stormFogBlueOrig, fogColorChangeRate);
    		
    		stormFogDensity = adjVal(stormFogDensity, stormFogDensityOrig, fogDensityChangeRate);
    		
    		stormFogStart = adjVal(stormFogStart, stormFogStartOrig, fogDistChangeRate);
    		stormFogEnd = adjVal(stormFogEnd, stormFogEndOrig, fogDistChangeRate);
    		stormFogStartClouds = adjVal(stormFogStartClouds, stormFogStartCloudsOrig, fogDistChangeRate);
    		stormFogEndClouds = adjVal(stormFogEndClouds, stormFogEndCloudsOrig, fogDistChangeRate);*/
    		
    		//System.out.println("OFF");
    	}
    }
    
    public static boolean isFogOverridding() {
    	return distToStorm < distToStormThreshold/* || 
    			(stormFogRed != stormFogRedOrig || stormFogGreen != stormFogGreenOrig || stormFogBlue != stormFogBlueOrig) || 
    			(stormFogDensity != stormFogDensityOrig || stormFogStart != stormFogStartOrig || stormFogEnd != stormFogEndOrig)*/;
    }
    
    public static float adjVal(float source, float target, float adj) {
    	if (source < target) {
    		source += adj;
    		//fix over adjust
    		if (source > target) {
    			source = target;
    		}
    	} else if (source > target) {
    		source -= adj;
    		//fix over adjust
    		if (source < target) {
    			source = target;
    		}
    	}
    	return source;
    }
    
    public static void renderWorldLast(RenderWorldLastEvent event) {
    	
    	
    	
    }
}
