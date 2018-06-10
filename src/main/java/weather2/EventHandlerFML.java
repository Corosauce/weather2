package weather2;

import CoroUtil.forge.CULog;
import extendedrenderer.EventHandler;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.shader.InstancedMeshFoliage;
import extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import weather2.client.foliage.FoliageEnhancerShader;
import weather2.client.foliage.FoliageReplacerBase;
import weather2.config.ConfigFoliage;
import weather2.config.ConfigMisc;

import java.util.List;
import java.util.Map;

public class EventHandlerFML {

	public static boolean sleepFlag = false;
	public static boolean wasRain = false;
	public static int rainTime = 0;
	public static boolean wasThunder = false;
	public static int thunderTime = 0;

	//initialized at post init after configs loaded in
	public static boolean extraGrassLast;

	@SubscribeEvent
	public void tickWorld(WorldTickEvent event) {
		if (event.phase == Phase.START) {

		}
	}

	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {

		if (event.phase == Phase.START) {
			//System.out.println("tick weather2");
			ServerTickHandler.onTickInGame();
		}

		if (ConfigMisc.Global_Overcast_Prevent_Rain_Reset_On_Sleep) {
			WorldServer world = DimensionManager.getWorld(0);
			if (world != null) {
				if (event.phase == Phase.START) {
					if (world.areAllPlayersAsleep()) {
						sleepFlag = true;
						/*System.out.println("start: all players asleep");
						System.out.println("start rain: " + world.getWorldInfo().isRaining());
						System.out.println("start rain time: " + world.getWorldInfo().getRainTime());*/
						wasRain = world.getWorldInfo().isRaining();
						wasThunder = world.getWorldInfo().isThundering();
						rainTime = world.getWorldInfo().getRainTime();
						thunderTime = world.getWorldInfo().getThunderTime();
					} else {
						sleepFlag = false;
					}
				} else {
					if (sleepFlag) {
						/*System.out.println("end: sleep flag trigger");
						System.out.println("end rain: " + world.getWorldInfo().isRaining());
						System.out.println("end rain time: " + world.getWorldInfo().getRainTime());*/
						world.getWorldInfo().setRaining(wasRain);
						world.getWorldInfo().setRainTime(rainTime);
						world.getWorldInfo().setThundering(wasThunder);
						world.getWorldInfo().setThunderTime(thunderTime);
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void tickClient(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			try {
				ClientProxy.clientTickHandler.onTickInGame();

				if (extraGrassLast != ConfigFoliage.extraGrass) {
					extraGrassLast = ConfigFoliage.extraGrass;

					//note: foliage shaders var tracking will handle the reload if foliageShaders val changes
					//also extra grass doesnt actually replace a vanilla block, why do we invoke a reset
					/*if (ConfigCoroUtil.foliageShaders) {
						EventHandler.flagFoliageUpdate = true;
					}*/
					//clear the active grass etc
					//FoliageEnhancerShader.shadersReset();

					/*for (FoliageReplacerBase replacer : FoliageEnhancerShader.listFoliageReplacers) {
						replacer.markMeshesDirty();
					}*/
					/*for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : ExtendedRenderer.foliageRenderer.foliage.entrySet()) {
						InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

						mesh.dirtyVBO2Flag = true;
					}*/

					//repopulate the list with or without grass
					//FoliageEnhancerShader.setupReplacers();
				}

				boolean hackyLiveReplace = false;

				if (hackyLiveReplace && EventHandler.flagFoliageUpdate) {
					CULog.dbg("CoroUtil detected a need to reload resource packs, initiating");
					EventHandler.flagFoliageUpdate = false;
					//Minecraft.getMinecraft().refreshResources();
					FoliageEnhancerShader.liveReloadModels();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@SubscribeEvent
	public void tickRenderScreen(RenderTickEvent event) {
		if (event.phase == Phase.END) {
			ClientProxy.clientTickHandler.onRenderScreenTick();
		}
	}

	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP) {
			ServerTickHandler.syncServerConfigToClientPlayer((EntityPlayerMP) event.player);
		}
		//Weather.dbg("Weather2: PlayerLoggedInEvent: " + event.player.getName());
		//ServerTickHandler.playerJoinedServerSyncFull((EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		//Weather.dbg("Weather2: PlayerChangedDimensionEvent: " + event.player.getName() + " from " + event.fromDim + ", to " + event.toDim);
		//ServerTickHandler.playerChangedDimensionsSyncFull((EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void playerRespawned(PlayerEvent.PlayerRespawnEvent event) {
		//Weather.dbg("Weather2: PlayerRespawnEvent: " + event.player.getName());
		//ServerTickHandler.playerChangedDimensionsSyncFull((EntityPlayerMP) event.player);
	}


	/**
	 * Since teleporting from end or respawning to a different dimension doesnt trigger PlayerChangedDimensionEvent,
	 * we must use PlayerEvent.Clone and compare old and new dimension ID to determine resync need.
	 * if there are edge cases where this is triggered without needing, client will at least detect an ID overlap and not duplicate weather object
	 *
	 * nevermind, race condition problem with PlayerEvent.Clone but not with PlayerChangedDimensionEvent
	 *
	 * switching to client sending packet to server for requesting a full resync
	 *
	 */
	@SubscribeEvent
	public void playerCloned(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
		/*if (!event.isCanceled()) {
			if (event.getEntityPlayer().world.provider.getDimension() != event.getOriginal().world.provider.getDimension()) {
				Weather.dbg("Weather2: PlayerEvent.Clone: " + event.isCanceled() + ", " + event.isWasDeath() + ", " + event.getEntityPlayer().world.provider.getDimension() + ", " + event.getOriginal().world.provider.getDimension() + ", " );
				ServerTickHandler.playerChangedDimensionsSyncFull((EntityPlayerMP) event.getEntityPlayer());
			}
		}*/

	}
}
