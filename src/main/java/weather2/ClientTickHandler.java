package weather2;

import extendedrenderer.ParticleManagerExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmBackupScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import weather2.client.SceneEnhancer;
import weather2.util.WeatherUtil;
import weather2.util.WindReader;
import weather2.weathersystem.WeatherManagerClient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod.EventBusSubscriber(modid = Weather.MODID, value = Dist.CLIENT)
public class ClientTickHandler
{
	public static final ClientTickHandler INSTANCE = new ClientTickHandler();

	public static World lastWorld;
	
	public static WeatherManagerClient weatherManager;
	public static SceneEnhancer sceneEnhancer;

	public float smoothAngle = 0;

	public float smoothAngleRotationalVelAccel = 0;

	public float smoothAngleAdj = 0.1F;

	public int prevDir = 0;

	private static ParticleManagerExtended particleManagerExtended;

	private ClientTickHandler() {
		//this constructor gets called multiple times when created from proxy, this prevents multiple inits
		if (sceneEnhancer == null) {
			sceneEnhancer = new SceneEnhancer();
			(new Thread(sceneEnhancer, "Weather2 Scene Enhancer")).start();
		}
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
        World world = mc.world;

		//System.out.println(mc.currentScreen);

		if (mc.currentScreen instanceof ConfirmBackupScreen) {

		}

		if (world != null) {
			checkClientWeather();

			weatherManager.tick();
			sceneEnhancer.tickClient();

			if (!WeatherUtil.isPausedForClient()) {
				particleManagerExtended().tick();
			}

			//TODO: evaluate if best here
			float windDir = WindReader.getWindAngle(world);
			float windSpeed = WindReader.getWindSpeed(world);

			//windDir = 0;

			float diff = Math.abs(windDir - smoothAngle)/* - 180*/;

			if (true && diff > 10/* && (smoothAngle > windDir - give || smoothAngle < windDir + give)*/) {

				if (smoothAngle > 180) smoothAngle -= 360;
				if (smoothAngle < -180) smoothAngle += 360;

				float bestMove = MathHelper.wrapDegrees(windDir - smoothAngle);

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
		ClientWeather.reset();
	}

    public static void checkClientWeather() {

    	try {
			World world = Minecraft.getInstance().world;
    		if (weatherManager == null || world != lastWorld) {
    			init(world);
        	}
    	} catch (Exception ex) {
    		Weather.dbg("Weather2: Warning, client received packet before it was ready to use, and failed to init client weather due to null world");
    	}
    }

    public static void init(World world) {
		Weather.dbg("Weather2: Initializing WeatherManagerClient for client world and requesting full sync");

    	lastWorld = world;
    	weatherManager = new WeatherManagerClient(world.getDimensionKey());

    	Minecraft mc = Minecraft.getInstance();

    	if (particleManagerExtended == null) {
			particleManagerExtended = new ParticleManagerExtended(mc.world, mc.textureManager);
		} else {
			particleManagerExtended.clearEffects((ClientWorld) world);
		}

		//((IReloadableResourceManager)mc.getResourceManager()).addReloadListener(particleManagerExtended);
    }

	public static ParticleManagerExtended particleManagerExtended() {
		return particleManagerExtended;
	}
}
