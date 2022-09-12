package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public final class VanillaWeatherController implements WeatherController {
	private final ServerLevel world;
	private final WeatherState state = new WeatherState();

	public VanillaWeatherController(ServerLevel world) {
		this.world = world;
	}

	@Override
	public void onPlayerJoin(ServerPlayer player) {
	}

	@Override
	public void tick() {
		world.getLevelData().setRaining(state.isRaining());
	}

	@Override
	public void setRain(float amount, PrecipitationType type) {
		state.rainAmount = amount;
		state.precipitationType = type;
	}

	@Override
	public void setWind(float speed) {
		state.windSpeed = speed;
	}

	@Override
	public void setHeatwave(boolean heatwave) {
		state.heatwave = heatwave;
	}

	@Override
	public void setSandstorm(int buildupTickRate, int maxStackable) {
		state.sandstorm = new StormState(buildupTickRate, maxStackable);
	}

	@Override
	public void clearSandstorm() {
		state.sandstorm = null;
	}

	@Override
	public void setSnowstorm(int buildupTickRate, int maxStackable) {
		state.snowstorm = new StormState(buildupTickRate, maxStackable);
	}

	@Override
	public void clearSnowstorm() {
		state.snowstorm = null;
	}

	@Override
	public float getRainAmount() {
		return state.rainAmount;
	}

	@Override
	public PrecipitationType getPrecipitationType() {
		return state.precipitationType;
	}

	@Override
	public float getWindSpeed() {
		return state.windSpeed;
	}

	@Override
	public boolean isHeatwave() {
		return state.heatwave;
	}

	@Nullable
	@Override
	public StormState getSandstorm() {
		return state.sandstorm;
	}

	@Nullable
	@Override
	public StormState getSnowstorm() {
		return state.snowstorm;
	}
}
