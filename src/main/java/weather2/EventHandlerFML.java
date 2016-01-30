package weather2;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
		ServerTickHandler.playerJoinedServerSyncFull((EntityPlayerMP) event.player);
	}
}
