package weather2;

import com.corosus.coroutil.util.CULog;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Weather.MODID)
public class ServerTickHandler {
	private static final Map<ResourceKey<Level>, WeatherManagerServer> MANAGERS = new Reference2ObjectOpenHashMap<>();
	private static final HashMap<String, WeatherManagerServer> MANAGERSLOOKUP = new HashMap<>();

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            ResourceKey<Level> dimension = serverWorld.dimension();
            WeatherManagerServer weatherManagerServer = new WeatherManagerServer(serverWorld);
            weatherManagerServer.read();
            MANAGERS.put(dimension, weatherManagerServer);
            MANAGERSLOOKUP.put(dimension.location().toString(), weatherManagerServer);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof ServerLevel) {
            ServerLevel serverWorld = (ServerLevel) world;
            MANAGERS.remove(serverWorld.dimension());
            MANAGERSLOOKUP.remove(serverWorld.dimension().toString());
        }
    }

	@SubscribeEvent
	public static void tickServer(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			for (WeatherManagerServer manager : MANAGERS.values()) {
				manager.tick();
			}

			processIMCMessages();
		}
	}

	public static void processIMCMessages() {
		InterModComms.getMessages("weather2").forEach((msg) -> {
			CompoundTag tag = (CompoundTag) msg.messageSupplier().get();
			String dimResource = tag.getString("dimension");
			WeatherManagerServer wm = MANAGERSLOOKUP.get(dimResource);
			if (wm != null) {
				if (msg.method().equals("player_tornado")) {
					int timeTicks = tag.getInt("time_ticks");
					boolean baby = tag.getBoolean("baby");
					//boolean sharknado = tag.getBoolean("sharknado");
					String uuid = tag.getString("uuid");
					Player player = wm.getWorld().getPlayerByUUID(UUID.fromString(uuid));
					if (player != null) {
						StormObject stormObject = new StormObject(wm);

						stormObject.setupStorm(player);
						stormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
						stormObject.levelStormIntensityMax = StormObject.STATE_STAGE1;
						stormObject.setupPlayerControlledTornado(player);
						stormObject.setPlayerControlledTimeLeft(timeTicks);
						stormObject.setBaby(baby);
						//stormObject.setSharknado(sharknado);

						wm.addStormObject(stormObject);
						wm.syncStormNew(stormObject);

						CULog.dbg("processed imc message: " + tag);
					} else {
						CULog.err("error cant find player in dimension " + dimResource + " for uuid " + uuid + " via IMC");
					}
				} else if (msg.method().equals("sharknado")) {
					StormObject stormObject = new StormObject(wm);

					stormObject.setupStorm(null);
					stormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
					stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;
					stormObject.setSharknado(true);
					stormObject.setupTornadoAwayFromPlayersAimAtPlayers();

					wm.addStormObject(stormObject);
					wm.syncStormNew(stormObject);

					CULog.dbg("processed imc message: " + tag);
				} else if (msg.method().equals("tornado")) {
					StormObject stormObject = new StormObject(wm);

					stormObject.setupStorm(null);
					stormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
					stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;
					stormObject.setSharknado(false);
					stormObject.setupTornadoAwayFromPlayersAimAtPlayers();

					wm.addStormObject(stormObject);
					wm.syncStormNew(stormObject);

					CULog.dbg("processed imc message: " + tag);
				}
			} else {
				CULog.err("error cant find WeatherManagerServer for dimension " + dimResource + " via IMC");
			}
		});
	}

	@SubscribeEvent
	public static void tickPlayer(TickEvent.PlayerTickEvent event) {

	}

	public static WeatherManagerServer getWeatherManagerFor(ResourceKey<Level> dimension) {
		return MANAGERS.get(dimension);
	}

	public static void playerClientRequestsFullSync(ServerPlayer entP) {
		WeatherManagerServer wm = MANAGERS.get(entP.level.dimension());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}
	}
}
