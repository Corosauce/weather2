package weather2;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.weathersystem.WeatherManagerServer;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Weather.MODID)
public class ServerTickHandler {
	private static final Map<ResourceKey<Level>, WeatherManagerServer> MANAGERS = new Reference2ObjectOpenHashMap<>();

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		LevelAccessor world = event.getWorld();
		if (!world.isClientSide() && world instanceof ServerLevel) {
			ServerLevel serverWorld = (ServerLevel) world;
			ResourceKey<Level> dimension = serverWorld.dimension();
			MANAGERS.put(dimension, new WeatherManagerServer(serverWorld));
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		LevelAccessor world = event.getWorld();
		if (!world.isClientSide() && world instanceof ServerLevel) {
			ServerLevel serverWorld = (ServerLevel) world;
			MANAGERS.remove(serverWorld.dimension());
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

	public static WeatherManagerServer getWeatherManagerFor(ResourceKey<Level> dimension) {
		return MANAGERS.get(dimension);
	}

	public static void playerClientRequestsFullSync(ServerPlayer entP) {
		/*WeatherManagerServer wm = MANAGERS.get(entP.world.getDimensionKey());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}*/
	}
}
