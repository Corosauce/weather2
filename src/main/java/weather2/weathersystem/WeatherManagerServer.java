package weather2.weathersystem;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.config.ConfigSand;
import weather2.config.ConfigStorm;
import weather2.config.ConfigTornado;
import weather2.player.PlayerData;
import weather2.util.CachedNBTTagCompound;
import weather2.util.WeatherUtilBlock;
import weather2.util.WeatherUtilConfig;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;

public class WeatherManagerServer extends WeatherManagerBase {

	//storm logic, syncing to client
	
	public int syncRange = 256;

	public WeatherManagerServer(int parDim) {
		super(parDim);
	}
	
	@Override
	public World getWorld() {
		return DimensionManager.getWorld(dim);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		World world = getWorld();
		
		//wrap back to ID 0 just in case someone manages to hit 9223372036854775807 O_o
		if (WeatherObject.lastUsedStormID >= Long.MAX_VALUE) {
			WeatherObject.lastUsedStormID = 0;
		}
		
		if (world != null) {
			
			if (!ConfigMisc.overcastMode) {
				if (ConfigMisc.lockServerWeatherMode != -1) {
					world.getWorldInfo().setRaining(ConfigMisc.lockServerWeatherMode == 1);
			    	world.getWorldInfo().setThundering(ConfigMisc.lockServerWeatherMode == 1);
				}
			}
			
			if (ConfigStorm.preventServerThunderstorms) {
				world.getWorldInfo().setThundering(false);
			}
			
			//if (ConfigMisc.overcastMode) {
				if (world.getTotalWorldTime() % 400 == 0) {
					isVanillaRainActiveOnServer = getWorld().isRaining();
					syncWeatherVanilla();
				}
			//}
			
			if (world.getTotalWorldTime() % 400 == 0) {
				//Weather.dbg("for dim: " + world.provider.dimensionId + " - is server dimension raining?: " + world.isRaining() + " time: " + world.getWorldInfo().getRainTime());
			}
			
			//sync storms
			
			//System.out.println("getStormObjects().size(): " + getStormObjects().size());
			
			for (int i = 0; i < getStormObjects().size(); i++) {
				WeatherObject wo = getStormObjects().get(i);
				int updateRate = wo.getUpdateRateForNetwork();
				if (world.getTotalWorldTime() % updateRate == 0) {
					syncStormUpdate(wo);
				}
			}
			
			
			//sync volcanos
			if (world.getTotalWorldTime() % 40 == 0) {
				for (int i = 0; i < getVolcanoObjects().size(); i++) {
					syncVolcanoUpdate(getVolcanoObjects().get(i));
				}
			}
			
			//sync wind
			if (world.getTotalWorldTime() % 60 == 0) {
				syncWindUpdate(windMan);
			}
			
			//IMC
			if (world.getTotalWorldTime() % 60 == 0) {
				nbtStormsForIMC();
			}
			
			//temp
			//getVolcanoObjects().clear();
			
			//sim box work
			if (WeatherUtilConfig.listDimensionsClouds.contains(world.provider.getDimension()) && world.getTotalWorldTime() % 20 == 0) {
				for (int i = 0; i < getStormObjects().size(); i++) {
					WeatherObject so = getStormObjects().get(i);
					EntityPlayer closestPlayer = world.getClosestPlayer(so.posGround.xCoord, so.posGround.yCoord, so.posGround.zCoord, ConfigMisc.Misc_simBoxRadiusCutoff, false);
					
					//isDead check is done in WeatherManagerBase
					if (closestPlayer == null) {
						removeStormObject(so.ID);
						syncStormRemove(so);
					}
				}

				Random rand = new Random();
				
				//cloud formation spawning - REFINE ME!
				for (int i = 0; i < world.playerEntities.size(); i++) {
					EntityPlayer entP = (EntityPlayer) world.playerEntities.get(i);
					
					//Weather.dbg("getStormObjects().size(): " + getStormObjects().size());
					
					if (getStormObjectsByLayer(0).size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.playerEntities.size()) {
						if (rand.nextInt(5) == 0) {
							trySpawnStormCloudNearPlayerForLayer(entP, 0);
						}
					}
					if (getStormObjectsByLayer(1).size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.playerEntities.size()) {
						if (ConfigMisc.Cloud_Layer1_Enable) {
							if (rand.nextInt(5) == 0) {
								trySpawnStormCloudNearPlayerForLayer(entP, 1);
							}
						}
					}
				}
			}

			//if dimension can have storms, tick sandstorm spawning every 10 seconds
			if (!ConfigSand.Storm_NoSandstorms && WeatherUtilConfig.listDimensionsStorms.contains(world.provider.getDimension()) && world.getTotalWorldTime() % 200 == 0 && windMan.isHighWindEventActive()) {
				Random rand = new Random();
				if (ConfigSand.Sandstorm_OddsTo1 <= 0 || rand.nextInt(ConfigSand.Sandstorm_OddsTo1) == 0) {
					if (ConfigSand.Sandstorm_UseGlobalServerRate) {
						//get a random player to try and spawn for, will recycle another if it cant spawn
						EntityPlayer entP = world.playerEntities.get(rand.nextInt(world.playerEntities.size()));

						boolean sandstormMade = trySandstormForPlayer(entP, lastSandstormFormed);
						if (sandstormMade) {
							lastSandstormFormed = world.getTotalWorldTime();
						}
					} else {
						for (int i = 0; i < world.playerEntities.size(); i++) {
							EntityPlayer entP = world.playerEntities.get(i);
							NBTTagCompound playerNBT = PlayerData.getPlayerNBT(CoroUtilEntity.getName(entP));
							boolean sandstormMade = trySandstormForPlayer(entP, playerNBT.getLong("lastSandstormTime"));
							if (sandstormMade) {
								playerNBT.setLong("lastSandstormTime", world.getTotalWorldTime());
							}
						}
					}
				}
			}
		}
	}

