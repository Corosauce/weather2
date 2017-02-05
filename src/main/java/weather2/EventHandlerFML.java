package weather2;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class EventHandlerFML {

	public static World lastWorld = null;

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

	}

	@SubscribeEvent
	public void tickClient(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			//1.8: new scenario where this will tick even if world is unloaded?
			try {
				ClientProxy.clientTickHandler.onTickInGame();
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
			if (event.getEntityPlayer().worldObj.provider.getDimension() != event.getOriginal().worldObj.provider.getDimension()) {
				Weather.dbg("Weather2: PlayerEvent.Clone: " + event.isCanceled() + ", " + event.isWasDeath() + ", " + event.getEntityPlayer().worldObj.provider.getDimension() + ", " + event.getOriginal().worldObj.provider.getDimension() + ", " );
				ServerTickHandler.playerChangedDimensionsSyncFull((EntityPlayerMP) event.getEntityPlayer());
			}
		}*/

	}
}
