package weather2.weathersystem;

import java.util.ArrayList;
import java.util.List;

import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.entity.EntityLightningBolt;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

@OnlyIn(Dist.CLIENT)
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
		return Minecraft.getInstance().world;
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	public void nbtSyncFromServer(CompoundNBT parNBT) {
		//check command
		//commands:
		//new storm
		//tick storm
		//remove storm
		
		//new volcano
		//tick volcano
		//remove volcano???
		
		String command = parNBT.getString("command");
		
		if (command.equals("syncStormNew")) {
			//Weather.dbg("creating client side storm");
			CompoundNBT stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");
			Weather.dbg("syncStormNew, ID: " + ID);
			
			EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.get(stormNBT.getInt("weatherObjectType"));
			
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
			CompoundNBT stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");
			
			WeatherObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				removeStormObject(ID);
			} else {
				Weather.dbg("error removing storm, cant find by ID: " + ID);
			}
		} else if (command.equals("syncStormUpdate")) {
			//Weather.dbg("updating client side storm");
			CompoundNBT stormNBT = parNBT.getCompound("data");
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
		/*} else if (command.equals("syncVolcanoNew")) {
			Weather.dbg("creating client side volcano");
			CompoundNBT stormNBT = parNBT.getCompound("data");
			//long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = new VolcanoObject(ClientTickHandler.weatherManager);
			so.nbtSyncFromServer(stormNBT);
			
			addVolcanoObject(so);
		} else if (command.equals("syncVolcanoRemove")) {
			Weather.dbg("removing client side volcano");
			CompoundNBT stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = lookupVolcanoes.get(ID);
			if (so != null) {
				removeVolcanoObject(ID);
			}
		} else if (command.equals("syncVolcanoUpdate")) {
			Weather.dbg("updating client side volcano");
			CompoundNBT stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");
			
			VolcanoObject so = lookupVolcanoes.get(ID);
			if (so != null) {
				so.nbtSyncFromServer(stormNBT);
			} else {
				Weather.dbg("error syncing volcano, cant find by ID: " + ID);
			}*/
		} else if (command.equals("syncWindUpdate")) {
			//Weather.dbg("updating client side wind");
			
			CompoundNBT nbt = parNBT.getCompound("data");
			
			windMan.nbtSyncFromServer(nbt);
		} else if (command.equals("syncLightningNew")) {
			//Weather.dbg("updating client side wind");
			
			CompoundNBT nbt = parNBT.getCompound("data");
			
			int posXS = nbt.getInt("posX");
			int posYS = nbt.getInt("posY");
			int posZS = nbt.getInt("posZ");
			
			boolean custom = nbt.getBoolean("custom");
			
			//Weather.dbg("uhhh " + parNBT);
			
			double posX = (double)posXS;// / 32D;
			double posY = (double)posYS;// / 32D;
			double posZ = (double)posZS;// / 32D;
			Entity ent = null;
			if (!custom) {
				ent = new EntityLightningBolt(getWorld(), posX, posY, posZ);
				
			} else {
				//TODO: 1.14 undoing custom bolt here as it was a test
				ent = new EntityLightningBolt(getWorld(), posX, posY, posZ);
				
			}
			ent.serverPosX = posXS;
			ent.serverPosY = posYS;
			ent.serverPosZ = posZS;
			ent.rotationYaw = 0.0F;
			ent.rotationPitch = 0.0F;
			ent.setEntityId(nbt.getInt("entityID"));
			//TODO: 1.14 rethink use of old addWeatherEffect since it only takes vanilla lightning
			// our own server/client global weather effect list?
			//((ClientWorld)getWorld()).addWeatherEffect(ent);
			//((ClientWorld)getWorld()).addLightning(ent);
		} else if (command.equals("syncWeatherUpdate")) {
			//Weather.dbg("updating client side wind");
			
			//NBTTagCompound nbt = parNBT.getCompound("data");
			isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			isVanillaThunderActiveOnServer = parNBT.getBoolean("isVanillaThunderActiveOnServer");
			vanillaRainTimeOnServer = parNBT.getInt("vanillaRainTimeOnServer");
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