	public boolean trySandstormForPlayer(EntityPlayer player, long lastSandstormTime) {
		boolean sandstormMade = false;
		if (lastSandstormTime == 0 || lastSandstormTime + ConfigSand.Sandstorm_TimeBetweenInTicks < player.getEntityWorld().getTotalWorldTime()) {
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
			
			int x = MathHelper.floor_double(posIn.xCoord + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = MathHelper.floor_double(posIn.zCoord + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);
			
			BlockPos pos = new BlockPos(x, 0, z);
			
			if (!world.isBlockLoaded(pos)) continue;
			Biome biomeIn = world.getBiomeForCoordsBody(pos);
			
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
				if (!WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posLeft))) continue;
				
				BlockPos posRight = new BlockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight));
				if (!world.isBlockLoaded(posRight)) continue;
				if (!WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posRight))) continue;
				
				//go as far upwind as possible until no desert / unloaded area
				
				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;
				
				while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posFind))) {
					//update last good
					posFindLastGoodUpwind = new BlockPos(posFind);
					
					//scan against wind (upwind)
					int xx = MathHelper.floor_double(posFind.getX() + (dirX * -1D * tickDist));
					int zz = MathHelper.floor_double(posFind.getZ() + (dirZ * -1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				//reset for downwind scan
				posFind = new BlockPos(foundPos);
				
				while (world.isBlockLoaded(posFind) && WeatherObjectSandstorm.isDesert(world.getBiomeForCoordsBody(posFind))) {
					//update last good
					posFindLastGoodDownwind = new BlockPos(posFind);
					
					//scan with wind (downwind)
					int xx = MathHelper.floor_double(posFind.getX() + (dirX * 1D * tickDist));
					int zz = MathHelper.floor_double(posFind.getZ() + (dirZ * 1D * tickDist));
					
					posFind = new BlockPos(xx, 0, zz);
				}
				
				int minDistanceOfDesertStretchNeeded = 200;
				double dist = posFindLastGoodUpwind.getDistance(posFindLastGoodDownwind.getX(), posFindLastGoodDownwind.getY(), posFindLastGoodDownwind.getZ());
				
				if (dist >= minDistanceOfDesertStretchNeeded) {
					
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
	
	public void trySpawnStormCloudNearPlayerForLayer(EntityPlayer entP, int layer) {
		
		Random rand = new Random();
		
		int tryCountMax = 10;
		int tryCountCur = 0;
		int spawnX = -1;
		int spawnZ = -1;
		Vec3 tryPos = null;
		StormObject soClose = null;
		EntityPlayer playerClose = null;
		
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
			playerClose = entP.worldObj.getClosestPlayer(spawnX, 50, spawnZ, closestToPlayer, false);
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
			so.userSpawnedFor = CoroUtilEntity.getName(entP);
			addStormObject(so);
			syncStormNew(so);
		} else {
			//Weather.dbg("couldnt find space to spawn cloud formation");
		}
	}
	
	public void playerJoinedWorldSyncFull(EntityPlayerMP entP) {
		Weather.dbg("Weather2: playerJoinedWorldSyncFull for dim: " + dim);
		World world = getWorld();
		if (world != null) {
			Weather.dbg("Weather2: playerJoinedWorldSyncFull, sending " + getStormObjects().size() + " weather objects to: " + entP.getName() + ", dim: " + dim);
			//sync storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				syncStormNew(getStormObjects().get(i), entP);
			}
						
			//sync volcanos
			for (int i = 0; i < getVolcanoObjects().size(); i++) {
				syncVolcanoNew(getVolcanoObjects().get(i), entP);
			}
		}
	}
	
	//populate data with rain storms and deadly storms
	public void nbtStormsForIMC() {
		NBTTagCompound data = new NBTTagCompound();
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			WeatherObject wo = getStormObjects().get(i);
			
			if (wo instanceof StormObject) {
				StormObject so = (StormObject) wo;
				if (so.levelCurIntensityStage > 0 || so.attrib_precipitation) {
					NBTTagCompound nbtStorm = so.nbtForIMC();
					
					data.setTag("storm_" + so.ID, nbtStorm);
				}
			}
			
			
		}
		
		if (!data.hasNoTags()) {
			FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.modID, "weather.storms", data);
		}
	}
	
	public void syncLightningNew(Entity parEnt, boolean custom) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncLightningNew");
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("posX", MathHelper.floor_double(parEnt.posX/* * 32.0D*/));
		nbt.setInteger("posY", MathHelper.floor_double(parEnt.posY/* * 32.0D*/));
		nbt.setInteger("posZ", MathHelper.floor_double(parEnt.posZ/* * 32.0D*/));
		nbt.setInteger("entityID", parEnt.getEntityId());
		nbt.setBoolean("custom", custom);
		data.setTag("data", nbt);
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
		FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.modID, "weather.lightning", data);
	}
	
	public void syncWindUpdate(WindManager parManager) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncWindUpdate");
		data.setTag("data", parManager.nbtSyncForClient());
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
		FMLInterModComms.sendRuntimeMessage(Weather.instance, Weather.modID, "weather.wind", data);
	}

	public void syncStormNew(WeatherObject parStorm) {
		syncStormNew(parStorm, null);
	}
	
	public void syncStormNew(WeatherObject parStorm, EntityPlayerMP entP) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncStormNew");

		CachedNBTTagCompound cache = parStorm.getNbtCache();
		cache.setUpdateForced(true);
		parStorm.nbtSyncForClient();
		cache.setUpdateForced(false);
		data.setTag("data", cache.getNewNBT());

		if (entP == null) {
			Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
		} else {
			Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), entP);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncStormUpdate(WeatherObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncStormUpdate");
		parStorm.getNbtCache().setNewNBT(new NBTTagCompound());
		parStorm.nbtSyncForClient();
		data.setTag("data", parStorm.getNbtCache().getNewNBT());
		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("sending to client: " + parStorm.getNbtCache().getNewNBT().getKeySet().size());
			if (parStorm instanceof StormObject) {
				System.out.println("Real: " + ((StormObject) parStorm).levelCurIntensityStage);
				if (parStorm.getNbtCache().getNewNBT().hasKey("levelCurIntensityStage")) {
					System.out.println(" vs " + parStorm.getNbtCache().getNewNBT().getInteger("levelCurIntensityStage"));
				} else {
					System.out.println("no key!");
				}
			}

			Iterator iterator = parStorm.getNbtCache().getNewNBT().getKeySet().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("sending    " + keys);
		}
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
	}
	
	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.setTag("data", parStorm.getNbtCache().getNewNBT());
		//data.setTag("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompoundTag("data").setBoolean("isDead", true);
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
	}
	
	public void syncVolcanoNew(VolcanoObject parStorm) {
		syncVolcanoNew(parStorm, null);
	}
	
	public void syncVolcanoNew(VolcanoObject parStorm, EntityPlayerMP entP) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncVolcanoNew");
		data.setTag("data", parStorm.nbtSyncForClient());
		
		if (entP == null) {
			Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
		} else {
			Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), entP);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncVolcanoUpdate(VolcanoObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncVolcanoUpdate");
		data.setTag("data", parStorm.nbtSyncForClient());
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
	}
	
	public void syncVolcanoRemove(VolcanoObject parStorm) {
		
	}
	
	public void syncWeatherVanilla() {
		
		NBTTagCompound data = new NBTTagCompound();
		data.setString("packetCommand", "WeatherData");
		data.setString("command", "syncWeatherUpdate");
		data.setBoolean("isVanillaRainActiveOnServer", isVanillaRainActiveOnServer);
		Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().provider.getDimension());
	}
	
}
