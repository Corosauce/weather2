package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface WeatherController {
	void onPlayerJoin(ServerPlayer player);

	void tick();

	void setRain(float amount, RainType type);

	void setWind(float speed);

	void setHeatwave(boolean heatwave);

	void setSandstorm(int buildupTickRate, int maxStackable);

	void clearSandstorm();

	void setSnowstorm(int buildupTickRate, int maxStackable);

	void clearSnowstorm();

	float getRainAmount();

	RainType getRainType();

	float getWindSpeed();

	boolean isHeatwave();

	@Nullable
	StormState getSandstorm();

	@Nullable
	StormState getSnowstorm();

	default boolean isSandstorm() {
		return getSandstorm() != null;
	}

	default boolean isSnowstorm() {
		return getSnowstorm() != null;
	}

	default void reset() {
		setRain(0.0F, RainType.NORMAL);
		setWind(0.0F);
		setHeatwave(false);
		clearSandstorm();
		clearSnowstorm();
	}
}
