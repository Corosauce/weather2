package weather2.client;

import com.corosus.coroutil.config.ConfigCoroUtil;
import com.corosus.coroutil.util.*;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import extendedrenderer.particle.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SuspendedParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import weather2.*;
import weather2.client.entity.particle.ParticleHail;
import weather2.client.entity.particle.ParticleSandstorm;
import weather2.config.ConfigMisc;
import weather2.config.ConfigParticle;
import weather2.config.ConfigSand;
import weather2.datatypes.PrecipitationType;
import weather2.datatypes.WeatherEventType;
import weather2.util.*;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.fog.FogAdjuster;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;
import weather2.weathersystem.tornado.TornadoManagerTodoRenameMe;
import weather2.weathersystem.wind.WindManager;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class SceneEnhancer implements Runnable {

	private static final double PRECIPITATION_PARTICLE_EFFECT_RATE = 0.7;

	//this is for the thread we make
	public ClientLevel lastWorldDetected = null;

	public static List<Particle> spawnQueueNormal = new ArrayList<>();
    public static List<Particle> spawnQueue = new ArrayList<>();

    public static long threadLastWorldTickTime;
    public static int lastTickFoundBlocks;
    public static long lastTickAmbient;
    public static long lastTickAmbientThreaded;

    public static ArrayList<ChunkCoordinatesBlock> soundLocations = new ArrayList<>();
    public static HashMap<ChunkCoordinatesBlock, Long> soundTimeLocations = new HashMap<>();

    public static List<Block> LEAVES_BLOCKS = new ArrayList<>();

	private static final List<BlockPos> listPosRandom = new ArrayList<>();

	public static final ResourceLocation RAIN_TEXTURES_GREEN = new ResourceLocation(Weather.MODID, "textures/environment/rain_green.png");
	public static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");

	public static boolean FORCE_ON_DEBUG_TESTING = false;

	/*public static int fadeInTimer = 0;
	public static int fadeInTimerMax = 400;*/

	public static ParticleBehaviorSandstorm particleBehavior;

	private static FogAdjuster fogAdjuster;

	public static boolean isPlayerOutside = true;
	public static boolean isPlayerNearTornadoCached = false;

	public static WeatherEventType lastWeatherType = null;

	public static int particleRateLerp = 0;
	public static int particleRateLerpMax = 100;

	public static TornadoManagerTodoRenameMe playerManagerClient;

	private static Biome lastBiomeIn = null;

	public SceneEnhancer() {
		listPosRandom.clear();
		listPosRandom.add(new BlockPos(0, -1, 0));
		listPosRandom.add(new BlockPos(1, 0, 0));
		listPosRandom.add(new BlockPos(-1, 0, 0));
		listPosRandom.add(new BlockPos(0, 0, 1));
		listPosRandom.add(new BlockPos(0, 0, -1));

		Collections.addAll(LEAVES_BLOCKS, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES);
	}

	@Override
	public void run() {
		while (true) {
			try {
				tickClientThreaded();
				Thread.sleep(400);
			} catch (Throwable throwable) {
                throwable.printStackTrace();
            }
		}
	}

	//run from client side _client_ thread
	public void tickClient() {
		if (!Minecraft.getInstance().isPaused()) {
			Minecraft client = Minecraft.getInstance();

			if (client.level != null && lastWorldDetected != client.level) {
				lastWorldDetected = client.level;
				reset();
			}

			boolean testTornadoTech = false;

			if (testTornadoTech) {
				if (playerManagerClient == null) {
					playerManagerClient = new TornadoManagerTodoRenameMe();
				}

				playerManagerClient.tick(client.level);
			}

			WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
			if (weatherMan == null) return;
			WindManager windMan = weatherMan.getWindManager();
			if (windMan == null) return;

			ClientTickHandler.getClientWeather();
			ClientWeatherProxy weather = ClientWeatherProxy.get();

			WeatherEventType curWeather = getWeatherState();
			if (curWeather != lastWeatherType) {
				//System.out.println("new weather changed to: " + curWeather);
				particleRateLerp = 0;
			}
			lastWeatherType = getWeatherState();
			if (particleRateLerp < particleRateLerpMax) {
				particleRateLerp++;
			}

			if (weather.hasWeather() || windMan.getWindSpeed() > 0) {
				tryParticleSpawning();
			}

			FORCE_ON_DEBUG_TESTING = false;

			if (weather.hasWeather() || !Weather.isLoveTropicsInstalled()) {
				ClientWeatherHelper.get().tick();
				tickEnvironmentalParticleSpawning();
				trySoundPlaying();
				tryWind(client.level);
			}

			tickMisc();

			getFogAdjuster().tickGame(weather);
			//tickHeatwave(weather);

			if (particleBehavior == null) {
				particleBehavior = new ParticleBehaviorSandstorm(null);
			}
			particleBehavior.tickUpdateList();

			if (client.player != null && client.level != null && client.level.getGameTime() % 10 == 0) {
				isPlayerOutside = WeatherUtilEntity.isEntityOutside(client.player);
			}
		}
	}

	//run from our newly created thread
	public void tickClientThreaded() {
		Minecraft client = Minecraft.getInstance();

		if (client != null && client.level != null && client.player != null) {
			profileSurroundings();
			if (ClientWeatherProxy.get().hasWeather()) {
				tryAmbientSounds();
			}
		}
	}

	public synchronized void trySoundPlaying()
    {
		try {
			Minecraft client = Minecraft.getInstance();
			if (lastTickAmbient < System.currentTimeMillis()) {
	    		lastTickAmbient = System.currentTimeMillis() + 500;

	        	Level worldRef = client.level;
	        	Player player = client.player;

	        	int size = 32;
	            int hsize = size / 2;
	            BlockPos cur = player.blockPosition();

				RandomSource rand = RandomSource.create();

	            //trim out distant sound locations, also tick last time played
	            for (int i = 0; i < soundLocations.size(); i++) {

	            	ChunkCoordinatesBlock cCor = soundLocations.get(i);

	            	if (Math.sqrt(cCor.distSqr(cur)) > size) {
	            		soundLocations.remove(i--);
	            		soundTimeLocations.remove(cCor);
	            		//System.out.println("trim out soundlocation");
	            	} else {

	                    Block block = getBlock(worldRef, cCor.getX(), cCor.getY(), cCor.getZ());//Block.blocksList[id];

	                    if (block == null || (block.defaultBlockState().getMaterial() != Material.WATER && block.defaultBlockState().getMaterial() != Material.LEAVES)) {
	                    	soundLocations.remove(i);
	                		soundTimeLocations.remove(cCor);
	                    } else {

		            		long lastPlayTime = 0;

							float soundMuffle = 0.6F;
							if (getWeatherState() == WeatherEventType.SANDSTORM || getWeatherState() == WeatherEventType.SNOWSTORM) {
								soundMuffle = 0.15F;
							}

		            		if (soundTimeLocations.containsKey(cCor)) {
		            			lastPlayTime = soundTimeLocations.get(cCor);
		            		}

		            		//System.out.println(Math.sqrt(cCor.getDistanceSquared(curX, curY, curZ)));
							if (lastPlayTime < System.currentTimeMillis()) {
								if (LEAVES_BLOCKS.contains(cCor.block)) {
									float windSpeed = WindReader.getWindSpeed(client.level);
									if (windSpeed > 0.2F) {
										soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
										//client.getSoundHandler().playSound(Weather.modID + ":wind_calmfade", cCor.getPosX(), cCor.getPosY(), cCor.getPosZ(), (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
										//client.world.playSound(cCor.getPosX(), cCor.getPosY(), cCor.getPosZ(), Weather.modID + ":env.wind_calmfade", (float)(windSpeed * 4F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
										client.level.playLocalSound(cCor, SoundRegistry.get("env.wind_calmfade"), SoundSource.AMBIENT, (float)(windSpeed * 2F) * soundMuffle, 0.70F + (rand.nextFloat() * 0.1F), false);
										//System.out.println("play leaves sound at: " + cCor.getPosX() + " - " + cCor.getPosY() + " - " + cCor.getPosZ() + " - windSpeed: " + windSpeed);
									} else {
										windSpeed = WindReader.getWindSpeed(client.level);
										//if (windSpeed > 0.3F) {
										if (client.level.random.nextInt(15) == 0) {
											soundTimeLocations.put(cCor, System.currentTimeMillis() + 12000 + rand.nextInt(50));
											//client.getSoundHandler().playSound(Weather.modID + ":wind_calmfade", cCor.getPosX(), cCor.getPosY(), cCor.getPosZ(), (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F));
											//client.world.playSound(cCor.getPosX(), cCor.getPosY(), cCor.getPosZ(), Weather.modID + ":env.wind_calmfade", (float)(windSpeed * 2F * ConfigMisc.volWindTreesScale), 0.70F + (rand.nextFloat() * 0.1F), false);
											client.level.playLocalSound(cCor, SoundRegistry.get("env.wind_calmfade"), SoundSource.AMBIENT, windSpeed * soundMuffle, 0.70F + (rand.nextFloat() * 0.1F), false);
										}
											//System.out.println("play leaves sound at: " + cCor.getPosX() + " - " + cCor.getPosY() + " - " + cCor.getPosZ() + " - windSpeed: " + windSpeed);
										//}
									}


								}
							}
						}
	            	}
	            }
			}

			float vanillaCutoff = 0.2F;
			float precipStrength = ClientWeatherHelper.get().getPrecipitationStrength(client.player);
			//if (precipStrength <= vanillaCutoff) {
				tickRainSound();
			//}
		} catch (Exception ex) {
    		System.out.println("Weather2: Error handling sound play queue: ");
    		ex.printStackTrace();
    	}
    }

	/**
	 * This method is meant to keep playing rain sound past the point where vanilla cuts off its own rain sounds
	 * edit: used to, now we just fully override
	 * edit2: it also now prevents rain sounds when its actually snowing
	 * Modified copy of LevelRenderer.tickRain
	 * @param p_109694_
	 */
	private int rainSoundTime;
	public void tickRainSound() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = Minecraft.getInstance().player;

		float precipitationStrength = ClientWeatherHelper.get().getPrecipitationStrength(player);
		if (!(precipitationStrength <= 0.0F) && !shouldSnowHere(player.level, player.level.getBiome(player.blockPosition()).get(), player.blockPosition())) {
			RandomSource random = RandomSource.create(player.getLevel().getGameTime() * 312987231L);
			LevelReader levelreader = minecraft.level;
			BlockPos blockpos = player.blockPosition();
			BlockPos blockpos1 = null;
			int i = (int) (100.0F * precipitationStrength * precipitationStrength) / (minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

			for (int j = 0; j < i; ++j) {
				int k = random.nextInt(21) - 10;
				int l = random.nextInt(21) - 10;
				BlockPos blockpos2 = levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l));
				Biome biome = levelreader.getBiome(blockpos2).get();
				if (blockpos2.getY() > levelreader.getMinBuildHeight() && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10 && biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(blockpos2)) {
					blockpos1 = blockpos2.below();
					if (minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
						break;
					}

					double d0 = random.nextDouble();
					double d1 = random.nextDouble();
					BlockState blockstate = levelreader.getBlockState(blockpos1);
					FluidState fluidstate = levelreader.getFluidState(blockpos1);
					VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, blockpos1);
					double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
					double d3 = (double)fluidstate.getHeight(levelreader, blockpos1);
					double d4 = Math.max(d2, d3);
					ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
					minecraft.level.addParticle(particleoptions, (double)blockpos1.getX() + d0, (double)blockpos1.getY() + d4, (double)blockpos1.getZ() + d1, 0.0D, 0.0D, 0.0D);
				}
			}

			if (blockpos1 != null && random.nextInt(3) < this.rainSoundTime++) {
				this.rainSoundTime = 0;
				if (blockpos1.getY() > blockpos.getY() + 1 && levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float)blockpos.getY())) {
					minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F + (0.4F * precipitationStrength), 0.5F, false);
				} else {
					minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F + (0.6F * precipitationStrength), 1.0F, false);
				}
			}

		}
	}

	//Threaded function
    @OnlyIn(Dist.CLIENT)
    public static void tryAmbientSounds()
    {
    	Minecraft client = Minecraft.getInstance();

    	Level worldRef = client.level;
    	Player player = client.player;

    	if (lastTickAmbientThreaded < System.currentTimeMillis()) {
    		lastTickAmbientThreaded = System.currentTimeMillis() + 500;

    		int size = 32;
            int hsize = size / 2;
            int curX = (int)player.getX();
            int curY = (int)player.getY();
            int curZ = (int)player.getZ();

            //soundLocations.clear();



    		for (int xx = curX - hsize; xx < curX + hsize; xx++)
            {
                for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
                {
                    for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
                    {
                        Block block = getBlock(worldRef, xx, yy, zz);

                        if (block != null) {
                        	if (((block.defaultBlockState().getMaterial() == Material.LEAVES))) {
                            	boolean proxFail = false;
								for (ChunkCoordinatesBlock soundLocation : soundLocations) {
									if (Math.sqrt(soundLocation.distSqr(new Vec3i(xx, yy, zz))) < 15) {
										proxFail = true;
										break;
									}
								}

                				if (!proxFail) {
                					soundLocations.add(new ChunkCoordinatesBlock(xx, yy, zz, block));
                				}
                            }
                        }
                    }
                }
            }
    	}
    }

	public void reset() {
		if (WeatherUtilParticle.fxLayers == null) {
			WeatherUtilParticle.getFXLayers();
		}

	}

	private static void tickHeatwave(ClientWeatherProxy weather) {
		Minecraft client = Minecraft.getInstance();

		/*if (weather.isHeatwave() || true) {
			heatwaveIntensityTarget = 0.7F;
		} else {
			heatwaveIntensityTarget = 0.0F;
		}

		heatwaveIntensity = CoroUtilMisc.adjVal(heatwaveIntensity, heatwaveIntensityTarget, 0.01F);*/

		/*if (fogAdjuster.getActiveIntensity() > 0) {
			if (fogAdjuster.getActiveIntensity() < 0.33F) {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_low, 5, client.player, 1F);
			} else if (fogAdjuster.getActiveIntensity() < 0.66F) {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_med, 4, client.player, 1F);
			} else {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_high, 3, client.player, 1F);
			}
		}*/
	}

	public static boolean tryPlayPlayerLockedSound(String[] sound, int arrIndex, Entity source, float vol)
	{
		RandomSource rand = RandomSource.create();

		if (WeatherUtilSound.soundTimer[arrIndex] <= System.currentTimeMillis())
		{
			String soundStr = sound[WeatherUtilSound.snd_rand[arrIndex]];

			WeatherUtilSound.playPlayerLockedSound(source.position(), new StringBuilder().append("streaming." + soundStr).toString(), vol, 1.0F);

			int length = WeatherUtilSound.soundToLength.get(soundStr);
			//-500L, for blending
			WeatherUtilSound.soundTimer[arrIndex] = System.currentTimeMillis() + length - 500L;
			WeatherUtilSound.snd_rand[arrIndex] = rand.nextInt(sound.length);
		}

		return false;
	}

	public void tickMisc() {

		/*ClientWeatherProxy weather = ClientWeatherProxy.get();
		if (weather.getPrecipitationType() == RainType.ACID) {
			if (LevelRenderer.RAIN_LOCATION != RAIN_TEXTURES_GREEN) {
				LevelRenderer.RAIN_LOCATION = RAIN_TEXTURES_GREEN;
			}
		} else {
			if (LevelRenderer.RAIN_LOCATION != RAIN_TEXTURES) {
				LevelRenderer.RAIN_LOCATION = RAIN_TEXTURES;
			}
		}*/

	}

	public void tickEnvironmentalParticleSpawning() {

		FORCE_ON_DEBUG_TESTING = false;

		Player entP = Minecraft.getInstance().player;
		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
		if (weatherMan == null) return;
		WindManager windMan = weatherMan.getWindManager();
		if (windMan == null) return;
		if (particleBehavior == null) return;

		ClientWeatherProxy weather = ClientWeatherProxy.get();

		//returns 0 to 1 now
		float curPrecipVal = weather.getRainAmount();

		if (FORCE_ON_DEBUG_TESTING) {
			curPrecipVal = 1F;
			//curPrecipVal = (float)((entP.getLevel().getGameTime() / 10) % 100) / 100F;
			//curPrecipVal = 0F;
		}

		//workaround until i clean up logic that is flickering the state between heavy rain and null
		if (Weather.isLoveTropicsInstalled()) {
			if (curPrecipVal < 0.0001F) {
				curPrecipVal = 0;
			}
		}
		float maxPrecip = 1F;


		BlockPos posPlayer = new BlockPos(Mth.floor(entP.getX()), entP.getY(), Mth.floor(entP.getZ()));
		Biome biome = entP.level.getBiome(posPlayer).get();
		lastBiomeIn = biome;

		Level world = entP.level;
		RandomSource rand = entP.level.random;

		//funnel.tickGame();


		//now absolute it for ez math
		//off cause ltminigames can give 1 tick under 0 worth of rain value change
		//curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
		if (Weather.isLoveTropicsInstalled()) {
			curPrecipVal = Math.min(maxPrecip, Math.max(0, curPrecipVal));
		} else {
			curPrecipVal = Math.min(maxPrecip, Math.abs(curPrecipVal));
		}

		float particleSettingsAmplifier = 1F;
		if (Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED) {
			particleSettingsAmplifier = 0.5F;
		} else if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL) {
			particleSettingsAmplifier = 0.2F;
		}

		/**
		 * we set the spawn amount for actual particles up to 0.5 intensity
		 * then after 0.5, we up the extra render amount
		 * ensures super low precip isnt patchy, and stops increasing rate of real particles at higher precip
		 */
		float curPrecipValMaxNoExtraRender = 0.3F;
		int extraRenderCountMax = 10;
		int extraRenderCount = 0;

		if (curPrecipVal > curPrecipValMaxNoExtraRender) {
			float precipValForExtraRenders = curPrecipVal - curPrecipValMaxNoExtraRender;
			float precipValExtraRenderRange = 1F - curPrecipValMaxNoExtraRender;
			extraRenderCount = Math.min((int) (extraRenderCountMax * (precipValForExtraRenders / precipValExtraRenderRange)), extraRenderCountMax);
		}

		//cap at amount before extra rendering starts
		float curPrecipCappedForSpawnNeed = Math.min(curPrecipVal, curPrecipValMaxNoExtraRender);

		int spawnCount;
		double spawnNeedBase = curPrecipCappedForSpawnNeed * ConfigParticle.Precipitation_Particle_effect_rate * particleSettingsAmplifier;
		int safetyCutout = 100;

		if (world.getGameTime() % 20 == 0) {
			StormObject stormObject = ClientTickHandler.weatherManager.getClosestStorm(entP.position(), ConfigMisc.sirenActivateDistance, StormObject.STATE_FORMING);
			if (stormObject != null && entP.position().distanceTo(stormObject.pos) < stormObject.getSize()) {
				isPlayerNearTornadoCached = true;
			} else {
				isPlayerNearTornadoCached = false;
			}
		}

		//adjusted to this way to make it work with serene seasons
		boolean canPrecip = weather.getPrecipitationType(biome) == PrecipitationType.NORMAL || weather.getPrecipitationType(biome) == PrecipitationType.SNOW;

		boolean isRain = canPrecip && shouldRainHere(world, biome, posPlayer);
		boolean isHail = weather.isHail();
		boolean isSnowstorm = weather.isSnowstorm();
		boolean isSandstorm = weather.isSandstorm();
		boolean isSnow = canPrecip && !weather.isSnowstorm() && !isHail && shouldSnowHere(world, biome, posPlayer);
		boolean isRain_WaterParticle = true;
		boolean isRain_GroundSplash = true;
		boolean isRain_DownfallSheet = true;
		if (isHail) {
			isRain_WaterParticle = false;
			isRain_GroundSplash = false;
			isRain_DownfallSheet = false;
		}

		//wind should be so rediculous that sheets of rain isnt gonna be happening in your face
		if (isPlayerNearTornadoCached) {
			isRain_DownfallSheet = false;
		}

		float particleStormIntensity = 0;
		if (isSandstorm) {
			WeatherObjectParticleStorm storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(entP.position(), WeatherObjectParticleStorm.StormType.SANDSTORM);
			if (storm != null) {
				particleStormIntensity = storm.getIntensity();
			}
		}
		if (isSnowstorm) {
			WeatherObjectParticleStorm storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(entP.position(), WeatherObjectParticleStorm.StormType.SNOWSTORM);
			if (storm != null) {
				particleStormIntensity = storm.getIntensity();
			}
		}

		//dev testing
		boolean devTest = false;
		if (devTest) {
			/*isRain = false;
			isSnow = false;
			isSnowstorm = false;
			isSandstorm = true;
			particleStormIntensity = 1F;
			*/

			//isRain = false;
			//isRain_DownfallSheet = false;


			if (entP.getLevel().getGameTime() % 40 == 0) {
				if (ConfigCoroUtil.useLoggingDebug) {
					System.out.printf("curPrecipVal: %.2f", curPrecipVal);
					System.out.println("");
				}
				CULog.dbg("spawnNeedBase: " + spawnNeedBase);
				CULog.dbg("extraRenderCount: " + extraRenderCount);
				CULog.dbg("particleStormIntensity: " + particleStormIntensity);
			}
		}

		//check rules same way vanilla texture precip does
		if (biome != null && (biome.getPrecipitation() != Biome.Precipitation.NONE)) {
			if (curPrecipVal > 0) {
				if (isRain) {
					spawnCount = 0;
					int spawnAreaSize = 30;

					int spawnNeed = (int) (spawnNeedBase * 300);
					if (entP.getLevel().getGameTime() % 40 == 0) {
						CULog.dbg("rain spawnNeed: " + spawnNeed);
					}

					if (isRain_WaterParticle && spawnNeed > 0) {

						for (int i = 0; i < safetyCutout; i++) {
							BlockPos pos = new BlockPos(
									entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
									entP.getY() - 5 + rand.nextInt(25),
									entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

							if (canPrecipitateAt(world, pos)) {
								ParticleTexExtraRender rain = new ParticleTexExtraRender((ClientLevel) entP.level,
										pos.getX(),
										pos.getY(),
										pos.getZ(),
										0D, 0D, 0D, ParticleRegistry.rain_white);
								particleBehavior.initParticleRain(rain, extraRenderCount);

								spawnCount++;
								if (spawnCount >= spawnNeed) {
									break;
								}
							}
						}
					}

					//TODO: make ground splash and downfall use spawnNeed var style design

					spawnAreaSize = 40;
					if (isRain_GroundSplash && curPrecipVal > 0.15) {
						for (int i = 0; i < 30F * curPrecipVal * PRECIPITATION_PARTICLE_EFFECT_RATE * 8F * particleSettingsAmplifier; i++) {
							BlockPos pos = new BlockPos(
									entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
									entP.getY() - 5 + rand.nextInt(15),
									entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));


							//get the block on the topmost ground
							pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).below();

							BlockState state = world.getBlockState(pos);
							double maxY = 0;
							double minY = 0;
							VoxelShape shape = state.getShape(world, pos);
							if (!shape.isEmpty()) {
								minY = shape.bounds().minY;
								maxY = shape.bounds().maxY;
							}

							if (pos.distSqr(entP.blockPosition()) > (spawnAreaSize / 2) * (spawnAreaSize / 2))
								continue;

							//block above topmost ground
							if (canPrecipitateAt(world, pos.above())) {

								//fix for splash spawning invisibly 1 block underwater
								if (world.getBlockState(pos).getMaterial() == Material.WATER) {
									pos = pos.offset(0,1,0);
								}

								ParticleTexFX rain = new ParticleTexFX((ClientLevel) entP.level,
										pos.getX() + rand.nextFloat(),
										pos.getY() + 0.01D + maxY,
										pos.getZ() + rand.nextFloat(),
										0D, 0D, 0D, ParticleRegistry.cloud256_6);
								particleBehavior.initParticleGroundSplash(rain);

								rain.spawnAsWeatherEffect();
							}
						}
					}

					spawnAreaSize = 30;
					//downfall - at just above 0.3 cause rainstorms lock at 0.3 but flicker a bit above and below
					if (isRain_DownfallSheet && curPrecipVal > 0.32) {

						int scanAheadRange = 0;
						//quick is outside check, prevent them spawning right near ground
						//and especially right above the roof so they have enough space to fade out
						//results in not seeing them through roofs
						if (WeatherUtilDim.canBlockSeeSky(world, entP.blockPosition())) {
							scanAheadRange = 3;
						} else {
							scanAheadRange = 10;
						}

						double closeDistCutoff = 10D;

						for (int i = 0; i < 2F * curPrecipVal * PRECIPITATION_PARTICLE_EFFECT_RATE * particleSettingsAmplifier * 0.5F; i++) {
							BlockPos pos = new BlockPos(
									entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
									entP.getY() + 5 + rand.nextInt(15),
									entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

							if (WeatherUtilEntity.getDistanceSqEntToPos(entP, pos) < closeDistCutoff * closeDistCutoff) continue;

							if (canPrecipitateAt(world, pos.above(-scanAheadRange))/*world.isRainingAt(pos)*/) {
								ParticleTexFX rain = new ParticleTexFX((ClientLevel) entP.level,
										pos.getX() + rand.nextFloat(),
										pos.getY() - 1 + 0.01D,
										pos.getZ() + rand.nextFloat(),
										0D, 0D, 0D, ParticleRegistry.downfall3);
								particleBehavior.initParticleRainDownfall(rain);

								rain.spawnAsWeatherEffect();
							}
						}
					}
				//snow
				} else if (isSnow) {
					spawnCount = 0;
					int spawnAreaSize = 50;
					int spawnNeed = (int) (spawnNeedBase * 80);
					if (entP.getLevel().getGameTime() % 40 == 0) {
						CULog.dbg("rain spawnNeed: " + spawnNeed);
					}

					if (spawnNeed > 0) {
						for (int i = 0; i < safetyCutout; i++) {
							BlockPos pos = new BlockPos(
									entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
									entP.getY() - 5 + rand.nextInt(25),
									entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

							if (canPrecipitateAt(world, pos)) {
								ParticleTexExtraRender snow = new ParticleTexExtraRender((ClientLevel) entP.level, pos.getX(), pos.getY(), pos.getZ(),
										0D, 0D, 0D, ParticleRegistry.snow);

								particleBehavior.initParticleSnow(snow, extraRenderCount);
								snow.spawnAsWeatherEffect();

								spawnCount++;
								if (spawnCount >= spawnNeed) {
									break;
								}
							}

						}
					}

				}

				int spawnAreaSize = 30;
				spawnCount = 0;
				int spawnNeed = (int) (spawnNeedBase * 80);

				if ((getWeatherState() == WeatherEventType.HAIL || isHail) && spawnNeed > 0) {
					for (int i = 0; i < safetyCutout / 4; i++) {
						BlockPos pos = new BlockPos(
								entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
								entP.getY() - 5 + rand.nextInt(25),
								entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

						if (canPrecipitateAt(world, pos)) {
							ParticleHail hail = new ParticleHail((ClientLevel) entP.level,
									pos.getX(),
									pos.getY(),
									pos.getZ(),
									0D, 0D, 0D, ParticleRegistry.hail);

							/*ParticleCube hail = new ParticleCube((ClientLevel) entP.level,
									pos.getX(),
									pos.getY(),
									pos.getZ(),
									0D, 0D, 0D, ParticleRegistry.hail);*/
							particleBehavior.initParticleHail(hail);

							hail.spawnAsWeatherEffect();

							spawnCount++;
							if (spawnCount >= spawnNeed) {
								break;
							}
						}
					}
				}
			}



			boolean groundFire = ClientWeatherProxy.get().isHeatwave();
			int spawnAreaSize = 40;

			if (groundFire) {
				for (int i = 0; i < 10F * PRECIPITATION_PARTICLE_EFFECT_RATE * 1F * particleSettingsAmplifier; i++) {
					BlockPos pos = new BlockPos(
							entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
							entP.getY() - 5 + rand.nextInt(15),
							entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));


					//get the block on the topmost ground
					pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).below();

					BlockState state = world.getBlockState(pos);
					double maxY = 0;
					double minY = 0;
					VoxelShape shape = state.getShape(world, pos);
					if (!shape.isEmpty()) {
						minY = shape.bounds().minY;
						maxY = shape.bounds().maxY;
					}

					if (pos.distSqr(entP.blockPosition()) > (spawnAreaSize / 2) * (spawnAreaSize / 2))
						continue;

					//block above topmost ground
					if (canPrecipitateAt(world, pos.above()) && world.getBlockState(pos).getMaterial() != Material.WATER) {

						world.addParticle(ParticleTypes.SMOKE, pos.getX() + rand.nextFloat(), pos.getY() + 0.01D + maxY, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
						world.addParticle(ParticleTypes.FLAME, pos.getX() + rand.nextFloat(), pos.getY() + 0.01D + maxY, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);

					}
				}
			}
		}

		//extra dust in the air
		{
			int spawnAreaSize = 25;
			int spawnNeed = (int) (particleSettingsAmplifier * 5 * windMan.getWindSpeed());
			spawnCount = 0;

			for (int i = 0; i < safetyCutout; i++) {
				if (windMan.getWindSpeed() >= 0.1F/* && rand.nextInt(1) == 0*/) {
					BlockPos pos = new BlockPos(
							entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
							entP.getY() - 5 + rand.nextInt(25),
							entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

					if (canPrecipitateAt(world, pos)) {
						ParticleTexExtraRender dust = new ParticleTexExtraRender((ClientLevel) entP.level,
								pos.getX(),
								pos.getY(),
								pos.getZ(),
								0D, 0D, 0D, ParticleRegistry.squareGrey);
						particleBehavior.initParticleDustAir(dust);

						dust.spawnAsWeatherEffect();

						spawnCount++;
						if (spawnCount >= spawnNeed) {
							break;
						}
					}
				}
			}
		}

		if (isSnowstorm) {
			spawnCount = 0;
			//less for snow, since it falls slower so more is on screen longer
			float particleSettingsAmplifierExtra = particleSettingsAmplifier;
			spawnNeedBase = ConfigParticle.Precipitation_Particle_effect_rate * particleSettingsAmplifierExtra;
			int spawnNeed = (int) Math.max(1, spawnNeedBase * 5);
			safetyCutout = 60;
			int spawnAreaSize = 20;
			double closeDistCutoff = 7D;
			float yetAnotherRateNumber = 120 * getParticleFadeInLerpForNewWeatherState();
			boolean farSpawn = Minecraft.getInstance().player.isSpectator() || !isPlayerOutside;
			if (farSpawn) {
				safetyCutout = 20;
				spawnAreaSize = 100;
				yetAnotherRateNumber = 40 * getParticleFadeInLerpForNewWeatherState();
			}

			if (particleStormIntensity >= 0.1F) {

				if (spawnNeed > 0) {

					if (getParticleFadeInLerpForNewWeatherState() > 0.5F) {
						particleSettingsAmplifierExtra *= (getParticleFadeInLerpForNewWeatherState() - 0.5F) * 2F;
					} else {
						particleSettingsAmplifierExtra = 0;
					}

					//snow
					for (int i = 0; i < Math.max(1, safetyCutout * particleSettingsAmplifierExtra)/*curPrecipVal * 20F * PRECIPITATION_PARTICLE_EFFECT_RATE*/; i++) {
						BlockPos pos = new BlockPos(
								entP.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
								entP.getY() - 5 + rand.nextInt(20),
								entP.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

						Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce();
						double upwindDistAdjust = -10D;
						windForce = windForce.multiply(upwindDistAdjust, upwindDistAdjust, upwindDistAdjust);
						pos = pos.offset(windForce.x, windForce.y, windForce.z);

						if (WeatherUtilEntity.getDistanceSqEntToPos(entP, pos) < closeDistCutoff * closeDistCutoff)
							continue;

						if (canPrecipitateAt(world, pos)) {
							ParticleTexExtraRender snow = new ParticleTexExtraRender((ClientLevel) entP.level, pos.getX(), pos.getY(), pos.getZ(),
									0D, 0D, 0D, ParticleRegistry.snow);

							particleBehavior.initParticleSnow(snow, (int)(10 * particleStormIntensity));
							snow.spawnAsWeatherEffect();
						}
					}

					double sandstormParticleRateDust = ConfigSand.Sandstorm_Particle_Dust_effect_rate;

					Minecraft client = Minecraft.getInstance();
					Player player = client.player;
					//float adjustAmountSmooth75 = (particleStormIntensity * 8F) - 7F;
					float adjustAmountSmooth75 = particleStormIntensity;

					//extra snow cloud dust
					for (int i = 0; i < (particleSettingsAmplifier * yetAnotherRateNumber * adjustAmountSmooth75 * sandstormParticleRateDust)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {

						BlockPos pos = new BlockPos(
								player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
								player.getY() - 2 + rand.nextInt(10),
								player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

						if (WeatherUtilEntity.getDistanceSqEntToPos(entP, pos) < closeDistCutoff * closeDistCutoff)
							continue;

						if (canPrecipitateAt(world, pos)) {
							TextureAtlasSprite sprite = ParticleRegistry.cloud256;

							ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
									pos.getY(),
									pos.getZ(),
									0, 0, 0, sprite);
							particleBehavior.initParticle(part);
							particleBehavior.initParticleSnowstormCloudDust(part);
							particleBehavior.particles.add(part);
							part.spawnAsWeatherEffect();
						}
					}
				}
			}

			//works for snowstorms too
			tickSandstormSound();
		}

		if (isSandstorm) {
			Minecraft client = Minecraft.getInstance();
			Player player = client.player;
			ClientTickHandler.getClientWeather();

			boolean farSpawn = Minecraft.getInstance().player.isSpectator() || !isPlayerOutside;

			//enhance the scene further with particles around player, check for sandstorm to account for pocket sand modifying adjustAmountTarget
			if (particleStormIntensity >= 0.1F) {

				rand = client.level.random;
				int spawnAreaSize = 60;

				double sandstormParticleRateDebris = ConfigSand.Sandstorm_Particle_Debris_effect_rate;
				double sandstormParticleRateDust = ConfigSand.Sandstorm_Particle_Dust_effect_rate;

				//float adjustAmountSmooth75 = (particleStormIntensity * 8F) - 7F;
				float adjustAmountSmooth75 = particleStormIntensity;

				if (farSpawn) {
					adjustAmountSmooth75 *= 0.3F;
				}

				/*if (Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED) {
					adjustAmountSmooth75 *= 0.5F;
				} else if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL) {
					adjustAmountSmooth75 *= 0.25F;
				}*/

				adjustAmountSmooth75 *= particleSettingsAmplifier;

				adjustAmountSmooth75 *= getParticleFadeInLerpForNewWeatherState();

				//extra dust
				for (int i = 0; i < ((float) 60 * adjustAmountSmooth75 * sandstormParticleRateDust)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {

					BlockPos pos = new BlockPos(
							player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
							player.getY() - 2 + rand.nextInt(10),
							player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

					if (canPrecipitateAt(world, pos)) {
						TextureAtlasSprite sprite = ParticleRegistry.cloud256;

						ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
								pos.getY(),
								pos.getZ(),
								0, 0, 0, sprite);
						particleBehavior.initParticle(part);
						particleBehavior.initParticleSandstormDust(part);
						particleBehavior.particles.add(part);
						part.spawnAsWeatherEffect();

					}
				}

				//tumbleweed
				for (int i = 0; i < ((float) 1 * adjustAmountSmooth75 * sandstormParticleRateDebris)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {
					BlockPos pos = new BlockPos(
							player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
							player.getY() - 2 + rand.nextInt(10),
							player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

					if (canPrecipitateAt(world, pos)) {
						TextureAtlasSprite sprite = ParticleRegistry.tumbleweed;

						ParticleCrossSection part = new ParticleCrossSection(world, pos.getX(),
								pos.getY(),
								pos.getZ(),
								0, 0, 0, sprite);
						particleBehavior.initParticle(part);
						particleBehavior.initParticleSandstormTumbleweed(part);
						particleBehavior.particles.add(part);
						part.spawnAsWeatherEffect();
					}
				}

				//debris
				for (int i = 0; i < ((float) 8 * adjustAmountSmooth75 * sandstormParticleRateDebris)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {
					BlockPos pos = new BlockPos(
							player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
							player.getY() - 2 + rand.nextInt(10),
							player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

					if (canPrecipitateAt(world, pos)) {
						TextureAtlasSprite sprite = null;
						int tex = rand.nextInt(3);
						if (tex == 0) {
							sprite = ParticleRegistry.debris_1;
						} else if (tex == 1) {
							sprite = ParticleRegistry.debris_2;
						} else if (tex == 2) {
							sprite = ParticleRegistry.debris_3;
						}

						ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
								pos.getY(),
								pos.getZ(),
								0, 0, 0, sprite);
						particleBehavior.initParticle(part);
						particleBehavior.initParticleSandstormDebris(part);
						particleBehavior.particles.add(part);
						part.spawnAsWeatherEffect();
					}
				}
			}

			tickSandstormSound();
		}
	}

	public static boolean canPrecipitateAt(Level world, BlockPos strikePosition)
	{
		return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, strikePosition).getY() <= strikePosition.getY();
	}

	public synchronized void tryParticleSpawning()
    {
    	try {
			for (Particle ent : spawnQueue) {
				if (ent != null/* && ent.world != null*/) {
					if (ent instanceof EntityRotFX) {
						((EntityRotFX) ent).spawnAsWeatherEffect();
					}
				}
			}
			for (Particle ent : spawnQueueNormal) {
				if (ent != null/* && ent.world != null*/) {
					Minecraft.getInstance().particleEngine.add(ent);
				}
			}
    	} catch (Exception ex) {
    		//CMEs occur, its fine
    		//ex.printStackTrace();
    	}

        spawnQueue.clear();
        spawnQueueNormal.clear();
    }

	public void profileSurroundings()
    {
        //tryClouds();

    	Minecraft client = Minecraft.getInstance();
    	ClientLevel worldRef = lastWorldDetected;
    	Player player = Minecraft.getInstance().player;
        WeatherManagerClient manager = ClientTickHandler.weatherManager;

        if (worldRef == null || player == null || manager == null || manager.getWindManager() == null || manager.getWindManager().getWindSpeed() == 0)
        {
        	try {
        		Thread.sleep(1000L);
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
            return;
        }

        if (threadLastWorldTickTime == worldRef.getGameTime())
        {
            return;
        }

        threadLastWorldTickTime = worldRef.getGameTime();

		RandomSource rand = RandomSource.create();

        //mining a tree causes leaves to fall
        int size = 40;
        int hsize = size / 2;
        int curX = (int)player.getX();
        int curY = (int)player.getY();
        int curZ = (int)player.getZ();

        float windStr = manager.getWindManager().getWindSpeed();

        //Wind requiring code goes below
        int spawnRateRandChanceOdds = (int)(30 / (windStr + 0.001));



        float lastBlockCount = lastTickFoundBlocks;

        float particleCreationRate = 0.7F;

        float maxScaleSample = 15000;
        if (lastBlockCount > maxScaleSample) lastBlockCount = maxScaleSample-1;
        float scaleRate = (maxScaleSample - lastBlockCount) / maxScaleSample;

        spawnRateRandChanceOdds = (int) ((spawnRateRandChanceOdds / (scaleRate + 0.001F)) / (particleCreationRate + 0.001F));

		float particleSettingsAmplifier = 1F;
		if (Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED) {
			particleSettingsAmplifier = 0.5F;
		} else if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL) {
			particleSettingsAmplifier = 0.2F;
		}

		//spawnRate *= (client.options.particles().get().getId()+1);
        spawnRateRandChanceOdds /= particleSettingsAmplifier;
        //since reducing threaded ticking to 200ms sleep, 1/4 rate, must decrease rand size
        spawnRateRandChanceOdds /= 2;

        //performance fix
        if (spawnRateRandChanceOdds < 40)
        {
            spawnRateRandChanceOdds = 40;
        }

        lastTickFoundBlocks = 0;

		double particleAmp = 1F;

		spawnRateRandChanceOdds = (int)((double)spawnRateRandChanceOdds / particleAmp);

		//Weather.dbg("spawnRate: " + spawnRate);

        for (int xx = curX - hsize; xx < curX + hsize; xx++)
        {
            for (int yy = curY - (hsize / 2); yy < curY + hsize; yy++)
            {
                for (int zz = curZ - hsize; zz < curZ + hsize; zz++)
                {
					Block block = getBlock(worldRef, xx, yy, zz);

					if (block != null) {
						//leaf particle spawning
						if ((block.defaultBlockState().getMaterial() == Material.LEAVES
								|| block.defaultBlockState().getMaterial() == Material.REPLACEABLE_PLANT ||
								block.defaultBlockState().getMaterial() == Material.PLANT)) {

							lastTickFoundBlocks++;

							if (rand.nextInt(spawnRateRandChanceOdds) == 0) {
								//bottom of tree check || air beside vine check

								//far out enough to avoid having the AABB already inside the block letting it phase through more
								//close in as much as we can to make it look like it came from the block
								double relAdj = 0.70D;

								BlockPos pos = getRandomWorkingPos(worldRef, new BlockPos(xx, yy, zz));
								double xRand = 0;
								double yRand = 0;
								double zRand = 0;

								if (pos != null) {

									//further limit the spawn position along the face side to prevent it clipping into perpendicular blocks
									float particleAABB = 0.1F;
									float particleAABBAndBuffer = particleAABB + 0.05F;
									float invert = 1F - (particleAABBAndBuffer * 2F);

									if (pos.getY() != 0) {
										xRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										zRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									} else if (pos.getX() != 0) {
										yRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										zRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									} else if (pos.getZ() != 0) {
										yRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
										xRand = particleAABBAndBuffer + (rand.nextDouble() - 0.5D) * invert;
									}

									EntityRotFX particle = new ParticleTexLeafColor(worldRef, xx, yy, zz, 0D, 0D, 0D, ParticleRegistry.leaf);
									particle.setPos(xx + 0.5D + (pos.getX() * relAdj) + xRand,
											yy + 0.5D + (pos.getY() * relAdj) + yRand,
											zz + 0.5D + (pos.getZ() * relAdj) + zRand);
									particle.setPrevPosX(particle.getPosX());
									particle.setPrevPosY(particle.getPosY());
									particle.setPrevPosZ(particle.getPosZ());
									particleBehavior.initParticleLeaf(particle, particleAABB);

									spawnQueue.add(particle);
								}
							}
						}
						if (windStr >= 0.1F) {
							if (block instanceof GrassBlock || block.defaultBlockState().getMaterial() == Material.DIRT || block.defaultBlockState().getMaterial() == Material.SAND || block.defaultBlockState().getMaterial() == Material.REPLACEABLE_PLANT ||
									block.defaultBlockState().getMaterial() == Material.PLANT) {

								lastTickFoundBlocks++;

								boolean spawnInside = false;
								boolean spawnAbove = false;
								boolean spawnAboveSnow = false;

								//boolean placeAbove = false;
								if (block instanceof GrassBlock || block.defaultBlockState().getMaterial() == Material.DIRT || block.defaultBlockState().getMaterial() == Material.SAND) {
									spawnAbove = true;
								}

								int oddsTo1 = spawnRateRandChanceOdds;
								if (block.defaultBlockState().getMaterial() == Material.REPLACEABLE_PLANT ||
										block.defaultBlockState().getMaterial() == Material.PLANT) {
									oddsTo1 = spawnRateRandChanceOdds / 3;
									spawnInside = true;
								}

								//oddsTo1 = (int) (oddsTo1 * (5F * windStr));

								if (rand.nextInt(oddsTo1) == 0) {
									BlockPos pos = new BlockPos(xx, yy, zz);
									BlockPos posAbove = new BlockPos(xx, yy + 1, zz);
									BlockState blockStateAbove = getBlockState(worldRef, posAbove);

									if (blockStateAbove != null && (blockStateAbove.isAir() || blockStateAbove.getBlock() instanceof SnowLayerBlock)) {

										spawnAboveSnow = blockStateAbove.getBlock() instanceof SnowLayerBlock;

										boolean test = false;
										if (!test) {
											ParticleTexLeafColor dust = new ParticleTexLeafColor(worldRef,
													pos.getX(),
													spawnAboveSnow ? posAbove.getY() : pos.getY(),
													pos.getZ(),
													0D, 0D, 0D, ParticleRegistry.squareGrey);
											if (spawnAbove) {
												if (spawnAboveSnow) {
													dust.setPosition(posAbove.getX(), posAbove.getY() + 0.4F, posAbove.getZ());
												} else {
													dust.setPosition(posAbove.getX(), posAbove.getY() + 0.1F, posAbove.getZ());
												}
											} else if (spawnInside) {
												dust.setPosition(pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat());
											}
											dust.setPrevPosX(dust.getPosX());
											dust.setPrevPosY(dust.getPosY());
											dust.setPrevPosZ(dust.getPosZ());
											particleBehavior.initParticleDustGround(dust, spawnInside, spawnAboveSnow);
											spawnQueue.add(dust);
										} else {

											DustEmitter dustEmitter = new DustEmitter(worldRef,
													pos.getX(),
													spawnAboveSnow ? posAbove.getY() : pos.getY(),
													pos.getZ(),
													0D, 0D, 0D);
											dustEmitter.setLifetime(20);
											spawnQueue.add(dustEmitter);
										}
									}
								}
							}
						}
					}
                }
            }
        }
    }

	/**
	 * Returns the successful relative position
	 *
	 * @param world
	 * @param posOrigin
	 * @return
	 */
    public static BlockPos getRandomWorkingPos(Level world, BlockPos posOrigin) {
		Collections.shuffle(listPosRandom);
		for (BlockPos posRel : listPosRandom) {
			Block blockCheck = getBlock(world, posOrigin.offset(posRel));

			if (blockCheck != null && CoroUtilBlock.isAir(blockCheck)) {
				return posRel;
			}
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
    public static void tryWind(Level world)
    {

		Minecraft client = Minecraft.getInstance();
		Player player = client.player;

        if (player == null)
        {
            return;
        }


        WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
        if (weatherMan == null) return;
        WindManager windMan = weatherMan.getWindManager();
        if (windMan == null) return;

        //Weather Effects

		//System.out.println("particles moved: " + handleCount);

        //WindManager windMan = ClientTickHandler.weatherManager.windMan;

		//Built in particles
        if (WeatherUtilParticle.fxLayers != null && windMan.getWindSpeed() >= 0.10) {
			for (Queue<Particle> type : WeatherUtilParticle.fxLayers.values()) {
				for (Particle particle : type) {
	                    if (particle instanceof SuspendedParticle) {
	                    	continue;
						}

	                    if ((WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(Mth.floor(CoroUtilEntOrParticle.getPosX(particle)), 0, Mth.floor(CoroUtilEntOrParticle.getPosZ(particle)))).getY() - 1 < (int)CoroUtilEntOrParticle.getPosY(particle) + 1) || (particle instanceof ParticleTexFX))
	                    {
	                        if ((particle instanceof FlameParticle))
	                        {
	                        	if (windMan.getWindSpeed() >= 0.20) {
									particle.age += 1;
								}
	                        }

	                        //rustle!
							windMan.applyWindForceNew(particle, 1F/20F, 0.5F);
	                    }

                }
            }
        }
    }

	//Thread safe functions

	@OnlyIn(Dist.CLIENT)
	private static Block getBlock(Level parWorld, BlockPos pos)
	{
		return getBlock(parWorld, pos.getX(), pos.getY(), pos.getZ());
	}

    @OnlyIn(Dist.CLIENT)
    private static Block getBlock(Level parWorld, int x, int y, int z)
    {
        try
        {
            if (!parWorld.hasChunkAt(new BlockPos(x, 0, z)))
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

	@OnlyIn(Dist.CLIENT)
	private static BlockState getBlockState(Level parWorld, BlockPos pos)
	{
		return getBlockState(parWorld, pos.getX(), pos.getY(), pos.getZ());
	}

	@OnlyIn(Dist.CLIENT)
	private static BlockState getBlockState(Level parWorld, int x, int y, int z)
	{
		try
		{
			if (!parWorld.hasChunkAt(new BlockPos(x, 0, z)))
			{
				return null;
			}

			return parWorld.getBlockState(new BlockPos(x, y, z));
		}
		catch (Exception ex)
		{
			return null;
		}
	}

    public static boolean isFogOverridding() {
		Minecraft client = Minecraft.getInstance();
		BlockState blockAtCamera = client.gameRenderer.getMainCamera().getBlockAtCamera();
		if (blockAtCamera.getMaterial().isLiquid()) return false;
    	//return heatwaveIntensity > 0;
		//return true;
		return fogAdjuster.isFogOverriding();
    }

    public static void renderTick(TickEvent.RenderTickEvent event) {
		Minecraft client = Minecraft.getInstance();
		ClientWeatherProxy weather = ClientWeatherProxy.get();
		if (client.level != null) {
			ClientWeatherHelper.get().controlVisuals(weather.getVanillaRainAmount() > 0);
		}
	}

	public static void tickSandstorm() {

		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		Level world = client.level;
		WindManager windMan = ClientTickHandler.weatherManager.getWindManager();
		ClientTickHandler.getClientWeather();

		boolean farSpawn = Minecraft.getInstance().player.isSpectator() || !isPlayerOutside;

		float adjustAmountSmooth = 0;

		WeatherObjectParticleStorm sandstorm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(player.position(), WeatherObjectParticleStorm.StormType.SANDSTORM);
		if (sandstorm != null) {
			adjustAmountSmooth = sandstorm.getIntensity();
			//CULog.dbg("sandstorm: " + adjustAmountSmooth);
		}

		//enhance the scene further with particles around player, check for sandstorm to account for pocket sand modifying adjustAmountTarget
		if (adjustAmountSmooth >= 0.25F/* && sandstorm != null*/) {

			//porting whee
			adjustAmountSmooth += 0.5F;

			Vec3 windForce = windMan.getWindForce();

			RandomSource rand = RandomSource.create();
			int spawnAreaSize = 80;

			double sandstormParticleRateDebris = ConfigSand.Sandstorm_Particle_Debris_effect_rate;
			double sandstormParticleRateDust = ConfigSand.Sandstorm_Particle_Dust_effect_rate;

			float adjustAmountSmooth75 = (adjustAmountSmooth * 8F) - 7F;

			if (farSpawn) {
				adjustAmountSmooth75 *= 0.3F;
			}

			if (Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED) {
				adjustAmountSmooth75 *= 0.5F;
			} else if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL) {
				adjustAmountSmooth75 *= 0.25F;
			}

			adjustAmountSmooth75 *= getParticleFadeInLerpForNewWeatherState();

			//extra dust
			for (int i = 0; i < ((float)60 * adjustAmountSmooth75 * sandstormParticleRateDust)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {

				BlockPos pos = new BlockPos(
						player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.getY() - 2 + rand.nextInt(10),
						player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));



				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = ParticleRegistry.cloud256;

					ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);
					particleBehavior.initParticleSandstormDust(part);
					particleBehavior.particles.add(part);
					part.spawnAsWeatherEffect();


				}
			}

			//tumbleweed
			for (int i = 0; i < ((float)1 * adjustAmountSmooth75 * sandstormParticleRateDebris)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {

				BlockPos pos = new BlockPos(
						player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.getY() - 2 + rand.nextInt(10),
						player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = ParticleRegistry.tumbleweed;

					ParticleCrossSection part = new ParticleCrossSection(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);
					particleBehavior.initParticleSandstormTumbleweed(part);
					particleBehavior.particles.add(part);
					part.spawnAsWeatherEffect();
				}
			}

			//debris
			for (int i = 0; i < ((float)8 * adjustAmountSmooth75 * sandstormParticleRateDebris)/*adjustAmountSmooth * 20F * ConfigMisc.Particle_Precipitation_effect_rate*/; i++) {
				BlockPos pos = new BlockPos(
						player.getX() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2),
						player.getY() - 2 + rand.nextInt(10),
						player.getZ() + rand.nextInt(spawnAreaSize) - (spawnAreaSize / 2));

				if (canPrecipitateAt(world, pos)) {
					TextureAtlasSprite sprite = null;
					int tex = rand.nextInt(3);
					if (tex == 0) {
						sprite = ParticleRegistry.debris_1;
					} else if (tex == 1) {
						sprite = ParticleRegistry.debris_2;
					} else if (tex == 2) {
						sprite = ParticleRegistry.debris_3;
					}

					ParticleSandstorm part = new ParticleSandstorm(world, pos.getX(),
							pos.getY(),
							pos.getZ(),
							0, 0, 0, sprite);
					particleBehavior.initParticle(part);
					particleBehavior.initParticleSandstormDebris(part);
					particleBehavior.particles.add(part);
					part.spawnAsWeatherEffect();
				}
			}
		}

		tickSandstormSound();
	}

	public static void tickSandstormSound() {
		/**
		 * dist + storm intensity
		 * 0F - 1F
		 *
		 * 0 = low
		 * 0.33 = med
		 * 0.66 = high
		 *
		 * static sound volume, keep at player
		 */

		Minecraft mc = Minecraft.getInstance();
		if (particleRateLerp > 0) {
			if (particleRateLerp < 0.66F) {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_low, 5, mc.player, 0.6F);
			} else if (particleRateLerp < 0.85F) {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_med, 4, mc.player, 0.6F);
			} else {
				tryPlayPlayerLockedSound(WeatherUtilSound.snd_sandstorm_high, 3, mc.player, 0.6F);
			}
		}
	}

	public static FogAdjuster getFogAdjuster() {
		if (fogAdjuster == null) {
			fogAdjuster = new FogAdjuster();
		}
		return fogAdjuster;
	}

	public static WeatherEventType getWeatherState() {
		ClientWeatherProxy clientWeather = ClientWeatherProxy.get();
		if (clientWeather.isSandstorm()) {
			return WeatherEventType.SANDSTORM;
		} else if (clientWeather.isSnowstorm()) {
			return WeatherEventType.SNOWSTORM;
		} else if (clientWeather.isHeatwave()) {
			return WeatherEventType.HEATWAVE;
		} else if (clientWeather.getRainAmount() > 0 && clientWeather.getPrecipitationType(lastBiomeIn) == PrecipitationType.ACID) {
			return WeatherEventType.ACID_RAIN;
		} else if (clientWeather.getRainAmount() > 0 && clientWeather.getPrecipitationType(lastBiomeIn) == PrecipitationType.NORMAL) {
			return WeatherEventType.HEAVY_RAIN;
		} else if (clientWeather.getRainAmount() > 0 && clientWeather.getPrecipitationType(lastBiomeIn) == PrecipitationType.HAIL) {
			return WeatherEventType.HAIL;
		} else {
			return null;
		}
	}

	public static float getParticleFadeInLerpForNewWeatherState() {
    	return (float)particleRateLerp / (float)particleRateLerpMax;
	}

	/**
	 * Needed for serene seasons compat
	 *
	 * @param level
	 * @param biome
	 * @param pos
	 * @return
	 */
	public static boolean shouldRainHere(Level level, Biome biome, BlockPos pos) {
		return CoroUtilCompatibility.getAdjustedTemperature(level, biome, pos) >= 0.15F;
	}

	/**
	 * Needed for serene seasons compat
	 *
	 * @param level
	 * @param biome
	 * @param pos
	 * @return
	 */
	public static boolean shouldSnowHere(Level level, Biome biome, BlockPos pos) {
		return !shouldRainHere(level, biome, pos);
	}
}
