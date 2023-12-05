package weather2;

import com.corosus.coroutil.util.CULog;
import extendedrenderer.ParticleManagerExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import weather2.client.SceneEnhancer;
import weather2.config.ClientConfigData;
import weather2.config.ConfigDebug;
import weather2.util.WeatherUtil;
import weather2.util.WindReader;
import weather2.weathersystem.WeatherManagerClient;

@Mod.EventBusSubscriber(modid = Weather.MODID, value = Dist.CLIENT)
public class ClientTickHandler
{
	public static final ClientTickHandler INSTANCE = new ClientTickHandler();

	public static Level lastWorld;
	
	public static WeatherManagerClient weatherManager;
	public static SceneEnhancer sceneEnhancer;

	public static ClientConfigData clientConfigData;

	public float smoothAngle = 0;

	public float smoothAngleRotationalVelAccel = 0;

	public float smoothAngleAdj = 0.1F;

	public int prevDir = 0;

	public long lastParticleResetTime = 0;

	private static ParticleManagerExtended particleManagerExtended;

	private ClientTickHandler() {
		//this constructor gets called multiple times when created from proxy, this prevents multiple inits
		if (sceneEnhancer == null) {
			sceneEnhancer = new SceneEnhancer();
			(new Thread(sceneEnhancer, "Weather2 Scene Enhancer")).start();
		}

		clientConfigData = new ClientConfigData();
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			INSTANCE.onTickInGame();
		}
	}

    public void onTickInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;

		if (world != null) {
			getClientWeather();

			weatherManager.tick();
			sceneEnhancer.tickClient();

			if (!WeatherUtil.isPausedForClient()) {
				if (ConfigDebug.Particle_engine_tick) {
					particleManagerExtended().tick();
				}
			}

			if (ConfigDebug.Particle_Reset_Frequency > 0 && lastParticleResetTime + ConfigDebug.Particle_Reset_Frequency < world.getGameTime()) {
				CULog.log("clearing vanilla particles, set Weather2 Debug Particle_Reset_Frequency to 0 to disable");
				lastParticleResetTime = world.getGameTime();
				mc.particleEngine.clearParticles();
			}

			//TODO: evaluate if best here
			float windDir = WindReader.getWindAngle(world);
			float windSpeed = WindReader.getWindSpeed(world, mc.player != null ? mc.player.blockPosition() : null);

			//windDir = 0;
			//TODO: ???????????? what is all this even affecting now
			float diff = Math.abs(windDir - smoothAngle)/* - 180*/;

			if (true && diff > 10/* && (smoothAngle > windDir - give || smoothAngle < windDir + give)*/) {

				if (smoothAngle > 180) smoothAngle -= 360;
				if (smoothAngle < -180) smoothAngle += 360;

				float bestMove = Mth.wrapDegrees(windDir - smoothAngle);

				smoothAngleAdj = windSpeed;//0.2F;

				if (Math.abs(bestMove) < 180/* - (angleAdjust * 2)*/) {
					float realAdj = smoothAngleAdj;//Math.max(smoothAngleAdj, Math.abs(bestMove));

					if (realAdj * 2 > windSpeed) {
						if (bestMove > 0) {
							smoothAngleRotationalVelAccel -= realAdj;
							if (prevDir < 0) {
								smoothAngleRotationalVelAccel = 0;
							}
							prevDir = 1;
						} else if (bestMove < 0) {
							smoothAngleRotationalVelAccel += realAdj;
							if (prevDir > 0) {
								smoothAngleRotationalVelAccel = 0;
							}
							prevDir = -1;
						}
					}

					if (smoothAngleRotationalVelAccel > 0.3 || smoothAngleRotationalVelAccel < -0.3) {
						smoothAngle += smoothAngleRotationalVelAccel * 0.3F;
					} else {
						//smoothAngleRotationalVelAccel *= 0.9F;
					}

					smoothAngleRotationalVelAccel *= 0.80F;
				}
			}
		} else {
			resetClientWeather();
		}

    }

    public static void resetClientWeather() {
		weatherManager = null;
		ClientWeatherProxy.get().reset();
		ClientWeatherHelper.get().reset();
	}

    public static WeatherManagerClient getClientWeather() {

    	try {
			Level world = Minecraft.getInstance().level;
    		if (weatherManager == null || world != lastWorld) {
    			init(world);
        	}
    	} catch (Exception ex) {
    		Weather.dbg("Weather2: Warning, client received packet before it was ready to use, and failed to init client weather due to null world");
    	}
		return weatherManager;
    }

    public static void init(Level world) {
		Weather.dbg("Weather2: Initializing WeatherManagerClient for client world and requesting full sync");

    	lastWorld = world;
    	weatherManager = new WeatherManagerClient(world.dimension());

    	Minecraft mc = Minecraft.getInstance();

    	if (particleManagerExtended == null) {
			particleManagerExtended = new ParticleManagerExtended(mc.level, mc.textureManager);
		} else {
			particleManagerExtended.setLevel((ClientLevel) world);
		}

		//((IReloadableResourceManager)mc.getResourceManager()).addReloadListener(particleManagerExtended);
		CompoundTag data = new CompoundTag();
		data.putString("command", "syncFull");
		data.putString("packetCommand", "WeatherData");
		//Weather.eventChannel.sendToServer(PacketHelper.getNBTPacket(data, Weather.eventChannelName));
		WeatherNetworking.HANDLER.sendTo(new PacketNBTFromClient(data), mc.player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
    }

	public static ParticleManagerExtended particleManagerExtended() {
		return particleManagerExtended;
	}
}
