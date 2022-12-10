package weather2.weathersystem;

import com.corosus.coroutil.util.CoroUtilEntity;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import weather2.*;
import weather2.config.ConfigMisc;
import weather2.config.ConfigSand;
import weather2.config.ConfigStorm;
import weather2.config.WeatherUtilConfig;
import weather2.datatypes.StormState;
import weather2.player.PlayerData;
import weather2.util.CachedNBTTagCompound;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;
import weather2.weathersystem.wind.WindManager;

import javax.annotation.Nullable;
import java.util.*;

public class WeatherManagerServer extends WeatherManager {
	private final ServerLevel world;

	public WeatherManagerServer(ServerLevel world) {
		super(world.dimension());
		this.world = world;
	}

	@Override
	public Level getWorld() {
		return world;
	}

	@Override
	public void tick() {
		super.tick();

		StormState snowstorm = ServerWeatherProxy.getSnowstormForEverywhere(world);
		if (snowstorm != null) {
			tickStormBlockBuildup(snowstorm, Blocks.SNOW);
		}
		StormState sandstorm = ServerWeatherProxy.getSandstormForEverywhere(world);
		if (sandstorm != null) {
			tickStormBlockBuildup(sandstorm, WeatherBlocks.blockSandLayer);
		}

		//tickStormBlockBuildup(new StormState(1, 2), WeatherBlocks.blockSandLayer);

		tickWeatherCoverage();

		if (world != null) {
			WindManager windMan = getWindManager();

			getStormObjects().stream()
					.filter(wo -> world.getGameTime() % wo.getUpdateRateForNetwork() == 0)
					.forEach(this::syncStormUpdate);

			/*getStormObjects().stream()
					.filter(wo -> world.getGameTime() % 2 == 0)
					.forEach(this::syncStormUpdate);*/

			//sync wind
			if (world.getGameTime() % 60 == 0) {
				syncWindUpdate(windMan);
			}

			//sim box work
			int rate = 20;
			if (world.getGameTime() % rate == 0) {
				for (int i = 0; i < getStormObjects().size(); i++) {
					WeatherObject so = getStormObjects().get(i);
					Player closestPlayer = world.getNearestPlayer(so.posGround.x, so.posGround.y, so.posGround.z, ConfigMisc.Misc_simBoxRadiusCutoff, EntitySelector.ENTITY_STILL_ALIVE);

					if (so instanceof StormObject && ((StormObject) so).isPet()) continue;

					//removed check is done in WeatherManagerBase
					if (closestPlayer == null || ConfigMisc.Aesthetic_Only_Mode) {
						so.ticksSinceNoNearPlayer += rate;
						//finally remove if nothing near for 30 seconds, gives multiplayer server a chance to get players in
						if (so.ticksSinceNoNearPlayer > 20 * 30 || ConfigMisc.Aesthetic_Only_Mode) {
							if (world.getPlayers(LivingEntity::isAlive).size() == 0) {
								Weather.dbg("removing distant storm: " + so.ID + ", running without players");
							} else {
								Weather.dbg("removing distant storm: " + so.ID);
							}

							removeStormObject(so.ID);
							syncStormRemove(so);
						}
					} else {
						so.ticksSinceNoNearPlayer = 0;
					}
				}

				/*if (world.getGameTime() % rate == 0) {
					if (ConfigMisc.Aesthetic_Only_Mode) {
						getStormObjects().stream().forEach(this::removeWeatherObjectAndSync);
					} else {

						//streams are probably not actually ideal for this situation since we need to run code on both states of player presence
						//and streams work best for just filtering out instead of splitting, and running .stream() 3 times is costly probably

						//split weather objects into list of ones near player and ones not
						Map<Boolean, List<WeatherObject>> playersNearWeatherObjects = getStormObjects()
								.stream()
								.collect(Collectors.partitioningBy(wo -> world.getNearestPlayer(wo.posGround.x, wo.posGround.y, wo.posGround.z, ConfigMisc.Misc_simBoxRadiusCutoff, EntitySelector.ENTITY_STILL_ALIVE) != null));

						//ones near
						playersNearWeatherObjects.get(true).stream()
								.forEach(wo -> wo.ticksSinceNoNearPlayer = 0);

						//ones not near
						playersNearWeatherObjects.get(false).stream()
								.peek(wo -> wo.ticksSinceNoNearPlayer += rate)
								.filter(wo -> wo.ticksSinceNoNearPlayer > 20 * 30)
								.forEach(this::removeWeatherObjectAndSync);
					}
				}*/



				Random rand = new Random();

				//test with high wind to maximize movement/recycling
				//cloud data:
				//0.6: 19-20/20
				//0.5: 17-18/20
				//0.4: 16-17/20
				//0.3: 16-18/20?
				//0.2: 12/20?

				/**
				 * max size of cloud sets = 300 radius
				 * sim box size = 1024 radius
				 *
				 * ~9 cloud sets in a player simbox
				 */

				//TEMP!!!
				/*windMan.startHighWindEvent();
				cloudIntensity = 0.3F;
				int countDbg = 0;
				int countDbg2 = 0;
				for (StormObject so : getStormObjectsByLayer(0)) {
					if (!so.isCloudlessStorm()) {
						countDbg++;
					} else {
						countDbg2++;
					}
				}
				System.out.println("cloud/cloudless/max count: " + countDbg + "/" + countDbg2 + "/" + (ConfigStorm.Storm_MaxPerPlayerPerLayer * world.playerEntities.size()));*/

				//cloud formation spawning - REFINE ME!
				boolean spawnClouds = true;
				if (spawnClouds && !Weather.isLoveTropicsInstalled() && !ConfigMisc.Aesthetic_Only_Mode && WeatherUtilConfig.shouldTickClouds(world.dimension().location().toString())) {
					for (int i = 0; i < world.players().size(); i++) {
						Player entP = world.players().get(i);

						//Weather.dbg("getStormObjects().size(): " + getStormObjects().size());

						//layer 0
						if (getStormObjects().size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.players().size()) {
							if (rand.nextInt(5) == 0) {
								//if (rand.nextFloat() <= cloudIntensity) {
								trySpawnStormCloudNearPlayerForLayer(entP, 0);
								//}
							}
						}

						//layer 1
						/*if (getStormObjectsByLayer(1).size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.players().size()) {
							if (ConfigMisc.Cloud_Layer1_Enable) {
								if (rand.nextInt(5) == 0) {
									//if (rand.nextFloat() <= cloudIntensity) {
									trySpawnStormCloudNearPlayerForLayer(entP, 1);
									//}
								}
							}
						}*/
					}
				}
			}

			//if dimension can have storms, tick sandstorm spawning every 10 seconds
			if (!Weather.isLoveTropicsInstalled() && !ConfigMisc.Aesthetic_Only_Mode && !ConfigSand.Storm_NoSandstorms && WeatherUtilConfig.listDimensionsStorms.contains(world.dimension().location().toString()) && world.getGameTime() % 200 == 0 && windMan.isHighWindEventActive()) {
				Random rand = new Random();
				if (ConfigSand.Sandstorm_OddsTo1 <= 0 || rand.nextInt(ConfigSand.Sandstorm_OddsTo1) == 0) {
					if (ConfigSand.Sandstorm_UseGlobalServerRate) {
						//get a random player to try and spawn for, will recycle another if it cant spawn
						if (world.players().size() > 0) {
							Player entP = world.players().get(rand.nextInt(world.players().size()));

							boolean sandstormMade = tryParticleStormForPlayer(entP, lastSandstormFormed);
							if (sandstormMade) {
								lastSandstormFormed = world.getGameTime();
							}
						}
					} else {
                     	world.players().stream().forEach(player -> {
							CompoundTag playerNBT = PlayerData.getPlayerNBT(CoroUtilEntity.getName(player));
							boolean sandstormMade = tryParticleStormForPlayer(player, playerNBT.getLong("lastSandstormTime"));
							if (sandstormMade) {
								playerNBT.putLong("lastSandstormTime", world.getGameTime());
							}
						});
						/*for (int i = 0; i < world.players().size(); i++) {
							Player entP = world.playerEntities.get(i);
							CompoundTag playerNBT = PlayerData.getPlayerNBT(CoroUtilEntity.getName(entP));
							boolean sandstormMade = trySandstormForPlayer(entP, playerNBT.getLong("lastSandstormTime"));
							if (sandstormMade) {
								playerNBT.putLong("lastSandstormTime", world.getGameTime());
							}
						}*/
					}
				}
			}
		}
	}

