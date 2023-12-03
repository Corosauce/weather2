package weather2;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import weather2.config.ClientConfigData;
import weather2.config.ConfigMisc;
import weather2.config.WeatherUtilConfig;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.wind.WindManager;

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
			if (WeatherUtilConfig.listDimensionsWeather.contains(weatherManagerServer.getWorld().dimension().location().toString())) {
				weatherManagerServer.read();
			}
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
				//for non whitelisted dimensions i chose to still tick the manager, and also register it, so it can get cleaned up if people spawn stuff or change config
				//if (WeatherUtilConfig.listDimensionsWeather.contains(manager.getWorld().dimension().location().toString())) {
					manager.tick();
				//}
			}

			processIMCMessages();
		}
	}

	@SubscribeEvent
	public static void tickServer(TickEvent.LevelTickEvent event) {

		//TODO: TEMPPPPPPPPPPPP
		//ConfigMisc.Aesthetic_Only_Mode = true;
		//ConfigMisc.overcastMode = true;

		if (event.level.dimension() == Level.OVERWORLD && event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
			if (ConfigMisc.Aesthetic_Only_Mode) {
				if (!ConfigMisc.overcastMode) {
					ConfigMisc.overcastMode = true;
					CULog.dbg("detected Aesthetic_Only_Mode on, setting overcast mode on");
					//WeatherUtilConfig.setOvercastModeServerSide(ConfigMisc.overcastMode);
					ConfigMod.forceSaveAllFilesFromRuntimeSettings();
					syncServerConfigToClient(null);
				}
			}

			//TODO: only sync when things change? is now sent via PlayerLoggedInEvent at least
			/*if (event.level.getGameTime() % 200 == 0) {
				syncServerConfigToClient(null);
			}*/
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
				} else if (msg.method().equals("firenado")) {
					StormObject stormObject = new StormObject(wm);

					stormObject.setupStorm(null);
					stormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
					stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;
					stormObject.isFirenado = true;
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
		if (!event.player.level().isClientSide()) {
			syncServerConfigToClient(event.player);
		}
	}

	public static void joinPlayer(PlayerEvent.PlayerLoggedInEvent event) {

	}

	public static WeatherManagerServer getWeatherManagerFor(ResourceKey<Level> dimension) {
		return MANAGERS.get(dimension);
	}

	public static WeatherManagerServer getWeatherManagerFor(Level level) {
		return MANAGERS.get(level.dimension());
	}

	public static void playerClientRequestsFullSync(ServerPlayer entP) {
		WeatherManagerServer wm = MANAGERS.get(entP.level().dimension());
		if (wm != null) {
			wm.playerJoinedWorldSyncFull(entP);
		}
	}

	public static void syncServerConfigToClient(Player player) {
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "ClientConfigData");
		data.putString("command", "syncUpdate");
		ClientConfigData.writeNBT(data);
		if (player != null) {
			WeatherNetworking.HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new PacketNBTFromServer(data));
		} else {
			WeatherNetworking.HANDLER.send(PacketDistributor.ALL.noArg(), new PacketNBTFromServer(data));
		}
	}
}
