package weather2.weathersystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ICloudRenderHandler;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.weathersystem.sky.CloudRenderHandler;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager {
	public WeatherManagerClient(RegistryKey<World> dimension) {
		super(dimension);
	}

	@Override
	public void tick() {
		super.tick();
		ICloudRenderHandler cloudRenderHandler = ((ClientWorld)getWorld()).getDimensionRenderInfo().getCloudRenderHandler();
		if (cloudRenderHandler == null) {
			((ClientWorld)getWorld()).getDimensionRenderInfo().setCloudRenderHandler(new CloudRenderHandler());
		}
	}

	@Override
	public World getWorld() {
		return Minecraft.getInstance().world;
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
			if (weatherObjectType == EnumWeatherObjectType.SAND) {
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

			getWindManager().nbtSyncFromServer(nbt);
		} else if (command.equals("syncWeatherUpdate")) {
			//Weather.dbg("updating client side wind");

			//NBTTagCompound nbt = parNBT.getCompound("data");

			//TODO: do we need this?
			/*isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			isVanillaThunderActiveOnServer = parNBT.getBoolean("isVanillaThunderActiveOnServer");
			vanillaRainTimeOnServer = parNBT.getInt("vanillaRainTimeOnServer");*/

			//windMan.nbtSyncFromServer(nbt);
		}
	}
}
