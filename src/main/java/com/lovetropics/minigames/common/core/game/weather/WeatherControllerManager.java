package com.lovetropics.minigames.common.core.game.weather;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "ltminigames")
public final class WeatherControllerManager {
	private static final Map<ResourceKey<Level>, WeatherController> WEATHER_CONTROLLERS = new Reference2ObjectOpenHashMap<>();

	private static Function<ServerLevel, WeatherController> factory = VanillaWeatherController::new;

	public static void setFactory(Function<ServerLevel, WeatherController> factory) {
		WeatherControllerManager.factory = factory;
	}

	public static WeatherController forWorld(ServerLevel world) {
		ResourceKey<Level> dimension = world.dimension();
		WeatherController controller = WEATHER_CONTROLLERS.get(dimension);
		if (controller == null) {
			WEATHER_CONTROLLERS.put(dimension, controller = factory.apply(world));
		}
		return controller;
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		joinPlayerToDimension(event.getPlayer(), event.getPlayer().level.dimension());
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		joinPlayerToDimension(event.getPlayer(), event.getTo());
	}

	private static void joinPlayerToDimension(Player player, ResourceKey<Level> dimension) {
		MinecraftServer server = player.getServer();
		if (server == null || !(player instanceof ServerPlayer)) {
			return;
		}

		ServerLevel world = server.getLevel(dimension);
		if (world != null) {
			WeatherController controller = WeatherControllerManager.forWorld(world);
			controller.onPlayerJoin((ServerPlayer) player);
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		Level world = event.world;
		if (world.isClientSide || event.phase == TickEvent.Phase.END) {
			return;
		}

		WeatherController controller = WEATHER_CONTROLLERS.get(world.dimension());
		if (controller != null) {
			controller.tick();
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		LevelAccessor world = event.getWorld();
		if (world instanceof ServerLevelAccessor) {
			WEATHER_CONTROLLERS.remove(((ServerLevelAccessor) world).getLevel().dimension());
		}
	}
}
