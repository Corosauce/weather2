package weather2.weathersystem;

import extendedrenderer.particle.entity.ParticleCube;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.client.SceneEnhancer;
import weather2.client.shaderstest.CloudManager;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager {

	public CloudManager cloudManager = new CloudManager();

	public WeatherManagerClient(ResourceKey<Level> dimension) {
		super(dimension);
	}

	@Override
	public void tick() {
		super.tick();
		if (!Weather.isLoveTropicsInstalled()) {
			boolean cloudTest = false;
			if (cloudTest) {
				cloudManager.tick();
			}
		}
	}

	@Override
	public Level getWorld() {
		return Minecraft.getInstance().level;
	}

	public void nbtSyncFromServer(CompoundTag parNBT) {
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
			CompoundTag stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");
			Weather.dbg("syncStormNew, ID: " + ID);

			EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.get(stormNBT.getInt("weatherObjectType"));

			WeatherObject wo = null;
			if (weatherObjectType == EnumWeatherObjectType.CLOUD) {
				wo = new StormObject(ClientTickHandler.weatherManager);
			} else if (weatherObjectType == EnumWeatherObjectType.SAND) {
				wo = new WeatherObjectParticleStorm(ClientTickHandler.weatherManager);
				((WeatherObjectParticleStorm)wo).setType(WeatherObjectParticleStorm.StormType.SANDSTORM);
			} else if (weatherObjectType == EnumWeatherObjectType.SNOW) {
				wo = new WeatherObjectParticleStorm(ClientTickHandler.weatherManager);
				((WeatherObjectParticleStorm)wo).setType(WeatherObjectParticleStorm.StormType.SNOWSTORM);
			}

			//StormObject so
			wo.getNbtCache().setNewNBT(stormNBT);
			wo.nbtSyncFromServer();
			wo.getNbtCache().updateCacheFromNew();

			addStormObject(wo);

		} else if (command.equals("syncStormRemove")) {
			//Weather.dbg("removing client side storm");
			CompoundTag stormNBT = parNBT.getCompound("data");
			long ID = stormNBT.getLong("ID");

			WeatherObject so = lookupStormObjectsByID.get(ID);
			if (so != null) {
				Weather.dbg("syncStormRemove, ID: " + ID);
				removeStormObject(ID);
			} else {
				Weather.dbg("error removing storm, cant find by ID: " + ID);
			}
		} else if (command.equals("syncStormUpdate")) {
			//Weather.dbg("updating client side storm");
			CompoundTag stormNBT = parNBT.getCompound("data");
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
		} else if (command.equals("syncWindUpdate")) {
			//Weather.dbg("updating client side wind");

			CompoundTag nbt = parNBT.getCompound("data");

			getWindManager().nbtSyncFromServer(nbt);
		} else if (command.equals("syncWeatherUpdate")) {
			//Weather.dbg("updating client side wind");

			//NBTTagCompound nbt = parNBT.getCompound("data");

			//TODO: do we need this?
			/*isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			isVanillaThunderActiveOnServer = parNBT.getBoolean("isVanillaThunderActiveOnServer");
			vanillaRainTimeOnServer = parNBT.getInt("vanillaRainTimeOnServer");*/

			//windMan.nbtSyncFromServer(nbt);
		} else if (command.equals("syncBlockParticleNew")) {
			//Weather.dbg("updating client side wind");

			CompoundTag nbt = parNBT.getCompound("data");

			int posX = nbt.getInt("posX");
			int posY = nbt.getInt("posY") + 1;
			int posZ = nbt.getInt("posZ");

			BlockState state = NbtUtils.readBlockState(nbt.getCompound("blockstate"));

			long ownerID = nbt.getLong("ownerID");

			//CULog.dbg("add cube at " + posX + " " + posY + " " + posZ);

			StormObject storm = getStormObjectByID(ownerID);
			if (storm != null) {

				ParticleCube hail = new ParticleCube(getWorld(),
						posX,
						posY,
						posZ,
						0D, 0D, 0D, state);
				SceneEnhancer.particleBehavior.initParticleCube(hail);
				storm.listParticlesDebris.add(hail);

				hail.spawnAsWeatherEffect();
			}
		}
	}
}
