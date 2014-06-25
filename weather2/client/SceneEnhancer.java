package weather2.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.api.WindReader;
import weather2.client.entity.particle.EntityFallingRainFX;
import weather2.client.entity.particle.EntityFallingSnowFX;
import weather2.client.entity.particle.EntityWaterfallFX;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilParticle;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.OldUtil;
import CoroUtil.api.weather.WindHandler;
import CoroUtil.util.ChunkCoordinatesBlock;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.EntityTexBiomeColorFX;
import extendedrenderer.particle.entity.EntityTexFX;

@SideOnly(Side.CLIENT)
public class SceneEnhancer implements Runnable {

	//potential ideas:
    //TROPICRAFT FLOWER SPAWN POLLEN!
	
	//this is for the thread we make
	public World lastWorldDetected = null;

	//used for acting on fire/smoke
	public static ParticleBehaviors pm;
	
	public static List<EntityFX> spawnQueueNormal = new ArrayList();
    public static List<Entity> spawnQueue = new ArrayList();
    
    public static long threadLastWorldTickTime;
    public static int lastTickFoundBlocks;
    public static long lastTickAmbient;
    
    //consider caching somehow without desyncing or overflowing
    //WE USE 0 TO MARK WATER, 1 TO MARK LEAVES
    public static ArrayList<ChunkCoordinatesBlock> soundLocations = new ArrayList();
    public static HashMap<ChunkCoordinatesBlock, Long> soundTimeLocations = new HashMap();
    
    public static int SOUNDMARKER_WATER = 0;
    public static int SOUNDMARKER_LEAVES = 1;
    
    public static float curPrecipStr = 0F;
    public static float curPrecipStrTarget = 0F;
    
    public static float curOvercastStr = 0F;
    public static float curOvercastStrTarget = 0F;
	
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
			
