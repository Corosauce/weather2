package weather2.weathersystem;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import weather2.Weather;
import weather2.WeatherPacketHelper;
import weather2.config.ConfigMisc;
import weather2.entity.EntityLightningBolt;
import weather2.util.WeatherUtilConfig;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.wind.WindManager;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

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
		
		if (world != null) {
			
			if (!ConfigMisc.overcastMode) {
				if (ConfigMisc.lockServerWeatherMode != -1) {
					world.getWorldInfo().setRaining(ConfigMisc.lockServerWeatherMode == 1);
			    	world.getWorldInfo().setThundering(ConfigMisc.lockServerWeatherMode == 1);
				}
			}
			
			if (ConfigMisc.preventServerThunderstorms) {
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
			
			for (int i = 0; i < getStormObjects().size(); i++) {
				StormObject so = getStormObjects().get(i);
				if (world.getTotalWorldTime() % ((so.levelCurIntensityStage >= StormObject.STATE_HIGHWIND) ? 2 : 40) == 0) {
					syncStormUpdate(so);
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
			
			//temp
			getVolcanoObjects().clear();
			
			//sim box work
			if (WeatherUtilConfig.listDimensionsClouds.contains(world.provider.dimensionId) && world.getTotalWorldTime() % 20 == 0) {
				for (int i = 0; i < getStormObjects().size(); i++) {
					StormObject so = getStormObjects().get(i);
					EntityPlayer closestPlayer = world.getClosestPlayer(so.posGround.xCoord, so.posGround.yCoord, so.posGround.zCoord, ConfigMisc.Misc_simBoxRadiusCutoff);
					
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
					
					if (getStormObjectsByLayer(0).size() < ConfigMisc.Storm_MaxPerPlayerPerLayer * world.playerEntities.size()) {
						if (rand.nextInt(5) == 0) {
							trySpawnNearPlayerForLayer(entP, 0);
						}
					}
					if (getStormObjectsByLayer(1).size() < ConfigMisc.Storm_MaxPerPlayerPerLayer * world.playerEntities.size()) {
						if (ConfigMisc.Cloud_Layer1_Enable) {
							if (rand.nextInt(5) == 0) {
								//trySpawnNearPlayerForLayer(entP, 1);
							}
						}
					}
				}
			}
		}
	}
	
	public void trySpawnNearPlayerForLayer(EntityPlayer entP, int layer) {
		
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
			tryPos = Vec3.createVectorHelper(spawnX, StormObject.layers.get(layer), spawnZ);
			soClose = getClosestStormAny(tryPos, ConfigMisc.Cloud_Formation_MinDistBetweenSpawned);
			playerClose = entP.worldObj.getClosestPlayer(spawnX, 50, spawnZ, closestToPlayer);
		}
		
		if (soClose == null) {
			//Weather.dbg("spawning storm at: " + spawnX + " - " + spawnZ);
			
			StormObject so = new StormObject(this);
			so.initFirstTime();
			so.pos = tryPos;
			so.layer = layer;
			so.userSpawnedFor = entP.username;
			addStormObject(so);
			syncStormNew(so);
		} else {
			Weather.dbg("couldnt find space to spawn cloud formation");
		}
	}
	
	public void playerJoinedServerSyncFull(EntityPlayer entP) {
		World world = getWorld();
		if (world != null) {
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
	
	public void syncLightningNew(EntityLightningBolt parEnt) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncLightningNew");
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("posX", MathHelper.floor_double(parEnt.posX/* * 32.0D*/));
		nbt.setInteger("posY", MathHelper.floor_double(parEnt.posY/* * 32.0D*/));
		nbt.setInteger("posZ", MathHelper.floor_double(parEnt.posZ/* * 32.0D*/));
		nbt.setInteger("entityID", parEnt.entityId);
		data.setCompoundTag("data", nbt);
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
	public void syncWindUpdate(WindManager parManager) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncWindUpdate");
		data.setCompoundTag("data", parManager.nbtSyncForClient());
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}

	public void syncStormNew(StormObject parStorm) {
		syncStormNew(parStorm, null);
	}
	
	public void syncStormNew(StormObject parStorm, EntityPlayer entP) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncStormNew");
		data.setCompoundTag("data", parStorm.nbtSyncForClient());
		if (entP == null) {
			PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
		} else {
			PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), (Player)entP);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncStormUpdate(StormObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncStormUpdate");
		data.setCompoundTag("data", parStorm.nbtSyncForClient());
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
	public void syncStormRemove(StormObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncStormRemove");
		data.setCompoundTag("data", parStorm.nbtSyncForClient());
		//fix for client having broken states
		data.getCompoundTag("data").setBoolean("isDead", true);
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
	public void syncVolcanoNew(VolcanoObject parStorm) {
		syncVolcanoNew(parStorm, null);
	}
	
	public void syncVolcanoNew(VolcanoObject parStorm, EntityPlayer entP) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncVolcanoNew");
		data.setCompoundTag("data", parStorm.nbtSyncForClient());
		
		if (entP == null) {
			PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
		} else {
			PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), (Player)entP);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncVolcanoUpdate(VolcanoObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncVolcanoUpdate");
		data.setCompoundTag("data", parStorm.nbtSyncForClient());
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
	public void syncVolcanoRemove(VolcanoObject parStorm) {
		
	}
	
	public void syncWeatherVanilla() {
		
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncWeatherUpdate");
		data.setBoolean("isVanillaRainActiveOnServer", isVanillaRainActiveOnServer);
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
}
