package com.lovetropics.minigames.common.core.game.weather;

import java.util.function.Consumer;

public final class WeatherEvent {
	private final WeatherEventType type;
	private long time;

	private Consumer<WeatherController> apply;
	private Consumer<WeatherController> remove;

	private WeatherEvent(WeatherEventType type, long time) {
		this.type = type;
		this.time = time;
	}

	public static WeatherEvent heavyRain(long time) {
		return new WeatherEvent(WeatherEventType.HEAVY_RAIN, time)
				.applies(controller -> controller.setRain(1.0F, RainType.NORMAL))
				.removes(controller -> controller.setRain(0.0F, RainType.NORMAL));
	}

	public static WeatherEvent acidRain(long time) {
		return new WeatherEvent(WeatherEventType.ACID_RAIN, time)
				.applies(controller -> controller.setRain(1.0F, RainType.ACID))
				.removes(controller -> controller.setRain(0.0F, RainType.ACID));
	}

	public static WeatherEvent hail(long time) {
		return new WeatherEvent(WeatherEventType.HAIL, time)
				.applies(controller -> controller.setRain(1.0F, RainType.HAIL))
				.removes(controller -> controller.setRain(0.0F, RainType.HAIL));
	}

	public static WeatherEvent heatwave(long time) {
		return new WeatherEvent(WeatherEventType.HEATWAVE, time)
				.applies(controller -> controller.setHeatwave(true))
				.removes(controller -> controller.setHeatwave(false));
	}

	public static WeatherEvent sandstorm(long time, int buildupTickRate, int maxStackable) {
		return new WeatherEvent(WeatherEventType.SANDSTORM, time)
				.applies(controller -> controller.setSandstorm(buildupTickRate, maxStackable))
				.removes(WeatherController::clearSandstorm);
	}

	public static WeatherEvent snowstorm(long time, int buildupTickRate, int maxStackable) {
		return new WeatherEvent(WeatherEventType.SNOWSTORM, time)
				.applies(controller -> controller.setSnowstorm(buildupTickRate, maxStackable))
				.removes(WeatherController::clearSnowstorm);
	}

	public WeatherEvent applies(Consumer<WeatherController> apply) {
		this.apply = apply;
		return this;
	}

	public WeatherEvent removes(Consumer<WeatherController> remove) {
		this.remove = remove;
		return this;
	}

	public WeatherEventType getType() {
		return type;
	}

	public void apply(WeatherController controller) {
		if (this.apply != null) {
			this.apply.accept(controller);
		}
	}

	public void remove(WeatherController controller) {
		if (this.remove != null) {
			this.remove.accept(controller);
		}
	}

	public TickResult tick() {
		if (time-- <= 0) {
			return TickResult.STOP;
		}
		return TickResult.CONTINUE;
	}

	public enum TickResult {
		STOP,
		CONTINUE,
	}
}
