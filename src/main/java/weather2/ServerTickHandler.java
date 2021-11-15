package weather2;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.weathersystem.WeatherManagerServer;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Weather.MODID)
public class ServerTickHandler {
	private static final Map<RegistryKey<World>, WeatherManagerServer> MANAGERS = new Reference2ObjectOpenHashMap<>();

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		if (!world.isRemote() && world instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) world;
			RegistryKey<World> dimension = serverWorld.getDimensionKey();
			MANAGERS.put(dimension, new WeatherManagerServer(serverWorld));
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		if (!world.isRemote() && world instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) world;
			MANAGERS.remove(serverWorld.getDimensionKey());
		}
	}

	@SubscribeEvent
	public static void tickServer(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			for (WeatherManagerServer manager : MANAGERS.values()) {
				manager.tick();
			}
		}
	}

	@SubscribeEvent
	public static void tickPlayer(TickEvent.PlayerTickEvent event) {

	}

	public static WeatherManagerServer getWeatherManagerFor(RegistryKey<World> dimension) {
		return MANAGERS.get(dimension);
	}

	public static void playerClientRequestsFullSync(ServerPlayerEntity entP) {
		/*WeatherManagerServer wm = MANAGERS.get(entP.world.getDimensionKey());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}*/
	}
}