	public void tickStormBlockBuildup(StormState stormState, Block block) {
		Level world = getWorld();
		WindManager windMan = getWindManager();
		Random rand = world.random;

		float angle = windMan.getWindAngle(null);

		if (world.getGameTime() % stormState.getBuildupTickRate() == 0) {
			List<ChunkHolder> list = Lists.newArrayList(((ServerLevel)world).getChunkSource().chunkMap.getChunks());
			Collections.shuffle(list);
			list.forEach((p_241099_7_) -> {
				Optional<LevelChunk> optional = p_241099_7_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
				if (optional.isPresent()) {
					for (int i = 0; i < 10; i++) {
						BlockPos blockPos = new BlockPos((optional.get().getPos().x * 16) + rand.nextInt(16), 0, (optional.get().getPos().z * 16) + rand.nextInt(16));
						int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, blockPos).getY();
						Vec3 pos = new Vec3(blockPos.getX(), y, blockPos.getZ());
						WeatherUtilBlock.fillAgainstWallSmoothly(world, pos, angle, 15, 2, block, stormState.getMaxStackable());
					}
				}
			});
		}
	}

	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		//data.put("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompound("data").putBoolean("removed", true);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncWindUpdate(WindManager parManager) {
		//packets
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWindUpdate");
		data.put("data", parManager.nbtSyncForClient());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void tickWeatherCoverage() {
		ServerLevel world = (ServerLevel) this.getWorld();
		if (world != null) {
			if (!ConfigMisc.overcastMode) {
				if (ConfigMisc.lockServerWeatherMode != -1) {
					world.serverLevelData.setRaining(ConfigMisc.lockServerWeatherMode == 1);
					world.serverLevelData.setThundering(ConfigMisc.lockServerWeatherMode == 1);
				}
			}

			boolean test = world.serverLevelData.isRaining();

			if (ConfigStorm.preventServerThunderstorms) {
				world.serverLevelData.setThundering(false);
			}

			//if (ConfigMisc.overcastMode) {
			if (world.getGameTime() % 40 == 0) {
				isVanillaRainActiveOnServer = world.isRaining();
				isVanillaThunderActiveOnServer = world.isThundering();
				vanillaRainTimeOnServer = world.serverLevelData.getRainTime();
				syncWeatherVanilla();
			}
			//}

			if (world.getGameTime() % 400 == 0) {
				//Weather.dbg("for dim: " + world.provider.dimensionId + " - is server dimension raining?: " + world.isRaining() + " time: " + world.serverLevelData.getRainTime());
			}

			//tick partial cloud cover variation
			//windMan.startHighWindEvent();
			//windMan.stopLowWindEvent();
			//cloudIntensity = 0.3F;

			if (world.getGameTime() % 200 == 0) {
				Random rand = new Random();
				cloudIntensity += (float)((rand.nextDouble() * ConfigMisc.Cloud_Coverage_Random_Change_Amount) - (rand.nextDouble() * ConfigMisc.Cloud_Coverage_Random_Change_Amount));
				if (ConfigMisc.overcastMode && world.isRaining()) {
					cloudIntensity = 1;
				} else {
					if (cloudIntensity < ConfigMisc.Cloud_Coverage_Min_Percent / 100F) {
						cloudIntensity = (float) ConfigMisc.Cloud_Coverage_Min_Percent / 100F;
					} else if (cloudIntensity > ConfigMisc.Cloud_Coverage_Max_Percent / 100F) {
						cloudIntensity = (float) ConfigMisc.Cloud_Coverage_Max_Percent / 100F;
					}
				}
				if (world.getGameTime() % 2000 == 0) {
					//Weather.dbg("cloudIntensity FORCED MAX: " + cloudIntensity);
				}

				//force full cloudIntensity if server side raining
				//note: storms also revert to clouded storms for same condition

			}

			//temp lock to max for fps comparisons
			//cloudIntensity = 1F;
		}
	}

	public boolean tryParticleStormForPlayer(Player player, long lastSandstormTime) {
		boolean sandstormMade = false;
		if (lastSandstormTime == 0 || lastSandstormTime + ConfigSand.Sandstorm_TimeBetweenInTicks < player.level.getGameTime()) {
			sandstormMade = trySpawnParticleStormNearPos(player.level, player.position(), WeatherObjectParticleStorm.StormType.SANDSTORM);
		} else if (lastSandstormTime == 0 || lastSandstormTime + ConfigSand.Sandstorm_TimeBetweenInTicks < player.level.getGameTime()) {
			sandstormMade = trySpawnParticleStormNearPos(player.level, player.position(), WeatherObjectParticleStorm.StormType.SNOWSTORM);
		}
		return sandstormMade;
	}

	public boolean trySpawnParticleStormNearPos(Level world, Vec3 posIn, WeatherObjectParticleStorm.StormType type) {
		/**
		 * 1. Start upwind
		 * 2. Find random spot near there loaded and in desert
		 * 3. scan upwind and downwind, require a good stretch of sand for a storm
		 */

		int searchRadius = 64;

		double angle = wind.getWindAngleForClouds();
		//-1 for upwind
		double dirX = -Math.sin(Math.toRadians(angle));
		double dirZ = Math.cos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;

		Random rand = new Random();

		BlockPos foundPos = null;

		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {

			int x = Mth.floor(posIn.x + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = Mth.floor(posIn.z + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);

			BlockPos pos = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(x, 0, z));

			if (!world.isLoaded(pos)) continue;
			//Biome biomeIn = world.m_204166_ForCoordsBody(pos);
			Biome biomeIn = world.m_204166_(pos).m_203334_();

			if (WeatherObjectParticleStorm.canSpawnHere(world, pos, type, true)) {
				//found
				foundPos = pos;
				//break;

				//check left and right about 20 blocks, if its not still desert, force retry
				double dirXLeft = -Math.sin(Math.toRadians(angle-90));
				double dirZLeft = Math.cos(Math.toRadians(angle-90));
				double dirXRight = -Math.sin(Math.toRadians(angle+90));
				double dirZRight = Math.cos(Math.toRadians(angle+90));

				double distLeftRight = 20;
				BlockPos posLeft = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight)));
				if (!world.isLoaded(posLeft)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.m_204166_ForCoordsBody(posLeft))) continue;
				if (!WeatherObjectParticleStorm.canSpawnHere(world, posLeft, type, false)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.m_204166_(posLeft).m_203334_())) continue;

				BlockPos posRight = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight)));
				if (!world.isLoaded(posRight)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.m_204166_ForCoordsBody(posRight))) continue;
				if (!WeatherObjectParticleStorm.canSpawnHere(world, posRight, type, false)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.m_204166_(posRight).m_203334_())) continue;

				//go as far upwind as possible until no desert / unloaded area

				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;

				//while (world.isLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.m_204166_ForCoordsBody(posFind))) {
				while (world.isLoaded(posFind) && WeatherObjectParticleStorm.canSpawnHere(world, posFind, type, true)) {
					//tick last good
					posFindLastGoodUpwind = new BlockPos(posFind);

					//scan against wind (upwind)
					int xx = Mth.floor(posFind.getX() + (dirX * -1D * tickDist));
					int zz = Mth.floor(posFind.getZ() + (dirZ * -1D * tickDist));

					posFind = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(xx, 0, zz));
				}

				//reset for downwind scan
				posFind = new BlockPos(foundPos);

				//while (world.isLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.m_204166_ForCoordsBody(posFind))) {
				while (world.isLoaded(posFind) && WeatherObjectParticleStorm.canSpawnHere(world, posFind, type, true)) {
					//tick last good
					posFindLastGoodDownwind = new BlockPos(posFind);

					//scan with wind (downwind)
					int xx = Mth.floor(posFind.getX() + (dirX * 1D * tickDist));
					int zz = Mth.floor(posFind.getZ() + (dirZ * 1D * tickDist));

					posFind = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(xx, 0, zz));
				}

				int minDistanceOfDesertStretchNeeded = 20;
				double dist = posFindLastGoodUpwind.distSqr(posFindLastGoodDownwind);

				if (dist >= minDistanceOfDesertStretchNeeded * minDistanceOfDesertStretchNeeded) {

					WeatherObjectParticleStorm storm = new WeatherObjectParticleStorm(this);

					storm.setType(type);
					storm.initFirstTime();
					BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, posFindLastGoodUpwind)).above();
					storm.initStormSpawn(new Vec3(posSpawn.getX(), posSpawn.getY(), posSpawn.getZ()));
					addStormObject(storm);
					syncStormNew(storm);

					Weather.dbg("found decent spot and stretch for particle storm, stretch: " + dist + ", type: " + type);
					return true;
				}
			}
		}

		Weather.dbg("couldnt spawn particle storm");
		return false;

		/*if (foundPos != null) {

		} else {
			System.out.println("couldnt spawn sandstorm");
			return false;
		}*/
	}

	public void trySpawnStormCloudNearPlayerForLayer(Player entP, int layer) {

		Random rand = new Random();

		int tryCountMax = 10;
		int tryCountCur = 0;
		int spawnX = -1;
		int spawnZ = -1;
		Vec3 tryPos = null;
		StormObject soClose = null;
		Player playerClose = null;

		int closestToPlayer = 128;

		//use 256 or the cutoff val if its configured small
		float windOffsetDist = Math.min(256, ConfigMisc.Misc_simBoxRadiusCutoff / 4 * 3);
		double angle = wind.getWindAngleForClouds();
		double vecX = -Math.sin(Math.toRadians(angle)) * windOffsetDist;
		double vecZ = Math.cos(Math.toRadians(angle)) * windOffsetDist;

		while (tryCountCur++ == 0 || (tryCountCur < tryCountMax && (soClose != null || playerClose != null))) {
			spawnX = (int) (entP.getX() - vecX + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			spawnZ = (int) (entP.getZ() - vecZ + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			tryPos = new Vec3(spawnX, StormObject.layers.get(layer), spawnZ);
			soClose = getClosestStormAny(tryPos, ConfigMisc.Cloud_Formation_MinDistBetweenSpawned);
			playerClose = entP.level.getNearestPlayer(spawnX, 50, spawnZ, closestToPlayer, false);
		}

		if (soClose == null) {
			//Weather.dbg("spawning storm at: " + spawnX + " - " + spawnZ);

			StormObject so = new StormObject(this);
			so.initFirstTime();
			so.pos = tryPos;
			so.layer = layer;
			//make only layer 0 produce deadly storms
			if (layer != 0) {
				so.canBeDeadly = false;
			}
			so.spawnerUUID = entP.getStringUUID();
			if (rand.nextFloat() >= cloudIntensity) {
				so.setCloudlessStorm(true);
			}
			addStormObject(so);
			syncStormNew(so);
		} else {
			//Weather.dbg("couldnt find space to spawn cloud formation");
		}
	}

	public void playerJoinedWorldSyncFull(ServerPlayer entP) {
		Weather.dbg("Weather2: playerJoinedWorldSyncFull for dim: " + dimension);
		Level world = getWorld();
		if (world != null) {
			Weather.dbg("Weather2: playerJoinedWorldSyncFull, sending " + getStormObjects().size() + " weather objects to: " + entP.getName() + ", dim: " + dimension);
			//sync storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				syncStormNew(getStormObjects().get(i), entP);
			}
		}
	}

	//populate data with rain storms and deadly storms
	/*public void nbtStormsForIMC() {
		CompoundTag data = new CompoundTag();

		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);

			if (wo instanceof StormObject) {
				StormObject so = (StormObject) wo;
				if (so.levelCurIntensityStage > 0 || so.attrib_precipitation) {
					CompoundTag nbtStorm = so.nbtForIMC();

					data.put("storm_" + so.ID, nbtStorm);
				}
			}


		}

		if (!data.hasNoTags()) {
			FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.MODID, "weather.storms", data);
		}
	}*/

	public void syncLightningNew(Entity parEnt, boolean custom) {
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncLightningNew");
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("posX", Mth.floor(parEnt.getX()/* * 32.0D*/));
		nbt.putInt("posY", Mth.floor(parEnt.getY()/* * 32.0D*/));
		nbt.putInt("posZ", Mth.floor(parEnt.getZ()/* * 32.0D*/));
		nbt.putInt("entityID", parEnt.getId());
		nbt.putBoolean("custom", custom);
		data.put("data", nbt);
		/*Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.MODID, "weather.lightning", data);*/

		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncStormNew(WeatherObject parStorm) {
		syncStormNew(parStorm, null);
	}

	public void syncStormNew(WeatherObject parStorm, @Nullable ServerPlayer entP) {
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormNew");

		CachedNBTTagCompound cache = parStorm.getNbtCache();
		cache.setUpdateForced(true);
		parStorm.nbtSyncForClient();
		cache.setUpdateForced(false);
		data.put("data", cache.getNewNBT());

		if (entP == null) {
			//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getMinecartType().getId());
			WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
		} else {
			//Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), entP);
			//WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
			WeatherNetworking.HANDLER.sendTo(new PacketNBTFromServer(data), entP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}

	public void syncStormUpdate(WeatherObject parStorm) {
		//packets
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormUpdate");
		parStorm.getNbtCache().setNewNBT(new CompoundTag());
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("sending to client: " + parStorm.getNbtCache().getNewNBT().getAllKeys().size());
			if (parStorm instanceof StormObject) {
				System.out.println("Real: " + ((StormObject) parStorm).levelCurIntensityStage);
				if (parStorm.getNbtCache().getNewNBT().contains("levelCurIntensityStage")) {
					System.out.println(" vs " + parStorm.getNbtCache().getNewNBT().getInt("levelCurIntensityStage"));
				} else {
					System.out.println("no key!");
				}
			}

			Iterator iterator = parStorm.getNbtCache().getNewNBT().getAllKeys().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("sending    " + keys);
		}
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncWeatherVanilla() {

		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWeatherUpdate");
		data.putBoolean("isVanillaRainActiveOnServer", isVanillaRainActiveOnServer);
		data.putBoolean("isVanillaThunderActiveOnServer", isVanillaThunderActiveOnServer);
		data.putInt("vanillaRainTimeOnServer", vanillaRainTimeOnServer);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void removeWeatherObjectAndSync(WeatherObject parStorm) {
		//because stream()s
		if (parStorm == null) {
			return;
		}
		if (getWorld().players().size() == 0) {
			Weather.dbg("removing distant storm: " + parStorm.ID + ", running without players");
		} else {
			Weather.dbg("removing distant storm: " + parStorm.ID);
		}
		removeStormObject(parStorm.ID);
		syncStormRemove(parStorm);
	}

	public void clearAllStorms() {
		Iterator<WeatherObject> it = getStormObjects().iterator();
		while (it.hasNext()) {
			WeatherObject so = it.next();
			//removeStormObject(so.ID);
			so.remove();
			syncStormRemove(so);

		}
		getStormObjects().clear();
		lookupStormObjectsByID.clear();
	}
}
