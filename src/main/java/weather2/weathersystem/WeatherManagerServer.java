package weather2.weathersystem;

import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import weather2.PacketNBTFromServer;
import weather2.Weather;
import weather2.WeatherNetworking;
import weather2.config.ConfigMisc;
import weather2.config.ConfigSand;
import weather2.config.ConfigStorm;
import weather2.player.PlayerData;
import weather2.util.*;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.wind.WindManager;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WeatherManagerServer extends WeatherManagerBase {

	//storm logic, syncing to client
	
	public int syncRange = 256;

	public WeatherManagerServer(int parDim) {
		super(parDim);
	}
	
	@Override
	public World getWorld() {
		return WeatherUtil.getWorld(dim);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		World world = getWorld();
		
		//wrap back to ID 0 just in case someone manages to hit 9223372036854775807 O_o
		if (WeatherObject.lastUsedStormID >= Long.MAX_VALUE) {
			WeatherObject.lastUsedStormID = 0;
		}

		tickWeatherCoverage();
		
		if (world != null) {
			
			//sync storms
			
			//System.out.println("getStormObjects().size(): " + getStormObjects().size());

			/*for (int i = 0; i < getStormObjects().size(); i++) {
				WeatherObject wo = getStormObjects().get(i);
				int updateRate = wo.getUpdateRateForNetwork();
				if (world.getGameTime() % updateRate == 0) {
					syncStormUpdate(wo);
				}
			}*/

			getStormObjects().stream()
					.filter(wo -> world.getGameTime() % wo.getUpdateRateForNetwork() == 0)
					.forEach(this::syncStormUpdate);
			
			//sync wind
			if (world.getGameTime() % 60 == 0) {
				syncWindUpdate(windMan);
			}
			
			//IMC
			if (world.getGameTime() % 60 == 0) {
				//nbtStormsForIMC();
			}
			
			//temp
			//getVolcanoObjects().clear();
			
			//sim box work
			int rate = 20;
			if (world.getGameTime() % rate == 0) {
				for (int i = 0; i < getStormObjects().size(); i++) {
					WeatherObject so = getStormObjects().get(i);
					PlayerEntity closestPlayer = world.getClosestPlayer(so.posGround.xCoord, so.posGround.zCoord, ConfigMisc.Misc_simBoxRadiusCutoff);
					
					//removed check is done in WeatherManagerBase
					if (closestPlayer == null || ConfigMisc.Aesthetic_Only_Mode) {
						so.ticksSinceNoNearPlayer += rate;
						//finally remove if nothing near for 30 seconds, gives multiplayer server a chance to get players in
						if (so.ticksSinceNoNearPlayer > 20 * 30 || ConfigMisc.Aesthetic_Only_Mode) {
							if (world.getPlayers().size() == 0) {
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
								.collect(Collectors.partitioningBy(wo -> world.getClosestPlayer(wo.posGround.xCoord, wo.posGround.zCoord, ConfigMisc.Misc_simBoxRadiusCutoff) != null));

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
				if (!ConfigMisc.Aesthetic_Only_Mode && WeatherUtilConfig.listDimensionsClouds.contains(world.getDimension().getType().getId())) {
					for (int i = 0; i < world.getPlayers().size(); i++) {
						PlayerEntity entP = world.getPlayers().get(i);

						//Weather.dbg("getStormObjects().size(): " + getStormObjects().size());

						//layer 0
						if (getStormObjectsByLayer(0).size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.getPlayers().size()) {
							if (rand.nextInt(5) == 0) {
								//if (rand.nextFloat() <= cloudIntensity) {
								trySpawnStormCloudNearPlayerForLayer(entP, 0);
								//}
							}
						}

						//layer 1
						if (getStormObjectsByLayer(1).size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.getPlayers().size()) {
							if (ConfigMisc.Cloud_Layer1_Enable) {
								if (rand.nextInt(5) == 0) {
									//if (rand.nextFloat() <= cloudIntensity) {
									trySpawnStormCloudNearPlayerForLayer(entP, 1);
									//}
								}
							}
						}
					}
				}
			}

			//if dimension can have storms, tick sandstorm spawning every 10 seconds
			if (!ConfigMisc.Aesthetic_Only_Mode && !ConfigSand.Storm_NoSandstorms && WeatherUtilConfig.listDimensionsStorms.contains(world.getDimension().getType().getId()) && world.getGameTime() % 200 == 0 && windMan.isHighWindEventActive()) {
				Random rand = new Random();
				if (ConfigSand.Sandstorm_OddsTo1 <= 0 || rand.nextInt(ConfigSand.Sandstorm_OddsTo1) == 0) {
					if (ConfigSand.Sandstorm_UseGlobalServerRate) {
						//get a random player to try and spawn for, will recycle another if it cant spawn
						if (world.getPlayers().size() > 0) {
							PlayerEntity entP = world.getPlayers().get(rand.nextInt(world.getPlayers().size()));

							boolean sandstormMade = trySandstormForPlayer(entP, lastSandstormFormed);
							if (sandstormMade) {
								lastSandstormFormed = world.getGameTime();
							}
						}
					} else {
						world.getPlayers().stream().forEach(player -> {
							CompoundNBT playerNBT = PlayerData.getPlayerNBT(CoroUtilEntity.getName(player));
							boolean sandstormMade = trySandstormForPlayer(player, playerNBT.getLong("lastSandstormTime"));
							if (sandstormMade) {
								playerNBT.putLong("lastSandstormTime", world.getGameTime());
							}
						});
						/*for (int i = 0; i < world.getPlayers().size(); i++) {
							PlayerEntity entP = world.playerEntities.get(i);
							CompoundNBT playerNBT = PlayerData.getPlayerNBT(CoroUtilEntity.getName(entP));
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

	public void tickWeatherCoverage() {
		World world = this.getWorld();
		if (world != null) {
			if (!ConfigMisc.overcastMode) {
				if (ConfigMisc.lockServerWeatherMode != -1) {
					world.getWorldInfo().setRaining(ConfigMisc.lockServerWeatherMode == 1);
					world.getWorldInfo().setThundering(ConfigMisc.lockServerWeatherMode == 1);
				}
			}

			boolean test = world.getWorldInfo().isRaining();

			if (ConfigStorm.preventServerThunderstorms) {
				world.getWorldInfo().setThundering(false);
			}

			//if (ConfigMisc.overcastMode) {
			if (world.getGameTime() % 40 == 0) {
				isVanillaRainActiveOnServer = getWorld().isRaining();
				isVanillaThunderActiveOnServer = getWorld().isThundering();
				vanillaRainTimeOnServer = getWorld().getWorldInfo().getRainTime();
				syncWeatherVanilla();
			}
			//}

			if (world.getGameTime() % 400 == 0) {
				//Weather.dbg("for dim: " + world.provider.dimensionId + " - is server dimension raining?: " + world.isRaining() + " time: " + world.getWorldInfo().getRainTime());
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

	public boolean trySandstormForPlayer(PlayerEntity player, long lastSandstormTime) {
		boolean sandstormMade = false;
		if (lastSandstormTime == 0 || lastSandstormTime + ConfigSand.Sandstorm_TimeBetweenInTicks < player.getEntityWorld().getGameTime()) {
			sandstormMade = trySpawnSandstormNearPos(player.getEntityWorld(), new Vec3(player.getPositionVector()));
		}
		return sandstormMade;
	}
	
	public boolean trySpawnSandstormNearPos(World world, Vec3 posIn) {
		/**
		 * 1. Start upwind
		 * 2. Find random spot near there loaded and in desert
		 * 3. scan upwind and downwind, require a good stretch of sand for a storm
		 */
		
		int searchRadius = 512;
		
		double angle = windMan.getWindAngleForClouds();
		//-1 for upwind
		double dirX = -Math.sin(Math.toRadians(angle));
		double dirZ = Math.cos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;
		
		Random rand = new Random();
		
		BlockPos foundPos = null;
		
		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {
			
			int x = MathHelper.floor(posIn.xCoord + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = MathHelper.floor(posIn.zCoord + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);
			
			BlockPos pos = new BlockPos(x, 0, z);
			
			if (!world.isBlockLoaded(pos)) continue;
			//Biome biomeIn = world.getBiomeForCoordsBody(pos);
			Biome biomeIn = world.getBiome(pos);
			
			if (WeatherObjectSandstorm.isDesert(biomeIn, true)) {
				//found
				foundPos = pos;
				//break;
				
				//check left and right about 20 blocks, if its not still desert, force retry
				double dirXLeft = -Math.sin(Math.toRadians(angle-90));
				double dirZLeft = Math.cos(Math.toRadians(angle-90));
				double dirXRight = -Math.sin(Math.toRadians(angle+90));
				double dirZRight = Math.cos(Math.toRadians(angle+90));
				
				double distLeftRight = 20;
				BlockPos posLeft = new BlockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight));
				if (!world.isBlockLoaded(posLeft)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posLeft))) continue;
				if (!WeatherObjectSandstorm.isDesert(world.getBiome(posLeft))) continue;
				
				BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
				if (!world.isBlockLoaded(posRight)) continue;
				//if (!WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posRight))) continue;
				if (!WeatherObjectSandstorm.isDesert(world.getBiome(posRight))) continue;
				
				//go as far upwind as possible until no desert / unloaded area
				
				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;
				
				//while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posFind))) {
				while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiome(posFind))) {
					//tick last good
					posFindLastGoodUpwind = new BlockPos(posFind);
					
					//scan against wind (upwind)
					int xx = MathHelper.floor(posFind.getX() + (dirX * -1D * tickDist));
					int zz = MathHelper.floor(posFind.getZ() + (dirZ * -1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				//reset for downwind scan
				posFind = new BlockPos(foundPos);

				//while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posFind))) {
				while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiome(posFind))) {
					//tick last good
					posFindLastGoodDownwind = new BlockPos(posFind);
					
					//scan with wind (downwind)
					int xx = MathHelper.floor(posFind.getX() + (dirX * 1D * tickDist));
					int zz = MathHelper.floor(posFind.getZ() + (dirZ * 1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				int minDistanceOfDesertStretchNeeded = 200;
				double dist = posFindLastGoodUpwind.distanceSq(posFindLastGoodDownwind);
				
				if (dist >= minDistanceOfDesertStretchNeeded * minDistanceOfDesertStretchNeeded) {
					
					WeatherObjectSandstorm sandstorm = new WeatherObjectSandstorm(this);

					sandstorm.initFirstTime();
					BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, posFindLastGoodUpwind)).add(0, 1, 0);
					sandstorm.initSandstormSpawn(new Vec3(posSpawn));
					addStormObject(sandstorm);
					syncStormNew(sandstorm);

					Weather.dbg("found decent spot and stretch for sandstorm, stretch: " + dist);
					return true;
				}
				
				
			}
		}

		Weather.dbg("couldnt spawn sandstorm");
		return false;
		
		/*if (foundPos != null) {
			
		} else {
			System.out.println("couldnt spawn sandstorm");
			return false;
		}*/
	}
	
	public void trySpawnStormCloudNearPlayerForLayer(PlayerEntity entP, int layer) {
		
		Random rand = new Random();
		
		int tryCountMax = 10;
		int tryCountCur = 0;
		int spawnX = -1;
		int spawnZ = -1;
		Vec3 tryPos = null;
		StormObject soClose = null;
		PlayerEntity playerClose = null;
		
		int closestToPlayer = 128;
		
		//use 256 or the cutoff val if its configured small
		float windOffsetDist = Math.min(256, ConfigMisc.Misc_simBoxRadiusCutoff / 4 * 3);
		double angle = windMan.getWindAngleForClouds();
		double vecX = -Math.sin(Math.toRadians(angle)) * windOffsetDist;
		double vecZ = Math.cos(Math.toRadians(angle)) * windOffsetDist;
		
		while (tryCountCur++ == 0 || (tryCountCur < tryCountMax && (soClose != null || playerClose != null))) {
			spawnX = (int) (entP.posX - vecX + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			spawnZ = (int) (entP.posZ - vecZ + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			tryPos = new Vec3(spawnX, StormObject.layers.get(layer), spawnZ);
			soClose = getClosestStormAny(tryPos, ConfigMisc.Cloud_Formation_MinDistBetweenSpawned);
			playerClose = entP.world.getClosestPlayer(spawnX, 50, spawnZ, closestToPlayer, false);
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
			so.spawnerUUID = entP.getCachedUniqueIdString();
			if (rand.nextFloat() >= cloudIntensity) {
				so.setCloudlessStorm(true);
			}
			addStormObject(so);
			syncStormNew(so);
		} else {
			//Weather.dbg("couldnt find space to spawn cloud formation");
		}
	}
	
	public void playerJoinedWorldSyncFull(ServerPlayerEntity entP) {
		Weather.dbg("Weather2: playerJoinedWorldSyncFull for dim: " + dim);
		World world = getWorld();
		if (world != null) {
			Weather.dbg("Weather2: playerJoinedWorldSyncFull, sending " + getStormObjects().size() + " weather objects to: " + entP.getName() + ", dim: " + dim);
			//sync storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				syncStormNew(getStormObjects().get(i), entP);
			}
		}
	}
	
	//populate data with rain storms and deadly storms
	/*public void nbtStormsForIMC() {
		CompoundNBT data = new CompoundNBT();
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);
			
			if (wo instanceof StormObject) {
				StormObject so = (StormObject) wo;
				if (so.levelCurIntensityStage > 0 || so.attrib_precipitation) {
					CompoundNBT nbtStorm = so.nbtForIMC();
					
					data.put("storm_" + so.ID, nbtStorm);
				}
			}
			
			
		}
		
		if (!data.hasNoTags()) {
			FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.MODID, "weather.storms", data);
		}
	}*/
	
	public void syncLightningNew(Entity parEnt, boolean custom) {
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncLightningNew");
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("posX", MathHelper.floor(parEnt.posX/* * 32.0D*/));
		nbt.putInt("posY", MathHelper.floor(parEnt.posY/* * 32.0D*/));
		nbt.putInt("posZ", MathHelper.floor(parEnt.posZ/* * 32.0D*/));
		nbt.putInt("entityID", parEnt.getEntityId());
		nbt.putBoolean("custom", custom);
		data.put("data", nbt);
		/*Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.MODID, "weather.lightning", data);*/

		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
	}
	
	public void syncWindUpdate(WindManager parManager) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWindUpdate");
		data.put("data", parManager.nbtSyncForClient());
		/*Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.MODID, "weather.wind", data);*/

		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
	}

	public void syncStormNew(WeatherObject parStorm) {
		syncStormNew(parStorm, null);
	}
	
	public void syncStormNew(WeatherObject parStorm, @Nullable ServerPlayerEntity entP) {
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormNew");

		CachedNBTTagCompound cache = parStorm.getNbtCache();
		cache.setUpdateForced(true);
		parStorm.nbtSyncForClient();
		cache.setUpdateForced(false);
		data.put("data", cache.getNewNBT());

		if (entP == null) {
			//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getMinecartType().getId());
			WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
		} else {
			//Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), entP);
			//WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
			WeatherNetworking.HANDLER.sendTo(new PacketNBTFromServer(data), entP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncStormUpdate(WeatherObject parStorm) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormUpdate");
		parStorm.getNbtCache().setNewNBT(new CompoundNBT());
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("sending to client: " + parStorm.getNbtCache().getNewNBT().keySet().size());
			if (parStorm instanceof StormObject) {
				System.out.println("Real: " + ((StormObject) parStorm).levelCurIntensityStage);
				if (parStorm.getNbtCache().getNewNBT().contains("levelCurIntensityStage")) {
					System.out.println(" vs " + parStorm.getNbtCache().getNewNBT().getInt("levelCurIntensityStage"));
				} else {
					System.out.println("no key!");
				}
			}

			Iterator iterator = parStorm.getNbtCache().getNewNBT().keySet().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("sending    " + keys);
		}
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
	}
	
	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		//data.put("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompound("data").putBoolean("removed", true);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
	}
	
	public void syncWeatherVanilla() {
		
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWeatherUpdate");
		data.putBoolean("isVanillaRainActiveOnServer", isVanillaRainActiveOnServer);
		data.putBoolean("isVanillaThunderActiveOnServer", isVanillaThunderActiveOnServer);
		data.putInt("vanillaRainTimeOnServer", vanillaRainTimeOnServer);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
	}

	public void removeWeatherObjectAndSync(WeatherObject parStorm) {
		//because stream()s
		if (parStorm == null) {
			return;
		}
		if (getWorld().getPlayers().size() == 0) {
			Weather.dbg("removing distant storm: " + parStorm.ID + ", running without players");
		} else {
			Weather.dbg("removing distant storm: " + parStorm.ID);
		}
		removeStormObject(parStorm.ID);
		syncStormRemove(parStorm);
	}
	
}
