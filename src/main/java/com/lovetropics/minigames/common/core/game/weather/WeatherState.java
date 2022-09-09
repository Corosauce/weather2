package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.network.FriendlyByteBuf;

public final class WeatherState {
	public float rainAmount;
	public PrecipitationType precipitationType = PrecipitationType.NORMAL;
	public float windSpeed;
	public boolean heatwave;
	public StormState sandstorm;
	public StormState snowstorm;

	public void serialize(FriendlyByteBuf buffer) {
		buffer.writeFloat(this.rainAmount);
		buffer.writeByte(this.precipitationType.ordinal() & 0xFF);
		buffer.writeFloat(this.windSpeed);
		buffer.writeBoolean(this.heatwave);
		buffer.writeBoolean(this.sandstorm != null);
		if (this.sandstorm != null) {
			this.sandstorm.encode(buffer);
		}
		buffer.writeBoolean(this.snowstorm != null);
		if (this.snowstorm != null) {
			this.snowstorm.encode(buffer);
		}
	}

	public void deserialize(FriendlyByteBuf buffer) {
		this.rainAmount = buffer.readFloat();
		this.precipitationType = PrecipitationType.VALUES[buffer.readUnsignedByte()];
		this.windSpeed = buffer.readFloat();
		this.heatwave = buffer.readBoolean();
		this.sandstorm = buffer.readBoolean() ? StormState.decode(buffer) : null;
		this.snowstorm = buffer.readBoolean() ? StormState.decode(buffer) : null;
	}

	public boolean isRaining() {
		return this.rainAmount > 0.01F;
	}

	public boolean isWindy() {
		return this.windSpeed > 0.01F;
	}

	public boolean hasWeather() {
		return this.isRaining() || this.isWindy()
				|| this.heatwave
				|| this.sandstorm != null || this.snowstorm != null;
	}
}
