package weather2.weathersystem;

import weather2.ClientTickHandler;
import weather2.Weather;
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
		
		for (int i = 0; i < getStormObjects().size(); i++) {
			getStormObjects().get(i).tick();
		}
		
		for (int i = 0; i < getVolcanoObjects().size(); i++) {
			getVolcanoObjects().get(i).tick();
		}
	}
	
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		//check command
		//commands:
		//new storm
		//update storm
		//remove storm
		
		String command = parNBT.getString("command");
		
		if (command.equals("syncStormNew")) {
			NBTTagCompound stormNBT = parNBT.getCompoundTag("storm");
			//long ID = stormNBT.getLong("ID");
			
			StormObject so = new StormObject(ClientTickHandler.weatherManager);
			so.nbtSyncFromServer(stormNBT);
			
			addStormObject(so);
		} else if (command.equals("syncStormRemove")) {
			NBTTagCompound stormNBT = parNBT.getCompoundTag("storm");
			long ID = stormNBT.getLong("ID");
			
			StormObject so = lookupStormObjects.get(ID);
			if (so != null) {
				removeStormObject(ID);
			}
		} else if (command.equals("syncStormUpdate")) {
			NBTTagCompound stormNBT = parNBT.getCompoundTag("storm");
			long ID = stormNBT.getLong("ID");
			
			StormObject so = lookupStormObjects.get(ID);
			if (so != null) {
				so.nbtSyncFromServer(stormNBT);
			} else {
				Weather.dbg("error syncing storm, cant find by ID: " + ID);
			}
		}
	}
	
}
