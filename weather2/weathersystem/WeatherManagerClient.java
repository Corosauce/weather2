package weather2.weathersystem;

import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.entity.EntityLightningBolt;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.storm.StormObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WeatherManagerClient extends WeatherManagerBase {

	//data for client, stormfronts synced from server

	public WeatherManagerClient(int parDim) {
		super(parDim);
	}
	
	@Override
	public World getWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
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
			//long ID = stormNBT.getLong("ID");
			
			StormObject so = new StormObject(ClientTickHandler.weatherManager);
			so.nbtSyncFromServer(stormNBT);
			
			addStormObject(so);
		} else if (command.equals("syncStormRemove")) {
			//Weather.dbg("removing client side storm");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			StormObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				removeStormObject(ID);
			} else {
				Weather.dbg("error removing storm, cant find by ID: " + ID);
			}
		} else if (command.equals("syncStormUpdate")) {
			//Weather.dbg("updating client side storm");
			NBTTagCompound stormNBT = parNBT.getCompoundTag("data");
			long ID = stormNBT.getLong("ID");
			
			StormObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				so.nbtSyncFromServer(stormNBT);
			} else {
				Weather.dbg("error syncing storm, cant find by ID: " + ID);
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
			
			//Weather.dbg("uhhh " + parNBT);
			
			double posX = (double)posXS;// / 32D;
			double posY = (double)posYS;// / 32D;
			double posZ = (double)posZS;// / 32D;
			
			EntityLightningBolt ent = new EntityLightningBolt(getWorld(), posX, posY, posZ);
			ent.serverPosX = posXS;
			ent.serverPosY = posYS;
			ent.serverPosZ = posZS;
			ent.rotationYaw = 0.0F;
			ent.rotationPitch = 0.0F;
			ent.entityId = nbt.getInteger("entityID");
			getWorld().addWeatherEffect(ent);
		} else if (command.equals("syncWeatherUpdate")) {
			//Weather.dbg("updating client side wind");
			
			//NBTTagCompound nbt = parNBT.getCompoundTag("data");
			isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			
			//windMan.nbtSyncFromServer(nbt);
		}
	}
	
}