			Minecraft mc = FMLClientHandler.instance().getClient();
			tryWind(mc.theWorld);
		}
	}
	
	//run from our newly created thread
	public void tickClientThreaded() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		if (mc.theWorld != null && lastWorldDetected != mc.theWorld) {
			lastWorldDetected = mc.theWorld;
			reset();
		}
		
		if (mc.theWorld != null && mc.thePlayer != null && WeatherUtilConfig.listDimensionsWindEffects.contains(mc.theWorld.provider.dimensionId)) {
			profileSurroundings();
			tryAmbientSounds();
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
    	
    	if (lastTickAmbient < System.currentTimeMillis()) {
    		lastTickAmbient = System.currentTimeMillis() + 500;
    		
    		int size = 32;
            int hsize = size / 2;
            int curX = (int)player.posX;
            int curY = (int)player.posY;
            int curZ = (int)player.posZ;
            
            //soundLocations.clear();
            
            //trim out distant sound locations, also update last time played
            for (int i = 0; i < soundLocations.size(); i++) {
            	
            	ChunkCoordinatesBlock cCor = soundLocations.get(i);
            	
            	if (Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)) > size) {
            		soundLocations.remove(i--);
            		soundTimeLocations.remove(cCor);
            		//System.out.println("trim out soundlocation");
            	} else {
            		int id = getBlockId(worldRef, cCor.posX, cCor.posY, cCor.posZ);

                    Block block = Block.blocksList[id];
                    
                    if (block == null || (block.blockMaterial != Material.water && block.blockMaterial != Material.leaves)) {
                    	soundLocations.remove(i);
                		soundTimeLocations.remove(cCor);
                    } else {
                    	
	            		long lastPlayTime = 0;
	            		
	            		
	            		
	            		if (soundTimeLocations.containsKey(cCor)) {
	            			lastPlayTime = soundTimeLocations.get(cCor);
	            		}
	            		
	            		//System.out.println(Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)));
						if (lastPlayTime < System.currentTimeMillis()) {
							if (cCor.blockID == SOUNDMARKER_WATER) {
								soundTimeLocations.put(cCor, System.currentTimeMillis() + 2500 + rand.nextInt(50));
								mc.sndManager.playSound(Weather.modID + ":waterfall", cCor.posX, cCor.posY, cCor.posZ, (float)ConfigMisc.volWaterfallScale, 0.75F + (rand.nextFloat() * 0.05F));
								//System.out.println("play waterfall at: " + cCor.posX + " - " + cCor.posY + " - " + cCor.posZ);
							} else if (cCor.blockID == SOUNDMARKER_LEAVES) {
								
									
								float windSpeed = WindReader.getWindSpeed(mc.theWorld, Vec3.createVectorHelper(cCor.posX, cCor.posY, cCor.posZ), WindReader.WindType.EVENT);
								if (windSpeed > 0.2F) {
									soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
									mc.sndManager.playSound(Weather.modID + ":wind_calmfade", cCor.posX, cCor.posY, cCor.posZ, (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
									//System.out.println("play leaves sound at: " + cCor.posX + " - " + cCor.posY + " - " + cCor.posZ + " - windSpeed: " + windSpeed);
								} else {
									windSpeed = WindReader.getWindSpeed(mc.theWorld, Vec3.createVectorHelper(cCor.posX, cCor.posY, cCor.posZ));
									//if (windSpeed > 0.3F) {
									if (mc.theWorld.rand.nextInt(15) == 0) {
										soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
										mc.sndManager.playSound(Weather.modID + ":wind_calmfade", cCor.posX, cCor.posY, cCor.posZ, (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
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
    		
    		for (int xx = curX - hsize; xx < curX + hsize; xx++)
            {
                for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
                {
                    for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
                    {
                        int id = getBlockId(worldRef, xx, yy, zz);

                        Block block = Block.blocksList[id];
                        
                        if (block != null) {
                        	
                        	//Waterfall
                        	if (ConfigMisc.Wind_Particle_waterfall && ((block.blockMaterial == Material.water))) {
                            	
                            	int meta = getBlockMetadata(worldRef, xx, yy, zz);
                            	if ((meta & 8) != 0) {
                            		
                            		int bottomY = yy;
                            		int index = 0;
                            		
                            		//this scans to bottom till not water, kinda overkill? owell lets keep it, and also add rule if index > 4 (waterfall height of 4)
                            		while (yy-index > 0) {
                            			int id2 = getBlockId(worldRef, xx, yy-index, zz);
                            			if (Block.blocksList[id2] != null && !(Block.blocksList[id2].blockMaterial == Material.water)) {
                            				break;
                            			}
                            			index++;
                            		}
                            		
                            		bottomY = yy-index+1;
                            		
                            		//check if +10 from here is water with right meta too
                            		int id2 = getBlockId(worldRef, xx, bottomY+10, zz);
                            		int meta2 = getBlockMetadata(worldRef, xx, bottomY+10, zz);
                            		Block block2 = Block.blocksList[id2];
                            		
                        			if (index >= 4 && (block2 != null && block2.blockMaterial == Material.water && (meta2 & 8) != 0)) {
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
                            } else if (ConfigMisc.volWindTreesScale > 0 && ((block.blockMaterial == Material.leaves))) {
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
        	for (int i = 0; i < ExtendedRenderer.rotEffRenderer.layers; i++)                            
            {
                if (ExtendedRenderer.rotEffRenderer.fxLayers[i] != null)
                {
                    ExtendedRenderer.rotEffRenderer.fxLayers[i].clear();
                }
            }
        }
		
		lastWorldDetected.weatherEffects.clear();
		
		WeatherUtilParticle.getFXLayers();
		WeatherUtilSound.getSoundSystem();
	}
	
	public void tickParticlePrecipitation() {
		
		if (ConfigMisc.Particle_RainSnow) {
			EntityPlayer entP = FMLClientHandler.instance().getClient().thePlayer;
			
			float curPrecipVal = getRainStrengthAndControlVisuals(entP);
			
			float maxPrecip = 0.5F;
			
			/*if (entP.worldObj.getTotalWorldTime() % 20 == 0) {
				Weather.dbg("curRainStr: " + curRainStr);
			}*/
			
			//Weather.dbg("curPrecipVal: " + curPrecipVal * 100F);
			
			
			int precipitationHeight = entP.worldObj.getPrecipitationHeight(MathHelper.floor_double(entP.posX), MathHelper.floor_double(entP.posZ));
			
			BiomeGenBase biomegenbase = entP.worldObj.getBiomeGenForCoords(MathHelper.floor_double(entP.posX), MathHelper.floor_double(entP.posZ));

            if (true/* || biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()*/)
            {
			
				float temperature = biomegenbase.getFloatTemperature();
	            double d3;
	            float f10;
	
	            if (/*curPrecipVal > 0*/entP.worldObj.getWorldChunkManager().getTemperatureAtHeight(temperature, precipitationHeight) >= 0.15F) {
	            	
	            	//now absolute it for ez math
	            	curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
	            	
	            	//Weather.dbg("precip: " + curPrecipVal);
	            	
	            	//rain
					if (curPrecipVal > 0 && entP.worldObj.canLightningStrikeAt(MathHelper.floor_double(entP.posX), MathHelper.floor_double(entP.boundingBox.minY), MathHelper.floor_double(entP.posZ))) {
						
						//Weather.dbg("rate: " + curPrecipVal * 20F * ConfigMisc.Particle_Precipitation_effect_rate);
						
						for (int i = 0; i < curPrecipVal * 20F * ConfigMisc.Particle_Precipitation_effect_rate; i++) {
							int spawnAreaSize = 15;
							EntityFallingRainFX ent = new EntityFallingRainFX(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + 15, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, 1.5D, 3);
							ent.severityOfRainRate = (int)(curPrecipVal * 5F);
					        ent.renderDistanceWeight = 1.0D;
					        ent.setSize(1.2F, 1.2F);
					        ent.rotationYaw = ent.worldObj.rand.nextInt(360) - 180F;
					        ent.setGravity(0.00001F);
					        ent.spawnAsWeatherEffect();
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
							EntityFallingSnowFX ent = new EntityFallingSnowFX(entP.worldObj, (double)entP.posX + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), (double)entP.posY + spawnAbove, (double)entP.posZ + entP.worldObj.rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2), 0D, -5D - (entP.worldObj.rand.nextInt(5) * -1D), 0D, 5.5D, 6);
							ent.severityOfRainRate = (int)(curPrecipVal * 5F);
					        ent.renderDistanceWeight = 1.0D;
					        ent.setSize(1.2F, 1.2F);
					        ent.rotationYaw = ent.worldObj.rand.nextInt(360) - 180F;
					        ent.setGravity(0.00001F);
					        ent.spawnAsWeatherEffect();
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
		Vec3 plPos = Vec3.createVectorHelper(entP.posX, StormObject.static_YPos_layer0, entP.posZ);
		StormObject storm = null;
		
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
	            Entity ent = spawnQueue.get(i);
	
	            if (ent != null && ent.worldObj != null) {
	            
		            if (ent instanceof EntityRotFX)
		            {
		                ((EntityRotFX) ent).spawnAsWeatherEffect();
		            }
		            else
		            {
		                ent.worldObj.addWeatherEffect(ent);
		            }
	            }
	        }
	        for (int i = 0; i < spawnQueueNormal.size(); i++)
	        {
	            EntityFX ent = spawnQueueNormal.get(i);
	
	            if (ent != null && ent.worldObj != null) {
	            
	            	Minecraft.getMinecraft().effectRenderer.addEffect(ent);
	            }
	        }
    	} catch (Exception ex) {
    		System.out.println("Error handling particle spawn queue: ");
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

        if (mc.objectMouseOver != null) {
        	int id = mc.theWorld.getBlockId(mc.objectMouseOver.blockX,mc.objectMouseOver.blockY,mc.objectMouseOver.blockZ);
        	//System.out.println(mc.theWorld.getBlockId(mc.objectMouseOver.blockX,mc.objectMouseOver.blockY,mc.objectMouseOver.blockZ));
        	if (id > 0 && Block.blocksList[id].blockMaterial == Material.wood) {
        		float var5 = 0;

        		var5 = (Float)OldUtil.getPrivateValueSRGMCP(PlayerControllerMP.class, (PlayerControllerMP)mc.playerController, OldUtil.refl_curBlockDamageMP_obf, OldUtil.refl_curBlockDamageMP_mcp);

                if (var5 > 0) {
                	//weather2 disabled for now
                	//shakeTrees(8);
                }
        	}
        }

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
                            int id = getBlockId(worldRef, xx, yy, zz);

                            Block block = Block.blocksList[id];
                            
                            //if (block != null && block.blockMaterial == Material.leaves)
                            
                            if (/*id == ((Block)p_blocks_leaf.get(i)).blockID*/block != null && (block.blockMaterial == Material.leaves || block.blockMaterial == Material.vine))
                            {
                            	
                            	lastTickFoundBlocks++;
                            	
                            	if (/*true || */worldRef.rand.nextInt(spawnRate) == 0)
                                {
                            		//bottom of tree check || air beside vine check
	                                if (ConfigMisc.Wind_Particle_leafs && (getBlockId(worldRef, xx, yy - 1, zz) == 0 || getBlockId(worldRef, xx - 1, yy, zz) == 0))
	                                {
	                                	
	                                    EntityRotFX var31 = new EntityTexBiomeColorFX(worldRef, (double)xx, (double)yy - 0.5, (double)zz, 0D, 0D, 0D, 10D, 0, WeatherUtilParticle.effLeafID, id, getBlockMetadata(worldRef, xx, yy, zz), xx, yy, zz);
	                                    var31.particleGravity = 0.1F;
	                                    //WeatherUtil.setParticleGravity((EntityFX)var31, 0.1F);
	
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
                            else if (ConfigMisc.Wind_Particle_waterfall && player.getDistance(xx,  yy, zz) < 16 && (block != null && block.blockMaterial == Material.water)) {
                            	
                            	int meta = getBlockMetadata(worldRef, xx, yy, zz);
                            	if ((meta & 8) != 0) {
                            		lastTickFoundBlocks += 70; //adding more to adjust for the rate 1 waterfall block spits out particles
                            		int chance = (int)(1+(((float)BlockCountRate)/120F));
                            		
                            		int id2 = getBlockId(worldRef, xx, yy-1, zz);
                            		int meta2 = getBlockMetadata(worldRef, xx, yy-1, zz);
                            		int id3 = getBlockId(worldRef, xx, yy+10, zz);
                            		Block block2 = Block.blocksList[id2];
                            		Block block3 = Block.blocksList[id3];
                            		
                            		//if ((block2 == null || block2.blockMaterial != Material.water) && (block3 != null && block3.blockMaterial == Material.water)) {
                            			//chance /= 3;
                            			
                            		//}
                            		//System.out.println("woot! " + chance);
                                	if ((((block2 == null || block2.blockMaterial != Material.water) || (meta2 & 8) == 0) && (block3 != null && block3.blockMaterial == Material.water)) || worldRef.rand.nextInt(chance) == 0) {
                            		
	                            		float range = 0.5F;
	                            		
	                            		EntityFX waterP;
	                            		//if (rand.nextInt(10) == 0) {
	                            			//waterP = new EntityBubbleFX(worldRef, (double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D);
	                            		//} else {
	                            		waterP = new EntityWaterfallFX(worldRef, (double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), (double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
	                            		//}
                                	
	                            		
	                            		
                            			if (((block2 == null || block2.blockMaterial != Material.water) || (meta2 & 8) == 0) && (block3 != null && block3.blockMaterial == Material.water)) {
                            				
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
    	                            				waterP.motionY = 4.5F;
    	                            				//System.out.println("woot! " + chance);
    	                            				spawnQueueNormal.add(waterP);
                            					}
	                            				
                            				}
                            			} else {
                            				waterP = new EntityWaterfallFX(worldRef, 
                            						(double)xx + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
                            						(double)yy + 0.5F + ((rand.nextFloat() * range) - (range/2)), 
                            						(double)zz + 0.5F + ((rand.nextFloat() * range) - (range/2)), 0D, 0D, 0D, 6D, 2);
                            				
                            				waterP.motionY = 0.5F;
                            				
                            				spawnQueueNormal.add(waterP);
                            			}
	                            			
	                            		
	                            		//waterP.rotationYaw = rand.nextInt(360);
	                                	
                                	}
                            	}
                            	
                            }else if (ConfigMisc.Wind_Particle_fire && (block != null && block.blockID == Block.fire.blockID/*block.blockMaterial == Material.fire*/)) {
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
                            else if (false && id == 0)
                            {
                            	
                            	float temp = worldRef.getBiomeGenForCoords(xx, zz).getFloatTemperature();
                            	
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
        //if pre stage...
        //look for leaves? spawn particles

        //RAIN!! MODIFY BLUE COLOR SHADES IN CODE BITCH

        //logic
    	//weatherMan.wind.strength = 0.6F;
    	//debug = true;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;

        //if (true) return;
        if (player == null)
        {
            return;
        }

        int dist = 60;
        //if (side == Side.SERVER) {
        //List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.expand(dist, 80, dist));
        List list = null;

        /*if (side == Side.CLIENT)
        {
            list = player.worldObj.loadedEntityList;
        }
        else
        {
            
        }*/
        
        list = world.loadedEntityList;

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

        //}

        //weatherMan.wind.strength = 0.2F;

        //System.out.println("stuff: " + side);
        
        Random rand = new Random();
        
        int handleCount = 0;
        
        //Weather Effects
        if (ClientTickHandler.weatherManager.windMan.getWindSpeedForPriority() >= 0.10)
        {
            for (int i = 0; i < world.weatherEffects.size(); i++)
            {
            	
            	handleCount++;
            	
                Entity entity1 = (Entity)world.weatherEffects.get(i);
                
                if (!(entity1 instanceof EntityLightningBolt))
                {
                	
                	
                	
                    //applyWindForce(entity1);
                    if (entity1 instanceof EntityFX)
                    {
                    	
                    	
                    	
                        if (entity1 == null)
                        {
                            continue;
                        }

                        
                        
                        if ((world.getHeightValue(MathHelper.floor_double(entity1.posX), MathHelper.floor_double(entity1.posZ)) - 1 < (int)entity1.posY + 1) || (entity1 instanceof EntityTexFX))
                        {
                            if ((entity1 instanceof EntityFlameFX))
                            {
                            	WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + 2);
                            }
                            else if (entity1 instanceof WindHandler) {
                            	if (((WindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge((EntityFX)entity1) % 2 == 0)
                                {
                            		WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + ((WindHandler)entity1).getParticleDecayExtra());
                                }
                            }
                            else if (WeatherUtilParticle.getParticleAge((EntityFX)entity1) % 2 == 0)
                            {
                            	WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + 1);
                            }

                            //((EntityFX)entity1).particleAge=1;
                            
                            if ((entity1 instanceof EntityTexFX) && ((EntityTexFX)entity1).getParticleTextureIndex() == WeatherUtilParticle.effLeafID)
                            {
                                if (entity1.motionX < 0.01F && entity1.motionZ < 0.01F)
                                {
                                    entity1.motionY += rand.nextDouble() * 0.02;
                                }

                                //entity1.motionX += rand.nextDouble() * 0.03;
                                //entity1.motionZ += rand.nextDouble() * 0.03;
                                entity1.motionY -= 0.01F;
                                //do it twice!
                                
                            }
                        }

                        //if (canPushEntity(entity1)) {
                        /*if (!(entity1 instanceof EntTornado))
                        {
                            
                        }*/
                        
                        //we apply it twice apparently, k
                        applyWindForce(entity1);
                        applyWindForce(entity1);
                    }
                }
            }
        }
        
        //System.out.println("particles moved: " + handleCount);

        WindManager windMan = ClientTickHandler.weatherManager.windMan;
        
        //Particles
        if (WeatherUtilParticle.fxLayers != null && windMan.getWindSpeedForPriority() >= 0.10)
        {
        	//Built in particles
            for (int layer = 0; layer < 4; layer++)
            {
                for (int i = 0; i < WeatherUtilParticle.fxLayers[layer].size(); i++)
                {
                    Entity entity1 = (Entity)WeatherUtilParticle.fxLayers[layer].get(i);
                    
                    if (ConfigMisc.Particle_VanillaAndWeatherOnly) {
                    	String className = entity1.getClass().getName();
                    	if (className.contains("net.minecraft.") || className.contains("weather2.")) {
                    		
                    	} else {
                    		continue;
                    	}
                    	
                    	//Weather.dbg("process: " + className);
                    }

                    if ((world.getHeightValue(MathHelper.floor_double(entity1.posX), MathHelper.floor_double(entity1.posZ)) - 1 < (int)entity1.posY + 1) || (entity1 instanceof EntityTexFX))
                    {
                        if ((entity1 instanceof EntityFlameFX))
                        {
                        	if (windMan.getWindSpeedForPriority() >= 0.50) WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + 2);
                        }
                        else if (entity1 instanceof WindHandler) {
                        	if (((WindHandler)entity1).getParticleDecayExtra() > 0 && WeatherUtilParticle.getParticleAge((EntityFX)entity1) % 2 == 0)
                            {
                        		WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + ((WindHandler)entity1).getParticleDecayExtra());
                            }
                        }
                        else if (WeatherUtilParticle.getParticleAge((EntityFX)entity1) % 2 == 0)
                        {
                        	WeatherUtilParticle.setParticleAge((EntityFX)entity1, WeatherUtilParticle.getParticleAge((EntityFX)entity1) + 1);
                        }

                        //rustle!
                        if (!(entity1 instanceof EntityWaterfallFX)) {
	                        if (entity1.onGround)
	                        {
	                            //entity1.onGround = false;
	                            entity1.motionY += rand.nextDouble() * entity1.motionX;
	                        }
	
	                        if (entity1.motionX < 0.01F && entity1.motionZ < 0.01F)
	                        {
	                            entity1.motionY += rand.nextDouble() * 0.02;
	                        }
                        }

                        //entity1.motionX += rand.nextDouble() * 0.03;
                        //entity1.motionZ += rand.nextDouble() * 0.03;
                        //entity1.motionY += -0.04 + rand.nextDouble() * 0.04;
                        //if (canPushEntity(entity1)) {
                        //if (!(entity1 instanceof EntityFlameFX)) {
                        applyWindForce(entity1);
                    }
                }
            }

            //My particle renderer - actually, instead add ones you need to weatherEffects (add blank renderer file)
            for (int layer = 0; layer < ExtendedRenderer.rotEffRenderer.layers; layer++)
            {
                for (int i = 0; i < ExtendedRenderer.rotEffRenderer.fxLayers[layer].size(); i++)
                {
                    Entity entity1 = (Entity)ExtendedRenderer.rotEffRenderer.fxLayers[layer].get(i);
                    /*if (entity1 == null) continue;
                    if ((worldRef.getHeightValue((int)(entity1.posX+0.5F), (int)(entity1.posZ+0.5F))-1 < (int)entity1.posY+1) || (entity1 instanceof EntityTexFX)) {
                        if ((entity1 instanceof EntityFlameFX)) {
                        	((EntityFX)entity1).particleAge+=2;
                        } else if (entity1 instanceof EntityAnimTexFX) {
                        	if (((EntityAnimTexFX) entity1).type == 1) {
                        		if (activeTornado != null && !activeTornado.isDead) {
                        			//spin(activeTornado, (WeatherEntityConfig)weatherEntTypes.get(1), entity1);
                        		} else {
                        			//temp
                        			//spin(player, (WeatherEntityConfig)weatherEntTypes.get(1), entity1);
                        		}
                        	}
                        } else if (((EntityFX)entity1).particleAge % 2 == 0) {
                        	((EntityFX)entity1).particleAge+=1;
                        }

                        //rustle!
                        if (entity1 instanceof Entity) {}
                        if (entity1 instanceof EntityFallingRainFX) {
                            if (entity1.onGround) {
                            	//entity1.onGround = false;
                            	//entity1.motionY += rand.nextDouble() * entity1.motionX;
                            }

                            //entity1.motionY += -0.02 + rand.nextDouble() * 0.04;
                        } else if ((entity1 instanceof EntityTexFX) && ((EntityTexFX)entity1).getParticleTextureIndex() == mod_EntMover.effLeafID) {
                        	if (entity1.motionX < 0.01F && entity1.motionZ < 0.01F) {
                            	entity1.motionY += rand.nextDouble() * 0.08;
                            }
                        	entity1.motionX += rand.nextDouble() * 0.03;
                            entity1.motionZ += rand.nextDouble() * 0.03;
                            entity1.motionY -= 0.01F;
                        }

                    //if (canPushEntity(entity1)) {
                    //if (!(entity1 instanceof EntityFlameFX)) {
                    	applyWindForce(entity1);
                    }*/
                }
            }
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
	
	public static void applyWindForce(Entity ent)
    {
        applyWindForce(ent, 1D);
    }
	
    public static void applyWindForce(Entity ent, double multiplier)
    {
    	WindManager windMan = ClientTickHandler.weatherManager.windMan;
    	
    	float windSpeed = windMan.getWindSpeedForPriority();
    	float windAngle = windMan.getWindAngleForPriority();
    	
        double speed = windSpeed * 0.1D / WeatherUtilEntity.getWeight(ent);
        speed *= multiplier;

        if ((ent.onGround && windSpeed < 0.7) && speed < 0.3)
        {
            speed = 0D;
        }
        
        /*if (ent instanceof EntityKoaManly) {
        	System.out.println("wind move speed: " + speed + " | " + ent.worldObj.isRemote);
        }*/

        ent.motionX += speed * (double)(-MathHelper.sin(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI));
        ent.motionZ += speed * (double)(MathHelper.cos(windAngle / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI));
        //commented out for weather2, yStrength was 0
        //ent.motionY += weatherMan.wind.yStrength * 0.1D * (double)(-MathHelper.sin((weatherMan.wind.yDirection) / 180.0F * (float)Math.PI));
    }
	
	//Thread safe functions
    @SideOnly(Side.CLIENT)
    private static int getBlockId(World parWorld, int x, int y, int z)
    {
        try
        {
            if (!parWorld.checkChunksExist(x, 0, z , x, 128, z))
            {
                return 10;
            }

            return parWorld.getBlockId(x, y, z);
        }
        catch (Exception ex)
        {
            return 10;
        }
    }
    
    @SideOnly(Side.CLIENT)
    private static int getBlockMetadata(World parWorld, int x, int y, int z)
    {
        if (!parWorld.checkChunksExist(x, 0, z , x, 128, z))
        {
            return 0;
        }

        return parWorld.getBlockMetadata(x, y, z);
    }
}
