package weather2;

import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.StormState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: Consolidate with WeatherManager
@Mod.EventBusSubscriber(modid = Weather.MODID)
public final class ServerWeatherController implements WeatherController {
	public static final int UPDATE_INTERVAL = 20;

	private final PacketDistributor.PacketTarget packetTarget;

	private int ticks;
	private boolean dirty;

	private final WeatherState state = new WeatherState();

	ServerWeatherController(ServerLevel world) {
		ResourceKey<Level> dimension = world.dimension();
		this.packetTarget = PacketDistributor.DIMENSION.with(() -> dimension);
	}

	@Override
	public void onPlayerJoin(ServerPlayer player) {
		WeatherNetworking.HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new UpdateWeatherPacket(this.state));
	}

	@Override
	public void tick() {
		if (this.dirty && this.ticks++ % UPDATE_INTERVAL == 0) {
			this.dirty = false;
			WeatherNetworking.HANDLER.send(this.packetTarget, new UpdateWeatherPacket(this.state));
		}
	}

	@Override
	public void setRain(float amount, RainType type) {
		if (amount != this.state.rainAmount || type != this.state.rainType) {
			this.state.rainAmount = amount;
			this.state.rainType = type;
			this.dirty = true;
		}
	}

	@Override
	public void setWind(float speed) {
		if (speed != this.state.windSpeed) {
			this.state.windSpeed = speed;
			this.dirty = true;
		}
	}

	@Override
	public void setHeatwave(boolean heatwave) {
		if (heatwave != this.state.heatwave) {
			this.state.heatwave = heatwave;
			this.dirty = true;
		}
	}

	@Override
	public void setSandstorm(int buildupTickRate, int maxStackable) {
		this.state.sandstorm = new StormState(buildupTickRate, maxStackable);
		this.dirty = true;
	}

	@Override
	public void clearSandstorm() {
		if (this.state.sandstorm != null) {
			this.state.sandstorm = null;
			this.dirty = true;
		}
	}

	@Override
	public void setSnowstorm(int buildupTickRate, int maxStackable) {
		this.state.snowstorm = new StormState(buildupTickRate, maxStackable);
		this.dirty = true;
	}

	@Override
	public void clearSnowstorm() {
		if (this.state.snowstorm != null) {
			this.state.snowstorm = null;
			this.dirty = true;
		}
	}

	@Override
	public float getRainAmount() {
		return this.state.rainAmount;
	}

	@Override
	public RainType getRainType() {
		return this.state.rainType;
	}

	@Override
	public float getWindSpeed() {
		return this.state.windSpeed;
	}

	@Override
	public boolean isHeatwave() {
		return this.state.heatwave;
	}

	@Nullable
	@Override
	public StormState getSandstorm() {
		return this.state.sandstorm;
	}

	@Nullable
	@Override
	public StormState getSnowstorm() {
		return this.state.snowstorm;
	}
}
