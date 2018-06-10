package weather2.weathersystem;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityLightningBoltCustom;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

@SideOnly(Side.CLIENT)
public class WeatherManagerClient extends WeatherManagerBase {

	//data for client, stormfronts synced from server
	
	//new for 1.10.2, replaces world.weatherEffects use
	public List<Particle> listWeatherEffectedParticles = new ArrayList<Particle>();

	public static StormObject closestStormCached;



	public WeatherManagerClient(int parDim) {
		super(parDim);
	}
	
	@Override
	public World getWorld() {
		return FMLClientHandler.instance().getClient().world;
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		//check command
		//commands:
		//new storm
		//update storm
		//remove storm
		
		//new volcano
		//update volcano
		//remove volcano???
		
		String command = parNBT.getString("command");
		
		if (command.equals("syncStormNew")) {
			//Weather.dbg("creating client side storm");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			Weather.dbg("syncStormNew, ID: " + ID);
			
			EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.get(stormNBT.getInteger("weatherObjectType"));
			
			WeatherObject wo = null;
			if (weatherObjectType == EnumWeatherObjectType.CLOUD) {
				wo = new StormObject(ClientTickHandler.weatherManager);
			} else if (weatherObjectType == EnumWeatherObjectType.SAND) {
				wo = new WeatherObjectSandstorm(ClientTickHandler.weatherManager);
			}
			
			//StormObject so
			wo.getNbtCache().setNewNBT(stormNBT);
			wo.nbtSyncFromServer();
			wo.getNbtCache().updateCacheFromNew();
			
			addStormObject(wo);
		} else if (command.equals("syncStormRemove")) {
			//Weather.dbg("removing client side storm");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			WeatherObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				removeStormObject(ID);
			} else {
				Weather.dbg("error removing storm, cant find by ID: " + ID);
			}
		} else if (command.equals("syncStormUpdate")) {
			//Weather.dbg("updating client side storm");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			WeatherObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				so.getNbtCache().setNewNBT(stormNBT);
				so.nbtSyncFromServer();
				so.getNbtCache().updateCacheFromNew();
			} else {
				Weather.dbg("error syncing storm, cant find by ID: " + ID + ", probably due to client resetting and waiting on full resync (this is ok)");
				//Weather.dbgStackTrace();
			}
		} else if (command.equals("syncVolcanoNew")) {
			Weather.dbg("creating client side volcano");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			//long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = new VolcanoObject(ClientTickHandler.weatherManager);
			so.nbtSyncFromServer(stormNBT);
			
			addVolcanoObject(so);
		} else if (command.equals("syncVolcanoRemove")) {
			Weather.dbg("removing client side volcano");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = lookupVolcanoes.get(ID);
			if (so != null) {
				removeVolcanoObject(ID);
			}
		} else if (command.equals("syncVolcanoUpdate")) {
			Weather.dbg("updating client side volcano");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = lookupVolcanoes.get(ID);
			if (so != null) {
				so.nbtSyncFromServer(stormNBT);
			} else {
				Weather.dbg("error syncing volcano, cant find by ID: " + ID);
			}
		} else if (command.equals("syncWindUpdate")) {
			//Weather.dbg("updating client side wind");
			
			NBTTagCompound nbt = parNBT.getCompoundTag("data");
			
			windMan.nbtSyncFromServer(nbt);
		} else if (command.equals("syncLightningNew")) {
			//Weather.dbg("updating client side wind");
			
			NBTTagCompound nbt = parNBT.getCompoundTag("data");
			
			int posXS = nbt.getInteger("posX");
			int posYS = nbt.getInteger("posY");
			int posZS = nbt.getInteger("posZ");
			
			boolean custom = nbt.getBoolean("custom");
			
			//Weather.dbg("uhhh " + parNBT);
			
			double posX = (double)posXS;// / 32D;
			double posY = (double)posYS;// / 32D;
			double posZ = (double)posZS;// / 32D;
			Entity ent = null;
			if (!custom) {
				ent = new EntityLightningBolt(getWorld(), posX, posY, posZ);
				
			} else {
				ent = new EntityLightningBoltCustom(getWorld(), posX, posY, posZ);
				
			}
			ent.serverPosX = posXS;
			ent.serverPosY = posYS;
			ent.serverPosZ = posZS;
			ent.rotationYaw = 0.0F;
			ent.rotationPitch = 0.0F;
			ent.setEntityId(nbt.getInteger("entityID"));
			getWorld().addWeatherEffect(ent);
		} else if (command.equals("syncWeatherUpdate")) {
			//Weather.dbg("updating client side wind");
			
			//NBTTagCompound nbt = parNBT.getCompoundTag("data");
			isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			isVanillaThunderActiveOnServer = parNBT.getBoolean("isVanillaThunderActiveOnServer");
			vanillaRainTimeOnServer = parNBT.getInteger("vanillaRainTimeOnServer");
			//windMan.nbtSyncFromServer(nbt);
		}
	}
	
	public void addWeatheredParticle(Particle particle) {
		listWeatherEffectedParticles.add(particle);

		/*if (listWeatherEffectedParticles.size() > 5000) {
			listWeatherEffectedParticles.clear();
		}*/
	}

	@Override
	public void reset() {
		super.reset();

		listWeatherEffectedParticles.clear();

		closestStormCached = null;
	}
}
