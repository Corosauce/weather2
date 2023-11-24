package weather2.weathersystem;

import com.corosus.coroutil.util.CoroUtilMisc;
import extendedrenderer.particle.entity.ParticleCube;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
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
import weather2.config.ConfigParticle;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager {

	//public CloudManager cloudManager = new CloudManager();

	public WeatherManagerClient(ResourceKey<Level> dimension) {
		super(dimension);
	}

	@Override
	public void tick() {
		super.tick();
		if (!Weather.isLoveTropicsInstalled()) {
			//TODO: disabled for 1.20, might need to go mixin from here
			/*ICloudRenderHandler cloudRenderHandler = ((ClientLevel) getWorld()).effects().getCloudRenderHandler();
			if (cloudRenderHandler == null) {
				((ClientLevel) getWorld()).effects().setCloudRenderHandler(new CloudRenderHandler());
			}
			IWeatherParticleRenderHandler handler = ((ClientLevel) getWorld()).effects().getWeatherParticleRenderHandler();
			if (handler == null) {
				((ClientLevel) getWorld()).effects().setWeatherParticleRenderHandler(new WeatherParticleRenderHandler());
			}*/

			boolean cloudTest = false;
			if (cloudTest) {
				//cloudManager.tick();
			}
		}
		//((ClientLevel)getWorld()).effects().setCloudRenderHandler(null);

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

			isVanillaRainActiveOnServer = parNBT.getBoolean("isVanillaRainActiveOnServer");
			isVanillaThunderActiveOnServer = parNBT.getBoolean("isVanillaThunderActiveOnServer");
			vanillaRainTimeOnServer = parNBT.getInt("vanillaRainTimeOnServer");
			vanillaRainAmountOnServer = parNBT.getFloat("vanillaRainAmountOnServer");

			//windMan.nbtSyncFromServer(nbt);
		} else if (command.equals("syncBlockParticleNew")) {
			//Weather.dbg("updating client side wind");

			CompoundTag nbt = parNBT.getCompound("data");

			int posX = nbt.getInt("posX");
			int posY = nbt.getInt("posY") + 1;
			int posZ = nbt.getInt("posZ");

			BlockState state = NbtUtils.readBlockState(getWorld().holderLookup(Registries.BLOCK), nbt.getCompound("blockstate"));

			long ownerID = nbt.getLong("ownerID");

			//CULog.dbg("add cube at " + posX + " " + posY + " " + posZ);

			StormObject storm = getStormObjectByID(ownerID);
			if (storm != null) {

				if (ConfigParticle.Particle_effect_rate > 0) {
					int extraCubes = (int) (1 + ConfigParticle.Particle_Tornado_extraParticleCubes * ConfigParticle.Particle_effect_rate);
					Random rand = CoroUtilMisc.random();
					float randRange = 3;
					for (int i = 0; i < extraCubes; i++) {
						ParticleCube hail = new ParticleCube(getWorld(),
								posX + (rand.nextFloat() - rand.nextFloat()) * randRange,
								posY + (rand.nextFloat() - rand.nextFloat()) * randRange,
								posZ + (rand.nextFloat() - rand.nextFloat()) * randRange,
								0D, 0D, 0D, state);
						SceneEnhancer.checkParticleBehavior();
						SceneEnhancer.particleBehavior.initParticleCube(hail);
						storm.listParticlesDebris.add(hail);

						hail.spawnAsWeatherEffect();
					}
				}
			}
		}
	}
}
