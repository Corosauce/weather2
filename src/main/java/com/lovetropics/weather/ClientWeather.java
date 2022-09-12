package com.lovetropics.weather;

import com.lovetropics.minigames.common.core.game.weather.PrecipitationType;
import com.lovetropics.minigames.common.core.game.weather.WeatherState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LTWeather.MODID, value = Dist.CLIENT)
public final class ClientWeather {
	private static ClientWeather instance = new ClientWeather();

	private static final int LERP_TICKS = ServerWeatherController.UPDATE_INTERVAL;
	//10 seconds to transition precipitation fully
	private static final float LERP_RATE = 1F / 10F / 20F;

	private final WeatherState state = new WeatherState();

	private WeatherState lerpState = this.state;

	private ClientWeather() {
	}

	public static ClientWeather get() {
		return instance;
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isClientSide()) {
			reset();
		}
	}

	public static void reset() {
		instance = new ClientWeather();
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			instance.tick();
		}
	}

	public void onUpdateWeather(WeatherState state) {
		this.lerpState = state;
		//System.out.println("state.rainAmount: " + state.rainAmount);
	}

	public void tick() {
		this.state.precipitationType = this.lerpState.precipitationType;
		this.state.heatwave = this.lerpState.heatwave;
		this.state.sandstorm = this.lerpState.sandstorm;
		this.state.snowstorm = this.lerpState.snowstorm;

		this.state.rainAmount = this.state.rainAmount + (this.state.rainAmount < this.lerpState.rainAmount ? LERP_RATE : -LERP_RATE);
		this.state.windSpeed = this.state.windSpeed + (this.state.windSpeed < this.lerpState.windSpeed ? LERP_RATE : -LERP_RATE);
	}

	public float getRainAmount() {
		return this.state.rainAmount;
	}

	public float getVanillaRainAmount() {
		//have vanilla rain get to max saturation by the time rainAmount hits 0.33, rest of range is used for extra particle effects elsewhere
		return Math.min(this.state.rainAmount * 3F, 1F);
	}

	public PrecipitationType getPrecipitationType() {
		return this.state.precipitationType;
	}

	public float getWindSpeed() {
		return this.state.windSpeed;
	}

	public boolean isHeatwave() {
		return this.state.heatwave;
	}

	public boolean isSandstorm() {
		return this.state.sandstorm != null;
	}

	public boolean isSnowstorm() {
		return this.state.snowstorm != null;
	}

	public boolean hasWeather() {
		return this.state.hasWeather();
	}
}
